package org.petctviewer.scintigraphy.gallbladder.resultats;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.gallbladder.application.Gallbladder;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import ij.ImagePlus;

public class Model_Resultats_Gallblader extends ModelScin{
    
    private final XYSeries[][] datasetMain;
	private final XYSeries[][] datasetTransitTime;

	// 0: imageplus, 1: la roi
	private final ArrayList<Object[]> dicomRoi;

	private final ImagePlus[] condense;
	private final ImagePlus[] imageplusAndRoi;

	// pour le csv
	private final ArrayList<HashMap<String, ArrayList<Double>>> arrayList;
	private double[] longueurVessicule;
	private final double[] tempsMesureTransitTime;
	private final double[] retentionDecrease;

    public final Gallbladder gallPlugIn;
    
    public Model_Resultats_Gallblader(ArrayList<HashMap<String, ArrayList<Double>>> arrayList,
    ArrayList<Object[]> dicomRoi, String studyName, Gallbladder gallPlugIn, ImageSelection[] selectedImages){
        super(selectedImages, studyName);

        //CSV
        longueurVessicule = new double[arrayList.size()];
        tempsMesureTransitTime = new double[arrayList.size()];
        retentionDecrease = new double[arrayList.size()];

        this.arrayList = arrayList;

        //x examen et 4 courbes
        datasetMain = new XYSeries[arrayList.size()][4];

        //pour chaque acquisition
        for(int i = 0; i < arrayList.size(); i++){
            datasetMain[i][0] = this.listToXYSeries(arrayList.get(i).get("entier"), arrayList.get(i).get("temps"),
            "Full " + (i +1 ));
            datasetMain[i][1] = this.listToXYSeries(arrayList.get(i).get("unTier"), arrayList.get(i).get("temps"),
            "Upper " + (i + 1));
            datasetMain[i][2] = this.listToXYSeries(arrayList.get(i).get("deuxTier"), arrayList.get(i).get("temps"),
            "Middle " + (i + 1));
    datasetMain[i][3] = this.listToXYSeries(arrayList.get(i).get("troisTier"), arrayList.get(i).get("temps"),
            "Lower " + (i + 1));
        }

        //x examen et 4 courbes
        datasetTransitTime = new XYSeries[arrayList.size()][1];

        //pour chaque acquisition
        for (int i = 0; i < arrayList.size(); i++) {
			datasetTransitTime[i][0] = this.listToXYSeries(arrayList.get(i).get("entier"),
					arrayList.get(i).get("temps"), "Full " + (i + 1));
        }
        
        this.dicomRoi = dicomRoi;

        condense = new ImagePlus[arrayList.size()];
        imageplusAndRoi = new ImagePlus[arrayList.size()];
        this.gallPlugIn = gallPlugIn;
    }

    public XYSeries[][] getDataSetMain(){
        return this.datasetMain;
    }

    public XYSeries[][] getDataSetTransitTime() {
		return this.datasetTransitTime;
    }
    
    public double[] retentionAllPoucentage() {
		double[] res = new double[datasetTransitTime.length];
		// for each acqui
		for (int i = 0; i < datasetTransitTime.length; i++) {
			XYSeries serie = datasetTransitTime[i][0];

			double ymax = serie.getMaxY();
			double x = Library_JFreeChart.getAbsMaxY(serie);
			@SuppressWarnings("deprecation")
			double ycalc = Library_JFreeChart.getInterpolatedY(serie, x + 10);
			double fractionDecrease = (ycalc / ymax) * 100;

			ymax = Library_Quantif.round(ymax, 2);
			x = Library_Quantif.round(x, 2);
			ycalc = Library_Quantif.round(ycalc, 2);
			fractionDecrease = Library_Quantif.round(fractionDecrease, 2);

			res[i] = fractionDecrease;
		}
		return res;
    }
    
    public double[] retentionAllX() {
		double[] res = new double[datasetTransitTime.length];
		// for each acqui
		for (int i = 0; i < datasetTransitTime.length; i++) {
			XYSeries serie = datasetTransitTime[i][0];

			double x = Library_JFreeChart.getAbsMaxY(serie);
			res[i] = x;
		}
		return res;
    }
    
    public double retentionPoucentage(double xForYMax, int numeroSerie) {

		if (xForYMax < 1) {
			return Double.NaN;
		}

		XYSeries serie = datasetTransitTime[numeroSerie][0];

		// recherche du x le plus proche
		@SuppressWarnings("deprecation")
		double ymax = Library_JFreeChart.getInterpolatedY(serie, xForYMax);

		@SuppressWarnings("deprecation")
		double ycalc = Library_JFreeChart.getInterpolatedY(serie, xForYMax + 10);
		double fractionDecrease = (ycalc / ymax) * 100;

		xForYMax = Library_Quantif.round(xForYMax, 2);
		ycalc = Library_Quantif.round(ycalc, 2);
		fractionDecrease = Library_Quantif.round(fractionDecrease, 2);

		return fractionDecrease;
    }
    
    public XYSeriesCollection retentionForGraph() {
		XYSeriesCollection collection = new XYSeriesCollection();

		// for each acqui
		for (int i = 0; i < datasetTransitTime.length; i++) {
			XYSeries serie = new XYSeries("acqui " + (i + 1));
			double ymax = datasetTransitTime[i][0].getMaxY();

			// for each point
			for (int j = 0; j < datasetTransitTime[i][0].getItemCount(); j++) {
				double et = (double) datasetTransitTime[i][0].getY(j);

				double gallCount = et / ymax;

				double x = (double) datasetTransitTime[i][0].getX(j);

				serie.add(x, gallCount);
			}
			collection.addSeries(serie);
		}

		return collection;
    }
    
    private XYSeries listToXYSeries(List<Double> data, List<Double> time, String title) {

		if (data.size() != time.size()) {
			System.err.println("erreur : nombre de data !=  du nombre de temps");
		}

		XYSeries serie = new XYSeries(title);
		for (int i = 0; i < time.size(); i++) {
			serie.add(time.get(i), data.get(i));
		}
		return serie;
    }
    
    /* Condensé */

	public ImagePlus getCondense(int indiceAcquisition) {

		return condense[indiceAcquisition];
	}

	public ImagePlus getImagePlusAndRoi(int indiceAcquisition) {
		return imageplusAndRoi[indiceAcquisition];
    }
    
    public void rognerDicomCondenseRight(int pixelDroite, int indiceAcquisition) {
		Rectangle ancienRectangle = (Rectangle) dicomRoi.get(indiceAcquisition)[1];
		Rectangle nouveauRectangle = (Rectangle) dicomRoi.get(indiceAcquisition)[1];
		nouveauRectangle.setBounds((int) ancienRectangle.getX(), (int) ancienRectangle.getY(),
				(int) ancienRectangle.getWidth() - pixelDroite, (int) ancienRectangle.getHeight());
		dicomRoi.get(indiceAcquisition)[1] = nouveauRectangle;
	}

	public void rognerDicomCondenseLeft(int pixelGauche, int indiceAcquisition) {
		Rectangle ancienRectangle = (Rectangle) dicomRoi.get(indiceAcquisition)[1];
		Rectangle nouveauRectangle = (Rectangle) dicomRoi.get(indiceAcquisition)[1];
		nouveauRectangle.setBounds((int) ancienRectangle.getX() + pixelGauche, (int) ancienRectangle.getY(),
				(int) ancienRectangle.getWidth() - pixelGauche, (int) ancienRectangle.getHeight());
		dicomRoi.get(indiceAcquisition)[1] = nouveauRectangle;
    }
    
    public void calculImagePlusAndRoi(int indiceAcquisition) {
		@SuppressWarnings("deprecation")
		ImagePlus impProjete = Library_Dicom.projeter((ImagePlus) dicomRoi.get(indiceAcquisition)[0], 0,
				((ImagePlus) dicomRoi.get(indiceAcquisition)[0]).getStackSize(), "max");
		Library_Gui.setCustomLut(impProjete);
		Rectangle rectRoi = (Rectangle) dicomRoi.get(indiceAcquisition)[1];
		rectRoi.setSize((int) ((Rectangle) dicomRoi.get(indiceAcquisition)[1]).getWidth(),
				((ImagePlus) dicomRoi.get(indiceAcquisition)[0]).getHeight());
		impProjete.setRoi(rectRoi);
		imageplusAndRoi[indiceAcquisition] = impProjete.crop();
    }
    
    public void calculCond(int indiceAcquisition) {
		ImagePlus condese = buildCondense((ImagePlus) dicomRoi.get(indiceAcquisition)[0], // la dicom imp
				(Rectangle) dicomRoi.get(indiceAcquisition)[1]);
		condense[indiceAcquisition] = condese;
    }
    
    public void calculAllImagePlusAndRoi() {
		for (int i = 0; i < imageplusAndRoi.length; i++) {
			@SuppressWarnings("deprecation")
			ImagePlus impProjete = Library_Dicom.projeter((ImagePlus) dicomRoi.get(i)[0], 0,
					((ImagePlus) dicomRoi.get(i)[0]).getStackSize(), "max");
			Library_Gui.setCustomLut(impProjete);
			Rectangle rectRoi = (Rectangle) dicomRoi.get(i)[1];
			rectRoi.setSize((int) ((Rectangle) dicomRoi.get(i)[1]).getWidth(),
					((ImagePlus) dicomRoi.get(i)[0]).getHeight());
			impProjete.setRoi(rectRoi);
			imageplusAndRoi[i] = impProjete.crop();
		}

    }
    
    public void calculAllCondense() {
		for (int i = 0; i < condense.length; i++) {
			condense[i] = buildCondense((ImagePlus) dicomRoi.get(i)[0], // la dicom imp
					(Rectangle) dicomRoi.get(i)[1]);
		}

	}
}   