package gcfv2.fleetio;

public class Vendor {

	Long id;
	String name;
	
	public Vendor() {
		super();
	}

	public Vendor(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
