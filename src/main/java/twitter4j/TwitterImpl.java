/*
 * Copyright (C) 2007 Yusuke Yamamoto
 * Copyright (C) 2011 Twitter, Inc.
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

import twitter4j.auth.Authorization;
import twitter4j.conf.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A java representation of the <a href="https://dev.twitter.com/docs/api">Twitter REST API</a><br>
 * This class is thread safe and can be cached/re-used and used concurrently.<br>
 * Currently this class is not carefully designed to be extended. It is suggested to extend this class only for mock testing purpose.<br>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
class TwitterImpl extends TwitterBaseImpl implements Twitter {

    private static final ConcurrentHashMap<Configuration, HttpParameter[]> implicitParamsMap = new ConcurrentHashMap<Configuration, HttpParameter[]>();
    private static final ConcurrentHashMap<Configuration, String> implicitParamsStrMap = new ConcurrentHashMap<Configuration, String>();
    private final String IMPLICIT_PARAMS_STR;
    private final HttpParameter[] IMPLICIT_PARAMS;


    /*package*/
    TwitterImpl(Configuration conf, Authorization auth) {
        super(conf, auth);

        if (implicitParamsMap.containsKey(conf)) {
            this.IMPLICIT_PARAMS = implicitParamsMap.get(conf);
            this.IMPLICIT_PARAMS_STR = implicitParamsStrMap.get(conf);
        } else {
            String implicitParamsStr = "";


            List<HttpParameter> params = new ArrayList<HttpParameter>(3);

            HttpParameter[] implicitParams = params.toArray(new HttpParameter[params.size()]);

            // implicitParamsMap.containsKey() is evaluated in the above if clause.
            // thus implicitParamsStrMap needs to be initialized first
            implicitParamsStrMap.putIfAbsent(conf, implicitParamsStr);
            implicitParamsMap.putIfAbsent(conf, implicitParams);

            this.IMPLICIT_PARAMS = implicitParams;
            this.IMPLICIT_PARAMS_STR = implicitParamsStr;
        }
    }


    private void addParameterToList(List<HttpParameter> colors,
                                    String paramName, String color) {
        if (color != null) {
            colors.add(new HttpParameter(paramName, color));
        }
    }











    public HttpResponse get(String url) throws TwitterException {
        ensureAuthorizationEnabled();
        if (IMPLICIT_PARAMS_STR.length() > 0) {
            if (url.contains("?")) {
                url = url + "&" + IMPLICIT_PARAMS_STR;
            } else {
                url = url + "?" + IMPLICIT_PARAMS_STR;
            }
        }

            return http.get(url, null, auth, this);

    }

    private HttpResponse get(String url, HttpParameter... params) throws TwitterException {
        ensureAuthorizationEnabled();

            return http.get(url, mergeImplicitParams(params), auth, this);

    }

    private HttpResponse post(String url) throws TwitterException {
        ensureAuthorizationEnabled();

            return http.post(url, IMPLICIT_PARAMS, auth, this);

    }

    private HttpResponse post(String url, HttpParameter... params) throws TwitterException {
        ensureAuthorizationEnabled();

            return http.post(url, mergeImplicitParams(params), auth, this);

    }

    private HttpParameter[] mergeParameters(HttpParameter[] params1, HttpParameter[] params2) {
        if (params1 != null && params2 != null) {
            HttpParameter[] params = new HttpParameter[params1.length + params2.length];
            System.arraycopy(params1, 0, params, 0, params1.length);
            System.arraycopy(params2, 0, params, params1.length, params2.length);
            return params;
        }
        if (null == params1 && null == params2) {
            return new HttpParameter[0];
        }
        if (params1 != null) {
            return params1;
        } else {
            return params2;
        }
    }

    private HttpParameter[] mergeParameters(HttpParameter[] params1, HttpParameter params2) {
        if (params1 != null && params2 != null) {
            HttpParameter[] params = new HttpParameter[params1.length + 1];
            System.arraycopy(params1, 0, params, 0, params1.length);
            params[params.length - 1] = params2;
            return params;
        }
        if (null == params1 && null == params2) {
            return new HttpParameter[0];
        }
        if (params1 != null) {
            return params1;
        } else {
            return new HttpParameter[]{params2};
        }
    }

    private HttpParameter[] mergeImplicitParams(HttpParameter... params) {
        return mergeParameters(params, IMPLICIT_PARAMS);
    }

    private boolean isOk(HttpResponse response) {
        return response != null && response.getStatusCode() < 300;
    }
}
