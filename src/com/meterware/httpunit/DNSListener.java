package com.meterware.httpunit;

/**
 * A listener for DNS Requests. Users may implement this interface to bypass the normal DNS lookup.
 *
 * @author <a href="russgold@httpunit.org">Russell Gold</a>
 **/
public interface DNSListener {


    /**
     * Returns the IP address as a string for the specified host name.
     **/
    String getIpAddress( String hostName );

}
