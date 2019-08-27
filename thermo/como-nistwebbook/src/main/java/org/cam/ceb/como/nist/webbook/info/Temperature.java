package org.cam.ceb.como.nist.webbook.info;
/**
 * A model created to encode different temperatures of species.   
 * 
 * @author msff2
 *
 */
public class Temperature {
	private String value = "";
	private String units = "";
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}
}
