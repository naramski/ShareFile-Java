package com.sharefile.testv3;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.sharefile.api.SFApiClient;
import com.sharefile.api.SFApiQuery;
import com.sharefile.api.SFHttpPostUtils;
import com.sharefile.api.V3Error;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.entities.SFItemsEntity;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.https.SFApiRunnable;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFSymbolicLink;

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
				
			});
		} 
		catch (SFInvalidStateException e) 
		{			
			e.printStackTrace();
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
			mFolderIdStack.pop();
			
			String id = mFolderIdStack.peek();
			
			getContents(id,null);
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
		
	private Stack<String> mFolderIdStack = new Stack<String>();
	
	private SFFolder getFromCache(String folderid)
	{		
		return mapFolderContents.get(folderid);
	}
	
	private synchronized void getContents(final String folderid,final String link)
	{
		confirmExit = false;
		SFFolder folder = getFromCache(folderid);
						
		if(folder!=null)
		{			
			mFolderIdStack.push(folderid);
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
						mFolderIdStack.push(object.getId());
						mapFolderContents.put(object.getId(), (SFFolder) object);
						showContentsList((SFFolder) object);																
					}
				}
			});
								
		}

		@Override
		public void sfApiError(final V3Error v3error,final SFApiQuery<SFItem> asApiqueri) 
		{									
			SFLog.d2("SFSDK","get Item failed: " + v3error.message.value);
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
														
							getContents(fid,link);												
					}
				}
			}
         }); 	
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
		getContents(TOP,null);
	}
}