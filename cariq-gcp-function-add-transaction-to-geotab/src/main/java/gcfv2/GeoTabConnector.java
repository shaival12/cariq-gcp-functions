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

import com.geotab.api.GeotabApi;
import com.geotab.http.request.AuthenticatedRequest;
import com.geotab.http.request.param.EntityParameters;
import com.geotab.http.request.param.SearchParameters;
import com.geotab.http.response.IdResponse;
import com.geotab.model.Id;
import com.geotab.model.entity.fuel.FuelTransaction;
import com.geotab.model.login.Credentials;
import com.geotab.model.login.LoginResult;

public class GeoTabConnector {

	private static final Logger logger = Logger.getLogger(GeoTabConnector.class.getName());

	public static void connectToGeoTab() {

		 logger.info(" ########   START ########  ");
		 
		UUID jobId =  null;
		try {
			
			 // find last_inserted_dt from batch_job_log table
			 Timestamp last_inserted_dt = SQLRunner.findMaxBatchLogForFuelTxJob();
			 logger.info(" Step 1:  last_inserted_dt : " + last_inserted_dt);
			 
			 
			 // fetch records, if > 0 : run Job else not
			 List<Transaction> dblist = SQLRunner.fetchFuelTransactionResults(last_inserted_dt);
			 logger.info(" dblist size : " + dblist.size());
			 
		
			// check for size of the Tx list, if > 0; 
			if (dblist.size() > 0) {
				
				// if dateTime != null, insert new row with pending status
				jobId = UUID.randomUUID();
				logger.info(" jobId : " + jobId);
				 
				SQLRunner.insertNewBatchLog("FUEL_TX_GEOTAB_JOB", last_inserted_dt, "pending", jobId,
						"count : " + dblist.size());

				List<Transaction> list = new ArrayList<Transaction>();
				list.add(dblist.get(1)); // TODO fix of now

				// find list of VINs
				List<FuelTransaction> ftList =

						list.stream().map(e -> {

							FuelTransaction ft = FuelTransaction.fuelTransactionBuilder()
									.description(e.getDescription()).driverName(e.getDriverName())
									.providerProductDescription(e.getProviderProductDesc()).siteName(e.getSiteName())
									.vehicleIdentificationNumber(e.getVin()).cost(e.getCost())
									.currencyCode(e.getCurrencyCode()).dateTime(e.getDateTime())
									.location(e.getLocation()).odometer(e.getOdometer()).productType(e.getProductType())
									.volume(e.getVolume()).build();

							return ft;

						}).collect(Collectors.toList());

				// find records from Tx tables in status completed

				System.out.println(" ftList : " + ftList.size());

				Credentials credentials = Credentials.builder().database("cariq").userName("eric@gocariq.com")
						.password("trojan.orris.tricolor.piece").build();

				GeotabApi api = new GeotabApi(credentials);

				LoginResult loginResult = api.authenticate();

				AuthenticatedRequest<?> request = AuthenticatedRequest.authRequestBuilder().method("Get")
						.params(SearchParameters.searchParamsBuilder().credentials(loginResult.getCredentials())
								.typeName("Device").build())
						.build();

				// call GeoTab APi and push data one by one
				LocalDateTime last_inserted_at = null;
				for (FuelTransaction tf : ftList) {
					AuthenticatedRequest<?> request1 = AuthenticatedRequest.authRequestBuilder().method("Add").params(
							EntityParameters.entityParamsBuilder().typeName("FuelTransaction").entity(tf).build())
							.build();

					Optional<Id> response = api.call(request1, IdResponse.class);

					last_inserted_at = tf.getDateTime();
					logger.info(" response : " + response.get());
					logger.info(" last_inserted_at : " + last_inserted_at);
				}

				
				// update to status = success
				SQLRunner.updateBatchLog("success", jobId, Timestamp.valueOf(last_inserted_at));
				
				
			} else {
				logger.info(" Job not need to be run as dblist size : " + dblist.size());
			}

		} catch (Exception e) {
			SQLRunner.updateBatchLog("failed", jobId, null);
			logger.log(Level.SEVERE, "Error in connectToGeoTab1 : [ " + System.currentTimeMillis() + " ]");
			e.printStackTrace();
		}
		
		 logger.info("########  END ########  ");

	}

    // to test it locally 
	public static void main(String args[]) {
		connectToGeoTab();

	}

}
