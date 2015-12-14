package com.boye.trademe;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by boy on 11/12/15.
 */
public class Authorization {
    private static Authorization instance;

    private Authorization(){

    }

    public static synchronized Authorization getInstance(){
        if(instance==null){
            instance = new Authorization();
        }
        return instance;
    }

    private static final String CALLBACK_URL = "http://localhost:8080/trademe-api-oauth/watch-list-callback";

    private Twitter twitter = TwitterFactory.getSingleton();

    public String getAuthorizationURL() throws TwitterException {
        RequestToken requestToken = twitter.getOAuthRequestToken(CALLBACK_URL);
        String token = requestToken.getToken();
        System.out.println("token: " + token);
        return requestToken.getAuthorizationURL();
    }

    public AccessToken getAccessToken(String oauthVerifier) throws TwitterException {
        return twitter.getOAuthAccessToken(oauthVerifier);
    }

    public void setAccessToken(AccessToken accessToken){
       twitter.setOAuthAccessToken(accessToken);
    }

    public String call(String url) throws TwitterException {
        return twitter.get(url).asString();
    }
}
