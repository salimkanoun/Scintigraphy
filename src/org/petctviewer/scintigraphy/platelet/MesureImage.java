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
	
	final Date mesureTime;
	private double[] spleen=new double[2];
	private double[] liver=new double[2];
	private double[] heart=new double[2];
	private double[] spleenAnt=new double[2];
	private double[] liverAnt=new double[2];
	private double[] heartAnt=new double[2];
	private Boolean antPost=false;
	private double delayFromStart=0;
	private final HashMap<String, Double> resultats = new HashMap<>();
	
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
		this.antPost=true;
		this.spleenAnt=spleenAnt;
	}
	
	public void setliverAntValue(double[] liverAnt) {
		this.liverAnt=liverAnt;
	}
	
	public void setHeartAntValue(double[] heartAnt) {
		this.heartAnt=heartAnt;
	}
	
	public double[] getSpleenValue() {
		return this.spleen;
	}
	
	public double[] getLiverValue() {
		return this.liver;
	}
	
	public double[] getHeartValue() {
		return this.heart;
	}
	
	public double[] getSpleenAntValue() {
		return this.spleenAnt;
	}
	
	public double[] getLiverAntValue() {
		return this.liverAnt;
	}
	
	public double[] getHeartAntValue() {
		return this.heartAnt;
	}
	
	public boolean isAntPost(){
		return this.antPost;
	}
	
	public Date getMesureTime() {
		return this.mesureTime;
	}
	
	public void setDelayFromStart(double delayHour) {
		this.delayFromStart=delayHour;
	}
	
	public double getDelayFromStart() {
		return this.delayFromStart;
	}
	
	/**
	 * Mets tous les resultats dans la hashmap
	 */
	public HashMap<String, Double> calculateandGetResults(){
		
		this.resultats.put("Mean Ratio Spleen / Heart Post", this.spleen[1]/this.heart[1]);
		this.resultats.put("Mean Ratio Spleen / Liver Post", this.spleen[1]/this.liver[1]);
		this.resultats.put("Mean Ratio Liver / Heart Post", this.liver[1]/this.heart[1]);
		
		double delaySeconds = this.delayFromStart*3600;
		double indiumLambda=(Math.log(2)/(2.8*24*3600));
		double decayedFraction=Math.pow(Math.E, (indiumLambda*delaySeconds*(-1)));
		double correctedCount=this.spleen[0]/(decayedFraction);
		this.resultats.put("Corrected SpleenPosterior", correctedCount);
		
		if (this.antPost){
			double spleenMG = Math.sqrt(this.spleenAnt[0]*this.spleen[0]);
			double liverMG=Math.sqrt(this.liverAnt[0]*this.liver[0]);
			double heartMG=Math.sqrt(this.heartAnt[0]*this.heart[0]);
			
			this.resultats.put("Ratio GM Spleen / Heart", (spleenMG/heartMG));
			this.resultats.put("Ratio GM Spleen / Liver", (spleenMG/liverMG));
			this.resultats.put("Ratio GM Liver / Heart", (liverMG/heartMG));
		}
		
		return this.resultats;
	}
	
	

}
