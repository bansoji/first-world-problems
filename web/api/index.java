import java.util.*;
import java.io.*;

class hello
{
    public static void main( String args[] )
    {
        //
        //  Here is a minimalistic CGI program that uses cgi_lib
        //
        //
        //  Print the required CGI header.
        //
        System.out.println(cgi_lib.Header());
        //
        //  Parse the form data into a Hashtable.
        //
        Hashtable form_data = cgi_lib.ReadParse(System.in);
        //
        // Create the Top of the returned HTML page
        //
        String name = (String)form_data.get("name");
        System.out.println(cgi_lib.HtmlTop("Hello There " + name + "!"));
        System.out.println("&lth1 align=center&gtHello There " + name +
                "!</h1>");
        System.out.println("Here are the name/value pairs from the form:");
        //
        //  Print the name/value pairs sent from the browser.
        //
        System.out.println(cgi_lib.Variables(form_data));
        //
        //  Print the Environment variables sent in from the Unix script.
        //
        System.out.println("Here are the CGI environment variables/value pairs" +
                "passed in from the UNIX script:");
        System.out.println(cgi_lib.Environment());
        //
        // Create the Bottom of the returned HTML page to close it cleanly.
        //
        System.out.println(cgi_lib.HtmlBot());
    }
}