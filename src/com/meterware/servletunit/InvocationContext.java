package com.meterware.servletunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2001, Russell Gold
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
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This class represents the context in which a specific servlet request is being made.
 * It contains the objects needed to unit test the methods of a servlet.
 **/
public class InvocationContext {


    /**
     * Returns the request to be processed by the servlet.
     **/
    public HttpServletRequest getRequest() {
        return _request;
    }


    /**
     * Returns the response which the servlet should modify during its operation.
     **/
    public HttpServletResponse getResponse() {
        return _response;
    }


    /**
     * Returns the selected servlet, initialized to provide access to sessions
     * and servlet context information.
     **/
    public Servlet getServlet() throws ServletException {
        if (_servlet == null) {
            _servlet = _runner.getServlet( _requestURL );
            _servlet.init( new ServletUnitServletConfig( _servlet ) );
        }
        return _servlet;
    }


    /**
     * Returns the final response from the servlet. Note that this method should
     * only be invoked after all processing has been done to the servlet response.
     **/
    public WebResponse getServletResponse() {
        if (_webResponse == null) {
            HttpSession session = _request.getSession( /* create */ false );
            if (session != null && session.isNew()) {
                _response.addCookie( new Cookie( ServletUnitHttpSession.SESSION_COOKIE_NAME, session.getId() ) );
            }
            _webResponse = new ServletUnitWebResponse( _target, _requestURL, _response );
        }
        return _webResponse;
    }


//------------------------------ package methods ---------------------------------------


    /**
     * Constructs a servlet invocation context for a specified servlet container,
     * request, and cookie headers.
     **/
    InvocationContext( ServletRunner runner, WebRequest request, Cookie[] cookies ) throws MalformedURLException {
        _runner     = runner;
        _requestURL = request.getURL();
        _target     = request.getTarget();

        _request = new ServletUnitHttpRequest( request, runner.getContext() );
        for (int i = 0; i < cookies.length; i++) _request.addCookie( cookies[i] );
        
        HttpSession session = _request.getSession( /* create */ false );
        if (session != null) ((ServletUnitHttpSession) session).access();
    }


    String getTarget() {
        return _target;
    }


//------------------------------ private members ---------------------------------------


    private ServletRunner           _runner;
    private ServletUnitHttpRequest  _request;
    private ServletUnitHttpResponse _response = new ServletUnitHttpResponse();
    private URL                     _requestURL;
    private String                  _target;

    private Servlet                 _servlet;
    private WebResponse             _webResponse;
}




