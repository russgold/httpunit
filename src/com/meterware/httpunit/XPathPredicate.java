/********************************************************************************************************************
 * $Id: HTMLElementPredicate.java 336 2002-09-13 14:17:26Z russgold $
 *
 * Copyright (c) 2008, Russell Gold
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
package com.meterware.httpunit;

import javax.xml.xpath.*;
import org.w3c.dom.*;

/**
 * Provides an HTMLElement Predicate that is capable of matching based on an
 * XPath node specification. This allows for very advanced matching techniques.
 * 
 * THREAD: Instances are not thread safe, each thread should create its own
 * instance with a specific xpath. (The same instance can be used for multiple
 * documents, each change in document will result in its internal caches being
 * flushed).
 * 
 * @author <a href="mailto:edA-qa@disemia.com">edA-qa mort-ora-y</a>
 * @author <a href="mailto:stephane@mikaty.net">Stephane Mikaty</a>
 */
public class XPathPredicate implements HTMLElementPredicate {

	/** XPath which dictates matching nodes, from root */
	private XPathExpression	xpath;
	private String path;
	// set to true for debugging
	public static final boolean DEBUG=false;

	/**
	 * Constructs an HTMLElementPredicate that matches only those elements which
	 * match the provided XPath.
	 * 
	 * @param path
	 *          [in] XPath specification of valid/matching nodes
	 * @throws XPathExpressionException
	 *           if the xpath is invalid
	 */
	public XPathPredicate(String path) throws XPathExpressionException {
		this.path=path;
		this.xpath = XPathFactory.newInstance().newXPath().compile(path);
	}

	/**
	 * debug Output for node structure
	 * @param node
	 * @param indent
	 */
	private void debugOut(Node node, String indent) {
		System.out.print(indent+node.getNodeName()+":");
		System.out.println(indent+node.getNodeValue());
		NodeList nl=node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			debugOut(nl.item(i),indent+"\t");
		}
	}
	
	/**
	 * check whether the given criteria are matched for the given element
	 * @param someElement - the element to check
	 * @param criteria - the criteria to check
	 */
	public boolean matchesCriteria(final Object someElement,final Object criteria) {

		// this condition should normally be false
		if (!(someElement instanceof HTMLElement))
			return false;

		HTMLElement htmlElement = (HTMLElement) someElement;

		Node htmlNode = htmlElement.getNode();
		Document doc = htmlNode.getOwnerDocument();
		if (DEBUG) {
			debugOut(doc,"");
		}	
		
		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(doc, XPathConstants.NODESET);
			final int nodeCount=nodes.getLength();
			for (int i = 0; i < nodeCount; i++) {
				if (nodes.item(i).equals(htmlNode)) {
					return true;
				}
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException("unable to evaluate xpath '"+path+"'", e);
		}
		return false;
	}

}