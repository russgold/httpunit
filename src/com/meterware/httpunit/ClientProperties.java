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

/**
 * A class which represents the properties of a web client.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class ClientProperties {


    /**
     * Returns the current defaults for newly created web clients.
     */
    public static ClientProperties getDefaultProperties() {
        return _defaultProperties;
    }


    /**
     * Specifies the ID information for a client.
     */
    public void setApplicationID( String applicationName, String applicationCodeName, String applicationVersion ) {
        _applicationCodeName = applicationCodeName;
        _applicationName     = applicationName;
        _applicationVersion  = applicationVersion;
    }


    public String getApplicationCodeName() {
        return _applicationCodeName;
    }


    public void setApplicationCodeName( String applicationCodeName ) {
        _applicationCodeName = applicationCodeName;
    }


    public String getApplicationName() {
        return _applicationName;
    }


    public void setApplicationName( String applicationName ) {
        _applicationName = applicationName;
    }


    public String getApplicationVersion() {
        return _applicationVersion;
    }


    public void setApplicationVersion( String applicationVersion ) {
        _applicationVersion = applicationVersion;
    }


    /**
     * Returns the user agent identification. Unless this has been set explicitly, it will default to the
     * application code name followed by a slash and the application version.
     */
    public String getUserAgent() {
        return _userAgent != null ? _userAgent : _applicationCodeName + '/' + _applicationVersion;
    }


    public void setUserAgent( String userAgent ) {
        _userAgent = userAgent;
    }


    public String getPlatform() {
        return _platform;
    }


    public void setPlatform( String platform ) {
        _platform = platform;
    }


    static ClientProperties cloneProperties() {
        return new ClientProperties( getDefaultProperties() );
    }


    private String _applicationCodeName = "httpunit";
    private String _applicationName     = "HttpUnit";
    private String _applicationVersion  = "1.4";
    private String _userAgent;
    private String _platform            = "Java";

    private static ClientProperties _defaultProperties = new ClientProperties();


    private ClientProperties() {
    }


    private ClientProperties( ClientProperties source ) {
        _applicationCodeName = source._applicationCodeName;
        _applicationName     = source._applicationName;
        _applicationVersion  = source._applicationVersion;
        _userAgent           = source._userAgent;
        _platform            = source._platform;
    }
}
