package gcfv2;

import java.io.BufferedWriter;
import java.util.logging.Logger;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

public class HelloHttpFunction implements HttpFunction {
	
 private static final Logger logger = Logger.getLogger(HelloHttpFunction.class.getName());
	 
  public void service(final HttpRequest request, final HttpResponse response) throws Exception {
	 
	 logger.info("GCP function info log at : [ " + System.currentTimeMillis()+ " ]" );
	 logger.warning("GCP functiona warning log!");
	    
	 GeoTabConnector.connectToGeoTab();
	 
     final BufferedWriter writer = response.getWriter();
     
     writer.write("GCP function done at  : [ " + System.currentTimeMillis()+ " ]" );
 }
}
