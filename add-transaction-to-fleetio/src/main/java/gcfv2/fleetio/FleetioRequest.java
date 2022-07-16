package gcfv2.fleetio;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * { "vehicle_id": 1958594, "date": "2022-06-22T17:12:34.077-07:00",
 * "fuel_type_id": 309829, "liters": 5, "partial": true, "personal": false,
 * "price_per_volume_unit": 6, "us_gallons": 3, "meter_entry_attributes": {
 * "value": 1111 } }
 */
// to send FuelTransaction to Fleetio
public class FleetioRequest {

	Integer vehicle_id;
	LocalDateTime date;
	Integer fuel_type_id;
	double liters;
	Boolean partial;
	Boolean personal;
	double price_per_volume_unit;
	double us_gallons;
	Meter_entry_attributes meter_entry_attributes;
	String stationName;
	Integer vendor_id;

	public Integer getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(Integer vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public Integer getFuel_type_id() {
		return fuel_type_id;
	}

	public void setFuel_type_id(Integer fuel_type_id) {
		this.fuel_type_id = fuel_type_id;
	}

	public double getLiters() {
		return liters;
	}

	public void setLiters(double liters) {
		this.liters = liters;
	}

	public Boolean isPartial() {
		return partial;
	}

	public void setPartial(Boolean partial) {
		this.partial = partial;
	}

	public Boolean isPersonal() {
		return personal;
	}

	public void setPersonal(Boolean personal) {
		this.personal = personal;
	}

	public double getPrice_per_volume_unit() {
		return price_per_volume_unit;
	}

	public void setPrice_per_volume_unit(double price_per_volume_unit) {
		this.price_per_volume_unit = price_per_volume_unit;
	}

	public double getUs_gallons() {
		return us_gallons;
	}

	public void setUs_gallons(double us_gallons) {
		this.us_gallons = us_gallons;
	}

	@JsonProperty("meter_entry_attributes")
	public Meter_entry_attributes getMeter_entry_attributes() {
		return meter_entry_attributes;
	}

	public void setMeter_entry_attributes(Meter_entry_attributes meter_entry_attributes) {
		this.meter_entry_attributes = meter_entry_attributes;
	}
	
	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	
	public Integer getVendor_id() {
		return vendor_id;
	}

	public void setVendor_id(Integer vendor_id) {
		this.vendor_id = vendor_id;
	}

	@Override
	public String toString() {
		return "FleetioRequest [vehicle_id=" + vehicle_id + ", date=" + date + ", fuel_type_id=" + fuel_type_id
				+ ", liters=" + liters + ", partial=" + partial + ", personal=" + personal + ", price_per_volume_unit="
				+ price_per_volume_unit + ", us_gallons=" + us_gallons + ", meter_entry_attributes="
				+ meter_entry_attributes + ", stationName=" + stationName + ", vendor_id=" + vendor_id + "]";
	}


	
}
