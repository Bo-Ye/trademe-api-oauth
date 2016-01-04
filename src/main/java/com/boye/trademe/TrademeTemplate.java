package com.boye.trademe;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by boy on 11/12/15.
 */
public class TrademeTemplate {

    //properties
    private static final String OAUTH_COSUMER_KEY = "<place your one>";
    private static final String OAUTH_COSUMER_SECRET = "<place your one>";
    private static final String OAUTH_REQUEST_TOKEN_URL = "https://secure.tmsandbox.co.nz/Oauth/RequestToken?scope=MyTradeMeRead,MyTradeMeWrite";
    private static final String OAUTH_AUTHORIZATION_URL = "https://secure.tmsandbox.co.nz/Oauth/Authorize";
    private static final String OAUTH_ACCESS_TOKEN_URL = "https://secure.tmsandbox.co.nz/Oauth/AccessToken";
    //constants
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final Random RAND = new Random();
    //singleton
    private static TrademeTemplate instance;
    //variables
    private String token;
    private String tokenSecret;

    private TrademeTemplate() {

    }

    public static synchronized TrademeTemplate getInstance() {
        if (instance == null) {
            instance = new TrademeTemplate();
        }
        return instance;
    }

    //////encoding is intensively applied////
    /**
     * special URL encoding
     *
     * @param value
     * @return
     */
    private String encodeParameter(String value) {
        try {
            String encoded = URLEncoder.encode(value, "UTF-8");
            StringBuilder buf = new StringBuilder(encoded.length());
            char focus;
            for (int i = 0; i < encoded.length(); i++) {
                focus = encoded.charAt(i);
                if (focus == '*') {
                    buf.append("%2A");
                } else if (focus == '+') {
                    buf.append("%20");
                } else if (focus == '%' && (i + 1) < encoded.length() && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                    buf.append('~');
                    i += 2;
                } else {
                    buf.append(focus);
                }
            }
            return buf.toString();
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("Impossible exception!", e);
        }
    }

    private String encodeParametersToString(Map<String, String> params, String splitter, boolean quot) {
        StringBuilder buf = new StringBuilder();
        params.entrySet().stream().forEach((param) -> {
            if (buf.length() != 0) {
                if (quot) {
                    buf.append("\"");
                }
                buf.append(splitter);
            }
            buf.append(encodeParameter(param.getKey()));
            buf.append("=");
            if (quot) {
                buf.append("\"");
            }
            buf.append(encodeParameter(param.getValue()));
        });
        if (buf.length() != 0) {
            if (quot) {
                buf.append("\"");
            }
        }
        return buf.toString();
    }

    ////generating stuff
    /**
     * Remove query string and default ports, lowercase base url.
     *
     * @param url
     * @return
     */
    private String processRequestURL(String url) {
        //remove query string
        int index = url.indexOf("?");
        if (index != -1) {
            url = url.substring(0, index);
        }
        //base url to lowercase
        int slashIndex = url.indexOf("/", 8);
        String baseURL = url.substring(0, slashIndex).toLowerCase();
        // remove default ports
        int colonIndex = baseURL.indexOf(":", 8);
        if (colonIndex != -1) {
            if (baseURL.startsWith("http://") && baseURL.endsWith(":80")) {
                baseURL = baseURL.substring(0, colonIndex);
            } else if (baseURL.startsWith("https://") && baseURL.endsWith(":443")) {
                baseURL = baseURL.substring(0, colonIndex);
            }
        }
        url = baseURL + url.substring(slashIndex);
        return url;
    }

    private Map<String, String> generateOAuthParameters(String oauthConsumerKey, String oauthTimestamp, String oauthNonce, String oauthToken) {
        Map<String, String> oauthParams = new HashMap<>();
        oauthParams.put("oauth_consumer_key", oauthConsumerKey);
        oauthParams.put("oauth_signature_method", "HMAC-SHA1");
        oauthParams.put("oauth_timestamp", oauthTimestamp);
        oauthParams.put("oauth_nonce", oauthNonce);
        oauthParams.put("oauth_version", "1.0");
        if (oauthToken != null) {
            oauthParams.put("oauth_token", oauthToken);
        }
        return oauthParams;
    }

    private Map<String, String> generateExtraOAuthParameters(String oauthCallback, String oauthVerifier) {
        Map<String, String> oauthParams = new HashMap<>();
        if (oauthCallback != null) {
            oauthParams.put("oauth_callback", oauthCallback);
        }
        if (oauthVerifier != null) {
            oauthParams.put("oauth_verifier", oauthVerifier);
        }
        return oauthParams;
    }

    private Map<String, String> generateQueryStringParameters(String url) {
        try {
            Map<String, String> queryStringParams = new HashMap<>();
            int index = url.indexOf("?");
            if (index != -1) {
                String[] queryParams = url.substring(index + 1).split("&");
                for (String queryParam : queryParams) {
                    String[] splits = queryParam.split("=");
                    if (splits.length == 2) {
                        queryStringParams.put(URLDecoder.decode(splits[0], "UTF-8"), URLDecoder.decode(splits[1], "UTF-8"));
                    } else {
                        queryStringParams.put(URLDecoder.decode(splits[0], "UTF-8"), "");
                    }
                }
            }
            return queryStringParams;
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("Impossible exception!", e);
        }
    }

    private String generateSignature(String data, String oauthConsumerSecret, String token, String tokenSecret) {
        try {
            SecretKeySpec spec;
            if (token == null) {
                String secret = encodeParameter(oauthConsumerSecret) + "&";
                spec = new SecretKeySpec(secret.getBytes(), HMAC_SHA1);
            } else {
                String secret = encodeParameter(oauthConsumerSecret) + "&" + encodeParameter(tokenSecret);
                spec = new SecretKeySpec(secret.getBytes(), HMAC_SHA1);
            }
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(spec);
            byte[] byteHMAC = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(byteHMAC);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AssertionError("Impossible exception!", e);
        }
    }

    /**
    The most important and complicated method in OAuth flow
    @param httpMethod
    @param url
    @param oauthConsumerKey
    @param oauthTimestamp
    @param oauthNonce
    @param oauthToken
    @param oauthCallback
    @param oauthVerifier
    @param tokenSecret
    @return authorization header
     */
    private String generateAuthorizationHeader(String httpMethod, String url, String oauthConsumerKey, String oauthTimestamp, String oauthNonce, String oauthToken, String oauthCallback, String oauthVerifier, String tokenSecret) {
        //base http method
        String baseHttpMethod = httpMethod;
        //base url
        String baseURL = encodeParameter(processRequestURL(url));
        //base parameters
        Map<String, String> oauthParams = generateOAuthParameters(oauthConsumerKey, oauthTimestamp, oauthNonce, oauthToken);
        Map<String, String> extraOauthParams = generateExtraOAuthParameters(oauthCallback, oauthVerifier);
        Map<String, String> queryStringParams = generateQueryStringParameters(url);
        Map<String, String> sortedParams = new TreeMap<>();
        sortedParams.putAll(oauthParams);
        sortedParams.putAll(extraOauthParams);
        sortedParams.putAll(queryStringParams);
        String baseParameters = encodeParameter(encodeParametersToString(sortedParams, "&", false));
        //combine all
        String oauthBaseString = baseHttpMethod + "&" + baseURL + "&" + baseParameters;
        //generate signature
        String signature = generateSignature(oauthBaseString, OAUTH_COSUMER_SECRET, oauthToken, tokenSecret);
        //generate header string
        oauthParams.put("oauth_signature", signature);
        String result = "OAuth " + encodeParametersToString(oauthParams, ",", true);
        return result;
    }

    private String generateAuthorizationHeader(String httpMethod, String url, String oauthConsumerKey, String oauthToken, String oauthCallback, String oauthVerifier, String tokenSecret) {
        long timestamp = System.currentTimeMillis() / 1000;
        long nonce = timestamp + RAND.nextInt();
        return this.generateAuthorizationHeader(httpMethod, url, oauthConsumerKey, String.valueOf(timestamp), String.valueOf(nonce), oauthToken, oauthCallback, oauthVerifier, tokenSecret);
    }

    /**
     * Step 1,2,3 get request token to combine authorization URL.
     *
     * @param callbackURL
     * @return authorization URL
     * @throws java.io.IOException
     */
    public String getAuthorizationURL(String callbackURL) throws IOException {
        String authorizationHeader = this.generateAuthorizationHeader("POST", OAUTH_REQUEST_TOKEN_URL, OAUTH_COSUMER_KEY, null, callbackURL, null, null);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(OAUTH_REQUEST_TOKEN_URL);
        httpPost.addHeader("Authorization", authorizationHeader);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        String body = encodeParametersToString(this.generateExtraOAuthParameters(callbackURL, null), "&", false);
        httpPost.setEntity(new StringEntity(body));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String responseStr = EntityUtils.toString(entity);
            System.out.println("request token string: " + responseStr);
            this.token = getParameter(responseStr, "oauth_token");
            this.tokenSecret = getParameter(responseStr, "oauth_token_secret");
            return OAUTH_AUTHORIZATION_URL + "?oauth_token=" + token;
        }
    }

    /**
     * Step 7: set up access token by oauth_verifier.
     *
     * @param oauthVerifier
     * @throws java.io.IOException
     */
    public void setUpAccessToken(String oauthVerifier) throws IOException {
        String authorizationHeader = this.generateAuthorizationHeader("POST", OAUTH_ACCESS_TOKEN_URL, OAUTH_COSUMER_KEY, this.token, null, oauthVerifier, this.tokenSecret);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(OAUTH_ACCESS_TOKEN_URL);
        httpPost.addHeader("Authorization", authorizationHeader);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        String body = encodeParametersToString(this.generateExtraOAuthParameters(null, oauthVerifier), "&", false);
        httpPost.setEntity(new StringEntity(body));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String responseStr = EntityUtils.toString(entity);
            System.out.println("access token string: " + responseStr);
            this.token = getParameter(responseStr, "oauth_token");
            this.tokenSecret = getParameter(responseStr, "oauth_token_secret");
        }
    }

    /**
     * Step 8: call API.
     *
     * @param url
     * @return xml response
     * @throws java.io.IOException
     */
    public String call(String url) throws IOException {
        String authorizationHeader = this.generateAuthorizationHeader("GET", url, OAUTH_COSUMER_KEY, this.token, null, null, this.tokenSecret);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Authorization", authorizationHeader);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            System.out.println("watchlist in xml: " + result);
            return result;
        }
    }

    //trivial method
    private String getParameter(String responseStr, String parameter) {
        String value = null;
        for (String str : responseStr.split("&")) {
            if (str.startsWith(parameter + '=')) {
                value = str.split("=")[1].trim();
                break;
            }
        }
        return value;
    }
}
