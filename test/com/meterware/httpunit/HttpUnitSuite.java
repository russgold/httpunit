package com.meterware.httpunit;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Tests for the package.
 **/
public class HttpUnitSuite {

	public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
	}
	
	
	public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest( HttpUnitTest.suite() );
        suite.addTest( HtmlTablesTest.suite() );
		return suite;
	}


}

