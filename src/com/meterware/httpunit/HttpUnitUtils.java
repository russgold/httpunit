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
                String value = stripQuotes( st.nextToken() );
                if (parameter.trim().equalsIgnoreCase( "charset" )) result[1] = value;
            }
        }
        return result;
    }

    static String stripQuotes( String value ) {
        if (value.startsWith( "'" ) || value.startsWith( "\"" )) value = value.substring( 1 );
        if (value.endsWith( "'" ) || value.endsWith( "\"" )) value = value.substring( 0, value.length()-1 );
        return value;
    }

    /**
     * Returns an interpretation of the specified URL-encoded string.
     * FIXME: currently assumes iso-8859-1 character set.
     **/
    public static String decode( String byteString ) {
        StringBuffer sb = new StringBuffer();
        char[] chars = byteString.toCharArray();
        char[] hexNum = { '0', '0', '0' };

        int i = 0;
        while (i < chars.length) {
            if (chars[i] == '+') {
                i++;
                sb.append( ' ' );
            } else if (chars[i] == '%') {
                i++;
                hexNum[1] = chars[i++];
                hexNum[2] = chars[i++];
                sb.append( (char) Integer.parseInt( new String( hexNum ), 16 ) );
            } else {
                sb.append( chars[i++] );
            }
        }
        return sb.toString();
    }

}
