package com.meterware.httpunit;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;


/**
 * A unit test of the httpunit parsing classes.
 **/
public class HttpUnitTest extends TestCase {

	public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
	}
	
	
	public static Test suite() {
		return new TestSuite( HttpUnitTest.class );
	}


	public HttpUnitTest( String name ) {
	    super( name );
	}


    public void setUp() throws Exception {
        _noForms = new ReceivedPage( _baseURL, HEADER + "<body>This has no forms but it does" +
                                       "have <a href=\"/other.html\">an active link</A>" +
                                       " and <a name=here>an anchor</a>" +
                                       "</body></html>" );
        _oneForm = new ReceivedPage( _baseURL, HEADER + "<body><h2>Login required</h2>" +
                                     "<form method=POST action = \"/servlet/Login\"><B>" +
                                     "Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                     "<input type=\"checkbox\" name=first>Disabled" +
                                     "<input type=\"checkbox\" name=second checked>Enabled" +
                                     "<br><Input type=submit value = \"Log in\">" +
                                     "</form></body></html>" );
        _hidden  = new ReceivedPage( _baseURL, HEADER + "<body><h2>Login required</h2>" +
                                     "<form method=POST action = \"/servlet/Login\">" +
                                     "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">" +
                                     "<br><Input name=typeless>" +
                                     "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                     "<br><Input type=submit value = \"Log in\">" +
                                     "</form></body></html>" );
        _tableForm = new ReceivedPage( _baseURL, HEADER + "<body><h2>Login required</h2>" +
                                       "<form method=POST action = \"/servlet/Login\">" +
                                       "<table summary=\"\"><tr><td>" +
                                       "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                       "</td><td><Input type=Radio name=sex value=male>Masculine" +
                                       "</td><td><Input type=Radio name=sex value=female checked>Feminine" +
                                       "</td><td><Input type=Radio name=sex value=neuter>Neither" +
                                       "<Input type=submit value = \"Log in\"></tr></table>" +
                                       "</form></body></html>" );
        _selectForm = new ReceivedPage( _baseURL, HEADER + "<body><h2>Login required</h2>" +
                                       "<form method=POST action = \"/servlet/Login\">" +
                                       "<Select name=color><Option>blue<Option selected>red \n" +
                                       "<Option>green</select>" +
                                       "<TextArea name=\"text\">Sample text</TextArea>" +
                                       "</form></body></html>" );
    }
	
	
	public void testFindNoForm() {
        WebForm[] forms = _noForms.getForms();
        assertNotNull( forms );
        assertEquals( 0, forms.length );
    }

	public void testFindOneForm() {
        WebForm[] forms = _oneForm.getForms();
        assertNotNull( forms );
        assertEquals( 1, forms.length );
    }

    public void testFormParameters() {
        WebForm form = _oneForm.getForms()[0];
        String[] parameters = form.getParameterNames();
        assertNotNull( parameters );
        assertEquals( 3, parameters.length );
        assertEquals( "name", parameters[0] );

        assertEquals( "First checkbox",  "",   form.getParameterValue( "first" ) );
        assertEquals( "Second checkbox", "on", form.getParameterValue( "second" ) );
    }

    public void testFormRequest() throws Exception {
        WebForm form = _oneForm.getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter( "name", "master" );
        assert( "Should be a post request", !(request instanceof GetMethodWebRequest) );
        assertEquals( "http://www.meterware.com/servlet/Login", request.getURL().toExternalForm() );
    }

    public void testLinks() throws Exception {
        WebLink[] links = _noForms.getLinks();
        assertNotNull( links );
        assertEquals( 1, links.length );
    }
    
    public void testLinkRequest() throws Exception {
        WebLink link = _noForms.getLinks()[0];
        WebRequest request = link.getRequest();
        assert( "Should be a get request", request instanceof GetMethodWebRequest );
        assertEquals( "http://www.meterware.com/other.html", request.getURL().toExternalForm() );
    }

    public void testDefaultParameterType() throws Exception {
        WebForm form = _hidden.getForms()[0];
        assertEquals( 3, form.getParameterNames().length );
    }

    public void testHiddenParameters() throws Exception {
        WebForm form = _hidden.getForms()[0];
        WebRequest request = form.getRequest();
        assertEquals( "surprise", request.getParameter( "secret" ) );
    }


    public void testTableForm() throws Exception {
        WebForm form = _tableForm.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "Number of parameters", 2, parameterNames.length );
        assertEquals( "First parameter name", "name", parameterNames[0] );
        assertEquals( "Default name", "", form.getParameterValue( "name" ) );
        assertEquals( "Default sex", "female", form.getParameterValue( "sex" ) );
        WebRequest request = form.getRequest();
    }


    public void testSelect() throws Exception {
        WebForm form = _selectForm.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "Number of parameters", 2, parameterNames.length );
        assertEquals( "Default color", "red", form.getParameterValue( "color" ) );
        assertEquals( "Default text",  "Sample text", form.getParameterValue( "text" ) );
        WebRequest request = form.getRequest();
        assertEquals( "Submitted color", "red", request.getParameter( "color" ) );
        assertEquals( "Submitted text",  "Sample text", request.getParameter( "text" ) );
    }


    public void testMultiSelect() throws Exception {
        ReceivedPage page = new ReceivedPage( _baseURL, HEADER + "<body><form method=GET action = \"/ask\">" +
                                       "<Select multiple size=4 name=colors>" +
                                       "<Option>blue<Option selected>red \n" +
                                       "<Option>green<Option value=\"pink\" selected>salmon</select>" +
                                       "</form></body></html>" );
        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "num parameters", 1, parameterNames.length );
        assertEquals( "parameter name", "colors", parameterNames[0] );
        assertMatchingSet( "Select defaults", new String[] { "red", "pink" }, form.getParameterValues( "colors" ) );
        assertMatchingSet( "Select options", new String[] { "blue", "red", "green", "salmon" }, form.getOptions( "colors" ) );
        assertMatchingSet( "Select values", new String[] { "blue", "red", "green", "pink" }, form.getOptionValues( "colors" ) );
        WebRequest request = form.getRequest();
        assertMatchingSet( "Request defaults", new String[] { "red", "pink" }, request.getParameterValues( "colors" ) );
        assertEquals( "URL", "http://www.meterware.com/ask?colors=red&colors=pink", request.getURL().toExternalForm() );
    }                         
                              
    private static URL _baseURL;
     
    static {
        try {
            _baseURL = new URL( "http://www.meterware.com" );
        } catch (java.net.MalformedURLException e ) {}  // ignore
    }

    private final static String HEADER = "<html><head><title>A Sample Page</title></head>";
    private ReceivedPage _noForms;
    private ReceivedPage _oneForm;
    private ReceivedPage _hidden;
    private ReceivedPage _tableForm;
    private ReceivedPage _selectForm;


    private void assertMatchingSet( String comment, Object[] expected, Object[] found ) {
        Vector expectedItems = new Vector();
        Vector foundItems    = new Vector();

        for (int i = 0; i < expected.length; i++) expectedItems.addElement( expected[i] );
        for (int i = 0; i < found.length; i++) foundItems.addElement( found[i] );

        for (int i = 0; i < expected.length; i++) {
            if (!foundItems.contains( expected[i] )) {
                fail( comment + ": expected " + asText( expected ) + " but found " + asText( found ) );
            } else {
                foundItems.removeElement( expected[i] );
            }
        }

        for (int i = 0; i < found.length; i++) {
            if (!expectedItems.contains( found[i] )) {
                fail( comment + ": expected " + asText( expected ) + " but found " + asText( found ) );
            } else {
                expectedItems.removeElement( found[i] );
            }
        }

        if (!foundItems.isEmpty()) fail( comment + ": expected " + asText( expected ) + " but found " + asText( found ) );
    }


    private String asText( Object[] args ) {
        StringBuffer sb = new StringBuffer( "{" );
        for (int i = 0; i < args.length; i++) {
            if (i != 0) sb.append( "," );
            sb.append( '"' ).append( args[i] ).append( '"' );
        }
        sb.append( "}" );
        return sb.toString();
    }

}
