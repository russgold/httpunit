package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2003, Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 *******************************************************************************************************************/
import java.util.Enumeration;
import java.util.Map;
import java.util.Locale;
import java.security.Principal;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletInputStream;
import javax.servlet.RequestDispatcher;


/**
 * 
 * @author <a href="russgold@httpunit.org">Russell Gold</a>
 **/
class HttpServletRequestWrapper implements HttpServletRequest {

    /** The request being wrapped. **/
    private HttpServletRequest _baseRequest;


    HttpServletRequestWrapper( HttpServletRequest baseRequest ) {
        _baseRequest = baseRequest;
    }


    public HttpServletRequest getBaseRequest() {
        return _baseRequest;
    }


    public String getAuthType() {
        return _baseRequest.getAuthType();
    }


    public Cookie[] getCookies() {
        return _baseRequest.getCookies();
    }


    public long getDateHeader( String s ) {
        return _baseRequest.getDateHeader( s );
    }


    public String getHeader( String s ) {
        return _baseRequest.getHeader( s );
    }


    public Enumeration getHeaders( String s ) {
        return _baseRequest.getHeaders( s );
    }


    public Enumeration getHeaderNames() {
        return _baseRequest.getHeaderNames();
    }


    public int getIntHeader( String s ) {
        return _baseRequest.getIntHeader( s );
    }


    public String getMethod() {
        return _baseRequest.getMethod();
    }


    public String getPathInfo() {
        return _baseRequest.getPathInfo();
    }


    public String getPathTranslated() {
        return _baseRequest.getPathTranslated();
    }


    public String getContextPath() {
        return _baseRequest.getContextPath();
    }


    public String getQueryString() {
        return _baseRequest.getQueryString();
    }


    public String getRemoteUser() {
        return _baseRequest.getRemoteUser();
    }


    public boolean isUserInRole( String s ) {
        return _baseRequest.isUserInRole( s );
    }


    public Principal getUserPrincipal() {
        return _baseRequest.getUserPrincipal();
    }


    public String getRequestedSessionId() {
        return _baseRequest.getRequestedSessionId();
    }


    public String getRequestURI() {
        return _baseRequest.getRequestURI();
    }


    public StringBuffer getRequestURL() {
        return _baseRequest.getRequestURL();
    }


    public String getServletPath() {
        return _baseRequest.getServletPath();
    }


    public HttpSession getSession( boolean b ) {
        return _baseRequest.getSession( b );
    }


    public HttpSession getSession() {
        return _baseRequest.getSession();
    }


    public boolean isRequestedSessionIdValid() {
        return _baseRequest.isRequestedSessionIdValid();
    }


    public boolean isRequestedSessionIdFromCookie() {
        return _baseRequest.isRequestedSessionIdFromCookie();
    }


    public boolean isRequestedSessionIdFromURL() {
        return _baseRequest.isRequestedSessionIdFromURL();
    }


    public boolean isRequestedSessionIdFromUrl() {
        return _baseRequest.isRequestedSessionIdFromURL();
    }


    public Object getAttribute( String s ) {
        return _baseRequest.getAttribute( s );
    }


    public Enumeration getAttributeNames() {
        return _baseRequest.getAttributeNames();
    }


    public String getCharacterEncoding() {
        return _baseRequest.getCharacterEncoding();
    }


    public void setCharacterEncoding( String s ) throws UnsupportedEncodingException {
        _baseRequest.setCharacterEncoding( s );
    }


    public int getContentLength() {
        return _baseRequest.getContentLength();
    }


    public String getContentType() {
        return _baseRequest.getContentType();
    }


    public ServletInputStream getInputStream() throws IOException {
        return _baseRequest.getInputStream();
    }


    public String getParameter( String s ) {
        return _baseRequest.getParameter( s );
    }


    public Enumeration getParameterNames() {
        return _baseRequest.getParameterNames();
    }


    public String[] getParameterValues( String s ) {
        return _baseRequest.getParameterValues( s );
    }


    public Map getParameterMap() {
        return _baseRequest.getParameterMap();
    }


    public String getProtocol() {
        return _baseRequest.getProtocol();
    }


    public String getScheme() {
        return _baseRequest.getScheme();
    }


    public String getServerName() {
        return _baseRequest.getServerName();
    }


    public int getServerPort() {
        return _baseRequest.getServerPort();
    }


    public BufferedReader getReader() throws IOException {
        return _baseRequest.getReader();
    }


    public String getRemoteAddr() {
        return _baseRequest.getRemoteAddr();
    }


    public String getRemoteHost() {
        return _baseRequest.getRemoteHost();
    }


    public void setAttribute( String s, Object o ) {
        _baseRequest.setAttribute( s, o );
    }


    public void removeAttribute( String s ) {
        _baseRequest.removeAttribute( s );
    }


    public Locale getLocale() {
        return _baseRequest.getLocale();
    }


    public Enumeration getLocales() {
        return _baseRequest.getLocales();
    }


    public boolean isSecure() {
        return _baseRequest.isSecure();
    }


    public RequestDispatcher getRequestDispatcher( String s ) {
        return _baseRequest.getRequestDispatcher( s );
    }


    /**
     * @deprecated
     */
    public String getRealPath( String s ) {
        return _baseRequest.getRealPath( s );
    }
}
