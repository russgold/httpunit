package com.meterware.servletunit;

/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2001-2002, Russell Gold
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
import com.meterware.httpunit.WebResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;


/**
 * An interface which represents the invocation of a servlet.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public interface InvocationContext {


    /**
     * Returns the request to be processed by the servlet.
     **/
    HttpServletRequest getRequest();


    /**
     * Returns the response which the servlet should modify during its operation.
     **/
    HttpServletResponse getResponse();


    /**
     * Returns the selected servlet, initialized to provide access to sessions
     * and servlet context information.
     **/
    Servlet getServlet() throws ServletException;


    /**
     * Returns the final response from the servlet. Note that this method should
     * only be invoked after all processing has been done to the servlet response.
     **/
    WebResponse getServletResponse() throws IOException;


    /**
     * Returns the target for the original request.
     */
    String getTarget();

}
