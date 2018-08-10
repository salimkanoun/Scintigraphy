package org.petctviewer.scintigraphy.esophageus.resultats;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.esophageus.Condense_Dynamique;
import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;

import ij.ImagePlus;

public class Modele_Resultats_EsophagealTransit {

	private XYSeries [][] datasetMain;
	private XYSeries [][] datasetTransitTime;
	
	private ArrayList<Object[]> dicomRoi;

	public Modele_Resultats_EsophagealTransit(ArrayList<HashMap<String, ArrayList<Double>>> arrayList, ArrayList<Object[]> dicomRoi) {
			
		// x examen et 4 coubres
		datasetMain = new XYSeries[arrayList.size()][4];
		
		// pour chaque acquisition
		for(int i =0; i < arrayList.size(); i++) {
			datasetMain[i][0] = this.listToXYSeries(arrayList.get(i).get("entier"), arrayList.get(i).get("temps"), "Full "+(i+1));
			datasetMain[i][1] = this.listToXYSeries(arrayList.get(i).get("unTier"), arrayList.get(i).get("temps"), "Upper "+(i+1));
			datasetMain[i][2] = this.listToXYSeries(arrayList.get(i).get("deuxTier"), arrayList.get(i).get("temps"), "Middle "+(i+1));
			datasetMain[i][3] = this.listToXYSeries(arrayList.get(i).get("troisTier"), arrayList.get(i).get("temps"), "Lower "+(i+1));
		}
		
		// x examen et 4 coubres
		datasetTransitTime = new XYSeries[arrayList.size()][1];
		
		// pour chaque acquisition
		for(int i =0; i < arrayList.size(); i++) {
			datasetTransitTime[i][0] = this.listToXYSeries(arrayList.get(i).get("entier"), arrayList.get(i).get("temps"), "Full "+(i+1));
		}
		
		this.dicomRoi=dicomRoi;
	}
	
	public XYSeries[][] getDataSetMain() {
		return this.datasetMain;
	}
	
	public XYSeries[][] getDataSetTransitTime(){
		return this.datasetTransitTime;
	}
	
	public double[] retention() {	
		double[] res = new double[datasetTransitTime.length];
		for(int i =0 ; i < datasetTransitTime.length; i++) {
			XYSeries serie = datasetMain[i][0];
			
			double ymax = serie.getMaxY();
			double x = ModeleScinDyn.getAbsMaxY(serie);
			double ycalc = ModeleScinDyn.getInterpolatedY(serie, x+10);
			double fractionDecrease = 1-((ymax - ycalc)/100);
			
			ymax = ModeleScin.round(ymax, 2);
			x = ModeleScin.round(x, 2);
			ycalc = ModeleScin.round(ycalc, 2);
			fractionDecrease = ModeleScin.round(fractionDecrease, 2);
			
			//System.out.println("i:"+i+" ymax: "+ ymax+" x:"+x+"  interpol: "+ycalc+" %:"+fractionDecrease);
			res[i] = fractionDecrease;
		}
		return res;
	}
	
	public XYSeriesCollection retention2() {
		XYSeriesCollection collection = new XYSeriesCollection();
		
		//for each acqui
		for(int i = 0 ; i< datasetTransitTime.length; i++) {
			XYSeries serie = new XYSeries("acqui "+(i+1));
			double ymax = datasetTransitTime[i][0].getMaxY();

			//for each point
			for(int j = 0; j< datasetTransitTime[i][0].getItemCount();j++) {
				double et = (double)datasetTransitTime[i][0].getY(j);
				
//				double esoCount =1- ((ymax - et)/100) ;
				double esoCount =   et / ymax;
				
				double x = (double)datasetTransitTime[i][0].getX(j);
				
				serie.add(x, esoCount);
			}
			collection.addSeries(serie);
		}
		
		return collection;
	}
	
	private void printList(List<Double> list, String name) {
		System.out.println(name);
		for(int i =0; i< list.size(); i++) {
			System.out.println(list.get(i));
		}
	}
	
	private XYSeries listToXYSeries(List<Double> data, List<Double> time, String title) {
		//System.out.println("data size: "+data.size());
		//System.out.println("temps size: "+time.size());

		if(data.size() != time.size()) {
			System.err.println("erreur : nombre de data !=  du nombre de temps");
		}
		
		XYSeries serie = new XYSeries(title);
		for(int i = 0; i< time.size(); i++) {
			serie.add( time.get(i),data.get(i));
		}
		return serie;
	}

	public void rognerDicomCondenseRight(int pixelDroite, int indiceAcquisition) {
		Rectangle ancienRectangle = (Rectangle)dicomRoi.get(indiceAcquisition)[1];
		Rectangle nouveauRectangle =  (Rectangle)dicomRoi.get(indiceAcquisition)[1];
		nouveauRectangle.setBounds((int)ancienRectangle.getX(), 
									(int)ancienRectangle.getY(), 
									(int)ancienRectangle.getWidth()-pixelDroite, 
									(int)ancienRectangle.getHeight());	
		dicomRoi.get(indiceAcquisition)[1] = nouveauRectangle;
	}
	
	public void rognerDicomCondenseLeft(int pixelGauche, int indiceAcquisition) {
		Rectangle ancienRectangle = (Rectangle)dicomRoi.get(indiceAcquisition)[1];
		Rectangle nouveauRectangle =  (Rectangle)dicomRoi.get(indiceAcquisition)[1];
		nouveauRectangle.setBounds((int)ancienRectangle.getX()+pixelGauche, 
									(int)ancienRectangle.getY(), 
									(int)ancienRectangle.getWidth()-pixelGauche, 
									(int)ancienRectangle.getHeight());
		dicomRoi.get(indiceAcquisition)[1] = nouveauRectangle;

	}
	
	public DynamicImage calculCondense(int indiceAcquisition) {
		 System.out.println(  "w"+		 ((Rectangle)dicomRoi.get(indiceAcquisition)[1]).getWidth()   );
		Condense_Dynamique cond = new Condense_Dynamique();
		
		
		
		Scintigraphy.setCustomLut((ImagePlus)dicomRoi.get(indiceAcquisition)[0]);
		 return new DynamicImage((cond.condense3(
				 (ImagePlus)dicomRoi.get(indiceAcquisition)[0],// la dicom imp
				 (Rectangle)dicomRoi.get(indiceAcquisition)[1])).getBufferedImage());// la roi
		
	}
	
	public DynamicImage getImagePlusAndRoi(int indiceAcquisition) {
		ImagePlus m = DynamicScintigraphy.projeter((ImagePlus)dicomRoi.get(indiceAcquisition)[0], 0, ((ImagePlus)dicomRoi.get(indiceAcquisition)[0]).getStackSize(), "max");
		Scintigraphy.setCustomLut(m);
		m.setRoi((Rectangle)dicomRoi.get(indiceAcquisition)[1]);
		ImagePlus res = m.crop();
		
		return new DynamicImage(res.getBufferedImage());
	}
}
