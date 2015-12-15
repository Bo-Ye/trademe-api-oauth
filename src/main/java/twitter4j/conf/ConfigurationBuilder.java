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

/**
 * A builder that can be used to construct a twitter4j configuration with desired settings.  This
 * builder has sensible defaults such that {@code new ConfigurationBuilder().build()} would create a
 * usable configuration.  This configuration builder is useful for clients that wish to configure
 * twitter4j in unit tests or from command line flags for example.
 *
 * @author John Sirois - john.sirois at gmail.com
 */
public final class ConfigurationBuilder {

    private ConfigurationBase configurationBean = new PropertyConfiguration();





    public ConfigurationBuilder setOAuthConsumerKey(String oAuthConsumerKey) {
        checkNotBuilt();
        configurationBean.setOAuthConsumerKey(oAuthConsumerKey);
        return this;
    }

    public ConfigurationBuilder setOAuthConsumerSecret(String oAuthConsumerSecret) {
        checkNotBuilt();
        configurationBean.setOAuthConsumerSecret(oAuthConsumerSecret);
        return this;
    }

    public ConfigurationBuilder setOAuthAccessToken(String oAuthAccessToken) {
        checkNotBuilt();
        configurationBean.setOAuthAccessToken(oAuthAccessToken);
        return this;
    }







    public ConfigurationBuilder setOAuthRequestTokenURL(String oAuthRequestTokenURL) {
        checkNotBuilt();
        configurationBean.setOAuthRequestTokenURL(oAuthRequestTokenURL);
        return this;
    }

    public ConfigurationBuilder setOAuthAuthorizationURL(String oAuthAuthorizationURL) {
        checkNotBuilt();
        configurationBean.setOAuthAuthorizationURL(oAuthAuthorizationURL);
        return this;
    }

    public ConfigurationBuilder setOAuthAccessTokenURL(String oAuthAccessTokenURL) {
        checkNotBuilt();
        configurationBean.setOAuthAccessTokenURL(oAuthAccessTokenURL);
        return this;
    }











































    public Configuration build() {
        checkNotBuilt();
        configurationBean.cacheInstance();
        try {
            return configurationBean;
        } finally {
            configurationBean = null;
        }
    }

    private void checkNotBuilt() {
        if (configurationBean == null) {
            throw new IllegalStateException("Cannot use this builder any longer, build() has already been called");
        }
    }
}
