package com.meterware.httpunit.parsing;
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
import com.meterware.httpunit.parsing.HTMLParser;

/**
 * Factory for creating HTML parsers. Parsers customization properties can be specified but do not necessarily work
 * for every parser type.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:bw@xmlizer.biz">Bernhard Wagner</a>
 **/
abstract public class HTMLParserFactory {

    private static HTMLParser _htmlParser;
    private static HTMLParser _jtidyParser;
    private static HTMLParser _nekoParser;

    private static boolean _preserveTagCase = false;
    private static boolean _returnHTMLDocument = true;


    /**
     * Selects the JTidy parser, if present.
     */
    public static void useJTidyParser() {
        if (_jtidyParser == null) throw new RuntimeException( "JTidy parser not available" );
        _htmlParser = _jtidyParser;
    }


    /**
     * Selects the NekoHTML parser, if present.
     */
    public static void useNekoHTMLParser() {
        if (_nekoParser == null) throw new RuntimeException( "NekoHTML parser not available" );
        _htmlParser = _nekoParser;
    }


    /**
     * Specifies the parser to use.
     */
    public static void setHTMLParser( HTMLParser htmlParser ) {
        _htmlParser = htmlParser;
    }


    /**
     * Returns the current selected parser.
     */
    public static HTMLParser getHTMLParser() {
        if (_htmlParser == null) {
            if (_nekoParser != null) {
                _htmlParser = _nekoParser;
            } else if (_jtidyParser != null) {
                _htmlParser = _jtidyParser;
            } else {
                throw new RuntimeException( "No HTML parser found. Make sure that either nekoHTML.jar or Tidy.jar is in the in classpath" );
            }
        }
        return _htmlParser;
    }


    /**
     * Returns true if the current parser will preserve the case of HTML tags and attributes.
     */
    public static boolean isPreserveTagCase() {
        return _preserveTagCase && getHTMLParser().supportsPreserveTagCase();
    }


    /**
     * Specifies whether the parser should preserve the case of HTML tags and attributes. Not every parser can
     * support this capability.  Note that enabling this will disable support for the HTMLDocument class.
     * @see #setReturnHTMLDocument
     */
    public static void setPreserveTagCase( boolean preserveTagCase ) {
        _preserveTagCase = preserveTagCase;
        if (preserveTagCase) _returnHTMLDocument = false;
    }


    /**
     * Returns true if the current parser will return an HTMLDocument object rather than a Document object.
     */
    public static boolean isReturnHTMLDocument() {
        return _returnHTMLDocument && getHTMLParser().supportsReturnHTMLDocument();
    }


    /**
     * Specifies whether the parser should return an HTMLDocument object rather than a Document object.
     * Not every parser can support this capability.  Note that enabling this will disable preservation of tag case.
     * @see #setPreserveTagCase
     */
    public static void setReturnHTMLDocument( boolean returnHTMLDocument ) {
        _returnHTMLDocument = returnHTMLDocument;
        if (returnHTMLDocument) _preserveTagCase = false;
    }


    static {
        _jtidyParser = loadParserIfSupported( "org.w3c.tidy.Parser", "com.meterware.httpunit.parsing.JTidyHTMLParser" );
        _nekoParser  = loadParserIfSupported( "org.cyberneko.html.HTMLConfiguration", "com.meterware.httpunit.parsing.NekoHTMLParser" );
    }


    private static HTMLParser loadParserIfSupported( final String testClassName, final String parserClassName ) {
        try {
            Class.forName( testClassName );
            return (HTMLParser) Class.forName( parserClassName ).newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
        }
        return null;
    }


    public static void reset() {
        _preserveTagCase = false;
        _returnHTMLDocument = true;
    }
}
