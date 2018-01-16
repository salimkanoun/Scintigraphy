/*
Copyright (C) 2017 KANOUN Salim
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.petctviewer.scintigraphy.platelet;

import java.util.Date;
import java.util.HashMap;

public class MesureImage {
	
	Date mesureTime;
	private double[] spleen=new double[2];
	private double[] liver=new double[2];
	private double[] heart=new double[2];
	private double[] spleenAnt=new double[2];
	private double[] liverAnt=new double[2];
	private double[] heartAnt=new double[2];
	private Boolean antPost=false;
	private HashMap<String, Double> resultats = new HashMap<String,Double>();
	
	public MesureImage(Date dateAcquisition) {
		this.mesureTime=dateAcquisition;
	}
	
	public void setSpleenValue(double[] spleen) {
		this.spleen=spleen;
	}
	
	public void setLiverValue(double[] liver) {
		this.liver=liver;
	}
	
	public void setHeartValue(double[] heart) {
		this.heart=heart;
	}
	
	public void setSpleenAntValue(double[] spleenAnt) {
		antPost=true;
		this.spleenAnt=spleenAnt;
	}
	
	public void setliverAntValue(double[] liverAnt) {
		this.liverAnt=liverAnt;
	}
	
	public void setHeartAntValue(double[] heartAnt) {
		this.heartAnt=heartAnt;
	}
	
	public double[] getSpleenValue() {
		return spleen;
	}
	
	public double[] getLiverValue() {
		return liver;
	}
	
	public double[] getHeartValue() {
		return heart;
	}
	
	public double[] getSpleenAntValue() {
		return spleenAnt;
	}
	
	public double[] getLiverAntValue() {
		return liverAnt;
	}
	
	public double[] getHeartAntValue() {
		return heartAnt;
	}
	
	public boolean isAntPost(){
		return antPost;
	}
	
	public Date getMesureTime() {
		return mesureTime;
	}
	
	/**
	 * Mets tous les resultats dans la hashmap
	 */
	public HashMap<String, Double> calculateandGetResults(){
		
		//resultats.put("Number of Voxel Spleen Post", spleen[0]);
		//resultats.put("Number of Voxel Liver Post", liver[0]);
		//resultats.put("Number of Voxel Heart Post", heart[0]);
		resultats.put("Mean Uptake Spleen Post", spleen[1]);
		resultats.put("Mean Uptake Liver Post", liver[1]);
		resultats.put("Mean Uptake Heart Post", heart[1]);
		
		resultats.put("Total Count Spleen Post", spleen[0]);
		resultats.put("Total Count Liver Post", liver[0]);
		resultats.put("Total Count Heart Post", heart[0]);
		resultats.put("Mean Ratio Spleen / Heart Post", spleen[1]/heart[1]);
		resultats.put("Mean Ratio Spleen / Liver Post", spleen[1]/liver[1]);
		resultats.put("Mean Ratio Liver / Heart Post", liver[1]/heart[1]);
		//resultats.put("Total Count Ratio Spleen / Heart Post", ();
		//resultats.put("Total Count Ratio Spleen / Liver Post", ();
		//resultats.put("Total Count Ratio Liver / Heart Post", ();
		
		if (antPost){
			//resultats.put("Number of Voxel Spleen Ant", spleenAnt[0]);
			//resultats.put("Number of Voxel Liver Ant", liverAnt[0]);
			//resultats.put("Number of Voxel Heart Ant", heartAnt[0]);
			resultats.put("Mean Uptake Spleen Ant", spleenAnt[1]);
			resultats.put("Mean Uptake Liver Ant", liverAnt[1]);
			resultats.put("Mean Uptake Heart Ant", heartAnt[1]);
			
			resultats.put("Total Count Spleen Ant", spleenAnt[0]);
			resultats.put("Total Count Liver Ant", liverAnt[0]);
			resultats.put("Total Count Heart Ant", heartAnt[0]);
			resultats.put("Mean Ratio Spleen / Heart Ant", spleenAnt[1]/heartAnt[1]);
			resultats.put("Mean Ratio Spleen / Liver Ant", spleenAnt[1]/liverAnt[1]);
			resultats.put("Mean Ratio Liver / Heart Ant", liverAnt[1]/heartAnt[1]);
			//resultats.put("Total Count Ratio Spleen / Heart Ant", ();
			//resultats.put("Total Count Ratio Spleen / Liver Ant", ();
			//resultats.put("Total Count Ratio Liver / Heart Ant", ();
			
			double spleenMG = Math.sqrt(resultats.get("Total Count Spleen Ant")*resultats.get("Total Count Spleen Post"));
			double liverMG=Math.sqrt(resultats.get("Total Count Liver Ant")*resultats.get("Total Count Liver Post"));
			double heartMG=Math.sqrt(resultats.get("Total Count Heart Ant")*resultats.get("Total Count Heart Post"));
			
			resultats.put("Ratio GM Spleen / Heart", (spleenMG/heartMG));
			resultats.put("Ratio GM Spleen / Liver", (spleenMG/liverMG));
			resultats.put("Ratio GM Liver / Heart", (liverMG/heartMG));
			
	
		}
		
		return resultats;
	}
	
	

}
