package com.boye.trademe;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import twitter4j.HttpParameter;
import twitter4j.TwitterException;

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
    private static TrademeTemplate instance;

    private TrademeTemplate() {

    }

    public static synchronized TrademeTemplate getInstance() {
        if (instance == null) {
            instance = new TrademeTemplate();
        }
        return instance;
    }

    private  String token;
    private  String tokenSecret;
    private  SecretKeySpec secretKeySpec;



    /**
     * Remove query string and default ports, lowercase base url.
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
        if (colonIndex!=-1) {
            if (baseURL.startsWith("http://") && baseURL.endsWith(":80")) {
                baseURL = baseURL.substring(0, colonIndex);
            } else if (baseURL.startsWith("https://") && baseURL.endsWith(":443")) {
                baseURL = baseURL.substring(0, colonIndex);
            }
        }
        url = baseURL + url.substring(slashIndex);
        return url;
    }



    public String encodeParametersToString(Map<String, String> params, String splitter, boolean quot) {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (buf.length() != 0) {
                if (quot) {
                    buf.append("\"");
                }
                buf.append(splitter);
            }
            buf.append(HttpParameter.encode(param.getKey()));
            buf.append("=");
            if (quot) {
                buf.append("\"");
            }
            buf.append(HttpParameter.encode(param.getValue()));
        }
        if (buf.length() != 0) {
            if (quot) {
                buf.append("\"");
            }
        }
        return buf.toString();
    }


    private String generateSignature(String data, String oauthConsumerSecret, String token, String tokenSecret, SecretKeySpec secretKeySpec) {
        try {
            SecretKeySpec spec;
            if (token == null) {
                String secret = encodeParameter(oauthConsumerSecret) + "&";
                spec = new SecretKeySpec(secret.getBytes(), HMAC_SHA1);
            } else {
                spec = secretKeySpec;
                if (spec == null) {
                    String secret = encodeParameter(oauthConsumerSecret) + "&" + encodeParameter(tokenSecret);
                    spec = new SecretKeySpec(secret.getBytes(), HMAC_SHA1);
                    this.secretKeySpec = spec;
                }
            }
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(spec);
            byte[] byteHMAC = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(byteHMAC);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AssertionError("Impossible exception!", e);
        }
    }



    private Map<String, String> generateOAuthParameters(String oauthConsumerKey, String oauthTimestamp, String oauthNonce, String oauthToken, String oauthCallback, String oauthVerifier ){
        Map<String, String> oauthParams = new HashMap<>();
        oauthParams.put("oauth_consumer_key", oauthConsumerKey);
        oauthParams.put("oauth_signature_method", "HMAC-SHA1");
        oauthParams.put("oauth_timestamp", oauthTimestamp);
        oauthParams.put("oauth_nonce", oauthNonce);
        oauthParams.put("oauth_version", "1.0");
        oauthParams.put("oauth_token", oauthToken);
        oauthParams.put("oauth_callback", oauthCallback );
        oauthParams.put("oauth_verifier", oauthVerifier );
        return oauthParams;
    }

    private Map<String, String> generateQueryStringParameters(String url){
        try{
            Map<String, String> queryStringParams = new HashMap<>();
            int index = url.indexOf("?");
            if(index!=-1){
                String[] queryParams = url.substring(index+1).split("&");
                for(String queryParam : queryParams) {
                    String[] splits = queryParam.split("=");
                    if (splits.length == 2) {
                        queryStringParams.put(URLDecoder.decode(splits[0], "UTF-8"), URLDecoder.decode(splits[1], "UTF-8"));
                    } else {
                        queryStringParams.put(URLDecoder.decode(splits[0], "UTF-8"), "");
                    }
                }
            }
            return queryStringParams;
        }catch(UnsupportedEncodingException e){
            throw new AssertionError("Impossible exception!", e);
        }
    }

    /**
     * special URL encoding
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

    public String generateAuthorizationHeader(String httpMethod, String url, String oauthConsumerKey, String oauthTimestamp, String oauthNonce, String oauthToken, String oauthCallback, String oauthVerifier, String tokenSecret, SecretKeySpec secretKeySpec) {
        //base http method
        String baseHttpMethod = httpMethod;
        //base url
        String baseURL =  encodeParameter(processRequestURL(url));
        //base parameters
        Map<String, String> oauthParams = generateOAuthParameters(oauthConsumerKey, oauthTimestamp, oauthNonce, oauthToken,  oauthCallback,oauthVerifier );
        Map<String, String>  queryStringParams = generateQueryStringParameters(url);
        Map<String, String> sortedParams = new TreeMap<>();
        sortedParams.putAll(oauthParams);
        sortedParams.putAll(queryStringParams);
        String baseParameters = encodeParameter(encodeParametersToString(sortedParams, "&", false));
        //combine all
        String oauthBaseString = baseHttpMethod + "&" + baseURL + "&"+baseParameters;
        //generate signature
        String signature = generateSignature(oauthBaseString,OAUTH_COSUMER_SECRET, oauthToken, tokenSecret, secretKeySpec);



/*



            String signature = generateSignature(oauthBaseString, token, tokenSecret, secretKeySpec);
            System.out.println("In new the signature: " + signature);
            oauthHeaderParams.add(new HttpParameter("oauth_signature", signature));
            String result = "OAuth " + encodeParameters(oauthHeaderParams, ",", true);
            System.out.println("In new the generated auth header: " + result);*/
            return "";

    }

    private String generateAuthorizationHeader(String method, String url, HttpParameter[] params, String token, String tokenSecret, SecretKeySpec secretKeySpec) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        long timestamp = System.currentTimeMillis() / 1000;
        long nonce = timestamp + RAND.nextInt();
        return "";
        //return this.generateAuthorizationHeader(method, url, params, String.valueOf(nonce), String.valueOf(timestamp), token, tokenSecret, secretKeySpec);
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
            this.secretKeySpec = null;
        }
    }

    /**
     * Step 4: call API.
     *
     * @param url
     * @return
     * @throws TwitterException
     */
    public String call(String url) throws Exception {
        String authorizationHeader = this.generateAuthorizationHeader("GET", url, null, this.token, this.tokenSecret, this.secretKeySpec);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Authorization", authorizationHeader);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            System.out.println("url result: " + result);
            return result;
        }
    }
}
