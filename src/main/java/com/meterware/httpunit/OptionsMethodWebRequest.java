package com.meterware.httpunit;

import java.net.URL;

/**
 * A {@link WebRequest} using the OPTIONS method.
 * <p>
 * RFC 2616 section 9.2 defines:
 * <p>
 * The OPTIONS method represents a request for information about the communication options available
 * on the request/response chain identified by the Request-URI. This method allows the client to
 * determine the options and/or requirements associated with a resource, or the capabilities of a
 * server, without implying a resource action or initiating a resource retrieval.
 * <p>
 * Responses to this method are not cacheable.
 * <p>
 * If the OPTIONS request includes an entity-body (as indicated by the presence of Content-Length or
 * Transfer-Encoding), then the media type MUST be indicated by a Content-Type field. Although this
 * specification does not define any use for such a body, future extensions to HTTP might use the
 * OPTIONS body to make more detailed queries on the server. A server that does not support such an
 * extension MAY discard the request body.
 * <p>
 * If the Request-URI is an asterisk ("*"), the OPTIONS request is intended to apply to the server
 * in general rather than to a specific resource. Since a server's communication options typically
 * depend on the resource, the "*" request is only useful as a "ping" or "no-op" type of method; it
 * does nothing beyond allowing the client to test the capabilities of the server. For example, this
 * can be used to test a proxy for HTTP/1.1 compliance (or lack thereof).
 * <p>
 * If the Request-URI is not an asterisk, the OPTIONS request applies only to the options that are
 * available when communicating with that resource.
 * <p>
 * A 200 response SHOULD include any header fields that indicate optional features implemented by
 * the server and applicable to that resource (e.g., Allow), possibly including extensions not
 * defined by this specification. The response body, if any, SHOULD also include information about
 * the communication options. The format for such a
 * <p>
 * body is not defined by this specification, but might be defined by future extensions to HTTP.
 * Content negotiation MAY be used to select the appropriate response format. If no response body is
 * included, the response MUST include a Content-Length field with a field-value of "0".
 * <p>
 * The Max-Forwards request-header field MAY be used to target a specific proxy in the request
 * chain. When a proxy receives an OPTIONS request on an absoluteURI for which request forwarding is
 * permitted, the proxy MUST check for a Max-Forwards field. If the Max-Forwards field-value is zero
 * ("0"), the proxy MUST NOT forward the message; instead, the proxy SHOULD respond with its own
 * communication options. If the Max-Forwards field-value is an integer greater than zero, the proxy
 * MUST decrement the field-value when it forwards the request. If no Max-Forwards field is present
 * in the request, then the forwarded request MUST NOT include a Max-Forwards field.
 *
 * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">RFC-2616 Section 9</a>
 */
public class OptionsMethodWebRequest extends HeaderOnlyWebRequest {

	/**
	 * initialize me - set method to OPTIONS
	 */
	private void init() {
		setMethod("OPTIONS");
	}

	/**
	 * Creates a new options request from a complete URL string.
	 *
	 * @param urlString
	 *            the URL desired, including the protocol.
	 */
	public OptionsMethodWebRequest(String urlString) {
		super(urlString);
		init();
	}

	/**
	 * Creates a new options request using a relative URL and base.
	 *
	 * @param urlBase
	 *            the base URL.
	 * @param urlString
	 *            the relative URL
	 */
	public OptionsMethodWebRequest(URL urlBase, String urlString) {
		super(urlBase, urlString);
		init();
	}
}

