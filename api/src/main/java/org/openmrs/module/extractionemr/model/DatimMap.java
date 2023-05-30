package org.openmrs.module.extractionemr.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Please note that a corresponding table schema must be created in liquibase.xml.
 */
//Uncomment 2 lines below if you want to make the Item class persistable, see also NigeriaemrDaoTest and liquibase.xml
@Entity(name = "extractionemr.DatimMap")
@Table(name = "nigeria_datimcode_mapping")
public class DatimMap {
	
	@Id
	@Column(name = "ndr_id")
	private Integer ndrId;
	
	@Column(name = "datim_code")
	private String datimCode;
	
	@Column(name = "lga_code")
	private Integer lgaCode;
	
	@Column(name = "lga_name")
	private String lgaName;
	
	@Column(name = "state_code")
	private Integer stateCode;
	
	@Column(name = "state_name")
	private String stateName;
	
	public Integer getNdrId() {
		return ndrId;
	}
	
	public void setNdrId(Integer ndrId) {
		this.ndrId = ndrId;
	}
	
	public String getDatimCode() {
		return datimCode;
	}
	
	public void setDatimCode(String datimCode) {
		this.datimCode = datimCode;
	}
	
	public Integer getLgaCode() {
		return lgaCode;
	}
	
	public void setLgaCode(Integer lgaCode) {
		this.lgaCode = lgaCode;
	}
	
	public String getLgaName() {
		return lgaName;
	}
	
	public void setLgaName(String lgaName) {
		this.lgaName = lgaName;
	}
	
	public Integer getStateCode() {
		return stateCode;
	}
	
	public void setStateCode(Integer stateCode) {
		this.stateCode = stateCode;
	}
	
	public String getStateName() {
		return stateName;
	}
	
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}
}
