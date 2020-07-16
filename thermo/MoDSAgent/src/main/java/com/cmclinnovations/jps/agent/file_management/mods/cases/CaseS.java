package com.cmclinnovations.jps.agent.file_management.mods.cases;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class CaseS {
	@XmlElement(name = "case")
	private ArrayList<Case> caseList;

	public ArrayList<Case> getCase() {
		return caseList;
	}

	public void setCase(ArrayList<Case> caseList) {
		this.caseList = caseList;
	}

}
