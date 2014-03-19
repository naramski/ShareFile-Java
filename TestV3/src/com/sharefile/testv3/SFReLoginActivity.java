package com.sharefile.testv3;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.gson.JsonObject;
import com.sharefile.api.SFHttpPostUtils;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.mobile.shared.AlertDialogFinishDelegate;
import com.sharefile.mobile.shared.Utils;
import com.sharefile.mobile.shared.NTLMWebView.SFGetAuthCredentials;
import com.sharefile.mobile.shared.NTLMWebView.SFWebView;
import com.sharefile.mobile.shared.NTLMWebView.SFWebViewClient;
import com.sharefile.mobile.shared.dataobjects.v3.SFOAuthAccessToken;
import com.sharefile.mobile.shared.dataobjects.v3.SFOAuthCode;
import com.sharefile.mobile.shared.https.api.SFAPICaller;
import com.sharefile.mobile.shared.https.api.v3.SFV3ApiCaller.GetWebAuthAccessToken;
import com.sharefile.mobile.shared.https.api.v3.SFV3ApiCaller.GetWebAuthAccessTokenCallback;

public class SFReLoginActivity extends Activity implements com.sharefile.mobile.shared.NTLMWebView.SFWebloginInterface, GetWebAuthAccessTokenCallback
{	
	private WebView m_webViewLogin = null;
	private ProgressBar mThrobber = null;	
	protected boolean isLimitedMode = false;
	/**
	 * 	if the account uses ShareFile login show the WebView to handle the relogin process
	 */
	private void initShareFileWebView()
	{		
		setContentView(R.layout.webpop_login);
		
		View container = findViewById(R.id.WebPopLogin_mainLayout);
		m_webViewLogin = (WebView)findViewById(R.id.WebPopLogin_webview);	
		mThrobber = (ProgressBar) findViewById(R.id.WebPopLogin__throbber);
		
		initTopBarButtonHandlers(container);
		
		
	}
	
	private void initTopBarButtonHandlers(View container) {
		
		ImageButton closeButton = (ImageButton)container.findViewById(R.id.WebPopLogin_CloseButton);
		closeButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
						
		container.findViewById(R.id.WebPopLogin_info);
		
	}
	
	private boolean readSavedToken()
	{
		try 
		{
			FullscreenActivity.gToken = null;
			FullscreenActivity.mOAuthToken = PersistantToken.readToken(this);
			
			if(FullscreenActivity.mOAuthToken!=null && FullscreenActivity.mOAuthToken.isValid())
			{
				return true;				
			}
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	protected void onCreate(Bundle bundle) 
	{		
		super.onCreate(bundle);
													
		try 
		{
			String str = "https://szqatest2.sharefiletest.com/cifs/v3/Items(4L24TVJSEz6Ca22LWoZg41hIVgfFgqQx0GD2VoYSgXA_)";
			URI uri = new URI(str);
			
			SFLog.d2("", "%s", uri.toString());
		} 
		catch (URISyntaxException e) 
		{			
			e.printStackTrace();
		}
		
		if(readSavedToken())
		{			
			startSession(true);
			return;
		}		
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				
		
		initShareFileWebView();
		
		if(isNetworkConnected())
		{
			if(m_webViewLogin instanceof SFWebView)
			{
				((SFWebView)m_webViewLogin).initProgressBar(mThrobber);			
			}
			
			SFWebViewClient.startWebviewLogin(m_webViewLogin,SFAPICaller.buildWebLoginUrl(".sharefile.com"), this, ".sharefile.com");
			
			disableUI();
		}								
	}
		
	
	public boolean isNetworkConnected()
	{	
		boolean ret = false;
		
		ConnectivityManager connectivityMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
						
		NetworkInfo ninfo = connectivityMgr.getActiveNetworkInfo();
		
		if(ninfo !=null && ninfo.isConnected() && ninfo.isAvailable())
		{
			ret = true;
		}
						
		return ret;
	}
			
	protected void disableUI()
	{
		if(mThrobber!=null)
		{
			mThrobber.setVisibility(View.VISIBLE);
		}
	}
	
	protected void enableUI()
	{
		if(mThrobber!=null)
		{
			mThrobber.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public void proceedWithCredentials(HttpAuthHandler handler)
	{
		Utils.showWebLoginCredsDialog(this,handler);		
	}
	
	@Override
	public void handleOAuthCode(SFOAuthCode code) 
	{			
		disableUI();
		if(code != null && code.apicp!=null && code.subdomain != null)
		{
			Utils.instrument(false, "Read token from: " + code);
			GetWebAuthAccessToken.start(SFAPICaller.buildWebLoginTokenUrl(code.apicp, code.subdomain), code, this);
		}
		else
		{
			//Toast.makeText(this, "Error authenticating to ShareFile", Toast.LENGTH_LONG).show();
			//finish();
			
			final Activity thisActivity = this;
			
			Utils.showAlertDialog(thisActivity, "ShareFile", getString(R.string.strOAuthError),new AlertDialogFinishDelegate() 
			{				
				@Override
				public void dialogDone() 
				{					
					thisActivity.finish();
				}
			});
		}
	}

	private void saveToken()
	{
		if(FullscreenActivity.gToken!=null)
		{
			try
			{
				JSONObject jsonObject = new JSONObject();
										
				jsonObject.put(SFKeywords.ACCESS_TOKEN, FullscreenActivity.gToken.access_token);
				jsonObject.put(SFKeywords.REFRESH_TOKEN,FullscreenActivity.gToken.refresh_token);
				jsonObject.put(SFKeywords.TOKEN_TYPE,FullscreenActivity.gToken.token_type);
				jsonObject.put(SFKeywords.APP_CP,FullscreenActivity.gToken.appcp);
				jsonObject.put(SFKeywords.API_CP,FullscreenActivity.gToken.apicp);
				jsonObject.put(SFKeywords.SUBDOMAIN,FullscreenActivity.gToken.subdomain);
				jsonObject.put(SFKeywords.EXPIRES_IN,FullscreenActivity.gToken.expires_in);				
				
				PersistantToken.saveToken(this, jsonObject.toString());
				
				SFLog.d2("","SAVED token!!");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void successGetAccessToken(SFOAuthAccessToken accessToken) 
	{
		enableUI();		
		
		try 
		{
			FullscreenActivity.gToken = accessToken;			
			saveToken();						
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
		}
		
		startSession(true);    	    	
	}
	
	private void startSession(boolean finishCurrentActivity)
	{
		Intent intent = new Intent(getApplicationContext(), FullscreenActivity.class);
    	startActivity(intent);
    	
    	if(finishCurrentActivity)
    	{
    		finish();
    	}
	}

	@Override
	public void errorGetAccessToken(int error_code, String errMessage)
	{
		enableUI();		
		Utils.instrument(false, "!!!errorGetAccessToken = " + errMessage, "webview");				
	}
	
	@Override
	public void proceedWithCredentials(SFGetAuthCredentials handler,String sendbackurl) 
	{
		Utils.showWebLoginCredsDialog(this,handler,sendbackurl);		
	}		
}
