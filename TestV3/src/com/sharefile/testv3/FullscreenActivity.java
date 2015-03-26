package com.sharefile.testv3;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONObject;

import com.sharefile.api.SFApiClient;
import com.sharefile.api.SFQueryBuilder;
import com.sharefile.api.SFV3Error;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFInvalidTypeException;
import com.sharefile.api.exceptions.SFJsonException;
import com.sharefile.api.interfaces.IOAuthTokenChangeListener;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFAccount;
import com.sharefile.api.models.SFCapability;
import com.sharefile.api.models.SFODataFeed;
import com.sharefile.api.models.SFShare;
import com.sharefile.java.log.SLog;
import com.sharefile.mobile.shared.dataobjects.v3.SFOAuthAccessToken;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
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
public class FullscreenActivity extends Activity implements IOAuthTokenChangeListener
{
	public static SFOAuth2Token mOAuthToken = null;
	public static final String WEB_LOGIN_CLIENT_ID_SHAREFILE = "qhRBpcI7yj931hV2wzGlmsi6b";
	public static final String WEB_LOGIN_CLIENT_SECRET_SHAREFILE = "Nu8JDCC9EK598e4PmA2NBbF09oYBS8";	 	 
	public static SFApiClient mSFApiClient;
	public static SFOAuthAccessToken gToken = null;
	private final IOAuthTokenChangeListener mTokenChangeListener;
	
	public FullscreenActivity()
	{
		mTokenChangeListener = this;
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

		layoutParams.setMargins(0, 0, 0, 15);
		
		uiButton.setLayoutParams(layoutParams);
		uiButton.setPadding(0,18,0,18);
		uiButton.setText(text);
		uiButton.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Large);
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
			Intent intent = new Intent(getApplicationContext(), FoldersActivity.class);
	    	startActivity(intent);
		}
	};
	
	
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
	
	private OnClickListener getAccessControl = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			/*
			ISFQuery<SFAccessControl> query = SFAccessControlsEntity.get(mSFApiClient.getSession().getPrincipal().getId(),"top");
								
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFAccessControl>() 
				{														

					@Override
					public void sfApiSuccess(SFAccessControl object) 
					{
						SLog.d("SFSDK","getItem success: ");
						showToast("success");						
					}

					@Override
					public void sfApiError(SFV3Error v3error,ISFQuery<SFAccessControl> asApiqueri) 
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
			}*/
		}
	};
	
	private OnClickListener getCapabilities = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			ISFQuery<SFODataFeed<SFCapability>> query = SFQueryBuilder.CAPABILITIES.get();
			
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFODataFeed<SFCapability>>() 
				{														

					@Override
					public void sfApiSuccess(SFODataFeed<SFCapability> object) 
					{
						SLog.d("SFSDK","getItem success: ");
						showToast("success");
					}

					@Override
					public void sfApiError(SFV3Error v3error, ISFQuery<SFODataFeed<SFCapability>> asApiqueri) 
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
	};
	
	private OnClickListener getShares = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			ISFQuery<SFODataFeed<SFShare>> query = SFQueryBuilder.SHARES.get();
			
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFODataFeed<SFShare>>() 
				{														

					@Override
					public void sfApiSuccess(SFODataFeed<SFShare> object) 
					{
						SLog.d("SFSDK","getItem success: ");
						showToast("success");
					}

					@Override
					public void sfApiError(SFV3Error v3error, ISFQuery<SFODataFeed<SFShare>> asApiqueri) 
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
	};
	
	
	private OnClickListener getFavorites = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			/*
			ISFQuery<SFODataFeed<SFFavoriteFolder>> query = SFFavoriteFoldersEntity.getByUser(mSFApiClient.getSession().getPrincipal().getId());
			
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFODataFeed<SFFavoriteFolder>>() 
				{														

					@Override
					public void sfApiSuccess(SFODataFeed<SFFavoriteFolder> object) 
					{
						SLog.d("SFSDK","getItem success: ");
						showToast("success");
					}

					@Override
					public void sfApiError(SFV3Error v3error, ISFQuery<SFODataFeed<SFFavoriteFolder>> asApiqueri) 
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
			}*/
		}
	};
	
	private OnClickListener getAccount = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			ISFQuery<SFAccount> query = SFQueryBuilder.ACCOUNTS.get();
			
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFAccount>() 
				{														

					@Override
					public void sfApiSuccess(SFAccount object) 
					{
						SLog.d("SFSDK","getItem success: ");
						showToast("success");
					}

					@Override
					public void sfApiError(SFV3Error v3error, ISFQuery<SFAccount> asApiqueri) 
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
	};
	
	private OnClickListener getZones = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			/*
			ISFQuery<SFODataFeed<SFZone>> query = SFZonesEntity.get();
			
			try 
			{
				mSFApiClient.executeQuery(query, new SFApiResponseListener<SFODataFeed<SFZone>>() 
				{														

					@Override
					public void sfApiSuccess(SFODataFeed<SFZone> object) 
					{
						SLog.d("SFSDK","getItem success: ");
						showToast("success");
					}

					@Override
					public void sfApiError(SFV3Error v3error, ISFQuery<SFODataFeed<SFZone>> asApiqueri) 
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
			}*/
		}
	};
	
	private OnClickListener getDevices = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{		
		}
	};
	
	private OnClickListener deleteSavedToken = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			PersistantToken.deleteToken(getApplicationContext());
			finish();
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
			new APITest(BASE_BUTTON_ID+6,getDevices,"Get Devices"),
			new APITest(BASE_BUTTON_ID+7,deleteSavedToken,"Delete Saved Token"),
	};
								
	private void addTestButtonsToLayout(LinearLayout layout)
	{
		for(APITest test:mApiTests)
		{
			Button b = getNewButton(test.mButtonId, test.mOnClickListener, test.mText);
			mTestButtons.add(b);
			layout.addView(b);			
			
			if(test.mOnClickListener == deleteSavedToken)
			{
				b.setEnabled(true);
			}
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
	
	private void copyToken()
	{
		if(gToken!=null)
		{
			try
			{
				JSONObject jsonObject = new JSONObject();
										
				jsonObject.put(SFKeywords.ACCESS_TOKEN, gToken.access_token);
				jsonObject.put(SFKeywords.REFRESH_TOKEN,gToken.refresh_token);
				jsonObject.put(SFKeywords.TOKEN_TYPE,gToken.token_type);
				jsonObject.put(SFKeywords.APP_CP,gToken.appcp);
				jsonObject.put(SFKeywords.API_CP,gToken.apicp);
				jsonObject.put(SFKeywords.SUBDOMAIN,gToken.subdomain);
				jsonObject.put(SFKeywords.EXPIRES_IN,gToken.expires_in);
				
				
				mOAuthToken = new SFOAuth2Token(jsonObject.toString());
			}
			catch(Exception e)
			{
				showToast("Copy toke problem");
			}
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.activity_fullscreen);
		
		copyToken();
		
		try 
		{
			SFV3ElementType.registerSubClass(SFV3ElementType.File, String.class);
		} 
		catch (InstantiationException | IllegalAccessException | SFInvalidTypeException e1) 
		{			
			e1.printStackTrace();
		}
		
		try 
		{
			SFV3ElementType.registerSubClass(SFV3ElementType.File, SFFileEx.class);
		} 
		catch (InstantiationException | IllegalAccessException | SFInvalidTypeException e1) 
		{			
			e1.printStackTrace();
		}
		
		Button buttonInitSession = (Button)findViewById(R.id.buttonInitSession);
		
		buttonInitSession.setOnClickListener(new OnClickListener() 
		{			
			@Override
			public void onClick(View v) 
			{	
				
				 showToast("Stating auth");	
				 
				 SLog.d("SFSDK","staring auth task");
				 
				 //SFTask task = new SFTask();			 			 
				 //SFAsyncTask.execute(task, new Object(){});
				 try 
				 {
					mSFApiClient = new SFApiClient(mOAuthToken,"defaultuser",WEB_LOGIN_CLIENT_ID_SHAREFILE,WEB_LOGIN_CLIENT_SECRET_SHAREFILE,mTokenChangeListener);
					changeTestButtons(true);
				 } 
				 catch (SFInvalidStateException e) 
				 {					
					e.printStackTrace();
				 }
				 
			
				/*
				FileOutputStream f1=  fileOpen("/storage/sdcard0/a.txt");
				FileOutputStream f2= fileOpen("/storage/sdcard0/a.txt");
				
				writeToFile(f1, "file1");
				writeToFile(f2, "file2");
				
				safeClose(f1);
				safeClose(f2);
				*/
			}
		});
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayoutTestButtons);
		
		addTestButtonsToLayout(layout);
				
		
	}
	
	private void safeClose(FileOutputStream fos)
	{
		if(fos!=null)
		{			
			try 
			{
				fos.close();
			} 
			catch (IOException e) 
			{
				SLog.d("ShareFile", Log.getStackTraceString(e));
			}
		}
	}
	
	private void writeToFile(FileOutputStream f1,String str)
	{
		if(f1!=null)
		{
			try 
			{
				f1.write(str.getBytes());
				f1.flush();
			} 
			catch (IOException e) 
			{
				SLog.d("ShareFile", Log.getStackTraceString(e));
			}
		}
	}
	
	private FileOutputStream fileOpen(String path)
	{
		FileOutputStream fos = null;
		
		try
		{
			fos = new FileOutputStream(path,true);
		}
		catch(Exception e)
		{
			SLog.d("ShareFile", Log.getStackTraceString(e));
		}
		
		return fos;
	}

	@Override
	public void sfApiStoreNewToken(SFOAuth2Token newAccessToken) 
	{		
		saveToken(newAccessToken);
	}	
}
