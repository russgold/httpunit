package com.meterware.httpunit;
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
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.io.PrintWriter;
import java.net.URL;

/**
 * Basic "parser" for the JTidy error output.  Will get the line and column number as well
 * as the message.  It assumes that an error or warning is to be logged once println() has been
 * called or if a string starts with "line"
 *
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 **/
class JTidyPrintWriter extends PrintWriter {
    JTidyPrintWriter( URL pageURL ) {
        super(System.out);
        _url = pageURL;
    }

    public void print(boolean b) {
        print(String.valueOf(b));
    }

    public  void print(char c) {
        print(String.valueOf(c));
    }

    public  void print(char[] s) {
        print(String.valueOf(s));
    }

    public  void print(double d) {
        print(String.valueOf(d));
    }

    public  void print(float f) {
        print(String.valueOf(f));
    }

    public  void print(int i) {
        print(String.valueOf(i));
    }

    public  void print(long l) {
        print(String.valueOf(l));
    }

    public  void print(Object obj) {
        print(obj.toString());
    }

    /**
     * Tidy is apparently returning line number in a format x.xxx remove the dots
     **/
    private String removeDots(String string)
    {
        StringTokenizer tok = new StringTokenizer(string,".");
        StringBuffer buf = new StringBuffer();
        String elem;
        while(tok.hasMoreElements()) {
            elem = tok.nextToken();
            buf.append(elem);
        }
        return buf.toString();
    }

    /**
     * Detects a new log if starting with "line", a warning if message starts with "Warning"
     * and an error if it starts with "Error"
     **/
    public void print(String s) {
        if (s.startsWith("line")) {
            if (!_logged && _line > 0 && _msg!=null && _msg.length()>0) {
                log(); // log previous!!!
            }
            _logged = false; // new error....
            StringTokenizer tok = new StringTokenizer(s);
            // skip first "line"
            tok.nextToken();
            // get line
            _line = Integer.parseInt(removeDots(tok.nextToken()));
            // skip second "column"
            tok.nextToken();
            // get column
            _column = Integer.parseInt(removeDots(tok.nextToken()));
        } else if (s.startsWith("Warning")) {
            _error = false;
            _msg = s;
        } else if (s.startsWith("Error")) {
            _error = true;
            _msg = s;
        } else {
            // non structured msg
            _msg += s;
        }
    }

    public void println() {
        if (!_logged) {
            log();
        }
    }

    public void println(boolean x) {
        print(String.valueOf(x));
        println();
    }

    public void println(char c) {
        print(String.valueOf(c));
        println();
    }

    public  void println(char[] c) {
        print(String.valueOf(c));
        println();
    }

    public  void println(double d) {
        print(String.valueOf(d));
        println();
    }

    public  void println(float f) {
        print(String.valueOf(f));
        println();
    }

    public  void println(int i) {
        print(String.valueOf(i));
        println();
    }

    public  void println(long l) {
        print(String.valueOf(l));
        println();
    }

    public  void println(Object o) {
        print(o.toString());
        println();
    }

    public  void println(String s) {
        print(s);
        println();
    }

//----------------------------------------------- private members ------------------------------------------------------

    private int     _line = -1;
    private int     _column = -1;
    private String  _msg = "";
    private boolean _error = false;
    private boolean _logged = false;
    private URL     _url;

    /**
     * reports the warning or error and then resets the current error/warning.
     **/
    private void log() {
        //System.out.println("Logging.........................");
        if (_error) {
            reportError(_msg,_line,_column);
        } else {
            reportWarning(_msg,_line,_column);
        }
        _logged = true;
        _line = -1;
        _column=-1;
        _msg="";
    }

    private void reportError( String msg, int line, int column ) {
        Enumeration enum = HttpUnitOptions.getHtmlErrorListeners().elements();
        while (enum.hasMoreElements()) {
            ((HtmlErrorListener) enum.nextElement()).error( _url, msg, line, column );
        }
    }


    private void reportWarning( String msg, int line, int column ) {
        Enumeration enum = HttpUnitOptions.getHtmlErrorListeners().elements();
        while (enum.hasMoreElements()) {
            ((HtmlErrorListener) enum.nextElement()).warning( _url, msg, line, column );
        }
    }
}