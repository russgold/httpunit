package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2002, Russell Gold
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
import com.meterware.pseudoserver.HttpUserAgentTest;
import com.meterware.httpunit.parsing.HTMLParserFactory;
import org.junit.Before;

/**
 * a base class for HttpUnit regression tests.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
abstract
public class HttpUnitTest extends HttpUserAgentTest {

    @Before
    /**
     * setup the test by resetting the environment for Http Unit tests
     */
    public void setUpHttpUnitTest() throws Exception {
        HttpUnitOptions.reset();
        HTMLParserFactory.reset();
    }


    /**
     * handling of tests that are temporarily disabled
     */
    public static boolean WARN_DISABLED=true;
    public static int disabledIndex=0;
    public static boolean firstWarn=true;
    
    /**
     * return a left padded string
     * @param s
     * @param pad
     * @return
     */
    private static String padLeft(String s, int pad) {
    	String result=s;
    	String space="                                                         ";
    	if (result.length()>pad) {
    		result=result.substring(0,pad);
    	} else if (result.length()<pad) {
    		result=space.substring(0,pad-result.length())+result;
    	}
    	return result;    	
    }
		public static String warnDelim="";
		
		
    /**
     * show a warning for disabled Tests
     * @param testName
     * @param comment
     */
    public static void warnDisabled(String testName,String priority,int urgency,String comment) {
    	if (WARN_DISABLED) {
    		if (firstWarn) {
    			firstWarn=false;
    			System.err.println("\n The following tests are not active - the features tested are not part of the current release:");
    			System.err.println(" #  |        testname               | priority | urgency | reason  ");
    			System.err.println("----+-------------------------------+----------+---------+----------------------------------------");
    		}
    		disabledIndex++;
    		System.err.println(warnDelim+padLeft(""+disabledIndex,3)+
    				" | "+padLeft(testName,29)+
    				" | "+padLeft(priority, 8)+
    				" | "+padLeft(""+urgency, 7)+
    				" | "+comment);
    	}	
    }

    static {
        new WebConversation();
    }


}
