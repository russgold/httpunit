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
import java.util.Dictionary;

/**
 * A basic simulated servlet for testing the HttpUnit library.
 **/
abstract
public class PseudoServlet {


    final static public String CONTENTS = "contents";


    /**
     * Returns a resource object as a result of a get request. 
     * @param parameters a mapping of parameter names to arrays of value string.
     * @param headers a mapping of header names to header contents. Also contains a special 'header' named CONTENTS
     *        which is the raw bytes of the request contents stored in a string.
     **/ 
    public WebResource getResponse( String methodType, Dictionary parameters, Dictionary headers ) {
        if (methodType.equalsIgnoreCase( "GET" )) {
            return getGetResponse( parameters, headers );
        } else if (methodType.equalsIgnoreCase( "PUT" )) {
            return getPutResponse( parameters, headers );
        } else if (methodType.equalsIgnoreCase( "POST" )) {
            return getPostResponse( parameters, headers );
        } else {
            throw new RuntimeException( methodType + " not implemented" );
        }
    }


    /**
     * Returns a resource object as a result of a get request. 
     * @param parameters a mapping of parameter names to arrays of value string.
     * @param headers a mapping of header names to header contents. Also contains a special 'header' named CONTENTS
     *        which is the raw bytes of the request contents stored in a string.
     **/ 
    public WebResource getGetResponse( Dictionary parameters, Dictionary headers ) {
        throw new RuntimeException( "get not implemented" );
    }


    /*
     * Returns a resource object as a result of a post request. 
     * @param parameters a mapping of parameter names to arrays of value string.
     * @param headers a mapping of header names to header contents. Also contains a special 'header' named CONTENTS
     *        which is the raw bytes of the request contents stored in a string.
     **/ 
    public WebResource getPostResponse( Dictionary parameters, Dictionary headers ) {
        throw new RuntimeException( "post not implemented" );
    }


    /*
     * Returns a resource object as a result of a put request. 
     * @param parameters a mapping of parameter names to arrays of value string.
     * @param headers a mapping of header names to header contents. Also contains a special 'header' named CONTENTS
     *        which is the raw bytes of the request contents stored in a string.
     **/ 
    public WebResource getPutResponse( Dictionary parameters, Dictionary headers ) {
        throw new RuntimeException( "put not implemented" );
    }
}



