import com.meterware.httpunit.*;

import java.io.IOException;
import java.net.MalformedURLException;

import org.xml.sax.*;

public class Example {


    public static void main( String[] params ) {
        try {
            WebRequest          request;
            WebResponse         response;
            WebConversation     conversation = new WebConversation();
            
            request = new GetMethodWebRequest( "http://www.meterware.com/servlet/TopSecret" );
            response = conversation.getResponse( request );
            System.out.println( response );

            WebForm loginForm = response.getForms()[0];

            request = loginForm.getRequest();
            request.setParameter( "name", "master" );
            response = conversation.getResponse( request );
            System.out.println( response );

        } catch (Exception e) {
            System.err.println( "Exception: " + e );
        }
    }
}

