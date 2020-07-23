package com.cmclinnovations.jps.agent.file_management.mods.algorithms;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlAccessorType(XmlAccessType.FIELD)
public class AlgorithmS {
	@JsonProperty("algorithm")
	@XmlElement(name = "algorithm")
	private ArrayList<Algorithm> algorithmList;

	public ArrayList<Algorithm> getAlgorithm() {
		return algorithmList;
	}

	public void setAlgorithm(ArrayList<Algorithm> algorithmList) {
		this.algorithmList = algorithmList;
	}
}
