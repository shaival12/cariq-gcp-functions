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

import com.geotab.model.coordinate.Coordinate;
import com.geotab.model.entity.fuel.FuelTransactionProductType;

public class SQLRunner {

	private static final Logger logger = Logger.getLogger(SQLRunner.class.getName());
	
    private static final String url = "jdbc:postgresql://10.21.1.3:5432/cariq_prod";
    private static final String user = "shaival";
    private static final String password = "4QQEKJn4uRtwtzD2";
    
	public static void main(String[] args) {
		
		System.out.println(SQLRunner.findMaxBatchLogForFuelTxJob());
	
    }
	
	
	/**
	 * insert new row in batch log table
	 * @param jobName
	 * @param lastInsertedDT
	 * @param status
	 * @param jobId
	 * @return
	 */
	public static int insertNewBatchLog(String jobName, Timestamp lastInsertedDT, String status, UUID jobId, String log ) {

		int result = 0;
		try (Connection con = DriverManager.getConnection(url, user, password);
				Statement st = con.createStatement();
				PreparedStatement preparedStatement = con.prepareStatement(INSERT_BATCH_JOB_SQL_PENDING_STATUS)) {
			preparedStatement.setString(1, jobName);
			preparedStatement.setTimestamp(2,
					lastInsertedDT != null ? lastInsertedDT : new Timestamp(System.currentTimeMillis()));
			preparedStatement.setString(3, status);
			preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			preparedStatement.setString(5, log );
			preparedStatement.setObject(6, jobId, java.sql.Types.OTHER);

			System.out.println(preparedStatement);
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
	 *  update status in BatchLog 
	 *  
	 **/
	public static int updateBatchLog(String status, UUID jobId) {

		int result = 0;
		try (Connection con = DriverManager.getConnection(url, user, password);
				Statement st = con.createStatement();
				PreparedStatement preparedStatement = con.prepareStatement(UPDATE_BATCH_JOB_SQL_STATUS)) {

			preparedStatement.setString(1, status);
			preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			preparedStatement.setObject(3, jobId, java.sql.Types.OTHER);

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
	 * find max insertDate from batch_job_log table
	 * @param date
	 * @return
	 */
	public static Timestamp findMaxBatchLogForFuelTxJob() {

		try (Connection con = DriverManager.getConnection(url, user, password);
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
	public static List<Transaction>  fetchFuelTransactionResults(Timestamp timeStamp) {
		
		List<Transaction> list = new ArrayList<Transaction>();
		
		try (Connection con = DriverManager.getConnection(url, user, password);
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(SQL_FETCH_FUEL_TX)) {

        	
            while ( rs.next() ) {
            	
	            try {	
	               String driverName = rs.getString("driverName");
	               String siteName = rs.getString("siteName");
	               String vin = rs.getString("vin");
	               BigDecimal cost = new BigDecimal(rs.getString("cost"));
	               
	               //odometer
	               Integer odstr = rs.getInt("odometer");
	               System.out.println("odstr1 :" + odstr);
	               DecimalFormat f = new DecimalFormat("##0.00");
	             
	               double odometer = milesTokm(new Double(odstr));
	               System.out.println("odometer KM :" + odometer);
	               
	               //location
	               Coordinate location = new Coordinate();
	               location.setX(rs.getDouble("long"));
	       		   location.setY(rs.getDouble("lat"));
	             
	              // System.out.println("loc :" + location.getX() + "---" + location.getY());
	               
	               String currency = rs.getString("currency");
	               Timestamp time = rs.getTimestamp("inserted_at");
	               LocalDateTime dateTime = time.toLocalDateTime();
	               
	               //volume
	               Double volume = rs.getDouble("volume");
	               System.out.println("volume :" + volume); //gallon to liiter
	               volume =  gallonToLiter(volume);
	               System.out.println("volume in lt :" + volume);
	               
	               //description
	               String description = rs.getString("description");
	               String providerProductDesc = "unknown";
	               FuelTransactionProductType productType = FuelTransactionProductType.REGULAR; //todo hardcoded
	               
				 
				Transaction transaction = new Transaction(description,  driverName,  providerProductDesc,  
						 siteName,  vin,
	           			 cost, currency.toUpperCase()
	           			 ,dateTime,  location,  odometer,
	        			 productType,  volume);
	               
				   list.add(transaction);
				   
	               
	               //System.out.println( "transaction = " + transaction.toString() );
	            }catch(Exception e) {
	            	e.printStackTrace();
	            	logger.warning("Error while processing resultset");
	            }
            
            }
            System.out.println( "list = " +  list.size());
            
            rs.close();
            st.close();
            con.close();

        } catch (SQLException ex) {
        	logger.log(Level.SEVERE, "Error in fetchFuelTransactionResults : [ " + System.currentTimeMillis() + " ]" + ex.getMessage());
            ex.printStackTrace();
        }
		
		return list;
	}
	
	 private static double milesTokm(double distanceInMiles) {
	        return distanceInMiles * 1.609344;
	    }
	 
	 private static double gallonToLiter(double volumeInGallon) {
		return  volumeInGallon * 3.7854118;
	 }
	 
	private static final String SQL_FETCH_FUEL_TX = "select u.full_name as driverName,  s.name as siteName, v.vin, "
			+ " vs.odometer as odometer, "
			+ " ST_X(ST_AsText(tx.location)) as long, ST_Y(ST_AsText(tx.location)) as lat , "
			+ " tx.currency, tx.total_amount as cost,  tx.inserted_at, "
			+ " titems.quantity as volume, titems.description as description "
			+ " from transactions tx, vehicles v, users u, vehicle_states vs, stations s, transaction_items titems\n"
			+ " where tx.vehicle_id = v.id \n"
			+ "  and v.id = vs.vehicle_id\n"
			+ "  and tx.user_id = u.id\n"
			+ "  and tx.station_id = s.id\n"
			+ "  and titems.transaction_id = tx.id\n"
			+ "  and tx.status = 'finished'\n"
			+ "  and v.vin ='4S4BSANC8F3327518' order by tx.inserted_at DESC;";

	private static final String SQL_MAX_BATCH_JOB = "select Max(last_inserted_at) as last_inserted_at from batch_job_log where "
			+ " job_name = 'FUEL_TX_GEOTAB_JOB' and "
			+ " status = 'success';";

	
	 private static final String INSERT_BATCH_JOB_SQL_PENDING_STATUS = "INSERT INTO batch_job_log " +
		        "  (job_name, last_inserted_at, status, inserted_at, log, job_id) VALUES " +
		        " (?, ?, ?, ?, ?, ?) ;";
	 
	 private static final String UPDATE_BATCH_JOB_SQL_STATUS = "UPDATE  batch_job_log SET " +
		        "  status = ?, updated_at = ? WHERE job_id = ? ;" ;
}
