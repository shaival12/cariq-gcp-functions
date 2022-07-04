package gcfv2;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.geotab.model.coordinate.Coordinate;
import com.geotab.model.entity.fuel.FuelTransactionProductType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SQLRunner {

	private static final Logger logger = Logger.getLogger(SQLRunner.class.getName());

	private static final Object DB_NAME = Constants.DB_NAME;
	private static final String DB_USER = Constants.DB_USER;
	private static final String DB_PASS = Constants.DB_PASS;
	private static final String INSTANCE_CONNECTION_NAME = Constants.INSTANCE_CONNECTION_NAME; // prod

	/**
	 * get connection with Postgres DB
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {

		HikariConfig config = new HikariConfig();

		// Configure which instance and what database user to connect with.
		config.setJdbcUrl(String.format("jdbc:postgresql:///%s", "cariq_prod"));
		config.setUsername(DB_USER);  
		config.setPassword(DB_PASS); 
		config.setMaximumPoolSize(3);
		config.setMinimumIdle(1);
		config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
		config.addDataSourceProperty("cloudSqlInstance", INSTANCE_CONNECTION_NAME);

		// The ipTypes argument can be used to specify a comma delimited list of
		// preferred IP types
		// for connecting to a Cloud SQL instance. The argument ipTypes=PRIVATE will
		// force the
		// SocketFactory to connect with an instance's associated private IP.
		config.addDataSourceProperty("ipTypes", "PRIVATE");

		// ... Specify additional connection properties here.
		// ...

		// Initialize the connection pool using the configuration object.
		DataSource pool = new HikariDataSource(config);

		return pool.getConnection();
	}

	/**
	 * insert new row in batch log table
	 * 
	 * @param jobName
	 * @param lastInsertedDT
	 * @param status
	 * @param jobId
	 * @return
	 */
	public int insertNewBatchLog(String jobName, Timestamp lastInsertedDT, String status, UUID jobId, String log) {

		int result = 0;
		try (Connection con = getConnection();
				Statement st = con.createStatement();
				PreparedStatement preparedStatement = con.prepareStatement(INSERT_BATCH_JOB_SQL_PENDING_STATUS)) {
			preparedStatement.setString(1, jobName);
			preparedStatement.setTimestamp(2,
					lastInsertedDT != null ? lastInsertedDT : new Timestamp(System.currentTimeMillis()));
			preparedStatement.setString(3, status);
			preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			preparedStatement.setString(5, log);
			preparedStatement.setObject(6, jobId, java.sql.Types.OTHER);
			
			// Step 3: Execute the query or update query
			result = preparedStatement.executeUpdate();
			logger.info("insert result : [ " + result + " ]");

			preparedStatement.close();
			st.close();
			con.close();

		} catch (SQLException ex) {
			ex.printStackTrace();
			logger.log(Level.SEVERE, "Error in insertNewBatchLog : " + ex.getMessage());
		}

		return result;

	}

	/**
	 * update status in BatchLog
	 * 
	 **/
	public int updateBatchLog(String status, UUID jobId, Timestamp lastInsertedDT) {

		int result = 0;
		try (Connection con = getConnection();
				Statement st = con.createStatement();
				PreparedStatement preparedStatement = con.prepareStatement(UPDATE_BATCH_JOB_SQL_STATUS)) {

			preparedStatement.setString(1, status);
			preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			preparedStatement.setTimestamp(3, lastInsertedDT);
			preparedStatement.setObject(4, jobId, java.sql.Types.OTHER);

			logger.info(preparedStatement.toString());
			result = preparedStatement.executeUpdate();
			logger.info("insert result : [ " + result + " ]");

			preparedStatement.close();
			st.close();
			con.close();

		} catch (SQLException ex) {
			ex.printStackTrace();
			logger.log(Level.SEVERE, "Error in insertNewBatchLog : " + ex.getMessage());
		}

		return result;

	}

	/**
	 * find max insertDate from batch_job_log table by Hikari
	 * 
	 * @param date
	 * @return
	 */
	public Timestamp findMaxBatchLogForFuelTxJob() {

		try (Connection con = getConnection();
				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery(SQL_MAX_BATCH_JOB)) {

			while (rs.next()) {

				try {
					return rs.getTimestamp("last_inserted_at");

				} catch (Exception e) {
					e.printStackTrace();
					logger.log(Level.SEVERE,
							"Error in findMaxBatchLogForFuelTxJob while processing resultset : " + e.getMessage());
				}

			}

			rs.close();
			st.close();
			con.close();

		} catch (SQLException ex) {
			ex.printStackTrace();
			logger.log(Level.SEVERE,
					"Error in findMaxBatchLogForFuelTxJob : [ " + System.currentTimeMillis() + " ]" + ex.getMessage());
		}
		return null;
	}

	/**
	 * 
	 * @param url
	 * @param user
	 * @param password
	 * @return
	 */
	public List<Transaction> fetchFuelTransactionResults(Timestamp timeStamp, List<String> fleetIds) {

		List<Transaction> list = new ArrayList<Transaction>();

		String sql = SQL_FETCH_FUEL_TX;
		if (fleetIds.size() > 0) {
			String sqlIN = fleetIds.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",", "(", ")"));
			sql = sql.replace("(?)", sqlIN);
		}
		//logger.info("SQL" + sql);

		try (Connection con = getConnection();
				Statement st = con.createStatement();
				PreparedStatement preparedStatement = con.prepareStatement(sql)) {

			preparedStatement.setTimestamp(1, timeStamp);
			ResultSet rs = preparedStatement.executeQuery();

			while (rs != null && rs.next()) {

				try {
					String driverName = rs.getString("driverName");
					String siteName = rs.getString("siteName");
					String vin = rs.getString("vin");
					BigDecimal cost = new BigDecimal(rs.getString("cost"));

					// odometer
					Integer odstr = rs.getInt("odometer");
					double odometer = metersToKM(new Double(odstr));
					
					// location
					Coordinate location = new Coordinate();
					location.setX(rs.getDouble("long"));
					location.setY(rs.getDouble("lat"));

					String currency = rs.getString("currency");
					Timestamp time = rs.getTimestamp("time");
					LocalDateTime dateTime = time.toLocalDateTime();

					// volume
					Double volume = rs.getDouble("volume");
					volume = gallonToLiter(volume);
				
					// description
					String description = rs.getString("description");
					String providerProductDesc = "unknown";
					FuelTransactionProductType productType = FuelTransactionProductType.REGULAR; // todo hardcoded

					String fleetId = rs.getString("fleetid");

					Transaction transaction = new Transaction(description, driverName, providerProductDesc, siteName,
							vin, cost, currency.toUpperCase(), dateTime, location, odometer, productType, volume,
							fleetId);

					list.add(transaction);
					
				} catch (Exception e) {
					e.printStackTrace();
					logger.warning("Error while processing resultset");
				}

			}
			logger.info("list = " + list.size());

			rs.close();
			st.close();
			con.close();

		} catch (SQLException ex) {
			logger.log(Level.SEVERE,
					"Error in fetchFuelTransactionResults : [ " + System.currentTimeMillis() + " ]" + ex.getMessage());
			ex.printStackTrace();
		}

		return list;
	}

	private static double metersToKM(double distanceInMeters) {
		return distanceInMeters * 0.001;
	}

	private static double milesTokm(double distanceInMiles) {
		return distanceInMiles * 1.609344;
	}

	private static double gallonToLiter(double volumeInGallon) {
		return volumeInGallon * 3.7854118;
	}

	// + " and v.vin ='4S4BSANC8F3327518' "
	private static final String SQL_FETCH_FUEL_TX = "select u.full_name as driverName,  s.name as siteName, v.vin, "
			+ " vs.odometer as odometer, "
			+ " ST_X(ST_AsText(tx.location)) as long, ST_Y(ST_AsText(tx.location)) as lat , "
			+ " tx.currency, tx.total_amount as cost,  tx.time, "
			+ " titems.quantity as volume, titems.description as description, v.fleet_id as fleetid"
			+ " from transactions tx, vehicles v, users u, vehicle_states vs, stations s, transaction_items titems\n"
			+ " where tx.vehicle_id = v.id \n" + "  and v.id = vs.vehicle_id\n" + "  and tx.user_id = u.id\n"
			+ "  and tx.station_id = s.id\n" + "  and titems.transaction_id = tx.id\n"
			+ "  and tx.status = 'finished'\n" + "  and v.fleet_id in (?) " + "  and tx.time > ? "
			+ "order by tx.time ASC;";

	private static final String SQL_MAX_BATCH_JOB = "select Max(last_inserted_at) as last_inserted_at from batch_job_log where "
			+ " job_name = 'FUEL_TX_GEOTAB_JOB' and " + " status = 'success';";

	private static final String INSERT_BATCH_JOB_SQL_PENDING_STATUS = "INSERT INTO batch_job_log "
			+ "  (job_name, last_inserted_at, status, inserted_at, log, job_id) VALUES " + " (?, ?, ?, ?, ?, ?) ;";

	private static final String UPDATE_BATCH_JOB_SQL_STATUS = "UPDATE  batch_job_log SET "
			+ "  status = ?, updated_at = ?, last_inserted_at = ? WHERE job_id = ? ;";

}
