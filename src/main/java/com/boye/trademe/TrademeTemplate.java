package com.boye.trademe;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import twitter4j.BASE64Encoder;
import twitter4j.HttpParameter;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthToken;
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

	private static final String OAUTH_COSUMER_KEY = "";
	private static final String OAUTH_COSUMER_SECRET = "";
	private static final String OAUTH_REQUEST_TOKEN_URL = "https://secure.tmsandbox.co.nz/Oauth/RequestToken";
	private static final String OAUTH_AUTHORIZATION_URL = "https://secure.tmsandbox.co.nz/Oauth/Authorize";
	private static final String OAUTH_ACCESS_TOKEN_URL = "https://secure.tmsandbox.co.nz/Oauth/AccessToken";

	private static final String HMAC_SHA1 = "HmacSHA1";
	private static final HttpParameter OAUTH_SIGNATURE_METHOD = new HttpParameter("oauth_signature_method", "HMAC-SHA1");
	private static final Random RAND = new Random();

	private static final String CALLBACK_URL = "http://localhost:8080/trademe-api-oauth/watch-list-callback";

	private Twitter twitter = TwitterFactory.getSingleton();

	private static List<HttpParameter> toParamList(HttpParameter[] params) {
		List<HttpParameter> paramList = new ArrayList<HttpParameter>(params.length);
		paramList.addAll(Arrays.asList(params));
		return paramList;
	}

	private void parseGetParameters(String url, List<HttpParameter> signatureBaseParams) {
		int queryStart = url.indexOf("?");
		if (-1 != queryStart) {
			url.split("&");
			String[] queryStrs = url.substring(queryStart + 1).split("&");
			try {
				for (String query : queryStrs) {
					String[] split = query.split("=");
					if (split.length == 2) {
						signatureBaseParams.add(new HttpParameter(URLDecoder.decode(split[0], "UTF-8"), URLDecoder.decode(split[1], "UTF-8")));
					} else {
						signatureBaseParams.add(new HttpParameter(URLDecoder.decode(split[0], "UTF-8"), ""));
					}
				}
			} catch (UnsupportedEncodingException ignore) {
			}

		}

	}

	static String constructRequestURL(String url) {
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

	private static String normalizeRequestParameters(List<HttpParameter> params) {
		Collections.sort(params);
		return encodeParameters(params);
	}

	public static String encodeParameters(List<HttpParameter> httpParams) {
		return encodeParameters(httpParams, "&", false);
	}

	public static String encodeParameters(List<HttpParameter> httpParams, String splitter, boolean quot) {
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

	String generateSignature(String data, OAuthToken token) {
		byte[] byteHMAC = null;
		try {
			Mac mac = Mac.getInstance(HMAC_SHA1);
			SecretKeySpec spec;
			if (null == token) {
				String oauthSignature = HttpParameter.encode(OAUTH_COSUMER_KEY) + "&";
				spec = new SecretKeySpec(oauthSignature.getBytes(), HMAC_SHA1);
			} else {
				spec = token.getSecretKeySpec();
				if (null == spec) {
					String oauthSignature = HttpParameter.encode(OAUTH_COSUMER_SECRET) + "&" + HttpParameter.encode(token.getTokenSecret());
					spec = new SecretKeySpec(oauthSignature.getBytes(), HMAC_SHA1);
					token.setSecretKeySpec(spec);
				}
			}
			mac.init(spec);
			byteHMAC = mac.doFinal(data.getBytes());
		} catch (InvalidKeyException ike) {

			throw new AssertionError(ike);
		} catch (NoSuchAlgorithmException nsae) {

			throw new AssertionError(nsae);
		}
		return BASE64Encoder.encode(byteHMAC);
	}

	private String generateAuthorizationHeader(String method, String url, HttpParameter[] params, OAuthToken otoken) {
		long lTimestamp = System.currentTimeMillis() / 1000;
		long lNonce = lTimestamp + RAND.nextInt();
		String timestamp = String.valueOf(lTimestamp);
		String nonce = String.valueOf(lNonce);
		if (null == params) {
			params = new HttpParameter[0];
		}
		List<HttpParameter> oauthHeaderParams = new ArrayList<HttpParameter>(5);
		oauthHeaderParams.add(new HttpParameter("oauth_consumer_key", OAUTH_COSUMER_KEY));
		oauthHeaderParams.add(OAUTH_SIGNATURE_METHOD);
		oauthHeaderParams.add(new HttpParameter("oauth_timestamp", timestamp));
		oauthHeaderParams.add(new HttpParameter("oauth_nonce", nonce));
		oauthHeaderParams.add(new HttpParameter("oauth_version", "1.0"));
		if (otoken != null) {
			oauthHeaderParams.add(new HttpParameter("oauth_token", otoken.getToken()));
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

		String signature = generateSignature(oauthBaseString, otoken);

		oauthHeaderParams.add(new HttpParameter("oauth_signature", signature));

		return "OAuth " + encodeParameters(oauthHeaderParams, ",", true);
	}

	/**
	 * Step 1: get request token to combine authorization URL.
	 * 
	 * @return
	 * @throws TwitterException
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public String getAuthorizationURL() throws TwitterException, ClientProtocolException, IOException {
		//RequestToken requestToken = twitter.getOAuthRequestToken(CALLBACK_URL);
		List<HttpParameter> params = new ArrayList<HttpParameter>();
		 params.add(new HttpParameter("oauth_callback", CALLBACK_URL));
		 HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
		String authorizationHeader =  this.generateAuthorizationHeader("POST", OAUTH_REQUEST_TOKEN_URL, parameters, null);
		 CloseableHttpClient httpclient = HttpClients.createDefault();
	     HttpPost httpPost = new HttpPost(OAUTH_REQUEST_TOKEN_URL );
	     httpPost.addHeader("Authorization", authorizationHeader);
	     httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
         String postParam = HttpParameter.encodeParameters(parameters);
         //httpPost.
         //byte[] bytes = postParam.getBytes("UTF-8");
         //httpPost.addHeader("Content-Length",Integer.toString(bytes.length));
         try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
             HttpEntity entity = response.getEntity();
             String result = EntityUtils.toString(entity);
             System.out.println("result: "+result);
             return result;
         }
        
		//System.out.println("step 1, request token: " + requestToken.getToken());
		//return requestToken.getAuthorizationURL();
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
