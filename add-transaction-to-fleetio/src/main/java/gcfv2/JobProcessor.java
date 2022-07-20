package gcfv2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gcfv2.fleetio.FleetioConnector;
import gcfv2.fleetio.FleetioRequest;
import gcfv2.fleetio.Meter_entry_attributes;

/**
 * Job Processor for all GCP function jobs
 * @author shaival
 *
 */
public class JobProcessor {

	private static final Logger logger = Logger.getLogger(JobProcessor.class.getName());

	public void process() {

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
					logger.info("fleetIds size from secrets :" + fleetIds.size());
				}

			} catch (Exception ep) {
				ep.printStackTrace();
				logger.log(Level.SEVERE,"Error while getting secrets :" + ep);
			}

			// find last_inserted_dt from batch_job_log table
			if (fleetList != null && fleetList.size() > 0) {

				Timestamp last_inserted_dt = sqlRunner.findMaxBatchLogForFuelTxJob();
				logger.info("[ Step 2 ]:  last_inserted_dt from batch_job_log table : [" + last_inserted_dt + " ]");

				// push data 
				pushData(sqlRunner, jobId, fleetIds, last_inserted_dt, fleetList);

			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in JobProcessor.process : [ " + e.getMessage() + " ]");
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
			sqlRunner.insertNewBatchLog("FUEL_TX_FLEETIO_JOB", lastInsertedAt, "pending", jobId, "T:" + dblist.size());

			List<Transaction> successFulList = new ArrayList<Transaction>();
			Timestamp lastSuccess = null;
			try {
				lastSuccess = pushDataByFleet(fleetList, last_inserted_at, dblist, successFulList);
				logger.info("successFulList  : " + successFulList.size());

			} catch (Exception e) {
				logger.info("in catch, lastSuccess  : " + lastSuccess);
				logger.log(Level.SEVERE, "Error in pushData : [ " + e.getMessage() + " ]");
				e.printStackTrace();
			} finally {
				// find the last success run and update table
				logger.info("in finally, lastSuccess  : " + lastSuccess);
				logger.info("in finally, successCount  : " + successFulList.size());
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

	
	private static double gallonToLiters(double volumeInGallon) {
		return volumeInGallon * 3.7854118;
	}

	private Timestamp pushDataByFleet(List<Fleet> fleetList, LocalDateTime last_inserted_at, List<Transaction> list,
			List<Transaction> successFulList) throws Exception {
		logger.info(" pushDataByFleet ");

		// find last insertedDate
		Timestamp lastSuccess = null;

		try {

			int counter = 1;
			for (Transaction t : list) {

				
				Fleet fleet = getFleet(fleetList, t.getFleetId());
				logger.info(" fleet id : " + fleet.getFleetid());

				if (fleet != null) {
					try {
						FleetioConnector fleetioConnector = new FleetioConnector();
						Long vendorId = fleetioConnector.getVendor(t);
					
						logger.info(counter++ + ") before calling api for  : " + t.getVin() + " -- " + t.getDateTime());

						String vin = t.getVin();
					
						FleetioRequest request = new FleetioRequest();
						double odometer = t.getOdometer();
                       //odometer =  51880;
						if (Constants.FLEETIO_TEST) {
							vin = Constants.FLEETIO_TEST_VIN; //"1C4RJFLG2JC419001";
							 odometer = Constants.FLEETIO_TEST_ODOMETER;
							//request.setVehicle_id(1958594);
						}

						request.setLiters(covertToTwoDecimal(t.getVolume()));
						request.setUs_gallons(t.getVolume());  // gallons
						request.setPrice_per_volume_unit(t.getUnitPrice());
						request.setPersonal(null);
						request.setPartial(null);
						request.setStationName(t.getStationName());
						logger.info("odometer : " + odometer);
						request.setMeter_entry_attributes(new Meter_entry_attributes(odometer));
						request.setFuel_type_id(Constants.FLEETIO_FULE_TYPE_ID_GAS); // todo Fleetio FuelType for Gas
						request.setDate(t.getDateTime());
						request.setVendor_id(vendorId != null ? vendorId.intValue() : null); //set vendor id

						// call connector and send Fuel Tx to Fleetio
						fleetioConnector.sendToFleetio(vin, request);
						logger.info("done sendToFleetio  for vin: " + vin);

						lastSuccess = Timestamp.valueOf(t.getDateTime());
						successFulList.add(t);

					} catch (Exception exx) {
						logger.info("Error while calling Fleetio API, Exception log message and proceed : [ "
								+ exx.getMessage() + " ]");
						exx.printStackTrace();
						throw exx;
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
	
	
	private static double covertToTwoDecimal(double volume) {
		  BigDecimal bd=new BigDecimal(volume).setScale(2,RoundingMode.HALF_DOWN);
	      return bd.doubleValue();
	}
		

}