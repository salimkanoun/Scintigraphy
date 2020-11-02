package org.petctviewer.scintigraphy.calibration.resultats;

import ij.IJ;
import ij.ImagePlus;
import ij.process.StackStatistics;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import java.util.ArrayList;
import java.util.HashMap;

public class ModeleResultatsCalibration {

	private final Doublet[][] dataInitial;
	private final Doublet[][] dataCurrent;
	
	private Double a = null;
	private Double b = null;
	
	private final ArrayList<ArrayList<HashMap<String, Object>>> donneesCharge;
	
	private ArrayList<Double[][]> listTableauFinal;
	
	public ModeleResultatsCalibration(ArrayList<ArrayList<HashMap<String, Object>>> arrayList) {
		this.donneesCharge = arrayList;
		
		//chargement des data
		this.dataInitial = new Doublet[arrayList.size()][arrayList.get(0).size()];		
		for(int i =0; i<arrayList.size(); i++) {
			for(int j =0; j<arrayList.get(i).size(); j++) {
				this.dataInitial[i][j] = new Doublet((Double)arrayList.get(i).get(j).get("x"), (Double)arrayList.get(i).get(j).get("y"));
			}
		}
		
		this.dataCurrent = new Doublet[arrayList.size()][arrayList.get(0).size()];		
		for(int i =0; i<arrayList.size(); i++) {
			for(int j =0; j<arrayList.get(i).size(); j++) {
				this.dataCurrent[i][j] = new Doublet((Double)arrayList.get(i).get(j).get("x"), (Double)arrayList.get(i).get(j).get("y"));
			}
		}
	}
	
	//actualise les data depuis la tableau de checkbox
	 public void actualiserDatasetFromCheckbox(int x, int y, boolean visiblity) {
		 if(visiblity) {
			 this.dataCurrent[x][y].setA(this.dataInitial[x][y].getA());
			 this.dataCurrent[x][y].setB(this.dataInitial[x][y].getB());
		 }else {
			 this.dataCurrent[x][y]=new Doublet(Double.NaN, Double.NaN);
		 }
	 }

	 public XYSeriesCollection buildCollection() {
		 Double maxX = 0.0D;
		XYSeriesCollection collection = new XYSeriesCollection();
		//each acqui
	  	for(int i=0;i<this.dataCurrent.length;i++) {
	  		XYSeries serie = new XYSeries("Acqui "+(i+1));
	  		//each roi
	  		for(int j=0; j<this.dataCurrent[i].length;j++) {
	  			//on met x et y
	  			serie.add(this.dataCurrent[i][j].getA(), this.dataCurrent[i][j].getB());
			  	//avoir le x le plus grand
	  			if(this.dataCurrent[i][j].getA()>=maxX) {
	  				maxX = this.dataCurrent[i][j].getA();
	  			}
	  		}
	  		collection.addSeries(serie);	
	  	}
	  	
	  	Doublet fit = fitCalcul(this.dataCurrent);
	  	collection.addSeries(featSeries(fit.getA(), fit.getB(), maxX.intValue()));
	  	return collection;
	 }
	 
	 /**
	  * @return un doublet avec a et b, les paramètres recherches
	  */
	 private Doublet fitCalcul (Doublet[][] data) {
		 //calcul de a+b*x
		 Doublet[][] m = new Doublet[data.length][data[0].length];
		 for(int i =0; i<data.length; i++) {
			 for(int j =0; j<data[i].length; j++) {
					m[i][j] = new Doublet(data[i][j].getA(),data[i][j].getB()*data[i][j].getA());
			 }
		 }
		 
		 //passage dans un xyseries
  		XYSeries serikke = new XYSeries("ff ");
		 for (Doublet[] doublets : m) {
			 for (int j = 0; j < doublets.length; j++) {
				 if (!doublets[j].getA().equals(Double.NaN) && !doublets[j].getB().equals(Double.NaN)) {
					 serikke.add(doublets[j].getA(), doublets[j].getB());
				 }
			 }
		 }
		 //feat calcul
	  	 double[] resultRegression = new double[2];
	  	try {
			  resultRegression = Regression.getOLSRegression(new XYSeriesCollection(serikke),0);
	
			 this.a = resultRegression[1];
			 this.b = resultRegression[0] - resultRegression[1];
		} catch (IllegalArgumentException e) {
			resultRegression[0] = Double.NaN;
			resultRegression[1] = Double.NaN;
			this.a = Double.NaN;
			this.b = Double.NaN;
		}
		 
		 
		
		 
		return new Doublet(resultRegression[0],resultRegression[1]);
	 }
	 
	 //tracage du feat
	 private XYSeries featSeries(Double a, Double b, int max) {
		 //tracage du feat
		 XYSeries feat = new XYSeries("feat");
		 for(double i=0.1D; i< max ;i+=0.1D) {
				 feat.add(i,(a/i)+b);
		 }
		 return feat;

	 }

	public Double geta() {
		return this.a;
	}
	
	public Double getb() {
		return this.b;
	}
	
	public void runCalculDetails() {
		for (ArrayList<HashMap<String, Object>> hashMaps : this.donneesCharge) {
			for (int j = 0; j < hashMaps.size(); j++) {
				//Double suvMax = (Double)this.donneesCharge.get(i).get(j).get("SUVmax");
				Double suv70 = (Double) hashMaps.get(j).get("MEAN70");
				Double bg = (Double) hashMaps.get(j).get("BG");

				double TS = this.a * suv70 + this.b * bg;

				ImagePlus im = ((ImagePlus) hashMaps.get(j).get("image")).duplicate();
				StackStatistics ss = new StackStatistics(im);

				IJ.run(im, "Macro...", "code=[if(v<" + TS + ") v=NaN] stack");
//im.show();
				ss = new StackStatistics(im);
				//mesuré
				Double volumeCalculated = ss.pixelCount * (Double) hashMaps.get(j).get("VolumeVoxel");

				hashMaps.get(j).put("VolumeCalculated", volumeCalculated);
			}
		}
	}
	
	public ArrayList<Double[][]> getDataDetails(){
		listTableauFinal = new ArrayList<>();
				
	    //each roi
		for(int i = 0 ;i< this.donneesCharge.get(0).size(); i++) {
			//tableau final qui sera affiche
			Double[][] tableauFinal = new Double[this.donneesCharge.size()][6];
			
			//each exam
			for(int j = 0; j < this.donneesCharge.size(); j++) {
				//numero d'acquisition
				tableauFinal[j][0] = (double)(j+1);
				tableauFinal[j][1] = Library_Quantif.round(((Double)this.donneesCharge.get(j).get(i).get("SUVmax")),2);
				tableauFinal[j][2] = Library_Quantif.round((Double)this.donneesCharge.get(j).get(i).get("TrueSphereVolume")/1000,2);
				tableauFinal[j][3] = Library_Quantif.round((Double)this.donneesCharge.get(j).get(i).get("VolumeCalculated"),2);
				//difference en ml
				tableauFinal[j][4] = Library_Quantif.round((Double)this.donneesCharge.get(j).get(i).get("VolumeCalculated") - ((Double)this.donneesCharge.get(j).get(i).get("TrueSphereVolume")/1000),2);
				//difference en pourcentage
				tableauFinal[j][5] = Library_Quantif.round( (
								tableauFinal[j][4]		 / 
								((Double)this.donneesCharge.get(j).get(i).get("TrueSphereVolume")/1000) ) *100,2);
			}
			listTableauFinal.add(tableauFinal);
		}
		return listTableauFinal;
	}
	
	public ArrayList<Double> getMoyenneDifferenceDetails() {
		ArrayList<Double> listMoyenneDifferencePourcentage = new ArrayList<>();
	   //each roi
		for (Double[][] doubles : this.listTableauFinal) {
			//pour la moyenne des difference de pourcentage
			ArrayList<Double> listDifferencePourcentage = new ArrayList<>();
			//each variable
			for (int j = 0; j < doubles.length; j++) {
				listDifferencePourcentage.add(Math.abs(doubles[j][5]));
			}
			listMoyenneDifferencePourcentage
					.add(Library_Quantif.round(mean(listDifferencePourcentage.toArray(new Double[0])), 2));
		}
		return listMoyenneDifferencePourcentage;
	}

	public static double mean(Double[] m) {
	    double sum = 0.0D;
		for (Double aDouble : m) {
			sum += aDouble;
		}
	    return sum / m.length;
	}

}
