package com.meterware.httpunit;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;


/**
 * A test of the web form functionality.
 **/
public class WebFormTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( WebFormTest.class );
    }


    public WebFormTest( String name ) {
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

                              
    public void testUnspecifiedDefaults() throws Exception {
        ReceivedPage page = new ReceivedPage( _baseURL, HEADER + "<body><form method=GET action = \"/ask\">" +
                                       "<Select name=colors><Option>blue<Option>red</Select>" +
                                       "<Select name=fish><Option value=red>snapper<Option value=pink>salmon</select>" +
                                       "<Select name=media multiple size=2><Option>TV<Option>Radio</select>" +
                                       "</form></body></html>" );
        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "inferred color default", "blue", form.getParameterValue( "colors" ) );
        assertEquals( "inferred fish default", "red", form.getParameterValue( "fish" ) );
        assertMatchingSet( "inferred media default", new String[0], form.getParameterValues( "media" ) );

        WebRequest request = form.getRequest();
        assertEquals( "inferred color request", "blue", request.getParameter( "colors" ) );
        assertEquals( "inferred fish request",  "red", request.getParameter( "fish" ) );
        assertMatchingSet( "inferred media default", new String[0], request.getParameterValues( "media" ) );
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


}
