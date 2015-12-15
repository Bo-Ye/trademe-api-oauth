/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import twitter4j.auth.*;
import twitter4j.conf.Configuration;

/**
 * Base class of Twitter / AsyncTwitter / TwitterStream supports OAuth.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
abstract class TwitterBaseImpl implements TwitterBase,  OAuthSupport,  HttpResponseListener {
    private static final String WWW_DETAILS = "See http://twitter4j.org/en/configuration.html for details. See and register at http://apps.twitter.com/";


    Configuration conf;


    transient HttpClient http;




    Authorization auth;

    /*package*/ TwitterBaseImpl(Configuration conf, Authorization auth) {
        this.conf = conf;
        this.auth = auth;
        init();
    }

    private void init() {
        if (null == auth) {
            // try to populate OAuthAuthorization if available in the configuration
            String consumerKey = conf.getOAuthConsumerKey();
            String consumerSecret = conf.getOAuthConsumerSecret();
            // try to find oauth tokens in the configuration
            if (consumerKey != null && consumerSecret != null) {

                    OAuthAuthorization oauth = new OAuthAuthorization(conf);

                    this.auth = oauth;

            }
        }
        http = HttpClientFactory.getInstance(conf.getHttpClientConfiguration());

    }















    @Override
    public void httpResponseReceived(HttpResponseEvent event) {




    }

    @Override
    public final Authorization getAuthorization() {
        return auth;
    }

    @Override
    public Configuration getConfiguration() {
        return this.conf;
    }

    final void ensureAuthorizationEnabled() {
        if (!auth.isEnabled()) {
            throw new IllegalStateException(
                "Authentication credentials are missing. " + WWW_DETAILS);
        }
    }

    final void ensureOAuthEnabled() {
        if (!(auth instanceof OAuthAuthorization)) {
            throw new IllegalStateException(
                "OAuth required. Authentication credentials are missing. " + WWW_DETAILS);
        }
    }




    // methods declared in OAuthSupport interface

    @Override
    public synchronized void setOAuthConsumer(String consumerKey, String consumerSecret) {
        if (null == consumerKey) {
            throw new NullPointerException("consumer key is null");
        }
        if (null == consumerSecret) {
            throw new NullPointerException("consumer secret is null");
        }
         if (auth instanceof OAuthAuthorization ) {
            throw new IllegalStateException("consumer key/secret pair already set.");
        }
    }

    @Override
    public RequestToken getOAuthRequestToken() throws TwitterException {
        return getOAuthRequestToken(null);
    }

    @Override
    public RequestToken getOAuthRequestToken(String callbackUrl) throws TwitterException {
        return getOAuth().getOAuthRequestToken(callbackUrl);
    }

    @Override
    public RequestToken getOAuthRequestToken(String callbackUrl, String xAuthAccessType) throws TwitterException {
        return getOAuth().getOAuthRequestToken(callbackUrl, xAuthAccessType);
    }

    @Override
    public RequestToken getOAuthRequestToken(String callbackUrl, String xAuthAccessType, String xAuthMode) throws TwitterException {
        return getOAuth().getOAuthRequestToken(callbackUrl, xAuthAccessType, xAuthMode);
    }

    /**
     * {@inheritDoc}
     * Basic authenticated instance of this class will try acquiring an AccessToken using xAuth.<br>
     * In order to get access acquire AccessToken using xAuth, you must apply by sending an email to <a href="mailto:api@twitter.com">api@twitter.com</a> all other applications will receive an HTTP 401 error.  Web-based applications will not be granted access, except on a temporary basis for when they are converting from basic-authentication support to full OAuth support.<br>
     * Storage of Twitter usernames and passwords is forbidden. By using xAuth, you are required to store only access tokens and access token secrets. If the access token expires or is expunged by a user, you must ask for their login and password again before exchanging the credentials for an access token.
     *
     * @throws TwitterException When Twitter service or network is unavailable, when the user has not authorized, or when the client application is not permitted to use xAuth
     * @see <a href="https://dev.twitter.com/docs/oauth/xauth">xAuth | Twitter Developers</a>
     */
    @Override
    public synchronized AccessToken getOAuthAccessToken() throws TwitterException {
        Authorization auth = getAuthorization();
        AccessToken oauthAccessToken;

            if (auth instanceof XAuthAuthorization) {
                XAuthAuthorization xauth = (XAuthAuthorization) auth;
                this.auth = xauth;
                OAuthAuthorization oauthAuth = new OAuthAuthorization(conf);
                oauthAuth.setOAuthConsumer(xauth.getConsumerKey(), xauth.getConsumerSecret());
                oauthAccessToken = oauthAuth.getOAuthAccessToken(xauth.getUserId(), xauth.getPassword());
            } else {
                oauthAccessToken = getOAuth().getOAuthAccessToken();
            }


        return oauthAccessToken;
    }


    @Override
    public synchronized AccessToken getOAuthAccessToken(String oauthVerifier) throws TwitterException {
        AccessToken oauthAccessToken = getOAuth().getOAuthAccessToken(oauthVerifier);

        return oauthAccessToken;
    }

    @Override
    public synchronized AccessToken getOAuthAccessToken(RequestToken requestToken) throws TwitterException {
        OAuthSupport oauth = getOAuth();
        AccessToken oauthAccessToken = oauth.getOAuthAccessToken(requestToken);

        return oauthAccessToken;
    }

    @Override
    public synchronized AccessToken getOAuthAccessToken(RequestToken requestToken, String oauthVerifier) throws TwitterException {
        return getOAuth().getOAuthAccessToken(requestToken, oauthVerifier);
    }

    @Override
    public synchronized void setOAuthAccessToken(AccessToken accessToken) {
        getOAuth().setOAuthAccessToken(accessToken);
    }

    @Override
    public synchronized AccessToken getOAuthAccessToken(String screenName, String password) throws TwitterException {
        return getOAuth().getOAuthAccessToken(screenName, password);
    }
    /* OAuth support methods */

    private OAuthSupport getOAuth() {
        if (!(auth instanceof OAuthSupport)) {
            throw new IllegalStateException(
                "OAuth consumer key/secret combination not supplied");
        }
        return (OAuthSupport) auth;
    }










}
