package com.sharefile.testv3;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.net.ssl.HttpsURLConnection;

import com.sharefile.api.SFApiClient;
import com.sharefile.api.SFQueryBuilder;
import com.sharefile.api.SFV3Error;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFJsonException;
import com.sharefile.api.https.SFApiFileUploadRunnable.SFAPiUploadResponse;
import com.sharefile.api.https.SFApiRunnable;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.interfaces.SFApiDownloadProgressListener;
import com.sharefile.api.interfaces.SFApiUploadProgressListener;
import com.sharefile.api.models.SFAccessControl;
import com.sharefile.api.models.SFDownloadSpecification;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataFeed;
import com.sharefile.api.models.SFSymbolicLink;
import com.sharefile.api.models.SFUploadSpecification;
import com.sharefile.java.log.SLog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class FoldersActivity extends Activity
{
	private ListView mSFItemListView = null;
	private Activity thisActivity = null;
	private ProgressBar mThrobber = null;
	private String TOP = "top";
	
	private Map<String, SFFolder> mapFolderContents = new HashMap<String, SFFolder>();
	private Stack<String> mFolderIdStack = new Stack<String>();
		
	private void navigateForward(String folderid,String link) throws URISyntaxException
	{
		SLog.d("GET", "Nav to: " + link);
		confirmExit = false;
		mFolderIdStack.push(link);
		getContents(folderid, link);
	}
		

	private void storeToCache(String folderId,SFFolder object)
	{
		/*if(mFolderIdStack.peek().equalsIgnoreCase(TOP))
		{
			mFolderIdStack.pop();
			mFolderIdStack.push(folderId);
		}*/
		
		mapFolderContents.put(folderId, object);
	}
	
	private void navigateBack() throws URISyntaxException
	{		
		String folderid  = mFolderIdStack.peek();
		
		if(!folderid.equalsIgnoreCase(TOP))
		{
			folderid = mFolderIdStack.pop();
			folderid  = mFolderIdStack.peek();
			getContents(folderid,null);
		}				
		else
		{		
			if(confirmExit)
			{
				finish();
			}
			else
			{
				confirmExit = true;
				showToast("Press back again to exit");
				getContents(folderid,null);
			}			
		}				
	}
		
		
	private void showBusy(boolean busy)
	{
		if(mThrobber!=null)
		{
			if(busy)
			{
				mThrobber.setVisibility(View.VISIBLE);
			}
			else
			{
				mThrobber.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	private void callCreateFolderApi(String folderName,String folderDescription) throws URISyntaxException
	{
		String parenturl = mFolderIdStack.peek();
		SFFolder folder = new SFFolder();
		folder.setName(folderName);
		folder.setDescription(folderDescription);
		
		ISFQuery<SFFolder> createFolder = SFQueryBuilder.ITEMS.createFolder(new URI(parenturl), folder,false, false);
		
		try 
		{
			FullscreenActivity.mSFApiClient.executeQuery(createFolder, new ISFApiResultCallback<SFFolder>()
			{

				@Override
				public void sfApiSuccess(SFFolder object) 
				{					
					
				}

				@Override
				public void sfApiError(SFV3Error error,ISFQuery<SFFolder> sfapiApiqueri) 
				{					
					
				}								
			},null);
		} 
		catch (SFInvalidStateException e) 
		{			
			e.printStackTrace();
		}
	}
	
	private void saveToken(final SFOAuth2Token newAccessToken)
	{
		runOnUiThread(new Runnable() 
		{			
			@Override
			public void run() 
			{				
				try 
				{
					PersistantToken.saveToken(getApplicationContext(), newAccessToken);
				} 
				catch (SFJsonException | IOException e) 
				{							
					e.printStackTrace();
				}
			}
		});				
	}
	
	private void callGetAccessControlApi() throws URISyntaxException
	{
		String parenturl = mFolderIdStack.peek();
		
		ISFQuery<SFODataFeed<SFAccessControl>> query = SFQueryBuilder.ACCESS_CONTROL.getByItem(new URI(parenturl));
		
		try 
		{
			FullscreenActivity.mSFApiClient.executeQuery(query, new ISFApiResultCallback<SFODataFeed<SFAccessControl>>()
			{														

				@Override
				public void sfApiSuccess(SFODataFeed<SFAccessControl> object) 
				{
					SLog.d("SFSDK","getItem success: ");
					showToast("success");						
				}

				@Override
				public void sfApiError(SFV3Error v3error,ISFQuery<SFODataFeed<SFAccessControl>> asApiqueri) 
				{
					SLog.d("SFSDK","get Item failed: ");
					showToast("Failed");						
				}
			},null);
		} 
		catch (SFInvalidStateException e) 
		{							
			e.printStackTrace();
			showToast("Exception "+ e.getLocalizedMessage());							
		}
	}	
	
	SFApiUploadProgressListener mUploadListener = new SFApiUploadProgressListener() 
	{
		
		@Override
		public void uploadSuccess(long byteCount, SFUploadSpecification uploadSpec, SFApiClient client) 
		{
			SLog.d("-upload", "Success uploaded: "+ byteCount);
		}
						
		@Override
		public void bytesUploaded(long byteCount, SFUploadSpecification uploadSpec,SFApiClient client) 
		{	
			SLog.d("-upload", "upload progress bytes: "+ byteCount);
		}

		@Override
		public void uploadFailure(SFAPiUploadResponse v3error, SFUploadSpecification uploadSpec, SFApiClient client) 
		{
			SLog.d("-upload", "Failuer upload: "+ v3error.mExtraMessae);			
		}
	};
	
	
	private void callUploadApi()
	{
		/*
		String parentid = mFolderIdStack.peek();
		final File file = new File("/storage/sdcard0/sharefile/v3upload.bin");
		ISFQuery<SFUploadSpecification> query = SFItemsEntity.upload(parentid, SFUploadMethod.Streamed, true, "v3upload.bin", (long) file.length(), null, false, true, false, false, "sfsdk", true, "hello.txt", "sfsdk upload", false, null, null, 1, "json", false, 365);		
		
		try 
		{
			FullscreenActivity.mSFApiClient.executeQuery(query, new ISFApiResultCallback<SFUploadSpecification>()
			{														

				@Override
				public void onSuccess(SFUploadSpecification object)
				{
					SLog.d("SFSDK","getItem success: ");
					showToast("Actuall upload of file");				
					
					FileInputStream fis;
					try 
					{
						fis = new FileInputStream(file);
						FullscreenActivity.mSFApiClient.uploadFile(object, 0, file.length(), "v3upload.bin", fis, mUploadListener);
					} 
					catch (FileNotFoundException | SFInvalidStateException e) 
					{						
						SLog.d("SFSDK","!!Exception: "+ Log.getStackTraceString(e) );
					}						
				}

				@Override
				public void onError(SFV3Error v3error,ISFQuery<SFUploadSpecification> asApiqueri)
				{
					SLog.d("SFSDK","get Item failed: ");
					showToast("Failed");						
				}

			},null);
		} 
		catch (SFInvalidStateException e) 
		{							
			e.printStackTrace();
			showToast("Exception "+ e.getLocalizedMessage());							
		}
		*/
	}
	
	private void showCreateFolderDialog()
	{		
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dlg_create_folder);
		dialog.setTitle("Create Folder");

		// set the custom dialog components - text, image and button
		final EditText edxfolderName = (EditText) dialog.findViewById(R.id.create_folder_name);
		final EditText edxfolderDetails = (EditText) dialog.findViewById(R.id.create_folder_details);
		
		Button okButton = (Button) dialog.findViewById(R.id.ok);
		
		okButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				String folderName = edxfolderName.getText().toString().trim();
				String folderDetails = edxfolderDetails.getText().toString().trim();
				
				try {
					callCreateFolderApi(folderName, folderDetails);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				dialog.dismiss();
			}
		});
		
		
		Button cancelButton = (Button) dialog.findViewById(R.id.cancel);
		
		cancelButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				dialog.dismiss();
			}
		});

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
	    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
	    dialog.show();
	    dialog.getWindow().setAttributes(lp);
	    
		
	}
	
	
	private interface GetCredsCallback
	{
		public void doneGetCreds(String userName,String password);
	}
	
	private void showGetCredsDialog(final GetCredsCallback callback)
	{		
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dlg_get_creds);
		dialog.setTitle("Enter Credentials");

		// set the custom dialog components - text, image and button
		final EditText edxUserName = (EditText) dialog.findViewById(R.id.getcreds_username);
		final EditText edxPassword = (EditText) dialog.findViewById(R.id.getcreds_password);
		
		Button okButton = (Button) dialog.findViewById(R.id.ok);
		
		okButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				String userName = edxUserName.getText().toString().trim();
				String password = edxPassword.getText().toString().trim();
				
				callback.doneGetCreds(userName, password);
				
				dialog.dismiss();
			}
		});
		
		
		Button cancelButton = (Button) dialog.findViewById(R.id.cancel);
		
		cancelButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				dialog.dismiss();
			}
		});

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(dialog.getWindow().getAttributes());
	    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
	    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
	    dialog.show();
	    dialog.getWindow().setAttributes(lp);
	}
	
	public void showToast(final String msg)
	{
		runOnUiThread(new Runnable() 
		{			
			@Override
			public void run() 
			{				
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();;
			}
		});
	}
		
	private void showContentsList(SFFolder object)
	{
		SFFolder folderItem = (SFFolder) object;
		SFItemListViewAdapter adapter = new SFItemListViewAdapter(thisActivity, R.layout.sf_item, folderItem.getChildren());
						
		try
		{
			mSFItemListView.setAdapter(adapter);
			mSFItemListView.invalidate();
		}
		catch(Exception e)
		{
			SLog.d("Act", "Exception: "+ Log.getStackTraceString(e));							
		}
		
		showBusy(false);
	}
	
	boolean confirmExit = false;
	
	@Override
	public void onBackPressed() 
	{		
		//super.onBackPressed();
		
		try 
		{
			navigateBack();
		} 
		catch (Exception e) 
		{
			if(confirmExit)
			{
				finish();
			}
			else
			{
				confirmExit = true;
				showToast("Press back again to exit");
				return;
			}
		}		
								
	}
				
	private SFFolder getFromCache(String folderid)
	{		
		return mapFolderContents.get(folderid);
	}
	
	private URI getUriFromLink(String link) throws URISyntaxException
	{
		URI uri = null;
		
		SLog.d("GET", "build link for = " + link);
		
		if(link!=null)
		{
			uri = new URI(link);
		}
		else
		{
			uri = new URI("https://"+ FullscreenActivity.mOAuthToken.getSubdomain()+".sf-api.com/sf/v3/Items(top)");
		}
		
		SLog.d("GET", "ret URI = " + uri.toString());
		
		return uri;
	}
	
	
	private String getValidLink(String link)
	{
		if(link!=null)
		{
			return link;
		}
		else
		{
			return new String("https://"+ FullscreenActivity.mOAuthToken.getSubdomain()+".sf-api.com/sf/v3/Items(top)");
		}
	}
	
	
	private synchronized void getContents(final String folderid,final String link) throws URISyntaxException
	{		
		SFFolder folder = getFromCache(link);
						
		if(folder!=null)
		{						
			showContentsList(folder);
			return;
		}
		
		ISFQuery<SFItem> query =null;
				
		
		query = SFQueryBuilder.ITEMS.get(getUriFromLink(link),false);
		
		
		query.addQueryString("$expand", "Children");
		
		if(link!=null)
		{
			try 
			{
				query.setLink(link);
			}
			catch (URISyntaxException e) 
			{				
				e.printStackTrace();
			}
		}
		
		showBusy(true);
		try 
		{
			FullscreenActivity.mSFApiClient.executeQuery(query, getContentsListener ,null);
		} 
		catch (SFInvalidStateException e) 
		{							
			e.printStackTrace();
			showToast("Exception "+ e.getLocalizedMessage());							
		}
	}
		
	ISFApiResultCallback<SFItem> getContentsListener = new ISFApiResultCallback<SFItem>()
	{										
		@Override
		public void sfApiSuccess(final SFItem object) 
		{													
			runOnUiThread(new Runnable() 
			{			
				@Override
				public void run() 
				{				
					if(SFV3ElementType.isFolderType(object))
					{																				
						//mapFolderContents.put(object.getId(), (SFFolder) object);
						try
						{
							SLog.d("GET","Store to cache: " + object.geturl().toString());
						}
						catch(Exception e)
						{
							SLog.e("",e);
						}
						storeToCache(object.geturl().toString(), (SFFolder) object);
						showContentsList((SFFolder) object);																
					}
				}
			});
								
		}

		@Override
		public void sfApiError(final SFV3Error v3error,final ISFQuery<SFItem> asApiqueri) 
		{									
			SLog.d("SFSDK","get Item failed: " + v3error.message.value);
			showToast("Failed Get Item: " + v3error.message.value);
			
			runOnUiThread(new Runnable() 
			{			
				@Override
				public void run() 
				{																			
					showBusy(false);
					
					if(v3error.httpResponseCode == HttpsURLConnection.HTTP_UNAUTHORIZED)
					{
						showGetCredsDialog(new GetCredsCallback() 
						{		
							@Override
							public void doneGetCreds(final String userName, final String password) 
							{
								showBusy(true);
								try 
								{
									asApiqueri.setCredentials(userName, password);
									FullscreenActivity.mSFApiClient.executeQuery(asApiqueri, getContentsListener,null );
								} 
								catch (SFInvalidStateException e) 
								{							
									e.printStackTrace();
									showToast("Exception "+ e.getLocalizedMessage());							
								}
							}
						});											
					}
				}
			});
		}
	};
		
	
	private void initUIControls()
	{
		Button createFolder = (Button) findViewById(R.id.folderActions_buttonCreateFolder);
		
		createFolder.setOnClickListener(new OnClickListener() 
		{			
			@Override
			public void onClick(View v) 
			{				
				showCreateFolderDialog();
			}
		});
		
		
		Button getAccessControlList = (Button) findViewById(R.id.folderActions_buttonPreferences);
		
		getAccessControlList.setOnClickListener(new OnClickListener() 
		{			
			@Override
			public void onClick(View v) 
			{				
				try {
					callGetAccessControlApi();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		
		Button upload = (Button) findViewById(R.id.folderActions_buttonUpload);
		
		upload.setOnClickListener(new OnClickListener() 
		{			
			@Override
			public void onClick(View v) 
			{				
				callUploadApi();
			}
		});
		
		//////////////////  List View///////////////////////
		mSFItemListView = (ListView) findViewById(R.id.Folder_listview);
		mThrobber = (ProgressBar)findViewById(R.id.Folder_throbber);
		
		mSFItemListView.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) 
			{
				//SFItem item = (SFItem) view.getTag();
				SwipeListItemState state = (SwipeListItemState) view.getTag();
				SFItem item = state.mSFItem;				
				
				if(item!=null)
				{
					if(SFV3ElementType.isFolderType(item)) 
					{						
							String fid = item.getId();								
							String link = null;
							
							if(item instanceof SFSymbolicLink)
							{
								link = ((SFSymbolicLink)item).getLink().toString();
							}
							else
							{
								link = item.geturl().toString();
							}
														
							//getContents(fid,link);
							try {
								navigateForward(fid, item.geturl().toString());
							} catch (URISyntaxException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
					else if(SFV3ElementType.isFileType(item))
					{
						showToast("Starting download for: " + item.getName());
						
						callDownloadApi(item.getId());
					}
				}
			}
         }); 	
	}
	
	
	SFApiDownloadProgressListener mDownloadloadProgressListener = new SFApiDownloadProgressListener() 
	{		
		@Override
		public void downloadSuccess(long byteCount,SFDownloadSpecification downloaSpec, SFApiClient client) 
		{												
			SLog.d("download", "Download Sucess: "+ byteCount);
		}
		
		@Override
		public void downloadFailure(SFV3Error v3error, long byteCount, SFDownloadSpecification downloaSpec, SFApiClient client) 
		{	
			SLog.d("download", "Download failuer: "+ v3error.message);
		}
		
		@Override
		public void bytesDownloaded(long byteCount,SFDownloadSpecification downloaSpec, SFApiClient client) 
		{	
			SLog.d("download", "Download progress: "+ byteCount);
		}
	};
	
	private void callDownloadApi(String itemid )
	{
		/*
		ISFQuery<SFDownloadSpecification> downloadQuery = SFItemsEntity.download(itemid, true);
		
		try {
				FullscreenActivity.mSFApiClient.executeQuery(downloadQuery, new ISFApiResultCallback<SFDownloadSpecification>()
					{

						@Override
						public void onSuccess(SFDownloadSpecification object)
						{	
							showToast("Start actual download...");
							
							SLog.d("download","dspec = %s", object.getDownloadUrl() );
							
							FileOutputStream fileOutpuStream;
							try 
							{
								fileOutpuStream = new FileOutputStream("/storage/sdcard0/sharefile/v3download.bin");
								FullscreenActivity.mSFApiClient.downloadFile(object, 0, fileOutpuStream, mDownloadloadProgressListener);
							} 
							catch (FileNotFoundException | SFInvalidStateException e) 
							{								
								SLog.d("download", "!!Exception: %s" , Log.getStackTraceString(e));
							}																					
						}

						@Override
						public void onError(SFV3Error error,ISFQuery<SFDownloadSpecification> asApiqueri)
						{	
							SLog.d("download","error = %s" , error.message.value);
						}
			});
		}
		catch (SFInvalidStateException e) 
		{			
			e.printStackTrace();
		}*/		
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{		
		thisActivity = this;
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.folder);
		
		initUIControls();
											
	}
			
	@Override
	protected void onStart() 
	{		
		super.onStart();			
		//getContents(TOP,null);
		try {
			navigateForward(TOP, getValidLink(null));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}