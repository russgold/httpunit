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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import junit.runner.BaseTestRunner;
import junit.runner.TestSuiteLoader;
import junit.runner.StandardTestSuiteLoader;
import junit.framework.Test;
import junit.framework.AssertionFailedError;
import junit.framework.TestResult;
import junit.framework.TestCase;
import junit.framework.TestFailure;


/**
 * A base class for a servlet which can run unit tests inside a servlet context.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
abstract
public class JUnitServlet extends HttpServlet {


    protected JUnitServlet( InvocationContextFactory factory ) {
        _factory = factory;
    }


    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        response.setContentType( "text/html" );
        final String testName = request.getParameter( "test" );
        if (testName == null || testName.length() == 0) {
            reportCannotRunTest( response.getWriter(), "No test class specified" );
        } else {
            ServletTestRunner runner = new ServletTestRunner( testName, response.getWriter() );
        }
        response.getWriter().close();
    }

    private InvocationContextFactory _factory;


    private void reportCannotRunTest( PrintWriter writer, final String errorMessage ) {
        writer.print( "<html><head><title>Cannot run test</title></head><body>" + errorMessage + "</body></html>" );
    }


    class ServletTestRunner extends BaseTestRunner {
        private String _elapsedTime;
        private PrintWriter _writer;


        public ServletTestRunner( String testClassName, PrintWriter writer ) {
            _writer = writer;
            _testClassName = testClassName;
            Test suite = getTest( testClassName );
            ServletTestCase.setInvocationContextFactory( _factory );

            if (suite != null) {
                _testResult = new TestResult();
                _testResult.addListener( this );
                long startTime= System.currentTimeMillis();
                suite.run(_testResult);
                long endTime= System.currentTimeMillis();
                _elapsedTime = elapsedTimeAsString( endTime-startTime );
                displayResults( _writer );
            }
        }


        public void addError( Test test, Throwable throwable ) {
        }


        public void addFailure( Test test, AssertionFailedError error ) {
        }


        public void endTest( Test test ) {
        }


        protected void runFailed( String s ) {
            reportCannotRunTest( _writer, s );
        }


        public void startTest( Test test ) {
        }


        public void testStarted( String s ) {
        }


        public void testEnded( String s ) {
        }


        public void testFailed( int i, Test test, Throwable throwable ) {
        }


        /**
         * Always use the StandardTestSuiteLoader. Overridden from
         * BaseTestRunner.
         */
        public TestSuiteLoader getLoader() {
            return new StandardTestSuiteLoader();
        }


        void displayResults( PrintWriter writer ) {
            writer.println( "<html><head><title>Test Suite: " + _testClassName + "</title>" );
            writer.println( "<style type='text/css'>" );
            writer.println( "<!--" );
            writer.println( "  td.detail { font-size:smaller; vertical-align: top }" );
            writer.println( "  -->" );
            writer.println( "</style></head><body>" );
            writer.println( "<table id='results' border='1'><tr>" );
            writer.println( "<td>" + getFormatted( _testResult.runCount(), "test" ) + "</td>" );
            writer.println( "<td>Time: " + _elapsedTime + "</td>" );

            if (_testResult.wasSuccessful()) {
                writer.println( "<td>OK</td></tr>" );
            } else {
                writer.println( "<td>Problems Occurred</td></tr>" );
                displayProblems( writer, "failure", _testResult.failureCount(), _testResult.failures() );
                displayProblems( writer, "error", _testResult.errorCount(), _testResult.errors() );
            }
            writer.println( "</table>" );
            writer.println( "</body></html>" );
        }


        private void displayProblems( PrintWriter writer, String title, int count, Enumeration enumeration ) {
            if (count != 0) {
                writer.println( "<tr><td colspan=3>" + getFormatted( count, title ) + "</td></tr>" );
                Enumeration e = enumeration;
                for (int i = 1; e.hasMoreElements(); i++) {
                    TestFailure failure = (TestFailure) e.nextElement();
                    writer.println( "<tr><td class='detail' align='right'>" + i + "</td>" );
                    writer.println( "<td class='detail'>" + failure.failedTest() + "</td><td class='detail'>" );
                    if (failure.thrownException() instanceof AssertionFailedError) {
                        writer.println( htmlEscape( failure.thrownException().getMessage() ) );
                    } else {
                        writer.println( htmlEscape( getFilteredTrace( failure.thrownException() ) ) );
                    }
                    writer.println( "</td></tr>" );
                }
            }
        }

        private static final char LF = 10;
        private static final char CR = 13;

        private String getFormatted( int count, String name ) {
            return count + " " + name + (count == 1 ? "" : "s");
        }


        private String htmlEscape( String s ) {
            StringBuffer result = new StringBuffer( s.length() );
            char[] chars = s.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                switch( chars[i] ) {
                case '&':
                    result.append( "&amp;" );
                    break;
                case '<':
                    result.append( "&lt;" );
                    break;
                case '>':
                    result.append( "&gt;" );
                    break;
                case LF:
                    if (i > 0 && chars[i-1] == CR) {
                        result.append( chars[i] );
                        break;
                    }
                case CR:
                    result.append( "<br>" );
                default:
                    result.append( chars[i] );
                }
            }
            return result.toString();
        }


        private TestResult _testResult;
        private String _testClassName;
    }

}
