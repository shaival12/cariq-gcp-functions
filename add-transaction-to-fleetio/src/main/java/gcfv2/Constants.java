package gcfv2;

public interface Constants {
	
	//Prod 
	static final Object DB_NAME = "jdbc:postgresql:///cariq_prod";
	static final String DB_USER = "";
	static final String DB_PASS = "";
	static final String INSTANCE_CONNECTION_NAME = "cariq-vehicle-data-test:us-central1:vehicle-data";
	
	//Fleetio
	static final String fleetioGetUrl = "https://secure.fleetio.com/api/v1/vehicles?q[vin_in_s]=%s";
	static final String fleetioPostUrl = "https://secure.fleetio.com/api/v1/fuel_entries/";
	static final String token = "";
	static final String account = "";
    static final int FLEETIO_FULE_TYPE_ID_GAS = 309829;
	static final boolean FLEETIO_TEST = true;
	static final double FLEETIO_TEST_ODOMETER = 50734;
	static final String FLEETIO_TEST_VIN = "1C4RJFLG2JC419001";
	
	//secret info
	 static final String SECRET_VERSION = "latest";
	 static final String SECRET_NAME = "geotab_api_credentials";
	 static final String GCP_PROJECT_NAME = "cariq-vehicle-data-test";
}
