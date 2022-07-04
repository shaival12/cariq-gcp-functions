package gcfv2.fleetio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import gcfv2.Constants;

/**
 * Client to connect with Fleetio Apis
 * 
 * @author shaival
 *
 */
public class FleetioConnector {

private static final Logger logger = Logger.getLogger(FleetioConnector.class.getName());



	public void sendToFleetio(String vin, FleetioRequest request) throws Exception {
		List<FleetioResponse> fleetioresponse = getVehiclesFromFleetio(vin);
		logger.info("vin found response  :" + fleetioresponse.size());

		if (fleetioresponse != null && fleetioresponse.size() > 0) {
			request.setVehicle_id(fleetioresponse.get(0).getId().intValue());
			postFuelTranactionToFleetio(request);
		}
	}

	/**
	 * to get vehicle id of Fleetio based on single or multiple vins passed in e.g.
	 * vin,vin2
	 * 
	 * @param vins
	 * @return
	 */
	public  List<FleetioResponse> getVehiclesFromFleetio(String vins) throws Exception{

		String fleetioGetUrl = String.format(Constants.fleetioGetUrl, vins);
		List<FleetioResponse> fleetioresponse = getVehiclesFromFleetio(fleetioGetUrl, Constants.token,
				Constants.account);
		return fleetioresponse;
	}

	
	public  void postFuelTranactionToFleetio(FleetioRequest request) throws Exception {

		
		String requestJsonTemp = "{ \"vehicle_id\": "+request.getVehicle_id()+", \"date\": \" "+request.getDate()+"\",\n"
				+ "	 \"fuel_type_id\": "+request.getFuel_type_id()+", \"liters\": "+request.getLiters()+", \"partial\": "+request.isPartial()+", \"personal\": "+request.isPersonal()+",\n"
				+ "	 \"price_per_volume_unit\": "+request.getPrice_per_volume_unit()+", \"us_gallons\": "+request.getUs_gallons()+",  \"reference\": \""+" Car IQ " 
			    + " \", \"meter_entry_attributes\": {\n"
				+ "	 \"value\": "+request.getMeter_entry_attributes().getValue()+" } \n"
				+ "     }";
		
		RestTemplate restTemplate = new RestTemplate();
		//final HttpEntity<FleetioRequest> entity = new HttpEntity<FleetioRequest>(request, createHttpHeaders(Constants.token, Constants.account));
		final HttpEntity<String> entity = new HttpEntity<String>(requestJsonTemp, createHttpHeaders(Constants.token, Constants.account));
		logger.info(entity.getBody().toString());
		ResponseEntity<String> response = restTemplate.postForEntity(Constants.fleetioPostUrl, entity, String.class);
		logger.info("status code from Fleetio :" + response.getStatusCodeValue());
	}

	/**
	 * get vehicles from Fleetio based on input Vins
	 * 
	 * @param fooResourceUrl
	 * @param token
	 * @param account
	 * @return
	 */
	private List<FleetioResponse> getVehiclesFromFleetio(String fooResourceUrl, String token, String account) throws Exception {

		List<FleetioResponse> fleetioresponse = new ArrayList<FleetioResponse>();

			RestTemplate restTemplate = new RestTemplate();
			final HttpEntity<String> entity = new HttpEntity<String>(createHttpHeaders(token, account));
			ResponseEntity<String> response = restTemplate.exchange(fooResourceUrl + "", HttpMethod.GET, entity,
					String.class);
			
			JSONArray array = new JSONArray(response.getBody());
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				fleetioresponse.add(new FleetioResponse(object.getLong("id"), object.getString("vin")));
			}

		return fleetioresponse;
	}

	private static HttpHeaders createHttpHeaders(String token, String account) {
		String notEncoded = token + ":" + account;
		String encodedAuth = Base64.getEncoder().encodeToString(notEncoded.getBytes());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Token " + token);
		headers.add("Account-token", account);
		return headers;
	}
	
	
	public static void main(String args[]) {

		String vin = "1C4RJFLG2JC419001"; //"vin1X1234567890" ; 
		
		// send data to fleetio
		FleetioRequest request = new FleetioRequest();
		request.setVehicle_id(1958594);
		request.setUs_gallons(5);
		request.setPrice_per_volume_unit(6);
		// request.setPersonal(false);
		request.setPartial(true);
		request.setMeter_entry_attributes(new Meter_entry_attributes(50743));
		request.setLiters(10);
		request.setFuel_type_id(309829);
		request.setDate(LocalDateTime.now());
			
		FleetioConnector connector = new FleetioConnector();
		//connector.sendToFleetio(vin, request);
		
		Double d = 2.1927312E7;
		System.out.print(d.intValue());

	}

	
}
