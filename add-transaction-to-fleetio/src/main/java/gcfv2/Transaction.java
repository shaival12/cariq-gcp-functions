package gcfv2;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.geotab.model.coordinate.Coordinate;
import com.geotab.model.entity.fuel.FuelTransactionProductType;

public class Transaction {

	private String description;
	private String driverName;
	private String providerProductDesc;
	private String stationName;
	private String stationBrand;
	private String stationNumber;
	private String stationLine1;
	private String stationZip;
	private String stationCity;
	private String stationState;
	private String stationCountry;
	private String vin;
	private BigDecimal cost;
	private String currencyCode;
	private LocalDateTime dateTime;
	private Coordinate location;
	private Double odometer;
	private FuelTransactionProductType productType;
	private Double volume;
	private String fleetId;
	private Double unitPrice;

	public Transaction(String description, String driverName, String providerProductDesc, 
			String stationName,
			String stationBrand,
			String stationNumber,
			String stationLine1,
			String stationZip,
			String stationCity,
			String stationState,
			String stationCountry,
			String vin, BigDecimal cost, String currencyCode, LocalDateTime dateTime, Coordinate location,
			Double odometer, FuelTransactionProductType productType, Double volume, String fleetId, Double unitPrice) {
		super();
		this.description = description;
		this.driverName = driverName;
		this.providerProductDesc = providerProductDesc;
		
		this.stationName = stationName;
		this.stationBrand = stationBrand;
		this.stationNumber = stationNumber;
		this.stationLine1 = stationLine1;
		this.stationZip = stationZip;
		this.stationCity = stationCity;
		this.stationState = stationState;
		this.stationCountry = stationCountry;
		this.vin = vin;
		this.cost = cost;
		this.currencyCode = currencyCode;
		this.dateTime = dateTime;
		this.location = location;
		this.odometer = odometer;
		this.productType = productType;
		this.volume = volume;
		this.fleetId = fleetId;
		this.unitPrice = unitPrice;
	}

	public Transaction() {
		super();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getProviderProductDesc() {
		return providerProductDesc;
	}

	public void setProviderProductDesc(String providerProductDesc) {
		this.providerProductDesc = providerProductDesc;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Coordinate getLocation() {
		return location;
	}

	public void setLocation(Coordinate location) {
		this.location = location;
	}

	public Double getOdometer() {
		return odometer;
	}

	public void setOdometer(Double odometer) {
		this.odometer = odometer;
	}

	public FuelTransactionProductType getProductType() {
		return productType;
	}

	public void setProductType(FuelTransactionProductType productType) {
		this.productType = productType;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public String getFleetId() {
		return fleetId;
	}

	public void setFleetId(String fleetId) {
		this.fleetId = fleetId;
	}

	public Double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public String getStationNumber() {
		return stationNumber;
	}

	public void setStationNumber(String stationNumber) {
		this.stationNumber = stationNumber;
	}

	public String getStationLine1() {
		return stationLine1;
	}

	public void setStationLine1(String stationLine1) {
		this.stationLine1 = stationLine1;
	}

	public String getStationZip() {
		return stationZip;
	}

	public void setStationZip(String stationZip) {
		this.stationZip = stationZip;
	}

	public String getStationCity() {
		return stationCity;
	}

	public void setStationCity(String stationCity) {
		this.stationCity = stationCity;
	}

	public String getStationState() {
		return stationState;
	}

	public void setStationState(String stationState) {
		this.stationState = stationState;
	}

	public String getStationCountry() {
		return stationCountry;
	}

	public void setStationCountry(String stationCountry) {
		this.stationCountry = stationCountry;
	}

	public String getStationBrand() {
		return stationBrand;
	}

	public void setStationBrand(String stationBrand) {
		this.stationBrand = stationBrand;
	}
	

}
