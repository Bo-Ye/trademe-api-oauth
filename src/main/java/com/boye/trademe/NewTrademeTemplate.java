package com.boye.trademe;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import twitter4j.BASE64Encoder;
import twitter4j.HttpParameter;
import twitter4j.TwitterException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by boy on 11/12/15.
 */
public class NewTrademeTemplate {
    //properties
    private static final String OAUTH_COSUMER_KEY = "";
    private static final String OAUTH_COSUMER_SECRET = "";
    private static final String OAUTH_REQUEST_TOKEN_URL = "https://secure.tmsandbox.co.nz/Oauth/RequestToken";
    private static final String OAUTH_AUTHORIZATION_URL = "https://secure.tmsandbox.co.nz/Oauth/Authorize";
    private static final String OAUTH_ACCESS_TOKEN_URL = "https://secure.tmsandbox.co.nz/Oauth/AccessToken";
    //callback
    private static final String CALLBACK_URL = "http://localhost:8080/trademe-api-oauth/watch-list-callback";
    //constants
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final HttpParameter OAUTH_SIGNATURE_METHOD = new HttpParameter("oauth_signature_method", "HMAC-SHA1");
    private static final Random RAND = new Random();
    //singleton
    private static NewTrademeTemplate instance;

    private NewTrademeTemplate() {

    }

    public static synchronized NewTrademeTemplate getInstance() {
        if (instance == null) {
            instance = new NewTrademeTemplate();
        }
        return instance;
    }

    private  String token;
    private  String tokenSecret;
    private  SecretKeySpec secretKeySpec;

    //help methods
    private List<HttpParameter> toParamList(HttpParameter[] params) {
        List<HttpParameter> paramList = new ArrayList<HttpParameter>(params.length);
        paramList.addAll(Arrays.asList(params));
        return paramList;
    }

    private String constructRequestURL(String url) {
        int index = url.indexOf("?");
        if (-1 != index) {
            url = url.substring(0, index);
        }
        int slashIndex = url.indexOf("/", 8);
        String baseURL = url.substring(0, slashIndex).toLowerCase();
        int colonIndex = baseURL.indexOf(":", 8);
        if (-1 != colonIndex) {
            // url contains port number
            if (baseURL.startsWith("http://") && baseURL.endsWith(":80")) {
                // http default port 80 MUST be excluded
                baseURL = baseURL.substring(0, colonIndex);
            } else if (baseURL.startsWith("https://") && baseURL.endsWith(":443")) {
                // http default port 443 MUST be excluded
                baseURL = baseURL.substring(0, colonIndex);
            }
        }
        url = baseURL + url.substring(slashIndex);
        return url;
    }

    private String normalizeRequestParameters(List<HttpParameter> params) {
        Collections.sort(params);
        return encodeParameters(params);
    }

    public String encodeParameters(List<HttpParameter> httpParams) {
        return encodeParameters(httpParams, "&", false);
    }

    public String encodeParameters(List<HttpParameter> httpParams, String splitter, boolean quot) {
        StringBuilder buf = new StringBuilder();
        for (HttpParameter param : httpParams) {
            if (!param.isFile()) {
                if (buf.length() != 0) {
                    if (quot) {
                        buf.append("\"");
                    }
                    buf.append(splitter);
                }
                buf.append(HttpParameter.encode(param.getName())).append("=");
                if (quot) {
                    buf.append("\"");
                }
                buf.append(HttpParameter.encode(param.getValue()));
            }
        }
        if (buf.length() != 0) {
            if (quot) {
                buf.append("\"");
            }
        }
        return buf.toString();
    }

    private void parseGetParameters(String url, List<HttpParameter> signatureBaseParams) throws UnsupportedEncodingException {
        int queryStart = url.indexOf("?");
        if (-1 != queryStart) {
            url.split("&");
            String[] queryStrs = url.substring(queryStart + 1).split("&");
            for (String query : queryStrs) {
                String[] split = query.split("=");
                if (split.length == 2) {
                    signatureBaseParams.add(new HttpParameter(URLDecoder.decode(split[0], "UTF-8"), URLDecoder.decode(split[1], "UTF-8")));
                } else {
                    signatureBaseParams.add(new HttpParameter(URLDecoder.decode(split[0], "UTF-8"), ""));
                }
            }
        }
    }

    private String generateSignature(String data, String token, String tokenSecret, SecretKeySpec secretKeySpec) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] byteHMAC = null;
        Mac mac = Mac.getInstance(HMAC_SHA1);
        SecretKeySpec spec;
        if (null == token) {
            String oauthSignature = HttpParameter.encode(OAUTH_COSUMER_SECRET) + "&";
            System.out.println("In new oauthSignature: " + oauthSignature);
            spec = new SecretKeySpec(oauthSignature.getBytes(), HMAC_SHA1);
        } else {
            spec = secretKeySpec;
            if (null == spec) {
                String oauthSignature = HttpParameter.encode(OAUTH_COSUMER_SECRET) + "&" + HttpParameter.encode(tokenSecret);
                System.out.println("In new oauthSignature: " + oauthSignature);
                spec = new SecretKeySpec(oauthSignature.getBytes(), HMAC_SHA1);
                this.secretKeySpec = spec;
            }
        }
        mac.init(spec);
        byteHMAC = mac.doFinal(data.getBytes());
        return BASE64Encoder.encode(byteHMAC);
    }

    public String generateAuthorizationHeader(String method, String url, HttpParameter[] params, String nonce, String timestamp, String token, String tokenSecret, SecretKeySpec secretKeySpec) {
        try {
            if (null == params) {
                params = new HttpParameter[0];
            }
            List<HttpParameter> oauthHeaderParams = new ArrayList<HttpParameter>(5);
            oauthHeaderParams.add(new HttpParameter("oauth_consumer_key", OAUTH_COSUMER_KEY));
            oauthHeaderParams.add(OAUTH_SIGNATURE_METHOD);
            oauthHeaderParams.add(new HttpParameter("oauth_timestamp", timestamp));
            oauthHeaderParams.add(new HttpParameter("oauth_nonce", nonce));
            oauthHeaderParams.add(new HttpParameter("oauth_version", "1.0"));
            if (token != null) {
                oauthHeaderParams.add(new HttpParameter("oauth_token", token));
            }
            List<HttpParameter> signatureBaseParams = new ArrayList<HttpParameter>(oauthHeaderParams.size() + params.length);
            signatureBaseParams.addAll(oauthHeaderParams);
            if (!HttpParameter.containsFile(params)) {
                signatureBaseParams.addAll(toParamList(params));
            }
            parseGetParameters(url, signatureBaseParams);
            StringBuilder base = new StringBuilder(method).append("&").append(HttpParameter.encode(constructRequestURL(url))).append("&");
            base.append(HttpParameter.encode(normalizeRequestParameters(signatureBaseParams)));
            String oauthBaseString = base.toString();
            System.out.println("In new the base string: " + oauthBaseString);
            String signature = generateSignature(oauthBaseString, token, tokenSecret, secretKeySpec);
            System.out.println("In new the signature: " + signature);
            oauthHeaderParams.add(new HttpParameter("oauth_signature", signature));
            String result = "OAuth " + encodeParameters(oauthHeaderParams, ",", true);
            System.out.println("In new the generated auth header: " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String generateAuthorizationHeader(String method, String url, HttpParameter[] params, String token, String tokenSecret, SecretKeySpec secretKeySpec) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        long timestamp = System.currentTimeMillis() / 1000;
        long nonce = timestamp + RAND.nextInt();
        return this.generateAuthorizationHeader(method, url, params, String.valueOf(nonce), String.valueOf(timestamp), token, tokenSecret, secretKeySpec);
    }

    private String getParameter(String[] responseStr, String parameter) {
        String value = null;
        for (String str : responseStr) {
            if (str.startsWith(parameter + '=')) {
                value = str.split("=")[1].trim();
                break;
            }
        }
        return value;
    }

    /**
     * Step 1: get request token to combine authorization URL.
     *
     * @return
     * @throws TwitterException
     * @throws IOException
     * @throws ClientProtocolException
     */
    public String getAuthorizationURL() throws Exception {
        List<HttpParameter> params = new ArrayList<HttpParameter>();
        params.add(new HttpParameter("oauth_callback", CALLBACK_URL));
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        String authorizationHeader = this.generateAuthorizationHeader("POST", OAUTH_REQUEST_TOKEN_URL, parameters, null,null, null);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(OAUTH_REQUEST_TOKEN_URL);
        httpPost.addHeader("Authorization", authorizationHeader);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        String body = HttpParameter.encodeParameters(parameters);
        httpPost.setEntity(new StringEntity(body));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            System.out.println("request token string: " + result);
            String[] responseStr = result.split("&");
            this.token = getParameter(responseStr, "oauth_token");
            this.tokenSecret = getParameter(responseStr, "oauth_token_secret");
            return OAUTH_AUTHORIZATION_URL + "?oauth_token=" + token;
        }
    }

    /**
     * Step 3: set up access token by oauth_verifier from step 2.
     *
     * @param oauthVerifier
     * @return
     * @throws TwitterException
     */
    public void setUpAccessToken(String oauthVerifier) throws Exception {
        List<HttpParameter> params = new ArrayList<HttpParameter>();
        params.add(new HttpParameter("oauth_verifier", oauthVerifier));
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        String authorizationHeader = this.generateAuthorizationHeader("POST", OAUTH_ACCESS_TOKEN_URL, parameters, this.token, this.tokenSecret, this.secretKeySpec);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(OAUTH_ACCESS_TOKEN_URL);
        httpPost.addHeader("Authorization", authorizationHeader);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        String body = HttpParameter.encodeParameters(parameters);
        httpPost.setEntity(new StringEntity(body));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            System.out.println("access token result: " + result);
            String[] responseStr = result.split("&");
            this.token = getParameter(responseStr, "oauth_token");
            this.tokenSecret = getParameter(responseStr, "oauth_token_secret");
        }
    }

    /**
     * Step 4: call API.
     *
     * @param url
     * @return
     * @throws TwitterException
     */
    public String call(String url) throws TwitterException {
        //return twitter.get(url).asString();
        return null;
    }
}
