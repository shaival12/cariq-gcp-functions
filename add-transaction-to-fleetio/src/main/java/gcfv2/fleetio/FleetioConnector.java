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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import gcfv2.Constants;
import gcfv2.Transaction;

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
	public List<FleetioResponse> getVehiclesFromFleetio(String vins) throws Exception {

		String fleetioGetUrl = String.format(Constants.fleetioGetUrl, vins);
		List<FleetioResponse> fleetioresponse = getVehiclesFromFleetio(fleetioGetUrl, Constants.token,
				Constants.account);
		return fleetioresponse;
	}

	public void postFuelTranactionToFleetio(FleetioRequest request) throws Exception {

		String requestJsonTemp = "{ \"vehicle_id\": " + request.getVehicle_id() + ", \"date\": \" " + request.getDate()
				+ "\",\n" + "	 \"fuel_type_id\": " + request.getFuel_type_id() + ", \"liters\": "
				+ request.getLiters() + ", \"partial\": " + request.isPartial() + ", \"personal\": "
				+ request.isPersonal() + ",\n" + "	 \"price_per_volume_unit\": " + request.getPrice_per_volume_unit()
				+ ", \"us_gallons\": " + request.getUs_gallons() + ",  \"reference\": \"" + " Car IQ "
				+ " \", \"meter_entry_attributes\": {\n" + "	 \"value\": "
				+ request.getMeter_entry_attributes().getValue() + " } \n" + "     }";

		RestTemplate restTemplate = new RestTemplate();
		final HttpEntity<String> entity = new HttpEntity<String>(requestJsonTemp,
				createHttpHeaders(Constants.token, Constants.account));
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
	private List<FleetioResponse> getVehiclesFromFleetio(String fooResourceUrl, String token, String account)
			throws Exception {

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

	/**
	 * get vendors from Fleetio
	 * 
	 * @param fooResourceUrl
	 * @param token
	 * @param account
	 * @return
	 */
	public Long getVendor(Transaction transaction) throws Exception {
		String vendorName = transaction.getStationBrand() + " #" + transaction.getStationNumber(); // as per Fleetio
																									// standard
		logger.info("getVendor for name :" + vendorName);

		RestTemplate restTemplate = new RestTemplate();
		final HttpEntity<String> entity = new HttpEntity<String>(createHttpHeaders(Constants.token, Constants.account));
		String url = String.format(Constants.fleetioGetVendorsUrl, vendorName);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

		Long vendorId = null;
		if (response.getStatusCode() == HttpStatus.OK) {

			JSONArray array = new JSONArray(response.getBody());
			if (array.length() > 0) { // if vendor exists
				for (int i = 0; i < array.length(); i++) {
					JSONObject object = array.getJSONObject(i);
					vendorId = object.getLong("id");
				}

			} else { // create new Vendor if not found

				vendorId = postNewVendorToFleetio(vendorName, transaction);
			}

		}
		logger.info("vendorId :" + vendorId);
		return vendorId;
	}

	/**
	 * create new Vendor on Fleetio
	 */
	public Long postNewVendorToFleetio(String vendorName, Transaction transaction) throws Exception {

		String requestJsonTemp = "{ \"name\": " + vendorName + ", \n" + "	 \"city\": " + transaction.getStationCity()
				+ ", \"country\": " + transaction.getStationCountry() + ", \"fuel\": true," + " \"street_address\": "
				+ transaction.getStationLine1() + ",\n" + "	 \"postal_code\": " + transaction.getStationZip() + " }";

		RestTemplate restTemplate = new RestTemplate();
		final HttpEntity<String> entity = new HttpEntity<String>(requestJsonTemp,
				createHttpHeaders(Constants.token, Constants.account));
		logger.info(entity.getBody().toString());
		ResponseEntity<Vendor> response = restTemplate.postForEntity(Constants.fleetioPostVendorsUrl, entity,
				Vendor.class);
		logger.info("status code from Fleetio for Vendor creation :" + response.getStatusCodeValue());
		return response.getBody().getId();
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

	public static void main(String args[]) throws Exception {

		FleetioConnector connector = new FleetioConnector();
		// Long vendorId = connector.getVendor("Shell #1112", Constants.token,
		// Constants.account);
		// System.out.print(vendorId);

	}

}
