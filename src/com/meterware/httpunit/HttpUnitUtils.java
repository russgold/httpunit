package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2001, Russell Gold
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
import java.util.StringTokenizer;

/**
 * Utility code shared by httpunit and servletunit.
 **/
public class HttpUnitUtils {

    /**
     * Returns the content type and encoding as a pair of strings.
     * If no character set is specified, the second entry will be null.
     **/
    public static String[] parseContentTypeHeader( String header ) {
        String[] result = new String[] { "text/plain", null };
        StringTokenizer st = new StringTokenizer( header, ";=" );
        result[0] = st.nextToken();
        while (st.hasMoreTokens()) {
            String parameter = st.nextToken();
            if (st.hasMoreTokens()) {
                String value = st.nextToken();
                if (parameter.trim().equalsIgnoreCase( "charset" )) result[1] = value;
            }
        }
        return result;
    }

}
