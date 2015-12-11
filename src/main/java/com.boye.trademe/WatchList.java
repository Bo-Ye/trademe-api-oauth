package com.boye.trademe;
// Import required java libraries

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class WatchList extends HttpServlet {

    private static final String CALLBACK_URL = "http://localhost:8080/trademe-api-oauth/watch-list-callback";
    private String message;

    public void init() throws ServletException {
        message = "Bravo";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Twitter twitter = TwitterFactory.getSingleton();
            RequestToken requestToken = twitter.getOAuthRequestToken(CALLBACK_URL);
            String token = requestToken.getToken();
            System.out.println("token: " + token);
            response.sendRedirect(requestToken.getAuthorizationURL());
//            response.setContentType("text/html");
//            PrintWriter out = response.getWriter();
//            out.println("<h1>" + message + "</h1>");
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
    }
}