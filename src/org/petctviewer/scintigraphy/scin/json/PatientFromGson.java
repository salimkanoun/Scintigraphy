package org.petctviewer.scintigraphy.scin.json;

import java.util.Date;

/**
 * This class represent an Patient, as saved in a Json file.<br/>
 * Contains :<br/>
 * &emsp;&emsp; - Name<br/>
 * &emsp;&emsp; - ID<br/>
 * &emsp;&emsp; - Date<br/>
 * &emsp;&emsp; - AccessionNumber
 *
 */
public class PatientFromGson {

	private String Name;

	private String ID;

	private Date Date;

	private String AccessionNumber;
	
	private String ControllerName;

	public String getName() {
		return this.Name;
	}

	public String getID() {
		return this.ID;
	}

	public Date getDate() {
		return this.Date;
	}

	public String getAccessionNumber() {
		return this.AccessionNumber;
	}
	
	public String getControllerName() {
		return this.ControllerName;
	}

	@Override
	public String toString() {
		return "Bonjour, mon nom est  " + this.Name + ", j'ai pour ID " + this.ID + " car j'ai été admis le "
				+ this.Date + " ce qui donne comme accessionNumber : " + this.AccessionNumber+". Mon examen est un "+this.ControllerName;
	}
}