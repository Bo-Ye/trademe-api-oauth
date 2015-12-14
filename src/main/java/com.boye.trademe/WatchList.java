package com.boye.trademe;
// Import required java libraries

import twitter4j.TwitterException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class WatchList extends HttpServlet {
    private Authorization authorization = Authorization.getInstance();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            response.sendRedirect(authorization.getAuthorizationURL()); // trade me login page
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }
}