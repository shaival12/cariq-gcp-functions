package gcfv2.fleetio;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

		String requestJsonTemp = "{ \"vendor_id\": " + request.getVendor_id() + ",\"vehicle_id\": "
				+ request.getVehicle_id() + ", \"date\": \" " + request.getDate() + "\",\n" + "	 \"fuel_type_id\": "
				+ request.getFuel_type_id() + ", \"liters\": " + request.getLiters() + ", \"partial\": "
				+ request.isPartial() + ", \"personal\": " + request.isPersonal() + ",\n"
				+ "	 \"price_per_volume_unit\": " + request.getPrice_per_volume_unit() + ", \"us_gallons\": "
				+ request.getUs_gallons() + ",  \"reference\": \"" + " Car IQ " + " \", \"meter_entry_attributes\": {\n"
				+ "	 \"value\": " + request.getMeter_entry_attributes().getValue() + " } \n" + "     }";

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
		String stationNumber = transaction.getStationNumber();
		stationNumber = stationNumber.length() > 4 ? stationNumber.substring(stationNumber.length() - 4)
				: stationNumber;
		String vendorName = transaction.getStationBrand() + " #" + stationNumber; // as per Fleetio

		logger.info("getVendor for name :" + vendorName);
		List<Vendor> list = getVendorList();
		logger.info("Vendor list: " + list.size());
		Long vendorId = null;
		if (list.size()>0) {
			// check if vendor already exists
			Optional<Vendor> vendor = list.stream().filter(e -> e.getName().equalsIgnoreCase(vendorName)).findFirst();

			if (vendor.isPresent()) {
				logger.info("vendor id : " + vendor.get().getId()); // 2
				vendorId = vendor.get().getId();
			}
		} 
    
    // no existing vendor
    if(vendorId == null) { // create new Vendor if not found
			logger.info("new vendor will be created : " + vendorId);
			vendorId = postNewVendorToFleetio(vendorName, transaction);
			logger.info("After new vendor will be created : " + vendorId);
		}

		logger.info("vendorId :" + vendorId);
		return vendorId;
	}

	/**
	 * create new Vendor on Fleetio
	 */
	public Long postNewVendorToFleetio(String vendorName, Transaction transaction) throws Exception {

		String requestJsonTemp = "{ \"name\": \"" + vendorName + "\", \n" + "	 \"city\": \""
				+ transaction.getStationCity() + "\", \"country\": \"" + transaction.getStationCountry()
				+ "\", \"fuel\": true," + " \"street_address\": \"" + transaction.getStationLine1() + "\",\n"
				+ "	 \"postal_code\": \"" + transaction.getStationZip() + "\" }";

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

		// FleetioConnector connector = new FleetioConnector();
		List<Vendor> list = new ArrayList<Vendor>();
		list.add(new Vendor(11l, "Shell #9195"));
		list.add(new Vendor(12l, "Shell #9193"));
		String name = "Shell #9195";

		Optional<Vendor> vendor = list.stream().filter(e -> e.getName().equalsIgnoreCase(name)).findFirst();

		if (vendor.isPresent()) {
			System.out.println(vendor.get().getId()); // 2
		} else {
			System.out.println("no value?");
		}

		// String url = "Shell #9091";
		// System.out.print(UriUtils.encode(url, StandardCharsets.UTF_8));

	}

	public List<Vendor> getVendorList() {
		RestTemplate restTemplate = new RestTemplate();
		final HttpEntity<String> entity = new HttpEntity<String>(createHttpHeaders(Constants.token, Constants.account));
		logger.info("get vendor url :" + Constants.fleetioPostVendorsUrl);
		ResponseEntity<List<Vendor>> response = restTemplate.exchange(Constants.fleetioPostVendorsUrl, HttpMethod.GET,
				entity, new ParameterizedTypeReference<List<Vendor>>() {
				});
		
		return response.getBody();
	}

}
