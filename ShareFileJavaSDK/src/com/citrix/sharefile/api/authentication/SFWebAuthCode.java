package com.citrix.sharefile.api.authentication;


import com.citrix.sharefile.api.log.Logger;

/**
	This is the code we receive from the WebPop as a part of the finishUrl.
    This class can take the finish URL and correctly parse out the necessary fields from it.
    The SFWebAuthCode can be used to obtaine the OAuthToken using the SFOAuthService.
 */
public class SFWebAuthCode
{
	private static final String TAG = "SFWebAuthCode";
	private static final String CODE = "code";
	private static final String STATE = "state";
	private static final String SUBDOMAIN = "subdomain";
	private static final String APPCP = "appcp";
	private static final String APICP = "apicp";

	public String mCode;
	public String mState;
	public String mSubDomain;
	public String mAppCp;
	public String mApiCp;

	public SFWebAuthCode()
	{

	}

	public SFWebAuthCode(String url)
	{
		parseTokensFromURL(url);
	}
	
	/**
	 * parse redirection url to extract parameters
	 * will return null if required parameters are not found
	 * @param url
	 * @return 
	 */
	public static SFWebAuthCode parse(String url) {
		SFWebAuthCode code = new SFWebAuthCode(url);
		if(code.mApiCp ==null || code.mSubDomain == null) {
			Logger.e(TAG, "Couldn't parse URL");
			return null;
		}
		
		return code;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(CODE + "=" + mCode + ",");
		sb.append(STATE+ "=" + mState + ",");
		sb.append(SUBDOMAIN +"=" + mSubDomain+",");
		sb.append(APPCP +"="+ mAppCp+ ",");
		sb.append(APICP + mApiCp);
		
		return sb.toString();
	}

	/**
	 *   http://secure.sharefiletest.com/oauth/oauthcomplete.aspx
	 *   ?code=C0K9TSkdf6DyTlvJEGSVjwDmbLFCtV
	 *   &state=1234
	 *   &subdomain=zachTest
	 *   &appcp=sharefiletest.com
	 *   &apicp=sharefiletest.com
	 */
	public void parseTokensFromURL(String url)
	{
		String[]split1 = url.split("\\?");
		
		if(split1.length>1)
		{
			String response = split1[1];
			
			if(response.length()>0)
			{
				String[]splitparams=response.split("\\&");
				
				for(int i=0;i<splitparams.length;i++)
				{
					String[]namevalue=splitparams[i].split("=");
					
					if(namevalue.length>1)
					{
						String name = namevalue[0];
						String value = namevalue[1];
													
						if(name.equalsIgnoreCase(CODE))
						{
							mCode = value;
						}
						else if(name.equalsIgnoreCase(STATE))
						{
							mState = value;
						}
						else if(name.equalsIgnoreCase(SUBDOMAIN))
						{
							mSubDomain = value;
						}
						else if(name.equalsIgnoreCase(APPCP))
						{
							mAppCp = value;
						}
						else if(name.equalsIgnoreCase(APICP))
						{
							mApiCp = value;
						}
					}
				}
			}
		}
		
		Logger.v(TAG, "Parsed: " + toString());
	}
}
