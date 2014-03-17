package com.sharefile.testv3;

import java.io.InputStream;
import java.util.ArrayList;

import com.sharefile.api.SFApiClient;
import com.sharefile.api.SFApiQuery;
import com.sharefile.api.android.utils.SFAsyncTask;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.authentication.SFOAuthSimpleAuthenticator;
import com.sharefile.api.entities.SFAccessControlsEntity;
import com.sharefile.api.entities.SFAccountsEntity;
import com.sharefile.api.entities.SFCapabilitiesEntity;
import com.sharefile.api.entities.SFFavoriteFoldersEntity;
import com.sharefile.api.entities.SFItemsEntity;
import com.sharefile.api.entities.SFSharesEntity;
import com.sharefile.api.entities.SFZonesEntity;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.interfaces.SFApiClientInitListener;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFAccessControl;
import com.sharefile.api.models.SFAccount;
import com.sharefile.api.models.SFCapability;
import com.sharefile.api.models.SFFavoriteFolder;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataFeed;
import com.sharefile.api.models.SFShare;
import com.sharefile.api.models.SFZone;

import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity 
{
	private SFOAuth2Token mOAuthToken = null;
	public static final String WEB_LOGIN_CLIENT_ID_SHAREFILE = "qhRBpcI7yj931hV2wzGlmsi6b";
	public static final String WEB_LOGIN_CLIENT_SECRET_SHAREFILE = "Nu8JDCC9EK598e4PmA2NBbF09oYBS8";	 	 
	private SFApiClient mSFApiClient;
	
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
	
	public class SFTask extends AsyncTask<Object, Object, Object>
	{

		@Override
		protected Object doInBackground(Object... params) 
		{			
			
			String hostname = "citrix.sharefile.com";
			String username = "nilesh.pawar@citrix.com";
			String password = "*";
			String clientId = WEB_LOGIN_CLIENT_ID_SHAREFILE;
			String clientSecret = WEB_LOGIN_CLIENT_SECRET_SHAREFILE;
			
			try 
			{
				mOAuthToken =  SFOAuthSimpleAuthenticator.authenticate(hostname, clientId, clientSecret, username, password);
				
				SFLog.d2("SFSDK","GOT Token = %s",mOAuthToken.toJsonString());
				
				mSFApiClient = new SFApiClient(mOAuthToken);
				
				mSFApiClient.init(new SFApiClientInitListener() 
				{
					
					@Override
					public void sfApiClientInitSuccess() 
					{
						SFLog.d2("SFSDK","SFApiclient Init Success: ");
						showToast("Got session");
						
						changeTestButtons(true);
						
					}
					
					
					@Override
					public void sfApiClientInitError(int errorCode, String errorMessage) 
					{												
						showToast("Error "+ errorMessage);						
						SFLog.d2("SFSDK","Error: ",errorMessage);
						changeTestButtons(false);
					}
				});				
			} 
			catch (Exception e) 
			{												
				showToast("Exception "+ e.getLocalizedMessage());					
				 				 
				SFLog.d2("","%s",Log.getStackTraceString(e));
			} 			
						
			return null;
		}
		
	}
	
	private String readFolder()
	{
		try 
		{
	        Resources res = getResources();
	        InputStream in_s = res.openRawResource(R.raw.v3folder);

	        byte[] b = new byte[in_s.available()];
	        in_s.read(b);
	        return new String(b);
	    } 
		catch (Exception e) 
	    {	        
	    }
		
		return null;
	}
	
	
	private ArrayList<Button> mTestButtons = new ArrayList<Button>();
	
	
	private Button getNewButton(int id, OnClickListener clickListener,String text)
	{
		final Button uiButton = new Button(this);				
		uiButton.setOnClickListener(clickListener);
		final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

		layoutParams.setMargins(0, 0, 0, 5);
		
		uiButton.setLayoutParams(layoutParams);
		uiButton.setPadding(0,0,0,0);
		uiButton.setText(text);
		uiButton.setId(id);
		uiButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bkg));
		uiButton.setEnabled(false);		
				
		return uiButton;
	}
	
	public static class APITest
	{
		public int mButtonId = 0;
		public OnClickListener mOnClickListener = null;
		public String mText;		
		
		public APITest(int id,OnClickListener clickLickClickListener, String text)
		{
			mButtonId = id;
			mOnClickListener = clickLickClickListener;
			mText = text ;					
		}
	}
	
	private static final int BASE_BUTTON_ID = R.id.buttonInitSession + 1;
	
	private OnClickListener getItems = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			
			SFApiQuery<SFItem> query = SFItemsEntity.get();	
			query.addQueryString("expand", "Children");			
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFItem>() 
				{										
					@Override
					public void sfapiSuccess(SFItem object) 
					{										
						SFLog.d2("SFSDK","getItem success: ");
						showToast("Got Item");
					}

					@Override
					public void sfApiError(int errorCode,String errorMessage,SFApiQuery<SFItem> asApiqueri) 
					{									
						SFLog.d2("SFSDK","get Item failed: ");
						showToast("Failed Get Item");
					}
				});
			} 
			catch (SFInvalidStateException e) 
			{							
				e.printStackTrace();
				showToast("Exception "+ e.getLocalizedMessage());							
			}
		}
	};
	
	
	private OnClickListener getAccessControl = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			SFApiQuery<SFAccessControl> query = SFAccessControlsEntity.get(mSFApiClient.getSession().getPrincipal().getId(),"top");
								
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFAccessControl>() 
				{														

					@Override
					public void sfapiSuccess(SFAccessControl object) 
					{
						SFLog.d2("SFSDK","getItem success: ");
						showToast("success");						
					}

					@Override
					public void sfApiError(int errorCode, String errorMessage,SFApiQuery<SFAccessControl> asApiqueri) 
					{
						SFLog.d2("SFSDK","get Item failed: ");
						showToast("Failed");						
					}
				});
			} 
			catch (SFInvalidStateException e) 
			{							
				e.printStackTrace();
				showToast("Exception "+ e.getLocalizedMessage());							
			}
		}
	};
	
	private OnClickListener getCapabilities = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			SFApiQuery<SFODataFeed<SFCapability>> query = SFCapabilitiesEntity.get();
			
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFODataFeed<SFCapability>>() 
				{														

					@Override
					public void sfapiSuccess(SFODataFeed<SFCapability> object) 
					{
						SFLog.d2("SFSDK","getItem success: ");
						showToast("success");
					}

					@Override
					public void sfApiError(int errorCode, String errorMessage, SFApiQuery<SFODataFeed<SFCapability>> asApiqueri) 
					{						
						SFLog.d2("SFSDK","get Item failed: ");
						showToast("Failed");
					}
				});
			} 
			catch (SFInvalidStateException e) 
			{							
				e.printStackTrace();
				showToast("Exception "+ e.getLocalizedMessage());							
			}
		}
	};
	
	private OnClickListener getShares = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			SFApiQuery<SFODataFeed<SFShare>> query = SFSharesEntity.get();
			
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFODataFeed<SFShare>>() 
				{														

					@Override
					public void sfapiSuccess(SFODataFeed<SFShare> object) 
					{
						SFLog.d2("SFSDK","getItem success: ");
						showToast("success");
					}

					@Override
					public void sfApiError(int errorCode, String errorMessage, SFApiQuery<SFODataFeed<SFShare>> asApiqueri) 
					{						
						SFLog.d2("SFSDK","get Item failed: ");
						showToast("Failed");
					}
				});
			} 
			catch (SFInvalidStateException e) 
			{							
				e.printStackTrace();
				showToast("Exception "+ e.getLocalizedMessage());							
			}
		}
	};
	
	
	private OnClickListener getFavorites = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			SFApiQuery<SFODataFeed<SFFavoriteFolder>> query = SFFavoriteFoldersEntity.getByUser(mSFApiClient.getSession().getPrincipal().getId());
			
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFODataFeed<SFFavoriteFolder>>() 
				{														

					@Override
					public void sfapiSuccess(SFODataFeed<SFFavoriteFolder> object) 
					{
						SFLog.d2("SFSDK","getItem success: ");
						showToast("success");
					}

					@Override
					public void sfApiError(int errorCode, String errorMessage, SFApiQuery<SFODataFeed<SFFavoriteFolder>> asApiqueri) 
					{						
						SFLog.d2("SFSDK","get Item failed: ");
						showToast("Failed");
					}
				});
			} 
			catch (SFInvalidStateException e) 
			{							
				e.printStackTrace();
				showToast("Exception "+ e.getLocalizedMessage());							
			}
		}
	};
	
	private OnClickListener getAccount = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			SFApiQuery<SFAccount> query = SFAccountsEntity.get();
			
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFAccount>() 
				{														

					@Override
					public void sfapiSuccess(SFAccount object) 
					{
						SFLog.d2("SFSDK","getItem success: ");
						showToast("success");
					}

					@Override
					public void sfApiError(int errorCode, String errorMessage, SFApiQuery<SFAccount> asApiqueri) 
					{						
						SFLog.d2("SFSDK","get Item failed: ");
						showToast("Failed");
					}
				});
			} 
			catch (SFInvalidStateException e) 
			{							
				e.printStackTrace();
				showToast("Exception "+ e.getLocalizedMessage());							
			}
		}
	};
	
	private OnClickListener getZones = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			SFApiQuery<SFZone> query = SFZonesEntity.get();
			
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFZone>() 
				{														

					@Override
					public void sfapiSuccess(SFZone object) 
					{
						SFLog.d2("SFSDK","getItem success: ");
						showToast("success");
					}

					@Override
					public void sfApiError(int errorCode, String errorMessage, SFApiQuery<SFZone> asApiqueri) 
					{						
						SFLog.d2("SFSDK","get Item failed: ");
						showToast("Failed");
					}
				});
			} 
			catch (SFInvalidStateException e) 
			{							
				e.printStackTrace();
				showToast("Exception "+ e.getLocalizedMessage());							
			}
		}
	};
	
	private APITest[] mApiTests = 
	{
			new APITest(BASE_BUTTON_ID,getItems,"Get Items"),
			new APITest(BASE_BUTTON_ID+1,getAccessControl,"Get Access Control"),
			new APITest(BASE_BUTTON_ID+2,getCapabilities,"Get Capabilities"),
			new APITest(BASE_BUTTON_ID+3,getShares,"Get Shares"),
			new APITest(BASE_BUTTON_ID+4,getFavorites,"Get Favorite Folders"),
			new APITest(BASE_BUTTON_ID+5,getAccount,"Get Account"),
			new APITest(BASE_BUTTON_ID+5,getZones,"Get Zones"),
	};
						
	private void addTestButtonsToLayout(LinearLayout layout)
	{
		for(APITest test:mApiTests)
		{
			Button b = getNewButton(test.mButtonId, test.mOnClickListener, test.mText);
			mTestButtons.add(b);
			layout.addView(b);			
		}
	}
	
	private void changeTestButtons(final boolean enable)
	{
		runOnUiThread(new Runnable() 
		{			
			@Override
			public void run() 
			{				
				for(Button b:mTestButtons)
				{						
					b.setEnabled(enable);
					b.invalidate();
				}
			}
		});
				
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		
		Button buttonInitSession = (Button)findViewById(R.id.buttonInitSession);
		
		buttonInitSession.setOnClickListener(new OnClickListener() 
		{			
			@Override
			public void onClick(View v) 
			{												
				 showToast("Stating auth");	
				 
				 SFLog.d2("SFSDK","staring auth task");
				 
				 SFTask task = new SFTask();			 			 
				 SFAsyncTask.execute(task, new Object(){});				 				
			}
		});
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayoutTestButtons);
		
		addTestButtonsToLayout(layout);
				
		
	}	
}
