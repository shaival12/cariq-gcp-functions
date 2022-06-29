package gcfv2.fleetio;

public class FleetioResponse {
	
	Long id;
	String vin;
	
	public FleetioResponse(Long id, String vin) {
		super();
		this.id = id;
		this.vin = vin;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	
	

}
