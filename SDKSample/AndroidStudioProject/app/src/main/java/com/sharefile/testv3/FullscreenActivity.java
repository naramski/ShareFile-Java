package com.sharefile.testv3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.citrix.sharefile.api.SFApiClient;
import com.citrix.sharefile.api.authentication.SFOAuth2Token;
import com.citrix.sharefile.api.exceptions.SFInvalidStateException;
import com.citrix.sharefile.api.exceptions.SFJsonException;
import com.citrix.sharefile.api.exceptions.SFSDKException;
import com.citrix.sharefile.api.interfaces.IOAuthTokenCallback;
import com.citrix.sharefile.api.interfaces.ISFApiClient;
import com.sharefile.testv3.Core.Core;

import java.io.IOException;
import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 */
public class FullscreenActivity extends Activity
{
    private static final String TAG = "FullscreenActivity";
    private static final SFLogger SLog = new SFLogger();
    private ProgressBar progressBar;

	public FullscreenActivity()
	{
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

	private OnClickListener getItems = new OnClickListener() 
	{		
		@Override
		public void onClick(View v) 
		{
			Intent intent = new Intent(getApplicationContext(), FoldersActivity.class);
	    	startActivity(intent);
		}
	};

	private APITest[] mApiTests = 
	{
			//new APITest(BASE_BUTTON_ID,getItems,"Get Items"),
			/*new APITest(BASE_BUTTON_ID+1,getAccessControl,"Get Access Control"),
			new APITest(BASE_BUTTON_ID+2,getCapabilities,"Get Capabilities"),
			new APITest(BASE_BUTTON_ID+3,getShares,"Get Shares"),
			new APITest(BASE_BUTTON_ID+4,getFavorites,"Get Favorite Folders"),
			new APITest(BASE_BUTTON_ID+5,getAccount,"Get Account"),
			new APITest(BASE_BUTTON_ID+5,getZones,"Get Zones"),
			new APITest(BASE_BUTTON_ID+6,getDevices,"Get Devices"),
			new APITest(BASE_BUTTON_ID+7,deleteSavedToken,"Delete Saved Token"),*/
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

    private void initApiClientAndLaunchFolderActivity(SFOAuth2Token token) throws SFInvalidStateException
    {
        ISFApiClient apiClient = new SFApiClient(token);

        Core.setApiClient(apiClient);

        changeTestButtons(true);

        Intent intent = new Intent(getApplicationContext(), FoldersActivity.class);

        startActivity(intent);
    }

    private void showBusy(final boolean val)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                progressBar.setVisibility(val?View.VISIBLE:View.GONE);
            }
        });
    }

    IOAuthTokenCallback mTokenCallback = new IOAuthTokenCallback()
    {
        @Override
        public void onSuccess(SFOAuth2Token token)
        {
            showBusy(false);

            try
            {

                PersistantToken.saveToken(getApplicationContext(),token);
                initApiClientAndLaunchFolderActivity(token);
            }
            catch (Exception e)
            {
                Log.e(TAG, "", e);
            }
        }

        @Override
        public void onError(SFSDKException exception)
        {
            showBusy(false);
            showToast(exception.getLocalizedMessage());
            changeTestButtons(false);
        }
    };

    private void initUI()
    {
        final EditText txtUsername = (EditText) findViewById(R.id.editTextUserName);
        final EditText txtPassword = (EditText) findViewById(R.id.editTextPassword);
        final EditText txtSubdomain = (EditText) findViewById(R.id.editTextSubdomain);
        progressBar = (ProgressBar)findViewById(R.id.progressbar);

        Button login = (Button) findViewById(R.id.buttonLogin);
        login.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String userName = txtUsername.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();
                String subdomain = txtSubdomain.getText().toString().trim();

                //no validations done!!
                showBusy(true);
                Core.getOAuthToken(getApplicationContext(),userName,password,subdomain,mTokenCallback);
            }
        });
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.activity_fullscreen);

        initUI();

	}

    @Override
    protected void onStart()
    {
        super.onStart();

        if(!Core.isIsInitialised())
        {
            finish();
            return;
        }

        try
        {
            SFOAuth2Token token = PersistantToken.readToken(getApplicationContext());

            if(token!=null)
            {
               initApiClientAndLaunchFolderActivity(token);
               finish();
            }
        }
        catch (Exception e)
        {
            showToast("No saved AuthToken found. Please login manually");
        }
    }
}