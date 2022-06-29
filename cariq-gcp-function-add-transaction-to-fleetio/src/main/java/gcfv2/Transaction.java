package gcfv2;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.geotab.model.coordinate.Coordinate;
import com.geotab.model.entity.fuel.FuelTransactionProductType;

public class Transaction {
	
	private String description;
	private String driverName;
	private String providerProductDesc;
	private String siteName;
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
	
	
	
	
	
	public Transaction(String description, String driverName, String providerProductDesc, String siteName, String vin,
			BigDecimal cost, String currencyCode, LocalDateTime dateTime, Coordinate location, Double odometer,
			FuelTransactionProductType productType, Double volume, String fleetId, Double unitPrice) {
		super();
		this.description = description;
		this.driverName = driverName;
		this.providerProductDesc = providerProductDesc;
		this.siteName = siteName;
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
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
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
	@Override
	public String toString() {
		return "Transaction [description=" + description + ", driverName=" + driverName + ", providerProductDesc="
				+ providerProductDesc + ", siteName=" + siteName + ", vin=" + vin + ", cost=" + cost + ", currencyCode="
				+ currencyCode + ", dateTime=" + dateTime + ", location=" + location + ", odometer=" + odometer
				+ ", productType=" + productType + ", volume=" + volume + ", fleetId=" + fleetId + ", unitPrice="
				+ unitPrice + "]";
	}
	



}
