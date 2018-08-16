package org.petctviewer.scintigraphy.esophageus.resultats;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ZProjector;

public class Modele_Resultats_EsophagealTransit {

	private XYSeries [][] datasetMain;
	private XYSeries [][] datasetTransitTime;
	
	private ArrayList<Object[]> dicomRoi;
	
	
	private ImagePlus[] condense;
	private ImagePlus[] imageplusAndRoi;


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
		
		
		condense = new ImagePlus[arrayList.size()];
		imageplusAndRoi = new ImagePlus[arrayList.size()];

	}
	
	public XYSeries[][] getDataSetMain() {
		return this.datasetMain;
	}
	
	public XYSeries[][] getDataSetTransitTime(){
		return this.datasetTransitTime;
	}
	
	public double[] retentionPoucentage() {	
		double[] res = new double[datasetTransitTime.length];
		//for each acqui
		for(int i =0 ; i < datasetTransitTime.length; i++) {
			XYSeries serie = datasetTransitTime[i][0];
			
			double ymax = serie.getMaxY();
			double x = ModeleScinDyn.getAbsMaxY(serie);
			double ycalc = ModeleScinDyn.getInterpolatedY(serie, x+10);
			double fractionDecrease = (ycalc/ymax)*100;
			
			ymax = ModeleScin.round(ymax, 2);
			x = ModeleScin.round(x, 2);
			ycalc = ModeleScin.round(ycalc, 2);
			fractionDecrease = ModeleScin.round(fractionDecrease, 2);
			
			System.out.println("i:"+i+" ymax: "+ ymax+" x:"+x+"  interpol: "+ycalc+" %:"+fractionDecrease);
			res[i] = fractionDecrease;
		}
		return res;
	}
	
	public XYSeriesCollection retentionForGraph() {
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

	
	
	/*Condensé*/

	
	public ImagePlus getCondense(int indiceAcquisition) {
		return condense[indiceAcquisition];		
	}
	
	public ImagePlus getImagePlusAndRoi(int indiceAcquisition) {
		return imageplusAndRoi[indiceAcquisition];
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
	
	
	
	
	
	
	
	public void calculImagePlusAndRoi(int indiceAcquisition) {
		ImagePlus impProjete = DynamicScintigraphy.projeter((ImagePlus)dicomRoi.get(indiceAcquisition)[0], 0, ((ImagePlus)dicomRoi.get(indiceAcquisition)[0]).getStackSize(), "max");
		Scintigraphy.setCustomLut(impProjete);
		Rectangle rectRoi = (Rectangle)dicomRoi.get(indiceAcquisition)[1];
		rectRoi.setSize((int)((Rectangle)dicomRoi.get(indiceAcquisition)[1]).getWidth(), ((ImagePlus)dicomRoi.get(indiceAcquisition)[0]).getHeight());
		impProjete.setRoi(rectRoi);
		imageplusAndRoi[indiceAcquisition] = impProjete.crop();
	}
	
	public void calculCond(int indiceAcquisition) {
		//Scintigraphy.setCustomLut((ImagePlus)dicomRoi.get(indiceAcquisition)[0]);
		ImagePlus condese= buildCondense(
				 (ImagePlus)dicomRoi.get(indiceAcquisition)[0],// la dicom imp
				 (Rectangle)dicomRoi.get(indiceAcquisition)[1]);
		condense[indiceAcquisition]=condese;
	}
	
	
	public void calculAllImagePlusAndRoi() {
		for(int i =0; i<imageplusAndRoi.length; i++) {
			ImagePlus impProjete = DynamicScintigraphy.projeter((ImagePlus)dicomRoi.get(i)[0], 0, ((ImagePlus)dicomRoi.get(i)[0]).getStackSize(), "max");
			Scintigraphy.setCustomLut(impProjete);
			Rectangle rectRoi = (Rectangle)dicomRoi.get(i)[1];
			rectRoi.setSize((int)((Rectangle)dicomRoi.get(i)[1]).getWidth(), ((ImagePlus)dicomRoi.get(i)[0]).getHeight());
			impProjete.setRoi(rectRoi);
			imageplusAndRoi[i] = impProjete.crop();
		}
	
	}

	public void calculAllCondense() {
		for(int i=0; i<condense.length; i++) {
			condense[i]=buildCondense(
				 (ImagePlus)dicomRoi.get(i)[0],// la dicom imp
				 (Rectangle)dicomRoi.get(i)[1]);
		}
		
	}
	
	
	private ImagePlus buildCondense(ImagePlus imp, Rectangle roi) {
		
		int coupes = imp.getStack().getSize();
		Dimension dimCondense = new Dimension((int)roi.getWidth()*coupes, imp.getHeight());

		ImagePlus imageCondensee = IJ.createImage("Image", "16-bit black", dimCondense.width, imp.getHeight() , coupes);
		// imp.hide();
		for (int i = 0; i < coupes; i++) {
			imp.setSlice(i + 1);
			Rectangle imageShift = new Rectangle();
			imageShift.setBounds(
					(int) Math.round(roi.getX()), 
					0,
					(int) Math.round(imp.getWidth() - roi.getX()), 
					(int) imp.getHeight() );
		
			imp.setRoi(imageShift);
			// On copie cette zone
			imp.copy();
			// on cree une nouvelle imagePlus de la taille finale
			ImagePlus image = IJ.createImage("Image", "16-bit black", dimCondense.width, imp.getHeight(), 1);
			// On met un nouveau rectangle qu'on shift de 9 pixel et on colle dans cette
			// image
			Rectangle recDestination = new Rectangle();
			recDestination.setBounds(i * (int)roi.getWidth(), imageShift.y, imageShift.width, imp.getHeight());
			// recDestination.setLocation(i*9, 0);
			image.setRoi(recDestination);
			// image.show();
			image.paste();
			image.killRoi();
			// On l'ajoute a l'image condensee
			imageCondensee.getStack().setProcessor(image.getProcessor(), i + 1);

		}
		// On fait la somme du stack pour avoir l'image finale
		ZProjector projector = new ZProjector();
		projector.setImage(imageCondensee);
		projector.setMethod(ij.plugin.ZProjector.SUM_METHOD);
		projector.setStartSlice(1);
		projector.setStopSlice(coupes);
		projector.doProjection();
		ImagePlus projete = projector.getProjection();
		Scintigraphy.setCustomLut(projete);
	   
		return projete;
		
		
	}

	/*Calcul longueur oesophage*/
	public double[] calculLongeurEsophage() {
		double [] res= new double[datasetTransitTime.length];
		//for each acqui
		for(int i =0; i< datasetTransitTime.length; i++) {
			double hauteurRoi = ((Rectangle)dicomRoi.get(i)[1]).getHeight();
			Calibration calibration = ((ImagePlus)dicomRoi.get(i)[0]).getLocalCalibration();
			calibration.setUnit("mm");// on met l'unité en mm
			double hauteurPixel = calibration.pixelHeight;
			res[i] = (hauteurPixel*hauteurRoi)/10;// pour l'avoir en centimetres
		}
		return res;
				
	}
	
}
