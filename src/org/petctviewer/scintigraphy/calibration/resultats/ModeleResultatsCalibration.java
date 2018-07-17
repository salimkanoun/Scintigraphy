package org.petctviewer.scintigraphy.calibration.resultats;

import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ij.IJ;
import ij.ImagePlus;
import ij.process.StackStatistics;

public class ModeleResultatsCalibration {

	private  Doublet[][] dataInitial=null;
	private  Doublet[][] dataCurrent=null;
	
	private Double a = null;
	private Double b = null;
	
	private ArrayList<ArrayList<HashMap<String, Object>>> donneesCharge;
	
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
			 this.dataCurrent[x][y].setA( (Double)this.dataInitial[x][y].getA() );
			 this.dataCurrent[x][y].setB( (Double)this.dataInitial[x][y].getB() );
		 }else {
			 this.dataCurrent[x][y]=new Doublet(Double.NaN, Double.NaN);
		 }
		 //graph.getXYPlot().setDataset(buildColletionFromDoublet(dataCurrent));
	 }

	 public XYSeriesCollection buildCollection() {
		 Double maxX = 0.0D;
			XYSeriesCollection collection = new XYSeriesCollection();
		  	for(int i=0;i<this.dataCurrent.length;i++) {
		  		XYSeries serie = new XYSeries("Acqui "+(i+1));
		  		for(int j=0; j<this.dataCurrent[i].length;j++) {
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
	  * @param data
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
		 
  		XYSeries serikke = new XYSeries("ff ");
	  	for(int i=0;i<m.length;i++) {
	  		for(int j=0; j<m[i].length;j++) {
				if( !m[i][j].getA().equals(Double.NaN) && !m[i][j].getB().equals(Double.NaN) ) {
					serikke.add(m[i][j].getA(), m[i][j].getB());
				}
	  		}
  		}
	  	
		 //feat
		 double[] resultRegression = Regression.getOLSRegression(new XYSeriesCollection(serikke),0);
		 System.out.println("a : "+resultRegression[0]);
		 System.out.println("b : "+resultRegression[1]);
		 this.a = resultRegression[1];
		 this.b = resultRegression[0] - resultRegression[1];
		 
		 
		return new Doublet(resultRegression[0],resultRegression[1]);
	 }
	 
	 //tracage du feat
	 private XYSeries featSeries(Double a, Double b, int max) {
		 //tracage du feat
		 XYSeries feat = new XYSeries("feat");
		 for(double i=0.1D; i< max ;i+=0.1D) {//commencé a 0.2D ? to xmax +3
				 feat.add(i,(a/i)+b);
			 //feat.add(i,((0.7D/i)+0.4D) );
			 //feat.add(i,resultRegression[1]*i+resultRegression[0]);
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
		
		
		for(int i =0; i<this.donneesCharge.size() ;i++) {
			for(int j=0; j<this.donneesCharge.get(i).size() ;j++) {

				//titre de la ligne
				Double suvMax = (Double)this.donneesCharge.get(i).get(j).get("SUVmax");
				
				
				Double suv70 = (Double)this.donneesCharge.get(i).get(j).get("MEAN70");
				Double bg = (Double)this.donneesCharge.get(i).get(j).get("BG");
				
				//System.out.println("i:"+i+"j;"+j+" bg ="+bg);
				Double TS = this.a 
						* suv70 
						+this.b
						* bg;
			//	System.out.println("a : "+this.a + " b : "+this.b+ "BG : "+bg+" TS ;"+TS);
				ImagePlus im = ((ImagePlus)this.donneesCharge.get(i).get(j).get("image")).duplicate();
				StackStatistics ss = new StackStatistics(im);

			//	System.out.println("pixel count :"+ss.pixelCount);
				
				IJ.run(im	,"Macro...", "code=[if(v<"+TS+") v=NaN] stack");
				//im.show();
				//IJ.showMessage("afiche");
				ss = new StackStatistics(im);
				
				//mesuré
				Double volumeCalculated = ss.pixelCount * (Double)this.donneesCharge.get(i).get(j).get("VolumeVoxel");
			//	System.out.println(" pixel count :"+ss.pixelCount);
			//	System.out.println(" volume voxel :"+(Double)this.donneesCharge.get(i).get(j).get("VolumeVoxel"));
				
				
				this.donneesCharge.get(i).get(j).put("VolumeCalculated", volumeCalculated);
			}
		}
	}
	
	public ArrayList<ArrayList<HashMap<String, Object>>> getDataDetails(){
		return this.donneesCharge;
	}


}
