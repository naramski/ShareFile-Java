package com.sharefile.testv3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.sharefile.api.SFApiQuery;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.entities.SFItemsEntity;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFItem;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
			String id = mFolderIdStack.pop();
			
			getContents(id);
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
	
	private synchronized void getContents(final String folderid)
	{
		confirmExit = false;
		SFFolder folder = getFromCache(folderid);
						
		if(folder!=null)
		{
			mCurrentFolderId = folderid;
			showContentsList(folder);
			return;
		}
		
		SFApiQuery<SFItem> query =null;
				
		
		if(folderid.equalsIgnoreCase(TOP))
		{
			query = SFItemsEntity.get();
		}
		else
		{
			query = SFItemsEntity.get(folderid);
		}
		
		query.addQueryString("expand", "Children");	
		
		showBusy(true);
		try 
		{
			FullscreenActivity.mSFApiClient.executeQuery(query, new SFApiResponseListener<SFItem>() 
			{										
				@Override
				public void sfapiSuccess(final SFItem object) 
				{										
					//SFLog.d2("SFSDK","getItem success: ");
					//showToast("Got Item");
					mCurrentFolderId = folderid;
					
					runOnUiThread(new Runnable() 
					{			
						@Override
						public void run() 
						{				
							if(object instanceof SFFolder)
							{								
								mapFolderContents.put(folderid, (SFFolder) object);
								showContentsList((SFFolder) object);																
							}
						}
					});
										
				}

				@Override
				public void sfApiError(int errorCode,String errorMessage,SFApiQuery<SFItem> asApiqueri) 
				{									
					SFLog.d2("SFSDK","get Item failed: ");
					showToast("Failed Get Item");
					
					runOnUiThread(new Runnable() 
					{			
						@Override
						public void run() 
						{																			
							showBusy(false);							
						}
					});
				}
			});
		} 
		catch (SFInvalidStateException e) 
		{							
			e.printStackTrace();
			showToast("Exception "+ e.getLocalizedMessage());							
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{		
		thisActivity = this;
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.folder);
		
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
					if(item instanceof SFFolder) 
					{						
							String fid = item.getId();	
							mFolderIdStack.push(mCurrentFolderId);
							getContents(fid);						
						
					}
				}
			}
         }); 										
	}
	
	private String mCurrentFolderId = null;
	
	@Override
	protected void onStart() 
	{		
		super.onStart();				
		getContents(TOP);
	}
}