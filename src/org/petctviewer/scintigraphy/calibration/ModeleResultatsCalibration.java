package org.petctviewer.scintigraphy.calibration;

import java.awt.Font;
import java.text.DecimalFormat;

import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ModeleResultatsCalibration {

	private  Doublet[][] dataInitial=null;
	private  Doublet[][] dataCurrent=null;
	
	public ModeleResultatsCalibration(Doublet[][] data) {

		//chargement des data
		this.dataInitial = new Doublet[data.length][data[0].length];
	    Doublet[][] d = new Doublet[data.length][data[0].length];
		for(int i =0; i<data.length; i++) {
			for(int j =0; j<data[i].length; j++) {
				this.dataInitial[i][j] = new Doublet(data[i][j].getA(),data[i][j].getB());
			}
		}
	  	this.dataCurrent = data.clone();
	  	
	  	
	  	
	
	}
	

	//actualise les data depuis la tableau de checkbox
	 public void actualiserDatasetFromCheckbox(int a, int b, boolean visiblity) {
		 if(visiblity) {
			 this.dataCurrent[a][b].setA( (Double)this.dataInitial[a][b].getA() );
			 this.dataCurrent[a][b].setB( (Double)this.dataInitial[a][b].getB() );
		 }else {
			 this.dataCurrent[a][b]=new Doublet(Double.NaN, Double.NaN);
		 }
		 this.graph.getXYPlot().setDataset(buildColletionFromDoublet(dataCurrent));
	 }
	
	 
	 private XYSeriesCollection buildColletionFromDoublet(Doublet[][] data) {
			XYSeriesCollection collection = new XYSeriesCollection();
		  	for(int i=0;i<data.length;i++) {
		  		XYSeries serie = new XYSeries("Acqui "+(i+1));
		  		for(int j=0; j<data[i].length;j++) {
		  			serie.add(data[i][j].getA(), data[i][j].getB());	
		  		}
		  		collection.addSeries(serie);	
		  	}
		  	collection.addSeries(this.feat(data));
		  	return collection;
	 }
	 
	 private XYSeries feat (Doublet[][] data) {
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

		 //tracage du feat
		 XYSeries feat = new XYSeries("feat");
		 for(double i=0.1D; i< serikke.getItemCount();i+=0.1D) {
				 feat.add(i,((resultRegression[0]/i)+resultRegression[1]) );
			 //feat.add(i,((0.7D/i)+0.4D) );
			 //feat.add(i,resultRegression[1]*i+resultRegression[0]);
		 }
		 
		 setCoef(resultRegression[0], resultRegression[1]); 
		 return feat;
	 }

	
}
