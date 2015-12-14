package com.boye.trademe;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by boy on 11/12/15.
 */
public class TrademeTemplate {
	private static TrademeTemplate instance;

	private TrademeTemplate() {

	}

	public static synchronized TrademeTemplate getInstance() {
		if (instance == null) {
			instance = new TrademeTemplate();
		}
		return instance;
	}

	private static final String CALLBACK_URL = "http://localhost:8080/trademe-api-oauth/watch-list-callback";

	private Twitter twitter = TwitterFactory.getSingleton();

	/**
	 * Step 1: get request token to combine authorization URL.
	 * 
	 * @return
	 * @throws TwitterException
	 */
	public String getAuthorizationURL() throws TwitterException {
		RequestToken requestToken = twitter.getOAuthRequestToken(CALLBACK_URL);
		System.out.println("step 1, request token: " + requestToken.getToken());
		return requestToken.getAuthorizationURL();
	}

	/**
	 * Step 3: set up access token by oauth_verifier from step 2.
	 * 
	 * @param oauthVerifier
	 * @return
	 * @throws TwitterException
	 */
	public void setUpAccessToken(String oauthVerifier) throws TwitterException {
		AccessToken accessToken = twitter.getOAuthAccessToken(oauthVerifier);
		System.out.println("step 3, access token: " + accessToken.getToken());
		twitter.setOAuthAccessToken(accessToken);
	}

	/**
	 * Step 4: call API.
	 * 
	 * @param url
	 * @return
	 * @throws TwitterException
	 */
	public String call(String url) throws TwitterException {
		return twitter.get(url).asString();
	}
}
