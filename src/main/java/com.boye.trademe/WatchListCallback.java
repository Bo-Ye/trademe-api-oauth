package com.boye.trademe;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by boy on 11/12/15.
 */
public class WatchListCallback extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String oauthVerifier = request.getParameter("oauth_verifier");
        System.out.println("oauth_verifier: " + oauthVerifier);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<h1>Well Done!</h1>");
    }
}
