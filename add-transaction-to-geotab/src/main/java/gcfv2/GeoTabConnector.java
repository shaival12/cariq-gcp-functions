package gcfv2;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geotab.api.GeotabApi;
import com.geotab.http.request.AuthenticatedRequest;
import com.geotab.http.request.param.EntityParameters;
import com.geotab.http.response.IdResponse;
import com.geotab.model.Id;
import com.geotab.model.entity.fuel.FuelTransaction;
import com.geotab.model.login.Credentials;

public class GeoTabConnector {

	private static final Logger logger = Logger.getLogger(GeoTabConnector.class.getName());

	public void connectToGeoTab() {

		logger.info(" ########   START ########  ");

		SQLRunner sqlRunner = new SQLRunner();
		UUID jobId = null;
		try {

			List<String> fleetIds = new ArrayList<String>();
			List<Fleet> fleetList = null;

			// access secrets
			try {
				String secrets = AccessSecretVersion.accessSecretVersion();
				logger.info("[Step 1] : get secrets ");

				ObjectMapper mapper = new ObjectMapper();
				fleetList = mapper.readValue(secrets.toLowerCase(), new TypeReference<List<Fleet>>() {
				});

				if (fleetList != null && fleetList.size() > 0) {
					fleetIds = fleetList.stream().map(e -> "'" + e.getFleetid() + "'").collect(Collectors.toList());
					System.out.println("fleetIds size from secrets :" + fleetIds.size());
				}

			} catch (Exception ep) {
				ep.printStackTrace();
				System.out.println("Error while getting secrets :" + ep);
			}

			// find last_inserted_dt from batch_job_log table
			if (fleetList != null && fleetList.size() > 0) {

				Timestamp last_inserted_dt = sqlRunner.findMaxBatchLogForFuelTxJob();
				logger.info("[ Step 2 ]:  last_inserted_dt from batch_job_log table : [" + last_inserted_dt + " ]");

				// push data to geoTab
				pushData(sqlRunner, jobId, fleetIds, last_inserted_dt, fleetList);

			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in connectToGeoTab1 : [ " + e.getMessage() + " ]");
			e.printStackTrace();
		}

		logger.info("########  END ########  ");

	}

	private void pushData(SQLRunner sqlRunner, UUID jobId, List<String> fleetIds, Timestamp lastInsertedAt,
			List<Fleet> fleetList) throws Exception {

		LocalDateTime last_inserted_at = null;

		// fetch records, if > 0 : run Job else not
		List<Transaction> dblist = sqlRunner.fetchFuelTransactionResults(lastInsertedAt, fleetIds);
		logger.info("[Step 3]: get fuel Transaction records given fleet ids,  dblist size : " + dblist.size());

		// check for size of the Tx list, if > 1;
		if (dblist.size() > 0) {

			// if dateTime != null, insert new row with pending status
			jobId = UUID.randomUUID();
			logger.info(" new jobId : " + jobId);

			// add a job with pending status
			sqlRunner.insertNewBatchLog("FUEL_TX_GEOTAB_JOB", lastInsertedAt, "pending", jobId, "T:" + dblist.size());

			int successCount = 0;
			Timestamp lastSuccess = null;
			try {
				lastSuccess = pushDataByFleet(fleetList, last_inserted_at, dblist, successCount);
			} catch (Exception e) {
				logger.info("in catch, lastSuccess  : " + lastSuccess);
				logger.log(Level.SEVERE, "Error in pushData : [ " + e.getMessage() + " ]");
				e.printStackTrace();
			} finally {
				// find the last success run and update table
				logger.info("in finally, lastSuccess  : " + lastSuccess);
				logger.info("in finally, successCount  : " + successCount);
				if (lastSuccess != null) {
					sqlRunner.updateBatchLog("success", jobId, lastSuccess != null ? lastSuccess : lastInsertedAt);
				} else {
					sqlRunner.updateBatchLog("failed", jobId, lastSuccess != null ? lastSuccess : lastInsertedAt);

				}
			}

		} else {
			logger.info(" No Job will run as dblist size : " + dblist.size());
		}

	}

	private Timestamp pushDataByFleet(List<Fleet> fleetList, LocalDateTime last_inserted_at, List<Transaction> list,
			int successCount) throws Exception {
		logger.info(" pushDataByFleet ");

		// find last insertedDate
		Timestamp lastSuccess = null;

		try {

			int counter = 1;
			for (Transaction e : list) {

				String fleetId = e.getFleetId();
				Fleet fleet = getFleet(fleetList, fleetId);
				logger.info(" fleet id : " + fleet.getFleetid());

				if (fleet != null) {// .odometer(e.getOdometer()) removed
					FuelTransaction ft = FuelTransaction.fuelTransactionBuilder().description(e.getDescription())
							.driverName(e.getDriverName()).providerProductDescription(e.getProviderProductDesc())
							.siteName(e.getSiteName()).vehicleIdentificationNumber(e.getVin()).cost(e.getCost())
							.odometer(e.getOdometer()).currencyCode(e.getCurrencyCode()).dateTime(e.getDateTime())
							.location(e.getLocation()).productType(e.getProductType()).volume(e.getVolume()).build();

					AuthenticatedRequest<?> request1 = AuthenticatedRequest.authRequestBuilder().method("Add").params(
							EntityParameters.entityParamsBuilder().typeName("FuelTransaction").entity(ft).build())
							.build();

					try {
						GeotabApi api = getGeoTabApiForAdd(fleet);
						logger.info(counter++ + ") before calling api for  : " + ft.getVehicleIdentificationNumber()
								+ " -- " + ft.getDateTime());

						Optional<Id> response = api.call(request1, IdResponse.class);
						logger.info(" response from geotab api : " + response.get());

						lastSuccess = Timestamp.valueOf(e.getDateTime());
						successCount++;

					} catch (com.geotab.http.exception.DuplicateException exx) {
						logger.info("Error while calling GeoTab API, DuplicateException log message and proceed : [ "
								+ exx.getMessage() + " ]");
						exx.printStackTrace();
					}

				}

			} // loop ends
		} catch (Exception et) {
			logger.log(Level.SEVERE, "Error in pushDataByFleet, coming out of loop : [ " + et.getMessage() + " ]");
		}

		logger.info(" before return lastSuccess : [" + lastSuccess + "]");
		return lastSuccess;
	}

	private Fleet getFleet(List<Fleet> fleetList, String fleetId) {
		for (Fleet fleet : fleetList) {
			if (fleet.getFleetid().equalsIgnoreCase(fleetId)) {
				return fleet;
			}
		}

		return null;
	}

	private static GeotabApi getGeoTabApiForAdd(Fleet fleet) {

		Credentials credentials = Credentials.builder().database(fleet.getDatabase()).userName(fleet.getUsername())
				.password(fleet.getPassword()).build();

		GeotabApi api = new GeotabApi(credentials);
		return api;
	}

	// to test it locally
	public static void main(String args[]) {
		GeoTabConnector geo = new GeoTabConnector();
		geo.connectToGeoTab();

	}

}
