package com.cmclinnovations.jps.agent.file_management.mods.algorithms;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.cmclinnovations.jps.agent.file_management.mods.parameters.DetailS;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlType(propOrder = { "detailS" })
@XmlAccessorType(XmlAccessType.FIELD)
public class Algorithm {
	@JsonProperty("name")
	@XmlAttribute
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@JsonProperty("displayName")
	@XmlAttribute(name = "display_name")
	private String displayName;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	@JsonProperty("details")
	@XmlElement(name = "details")
	private DetailS detailS;

	public DetailS getDetailS() {
		return detailS;
	}

	public void setDetailS(DetailS detailS) {
		this.detailS = detailS;
	}
}
