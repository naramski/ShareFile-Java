package com.sharefile.testv3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.net.ssl.HttpsURLConnection;

import com.sharefile.api.SFApiClient;
import com.sharefile.api.SFApiQuery;
import com.sharefile.api.V3Error;
import com.sharefile.api.utils.SFLog;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.entities.SFAccessControlsEntity;
import com.sharefile.api.entities.SFItemsEntity;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFJsonException;
import com.sharefile.api.https.SFApiRunnable;
import com.sharefile.api.interfaces.SFApiDownloadProgressListener;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.interfaces.SFApiUploadProgressListener;
import com.sharefile.api.models.SFAccessControl;
import com.sharefile.api.models.SFDownloadSpecification;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataFeed;
import com.sharefile.api.models.SFSymbolicLink;
import com.sharefile.api.models.SFUploadMethod;
import com.sharefile.api.models.SFUploadSpecification;

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
		
	private void navigateForward(String folderid,String link)
	{
		confirmExit = false;
		mFolderIdStack.push(folderid);
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
	
	private void navigateBack()
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
	
	private void callCreateFolderApi(String folderName,String folderDescription)
	{
		String parentid = mFolderIdStack.peek();
		SFApiQuery<SFItem> createFolder = SFItemsEntity.createFolder(parentid, folderName,folderDescription,null, false, false);
		
		try 
		{
			FullscreenActivity.mSFApiClient.executeQuery(createFolder, new SFApiResponseListener<SFItem>()
			{

				@Override
				public void sfapiSuccess(SFItem object) 
				{					
					
				}

				@Override
				public void sfApiError(V3Error error, SFApiQuery<SFItem> asApiqueri) 
				{										
				}

				@Override
				public void sfApiStoreNewToken(SFOAuth2Token newAccessToken,
						SFApiQuery<SFItem> sfapiApiqueri) {
					saveToken(newAccessToken);
					
				}
				
			});
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
	
	private void callGetAccessControlApi()
	{
		String parentid = mFolderIdStack.peek();
		
		SFApiQuery<SFODataFeed<SFAccessControl>> query = SFAccessControlsEntity.getByItem(parentid);
		
		try 
		{
			FullscreenActivity.mSFApiClient.executeQuery(query, new SFApiResponseListener<SFODataFeed<SFAccessControl>>() 
			{														

				@Override
				public void sfapiSuccess(SFODataFeed<SFAccessControl> object) 
				{
					SFLog.d2("SFSDK","getItem success: ");
					showToast("success");						
				}

				@Override
				public void sfApiError(V3Error v3error,SFApiQuery<SFODataFeed<SFAccessControl>> asApiqueri) 
				{
					SFLog.d2("SFSDK","get Item failed: ");
					showToast("Failed");						
				}

				@Override
				public void sfApiStoreNewToken(SFOAuth2Token newAccessToken,
						SFApiQuery<SFODataFeed<SFAccessControl>> sfapiApiqueri) {
					saveToken(newAccessToken);	
					
				}
			});
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
			SFLog.d2("-upload", "Success uploaded: %d", byteCount);
		}
		
		@Override
		public void uploadFailure(V3Error v3error, long byteCount, SFUploadSpecification uploadSpec, SFApiClient client) 
		{			
			SFLog.d2("-upload", "Failuer upload: %s", v3error.message.value);
		}
		
		@Override
		public void bytesUploaded(long byteCount, SFUploadSpecification uploadSpec,SFApiClient client) 
		{	
			SFLog.d2("-upload", "upload progress bytes: %s", byteCount);
		}
	};
	
	private void callUploadApi()
	{
		String parentid = mFolderIdStack.peek();
		final File file = new File("/storage/sdcard0/sharefile/v3upload.bin");
		SFApiQuery<SFUploadSpecification> query = SFItemsEntity.upload(parentid, SFUploadMethod.Streamed, true, "v3upload.bin", (long) file.length(), null, false, true, false, false, "sfsdk", true, "hello.txt", "sfsdk upload", false, null, null, 1, "json", false, 365);		
		
		try 
		{
			FullscreenActivity.mSFApiClient.executeQuery(query, new SFApiResponseListener<SFUploadSpecification>() 
			{														

				@Override
				public void sfapiSuccess(SFUploadSpecification object) 
				{
					SFLog.d2("SFSDK","getItem success: ");
					showToast("Actuall upload of file");				
					
					FileInputStream fis;
					try 
					{
						fis = new FileInputStream(file);
						FullscreenActivity.mSFApiClient.uploadFile(object, 0, file.length(), "v3upload.bin", fis, mUploadListener);
					} 
					catch (FileNotFoundException | SFInvalidStateException e) 
					{						
						SFLog.d2("SFSDK","!!Exception: %s", Log.getStackTraceString(e) );
					}						
				}

				@Override
				public void sfApiError(V3Error v3error,SFApiQuery<SFUploadSpecification> asApiqueri) 
				{
					SFLog.d2("SFSDK","get Item failed: ");
					showToast("Failed");						
				}

				@Override
				public void sfApiStoreNewToken(SFOAuth2Token newAccessToken,
						SFApiQuery<SFUploadSpecification> sfapiApiqueri) {
					saveToken(newAccessToken);
					
				}
			});
		} 
		catch (SFInvalidStateException e) 
		{							
			e.printStackTrace();
			showToast("Exception "+ e.getLocalizedMessage());							
		}
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
				
				callCreateFolderApi(folderName, folderDetails);
				
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
			SFLog.d2("Act", "Exception: %s", Log.getStackTraceString(e));							
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
	
	private synchronized void getContents(final String folderid,final String link)
	{		
		SFFolder folder = getFromCache(folderid);
						
		if(folder!=null)
		{						
			showContentsList(folder);
			return;
		}
		
		SFApiQuery<SFItem> query =null;
				
		
		query = SFItemsEntity.get(folderid);
		
		
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
			FullscreenActivity.mSFApiClient.executeQuery(query, getContentsListener );
		} 
		catch (SFInvalidStateException e) 
		{							
			e.printStackTrace();
			showToast("Exception "+ e.getLocalizedMessage());							
		}
	}
		
	SFApiResponseListener<SFItem> getContentsListener = new SFApiResponseListener<SFItem>() 
	{										
		@Override
		public void sfapiSuccess(final SFItem object) 
		{													
			runOnUiThread(new Runnable() 
			{			
				@Override
				public void run() 
				{				
					if(SFV3ElementType.isFolderType(object))
					{																				
						//mapFolderContents.put(object.getId(), (SFFolder) object);
						storeToCache(object.getId(), (SFFolder) object);
						showContentsList((SFFolder) object);																
					}
				}
			});
								
		}

		@Override
		public void sfApiError(final V3Error v3error,final SFApiQuery<SFItem> asApiqueri) 
		{									
			SFLog.d2("SFSDK","get Item failed: %s" , v3error.message.value);
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
									SFApiRunnable.setUsernamePassword(userName, password);
									FullscreenActivity.mSFApiClient.executeQuery(asApiqueri, getContentsListener );
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

		@Override
		public void sfApiStoreNewToken(SFOAuth2Token newAccessToken,
				SFApiQuery<SFItem> sfapiApiqueri) {
			saveToken(newAccessToken);
			
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
				callGetAccessControlApi();
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
							navigateForward(fid, link);
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
			SFLog.d2("download", "Download Sucess: %d bytes", byteCount);
		}
		
		@Override
		public void downloadFailure(V3Error v3error, long byteCount, SFDownloadSpecification downloaSpec, SFApiClient client) 
		{	
			SFLog.d2("download", "Download failuer: %s", v3error.message);
		}
		
		@Override
		public void bytesDownloaded(long byteCount,SFDownloadSpecification downloaSpec, SFApiClient client) 
		{	
			SFLog.d2("download", "Download progress: %d", byteCount);
		}
	};
	
	private void callDownloadApi(String itemid )
	{
		/*
		SFApiQuery<SFDownloadSpecification> downloadQuery = SFItemsEntity.download(itemid, true);
		
		try {
				FullscreenActivity.mSFApiClient.executeQuery(downloadQuery, new SFApiResponseListener<SFDownloadSpecification>() 
					{

						@Override
						public void sfapiSuccess(SFDownloadSpecification object) 
						{	
							showToast("Start actual download...");
							
							SFLog.d2("download","dspec = %s", object.getDownloadUrl() );
							
							FileOutputStream fileOutpuStream;
							try 
							{
								fileOutpuStream = new FileOutputStream("/storage/sdcard0/sharefile/v3download.bin");
								FullscreenActivity.mSFApiClient.downloadFile(object, 0, fileOutpuStream, mDownloadloadProgressListener);
							} 
							catch (FileNotFoundException | SFInvalidStateException e) 
							{								
								SFLog.d2("download", "!!Exception: %s" , Log.getStackTraceString(e));
							}																					
						}

						@Override
						public void sfApiError(V3Error error,SFApiQuery<SFDownloadSpecification> asApiqueri) 
						{	
							SFLog.d2("download","error = %s" , error.message.value);
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
		navigateForward(TOP, null);
	}
}