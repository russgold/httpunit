package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2002, Russell Gold
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
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import java.net.URL;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;


/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
class JTidyHTMLParser implements HTMLParser {


    public void parse( HTMLPage page, URL pageURL, String pageText ) throws IOException, SAXException {
        try {
            page.setRootNode( getParser( pageURL ).parseDOM( new ByteArrayInputStream( pageText.getBytes( UTF_ENCODING ) ), null ) );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException( "UTF-8 encoding failed" );
        }
    }


    public String getCleanedText( String string ) {
        return (string == null) ? "" : string.replace( NBSP, ' ' );
    }

    final private static char NBSP = (char) 160;   // non-breaking space, defined by JTidy

    final private static String UTF_ENCODING = "UTF-8";


    private static Tidy getParser( URL url ) {
        Tidy tidy = new Tidy();
        tidy.setCharEncoding( org.w3c.tidy.Configuration.UTF8 );
        tidy.setQuiet( true );
        tidy.setShowWarnings( HttpUnitOptions.getParserWarningsEnabled() );
        if (!HttpUnitOptions.getHtmlErrorListeners().isEmpty()) {
            tidy.setErrout(new JTidyPrintWriter( url ));
        }
        return tidy;
    }

}
