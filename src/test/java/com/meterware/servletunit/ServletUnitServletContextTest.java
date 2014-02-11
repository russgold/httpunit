package com.meterware.servletunit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletContext;

import org.junit.Test;

public class ServletUnitServletContextTest {
	private static final String EXISTENT_RESOURCE_PATH = "src/test/resources/existent.xml";
	private static final String NONEXISTENT_RESOURCE_PATH = "src/test/resources/nonexistent.xml";
	
	@Test
	public void testGetResource() throws Exception {
		WebApplication webapp = new WebApplication();
		ServletContext sc = new ServletUnitServletContext(webapp);

		// for existent resources
		InputStream is = sc.getResourceAsStream(EXISTENT_RESOURCE_PATH);
		assertNotNull("must not return a null", is);
		is.close();
		
		URL r = sc.getResource(EXISTENT_RESOURCE_PATH);
		assertNotNull("must not return a null", r);

		// for non-existent resources
		is = sc.getResourceAsStream(NONEXISTENT_RESOURCE_PATH);
		assertNull("must return a null", is);
		
		r = sc.getResource(NONEXISTENT_RESOURCE_PATH);
		assertNull("must return a null", r);

	}

}
