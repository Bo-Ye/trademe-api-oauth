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

package twitter4j.conf;

import twitter4j.HttpClientConfiguration;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration base class with default settings.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
class ConfigurationBase implements Configuration {

    private HttpClientConfiguration httpConf;



    private String oAuthConsumerKey = null;
    private String oAuthConsumerSecret = null;
    private String oAuthAccessToken = null;

    private String oAuthRequestTokenURL = "https://api.twitter.com/oauth/request_token";
    private String oAuthAuthorizationURL = "https://api.twitter.com/oauth/authorize";
    private String oAuthAccessTokenURL = "https://api.twitter.com/oauth/access_token";

    private int httpRetryCount = 0;
    private int httpRetryIntervalSeconds = 5;


    protected ConfigurationBase() {
        httpConf = new MyHttpClientConfiguration(null // proxy host
                , null // proxy user
                , null // proxy password
                , -1 // proxy port
                , 20000 // connection timeout
                , 120000 // read timeout
                , false // pretty debug
                , true // gzip enabled
        );
    }

    class MyHttpClientConfiguration implements HttpClientConfiguration {
        private static final long serialVersionUID = 8226866124868861058L;
        private String httpProxyHost = null;
        private String httpProxyUser = null;
        private String httpProxyPassword = null;
        private int httpProxyPort = -1;
        private int httpConnectionTimeout = 20000;
        private int httpReadTimeout = 120000;
        private boolean prettyDebug = false;
        private boolean gzipEnabled = true;

        MyHttpClientConfiguration(String httpProxyHost, String httpProxyUser, String httpProxyPassword, int httpProxyPort, int httpConnectionTimeout, int httpReadTimeout, boolean prettyDebug, boolean gzipEnabled) {
            this.httpProxyHost = httpProxyHost;
            this.httpProxyUser = httpProxyUser;
            this.httpProxyPassword = httpProxyPassword;
            this.httpProxyPort = httpProxyPort;
            this.httpConnectionTimeout = httpConnectionTimeout;
            this.httpReadTimeout = httpReadTimeout;
            this.prettyDebug = prettyDebug;
            this.gzipEnabled = gzipEnabled;
        }

        @Override
        public String getHttpProxyHost() {
            return httpProxyHost;
        }

        @Override
        public int getHttpProxyPort() {
            return httpProxyPort;
        }

        @Override
        public String getHttpProxyUser() {
            return httpProxyUser;
        }

        @Override
        public String getHttpProxyPassword() {
            return httpProxyPassword;
        }

        @Override
        public int getHttpConnectionTimeout() {
            return httpConnectionTimeout;
        }

        @Override
        public int getHttpReadTimeout() {
            return httpReadTimeout;
        }

        @Override
        public int getHttpRetryCount() {
            return httpRetryCount;
        }

        @Override
        public int getHttpRetryIntervalSeconds() {
            return httpRetryIntervalSeconds;
        }


        @Override
        public boolean isPrettyDebugEnabled() {
            return prettyDebug;
        }

        @Override
        public boolean isGZIPEnabled() {
            return gzipEnabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyHttpClientConfiguration that = (MyHttpClientConfiguration) o;

            if (gzipEnabled != that.gzipEnabled) return false;
            if (httpConnectionTimeout != that.httpConnectionTimeout) return false;
            if (httpProxyPort != that.httpProxyPort) return false;
            if (httpReadTimeout != that.httpReadTimeout) return false;
            if (prettyDebug != that.prettyDebug) return false;
            if (httpProxyHost != null ? !httpProxyHost.equals(that.httpProxyHost) : that.httpProxyHost != null)
                return false;
            if (httpProxyPassword != null ? !httpProxyPassword.equals(that.httpProxyPassword) : that.httpProxyPassword != null)
                return false;
            if (httpProxyUser != null ? !httpProxyUser.equals(that.httpProxyUser) : that.httpProxyUser != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = httpProxyHost != null ? httpProxyHost.hashCode() : 0;
            result = 31 * result + (httpProxyUser != null ? httpProxyUser.hashCode() : 0);
            result = 31 * result + (httpProxyPassword != null ? httpProxyPassword.hashCode() : 0);
            result = 31 * result + httpProxyPort;
            result = 31 * result + httpConnectionTimeout;
            result = 31 * result + httpReadTimeout;
            result = 31 * result + (prettyDebug ? 1 : 0);
            result = 31 * result + (gzipEnabled ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "MyHttpClientConfiguration{" +
                    "httpProxyHost='" + httpProxyHost + '\'' +
                    ", httpProxyUser='" + httpProxyUser + '\'' +
                    ", httpProxyPassword='" + httpProxyPassword + '\'' +
                    ", httpProxyPort=" + httpProxyPort +
                    ", httpConnectionTimeout=" + httpConnectionTimeout +
                    ", httpReadTimeout=" + httpReadTimeout +
                    ", prettyDebug=" + prettyDebug +
                    ", gzipEnabled=" + gzipEnabled +
                    '}';
        }
    }






    @Override
    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpConf;
    }



    protected final void setPrettyDebugEnabled(boolean prettyDebug) {
        httpConf = new MyHttpClientConfiguration(httpConf.getHttpProxyHost()
                , httpConf.getHttpProxyUser()
                , httpConf.getHttpProxyPassword()
                , httpConf.getHttpProxyPort()
                , httpConf.getHttpConnectionTimeout()
                , httpConf.getHttpReadTimeout()
                , prettyDebug
                , httpConf.isGZIPEnabled()
        );
    }

    protected final void setGZIPEnabled(boolean gzipEnabled) {
        httpConf = new MyHttpClientConfiguration(httpConf.getHttpProxyHost()
                , httpConf.getHttpProxyUser()
                , httpConf.getHttpProxyPassword()
                , httpConf.getHttpProxyPort()
                , httpConf.getHttpConnectionTimeout()
                , httpConf.getHttpReadTimeout()
                , httpConf.isPrettyDebugEnabled()
                , gzipEnabled
        );
    }

    // methods for HttpClientConfiguration

    protected final void setHttpProxyHost(String proxyHost) {
        httpConf = new MyHttpClientConfiguration(proxyHost
                , httpConf.getHttpProxyUser()
                , httpConf.getHttpProxyPassword()
                , httpConf.getHttpProxyPort()
                , httpConf.getHttpConnectionTimeout()
                , httpConf.getHttpReadTimeout()
                , httpConf.isPrettyDebugEnabled()
                , httpConf.isGZIPEnabled()
        );
    }

    protected final void setHttpProxyUser(String proxyUser) {
        httpConf = new MyHttpClientConfiguration(httpConf.getHttpProxyHost()
                , proxyUser
                , httpConf.getHttpProxyPassword()
                , httpConf.getHttpProxyPort()
                , httpConf.getHttpConnectionTimeout()
                , httpConf.getHttpReadTimeout()
                , httpConf.isPrettyDebugEnabled()
                , httpConf.isGZIPEnabled()
        );
    }

    protected final void setHttpProxyPassword(String proxyPassword) {
        httpConf = new MyHttpClientConfiguration(httpConf.getHttpProxyHost()
                , httpConf.getHttpProxyUser()
                , proxyPassword
                , httpConf.getHttpProxyPort()
                , httpConf.getHttpConnectionTimeout()
                , httpConf.getHttpReadTimeout()
                , httpConf.isPrettyDebugEnabled()
                , httpConf.isGZIPEnabled()
        );
    }

    protected final void setHttpProxyPort(int proxyPort) {
        httpConf = new MyHttpClientConfiguration(httpConf.getHttpProxyHost()
                , httpConf.getHttpProxyUser()
                , httpConf.getHttpProxyPassword()
                , proxyPort
                , httpConf.getHttpConnectionTimeout()
                , httpConf.getHttpReadTimeout()
                , httpConf.isPrettyDebugEnabled()
                , httpConf.isGZIPEnabled()
        );
    }

    protected final void setHttpConnectionTimeout(int connectionTimeout) {
        httpConf = new MyHttpClientConfiguration(httpConf.getHttpProxyHost()
                , httpConf.getHttpProxyUser()
                , httpConf.getHttpProxyPassword()
                , httpConf.getHttpProxyPort()
                , connectionTimeout
                , httpConf.getHttpReadTimeout()
                , httpConf.isPrettyDebugEnabled()
                , httpConf.isGZIPEnabled()
        );
    }

    protected final void setHttpReadTimeout(int readTimeout) {
        httpConf = new MyHttpClientConfiguration(httpConf.getHttpProxyHost()
                , httpConf.getHttpProxyUser()
                , httpConf.getHttpProxyPassword()
                , httpConf.getHttpProxyPort()
                , httpConf.getHttpConnectionTimeout()
                , readTimeout
                , httpConf.isPrettyDebugEnabled()
                , httpConf.isGZIPEnabled()
        );
    }



    // oauth related setter/getters

    @Override
    public final String getOAuthConsumerKey() {
        return oAuthConsumerKey;
    }

    protected final void setOAuthConsumerKey(String oAuthConsumerKey) {
        this.oAuthConsumerKey = oAuthConsumerKey;
    }

    @Override
    public final String getOAuthConsumerSecret() {
        return oAuthConsumerSecret;
    }

    protected final void setOAuthConsumerSecret(String oAuthConsumerSecret) {
        this.oAuthConsumerSecret = oAuthConsumerSecret;
    }

    @Override
    public String getOAuthAccessToken() {
        return oAuthAccessToken;
    }

    protected final void setOAuthAccessToken(String oAuthAccessToken) {
        this.oAuthAccessToken = oAuthAccessToken;
    }





















    @Override
    public String getOAuthRequestTokenURL() {
        return oAuthRequestTokenURL;
    }

    protected final void setOAuthRequestTokenURL(String oAuthRequestTokenURL) {
        this.oAuthRequestTokenURL = oAuthRequestTokenURL;
    }

    @Override
    public String getOAuthAuthorizationURL() {
        return oAuthAuthorizationURL;
    }

    protected final void setOAuthAuthorizationURL(String oAuthAuthorizationURL) {
        this.oAuthAuthorizationURL = oAuthAuthorizationURL;
    }

    @Override
    public String getOAuthAccessTokenURL() {
        return oAuthAccessTokenURL;
    }

    protected final void setOAuthAccessTokenURL(String oAuthAccessTokenURL) {
        this.oAuthAccessTokenURL = oAuthAccessTokenURL;
    }






















    static String fixURL(boolean useSSL, String url) {
        if (null == url) {
            return null;
        }
        int index = url.indexOf("://");
        if (-1 == index) {
            throw new IllegalArgumentException("url should contain '://'");
        }
        String hostAndLater = url.substring(index + 3);
        if (useSSL) {
            return "https://" + hostAndLater;
        } else {
            return "http://" + hostAndLater;
        }
    }



    private static final List<ConfigurationBase> instances = new ArrayList<ConfigurationBase>();

    private static void cacheInstance(ConfigurationBase conf) {
        if (!instances.contains(conf)) {
            instances.add(conf);
        }
    }

    protected void cacheInstance() {
        cacheInstance(this);
    }

    private static ConfigurationBase getInstance(ConfigurationBase configurationBase) {
        int index;
        if ((index = instances.indexOf(configurationBase)) == -1) {
            instances.add(configurationBase);
            return configurationBase;
        } else {
            return instances.get(index);
        }
    }

    // assures equality after deserializedation
    protected Object readResolve() throws ObjectStreamException {
        return getInstance(this);
    }
}
