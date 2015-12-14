package com.boye.trademe;

import twitter4j.TwitterException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by boy on 11/12/15.
 */
public class WatchListCallback extends HttpServlet {
    private static final String WATCH_URL = "https://api.tmsandbox.co.nz/v1/MyTradeMe/Watchlist/All.xml";

    private Authorization authorization = Authorization.getInstance();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String oauthVerifier = request.getParameter("oauth_verifier");
            System.out.println("oauth_verifier: " + oauthVerifier);
            authorization.setAccessToken(authorization.getAccessToken(oauthVerifier));
            String result = authorization.call(WATCH_URL);
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println(result);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }
}
