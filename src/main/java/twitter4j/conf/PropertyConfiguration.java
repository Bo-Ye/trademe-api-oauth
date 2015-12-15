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

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class PropertyConfiguration extends ConfigurationBase  {

    

    private static final String OAUTH_CONSUMER_KEY = "oauth.consumerKey";
    private static final String OAUTH_CONSUMER_SECRET = "oauth.consumerSecret";
    

    private static final String OAUTH_REQUEST_TOKEN_URL = "oauth.requestTokenURL";
    private static final String OAUTH_AUTHORIZATION_URL = "oauth.authorizationURL";
    private static final String OAUTH_ACCESS_TOKEN_URL = "oauth.accessTokenURL";
    






    PropertyConfiguration(String treePath) {
        super();
        Properties props;
        // load from system properties
        try {
            props = (Properties) System.getProperties().clone();
            try {
                Map<String, String> envMap = System.getenv();
                for (String key : envMap.keySet()) {
                    props.setProperty(key, envMap.get(key));
                }
            } catch (SecurityException ignore) {
            }
            normalize(props);
        } catch (SecurityException ignore) {
            // Unsigned applets are not allowed to access System properties
            props = new Properties();
        }
        final String TWITTER4J_PROPERTIES = "twitter4j.properties";
        // override System properties with ./twitter4j.properties in the classpath
        loadProperties(props, "." + File.separatorChar + TWITTER4J_PROPERTIES);
        // then, override with /twitter4j.properties in the classpath
        loadProperties(props, Configuration.class.getResourceAsStream("/" + TWITTER4J_PROPERTIES));
        // then, override with /WEB/INF/twitter4j.properties in the classpath
        loadProperties(props, Configuration.class.getResourceAsStream("/WEB-INF/" + TWITTER4J_PROPERTIES));
        // for Google App Engine
        try {
            loadProperties(props, new FileInputStream("WEB-INF/" + TWITTER4J_PROPERTIES));
        } catch (SecurityException ignore) {
        } catch (FileNotFoundException ignore) {
        }

        setFieldsWithTreePath(props, treePath);
    }

    /**
     * Creates a root PropertyConfiguration. This constructor is equivalent to new PropertyConfiguration("/").
     */
    PropertyConfiguration() {
        this("/");
    }

    private boolean notNull(Properties props, String prefix, String name) {
        return props.getProperty(prefix + name) != null;
    }

    private boolean loadProperties(Properties props, String path) {
        FileInputStream fis = null;
        try {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                fis = new FileInputStream(file);
                props.load(fis);
                normalize(props);
                return true;
            }
        } catch (Exception ignore) {
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ignore) {

            }
        }
        return false;
    }

    private boolean loadProperties(Properties props, InputStream is) {
        try {
            props.load(is);
            normalize(props);
            return true;
        } catch (Exception ignore) {
        }
        return false;
    }

    private void normalize(Properties props) {
        ArrayList<String> toBeNormalized = new ArrayList<String>(10);
        for (Object key : props.keySet()) {
            String keyStr = (String) key;
            if (-1 != (keyStr.indexOf("twitter4j."))) {
                toBeNormalized.add(keyStr);
            }
        }
        for (String keyStr : toBeNormalized) {
            String property = props.getProperty(keyStr);
            int index = keyStr.indexOf("twitter4j.");
            String newKey = keyStr.substring(0, index) + keyStr.substring(index + 10);
            props.setProperty(newKey, property);
        }
    }

    /**
     * passing "/foo/bar" as treePath will result:<br>
     * 1. load [twitter4j.]restBaseURL<br>
     * 2. override the value with foo.[twitter4j.]restBaseURL<br>
     * 3. override the value with foo.bar.[twitter4j.]restBaseURL<br>
     *
     * @param props    properties to be loaded
     * @param treePath the path
     */
    private void setFieldsWithTreePath(Properties props, String treePath) {
        setFieldsWithPrefix(props, "");
        String[] splitArray = treePath.split("/");
        String prefix = null;
        for (String split : splitArray) {
            if (!"".equals(split)) {
                if (null == prefix) {
                    prefix = split + ".";
                } else {
                    prefix += split + ".";
                }
                setFieldsWithPrefix(props, prefix);
            }
        }
    }

    private void setFieldsWithPrefix(Properties props, String prefix) {
        
        if (notNull(props, prefix, OAUTH_CONSUMER_KEY)) {
            setOAuthConsumerKey(getString(props, prefix, OAUTH_CONSUMER_KEY));
        }
        if (notNull(props, prefix, OAUTH_CONSUMER_SECRET)) {
            setOAuthConsumerSecret(getString(props, prefix, OAUTH_CONSUMER_SECRET));
        }
        

        if (notNull(props, prefix, OAUTH_REQUEST_TOKEN_URL)) {
            setOAuthRequestTokenURL(getString(props, prefix, OAUTH_REQUEST_TOKEN_URL));
        }

        if (notNull(props, prefix, OAUTH_AUTHORIZATION_URL)) {
            setOAuthAuthorizationURL(getString(props, prefix, OAUTH_AUTHORIZATION_URL));
        }

        if (notNull(props, prefix, OAUTH_ACCESS_TOKEN_URL)) {
            setOAuthAccessTokenURL(getString(props, prefix, OAUTH_ACCESS_TOKEN_URL));
        }

       
        cacheInstance();
    }



    String getString(Properties props, String prefix, String name) {
        return props.getProperty(prefix + name);
    }

    // assures equality after deserialization
    @Override
    protected Object readResolve() throws ObjectStreamException {
        return super.readResolve();
    }

}
