/**
Copyright (C) 2017 PING Xie and KANOUN Salim
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

package org.petctviewer.scintigraphy.gastric;

import java.awt.BasicStroke;
import java.awt.Color;
import ij.Prefs;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;
import ij.process.ImageStatistics;
import ij.util.DicomTools;

public class Modele_VG_Roi {
	public static Font italic = new Font("Arial", Font.ITALIC, 8);
	private HashMap<String, Integer> coups;//pour enregistrer les coups dans chaque organe sur chaque image
	private HashMap<String, Integer> mgs;//pour enregistrer le MG dans chaque organe pour chaque serie
	protected static double[] temps;//pour enregistrer l'horaire où on recupere  chaque serie
	protected static double[] estomacPourcent;//pour enregistrer le pourcentage de l'estomac(par rapport a total) pour chaque serie
	protected static double[] fundusPourcent;//pour enregistrer le pourcentage du fundus(par rapport a total) pour chaque serie
	protected static double[] antrePourcent;//pour enregistrer le pourcentage de l'antre(par rapport a total) pour chaque serie
	protected static double[] funDevEsto;//pour enregistrer le rapport fundus/estomac pour chaque serie
	protected static double[] estoInter;//pour enregistrer le rapport fundus/estomac pour chaque serie
	protected static double[] tempsInter;//pour enregistrerla derivee de la courbe de variation de l’estomac
	protected static boolean logOn;//signifie si log est ouvert
	protected static double[] intestinPourcent;//pour enregistrer le pourcentage de l'intestin(par rapport a total) pour chaque serie

	private String[] organes = { "Estomac", "Intestin", "Fundus", "Antre" };

	private String patient;
	private String date;
	private boolean trouve;//signifie si la valeur qu'on veut est trouvee sur la courbe

	public Modele_VG_Roi() {
		this.coups = new HashMap<>();
		this.mgs = new HashMap<>();
		Prefs.useNamesAsLabels = true;
		Modele_VG_Roi.logOn = false;
	}

	public enum Etat {
		ESTOMAC_ANT, INTESTIN_ANT, ESTOMAC_POS, INTESTIN_POS, CIR_ESTOMAC_ANT, CIR_INTESTIN_ANT, CIR_ESTOMAC_POS, CIR_INTESTIN_POS, FIN, RESULTAT;
		private static Etat[] vals = values();

		public Etat next() {
			return vals[(this.ordinal() + 1) % vals.length];
		}

		public Etat previous() {
			// On ajoute un vals.length car le modulo peut être < 0 en java
			return vals[((this.ordinal() - 1) + vals.length) % vals.length];
		}
	}

	// On recupère le nom du patient, la date et son id pour les resultats
	public void setPatient(String pat, ImagePlus imp) {
		this.patient = pat;
		this.date = DicomTools.getTag(imp, "0008,0020");
		if (date!=null && !date.isEmpty()) date = date.trim();
	}

	// Cree un montage a partir de l'ImageStack
	// En ajoutant les infos DICOM de l'image
	public ImagePlus montage(ImageStack stack, String nomProgramme) {
		MontageMaker mm = new MontageMaker();
		ImagePlus imp = new ImagePlus("Resultats Vidange Gastrique -" + this.patient, stack);
		imp = mm.makeMontage2(imp, 2, 2, 0.5, 1, 4, 1, 10, false);
		imp.setTitle("Resultats " + nomProgramme + " -" + this.patient);
		return imp;
	}

	// calcule le coups du ROI
	public void calculerCoups(String roi, int indexImage, ImagePlus imp) {
		// Ancienne methode mathis nombre de pixel * moyenne
		ImageStatistics is = imp.getStatistics();
		this.coups.put(roi + indexImage, (int) (is.pixelCount * is.mean));
	}

	public int getCoups(String roi, int indexImage) {
		return this.coups.get(roi + indexImage);
	}

	public void setCoups(String roi, int indexImage, int d) {
		this.coups.put(roi + indexImage, d);
	}

	private void mgs(int indexImage) {
		//on calcule cet de l'antre et de l'intestin 
		for (String roi : this.organes)
			this.moyenneGeo(roi, indexImage);
		//on calcule les MGs de l'estomac
		this.mgs.put("Estomac_MG" + indexImage,
				1 * this.mgs.get("Fundus_MG" + indexImage) + 1 * this.mgs.get("Antre_MG" + indexImage));
		//on calcule la somme des MGs 
		this.mgs.put("Total" + indexImage,
				1 * this.mgs.get("Estomac_MG" + indexImage) + 1 * this.mgs.get("Intestin_MG" + indexImage));
	}

	// Calcule la moyenne geometrique pour un organe specifique
	private void moyenneGeo(String organe, int indexImage) {
		int[] coupsa = new int[2];
		String[] asuppr = new String[2];
		int index = 0;
		for (Entry<String, Integer> entry : this.coups.entrySet()) {
			if (entry.getKey().contains(organe) && entry.getKey().contains(Integer.toString(indexImage))) {
				coupsa[index] = entry.getValue();
				asuppr[index] = entry.getKey();
				index++;
			}
		}
		this.mgs.put(organe + "_MG" + indexImage, this.moyenneGeometrique(coupsa));
	}

	// Calcule la moyenne geometrique des nombres en paramètre
	private int moyenneGeometrique(int[] vals) {
		double result = 1.0;
		for (int i = 0; i < vals.length; i++) {
			result *= vals[i];
		}
		result = Math.sqrt(result);
		return (int) result;
	}

	// calcule le temps de acquisition de l'image
	public void tempsImage(int indexImage, String acquisitionTime) throws ParseException {
		DateFormat df = new SimpleDateFormat("HHmmss");
		Date d1 = df.parse(acquisitionTime.substring(1, 7));
		Date d2 = df.parse(Vue_VG_Roi.timeStart);
		long diff = d1.getTime() - d2.getTime();
		long day = diff / (24 * 60 * 60 * 1000);
		long hour = (diff / (60 * 60 * 1000) - day * 24);
		long min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
		Modele_VG_Roi.temps[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 1] = (double) (day * 24 * 60 + hour * 60 + min);
	}

	//pour chaque serie, on calcule le pourcentage de l'estomac, le fundus, l'antre et l'intestin par rapport au total du repas
	//calcule le rapport fundus/estomac et la derivee de la courbe de variation de l’estomac
	public void pourcVGImage(int indexImage) {
		this.mgs(indexImage);

		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
		sym.setDecimalSeparator('.');
		DecimalFormat us = new DecimalFormat("##.#");
		us.setDecimalFormatSymbols(sym);

		double fundusPour = ((double) this.mgs.get("Fundus_MG" + indexImage)
				/ (double) this.mgs.get("Total" + indexImage)) * 100;
		Modele_VG_Roi.fundusPourcent[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 1] = Double
				.parseDouble(us.format(fundusPour));
		double antrePour = ((double) this.mgs.get("Antre_MG" + indexImage)
				/ (double) this.mgs.get("Total" + indexImage)) * 100;
		Modele_VG_Roi.antrePourcent[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 1] = Double
				.parseDouble(us.format(antrePour));
		double estomacPour = ((double) this.mgs.get("Estomac_MG" + indexImage)
				/ (double) this.mgs.get("Total" + indexImage)) * 100;
		Modele_VG_Roi.estomacPourcent[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 1] = Double
				.parseDouble(us.format(estomacPour));
		double intestinPour = ((double) this.mgs.get("Intestin_MG" + indexImage)
				/ (double) this.mgs.get("Total" + indexImage)) * 100;
		Modele_VG_Roi.intestinPourcent[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 1] = Double
				.parseDouble(us.format(intestinPour));
		double funDevEsto = (Modele_VG_Roi.fundusPourcent[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 1]
				/ Modele_VG_Roi.estomacPourcent[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 1]) * 100;
		Modele_VG_Roi.funDevEsto[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 1] = Double
				.parseDouble(us.format(funDevEsto));

		Modele_VG_Roi.tempsInter[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 2] = Modele_VG_Roi.temps[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 1];
		double estoInter = ((Modele_VG_Roi.estomacPourcent[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 2] - Modele_VG_Roi.estomacPourcent[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 1])
				/ (Modele_VG_Roi.temps[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 1] - Modele_VG_Roi.temps[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 2])) * 30;
		Modele_VG_Roi.estoInter[indexImage + Vue_VG_Roi.resultatsDynamique.length / 4 - 2] = Double
				.parseDouble(us.format(estoInter));
		if (Modele_VG_Roi.logOn) {
			// affiche le resultat au log pour que l'utilisateur peut le
			// verifier
			IJ.log("image " + (indexImage) + ": " + " Stomach " + Modele_VG_Roi.estomacPourcent[indexImage]
					+ " Intestine " + Modele_VG_Roi.intestinPourcent[indexImage] + " Fundus "
					+ Modele_VG_Roi.fundusPourcent[indexImage] + " Antre " + Modele_VG_Roi.antrePourcent[indexImage]);
		}

	}

	// initialisation des tables de resultats
	public void initResultat(ImagePlus imp) {
		Modele_VG_Roi.temps = new double[imp.getStackSize() / 2 + Vue_VG_Roi.resultatsDynamique.length / 4];
		Modele_VG_Roi.estomacPourcent = new double[imp.getStackSize() / 2 + Vue_VG_Roi.resultatsDynamique.length / 4];
		Modele_VG_Roi.fundusPourcent = new double[imp.getStackSize() / 2 + Vue_VG_Roi.resultatsDynamique.length / 4];
		Modele_VG_Roi.antrePourcent = new double[imp.getStackSize() / 2 + Vue_VG_Roi.resultatsDynamique.length / 4];
		Modele_VG_Roi.intestinPourcent = new double[imp.getStackSize() / 2 + Vue_VG_Roi.resultatsDynamique.length / 4];
		Modele_VG_Roi.funDevEsto = new double[imp.getStackSize() / 2 + Vue_VG_Roi.resultatsDynamique.length / 4];
		Modele_VG_Roi.estoInter = new double[imp.getStackSize() / 2 + Vue_VG_Roi.resultatsDynamique.length / 4 - 1];
		Modele_VG_Roi.tempsInter = new double[imp.getStackSize() / 2 + Vue_VG_Roi.resultatsDynamique.length / 4 - 1];

		for (int i = 0; i < Vue_VG_Roi.resultatsDynamique.length / 4; i++) {
			Modele_VG_Roi.temps[i] = Integer.parseInt(Vue_VG_Roi.resultatsDynamique[i * 4]);
			Modele_VG_Roi.estomacPourcent[i] = Double.parseDouble(Vue_VG_Roi.resultatsDynamique[i * 4 + 1]);
			Modele_VG_Roi.fundusPourcent[i] = Double.parseDouble(Vue_VG_Roi.resultatsDynamique[i * 4 + 2]);
			Modele_VG_Roi.antrePourcent[i] = Double.parseDouble(Vue_VG_Roi.resultatsDynamique[i * 4 + 3]);
			Modele_VG_Roi.intestinPourcent[i] = 100.0 - Modele_VG_Roi.estomacPourcent[i];
			Modele_VG_Roi.funDevEsto[i] = Modele_VG_Roi.fundusPourcent[i] / Modele_VG_Roi.estomacPourcent[i] * 100.0;
			if(i>0){
				Modele_VG_Roi.tempsInter[i-1]=Modele_VG_Roi.temps[i];
				Modele_VG_Roi.estoInter[i-1] = ((Modele_VG_Roi.estomacPourcent[i-1] - Modele_VG_Roi.estomacPourcent[i])
						/ (Modele_VG_Roi.temps[i] - Modele_VG_Roi.temps[i-1])) * 30.0;
				
			}
		}
	}

	// permet de transferer toutes les resultats en une tableau de chaine
	public String[] resultats(ImagePlus imp) {
		String[] retour = new String[4 * (imp.getStackSize() / 2 +Vue_VG_Roi.resultatsDynamique.length/4+ 1) + 12];
		//on enregistre la premiere partie des resultats
		retour[0] = "Time(min)";
		retour[1] = "Stomach(%)";
		retour[2] = "Fundus(%)";
		retour[3] = "Antrum(%)";
		for (int i = 0; i < imp.getStackSize() / 2+Vue_VG_Roi.resultatsDynamique.length/4; i++) {
			retour[i * 4 + 4] = Integer.toString((int) Modele_VG_Roi.temps[i]);
			retour[i * 4 + 5] = Double.toString(Modele_VG_Roi.estomacPourcent[i]);
			retour[i * 4 + 6] = Double.toString(Modele_VG_Roi.fundusPourcent[i]);
			retour[i * 4 + 7] = Double.toString(Modele_VG_Roi.antrePourcent[i]);
		}
		// on enregistre la deuxime partie des resultats
		int j = (imp.getStackSize() / 2+Vue_VG_Roi.resultatsDynamique.length/4) * 4 + 4;
		String institutionName = "";
		if (DicomTools.getTag(imp, "0008,0080") != null) {
			institutionName = DicomTools.getTag(imp, "0008,0080").trim();
		}
		retour[j++] = institutionName;
		String studyDescription = "";
		if (DicomTools.getTag(imp, "0008,1030") != null) {
			studyDescription = DicomTools.getTag(imp, "0008,1030").trim();
			//On remplace les virgules car elle cassent la structure du CSV
			studyDescription = studyDescription.replaceAll(",", "_");
		}
		retour[j++] = studyDescription;
		retour[j++] = "Patient : " + this.patient;
		retour[j++] = "Date : " + this.date.substring(6, 8) + "/" + this.date.substring(4, 6) + "/"
				+ this.date.substring(0, 4) + "  " + Vue_VG_Roi.timeStart.substring(0, 2) + "h"
				+ Vue_VG_Roi.timeStart.substring(2, 4);
		retour[j++] = "Start Antrum : " + this.getDebut("Antre") + " min";
		retour[j++] = "Start Intestine : " + this.getDebut("Intestin") + " min";
		//SI valeur interpolee on ajoute * a la string
		retour[j++] = "Lag Phase : " + this.getX(95.0) + (trouve? " min":" min*") ;
		retour[j++] = "T1/2 : " + this.getX(50.0) +  (trouve? " min":" min*") ;
		retour[j++] = "Retention at 1h : " +  this.getY(60.0) + (trouve? "%":"%*") ;
		retour[j++] = "Retention at 2h : " + this.getY(120.0) + (trouve? "%":"%*") ;
		retour[j++] = "Retention at 3h : " + this.getY(180.0) + (trouve? "%":"%*") ;
		retour[j] = "Retention at 4h : " + this.getY(240.0) + (trouve? "%":"%*") ;
		return retour;
	}

	// permet de obtenir un courbe
	protected ImagePlus createCourbeUn(String yAxisLabel, Color color, String titre, double[] resX, double[] resY,
			double upperBound) {
		JFreeChart xylineChart = ChartFactory.createXYLineChart("", "min", yAxisLabel,
				createDatasetUn(resX, resY, titre), PlotOrientation.VERTICAL, true, true, true);

		XYPlot plot = (XYPlot) xylineChart.getPlot();
		// Background
		plot.setBackgroundPaint(Color.WHITE);

		// XYLineAndShapeRenderer
		// reference:
		// https://stackoverflow.com/questions/28428991/setting-series-line-style-and-legend-size-in-jfreechart
		XYLineAndShapeRenderer lineAndShapeRenderer = new XYLineAndShapeRenderer();
		lineAndShapeRenderer.setSeriesPaint(0, color);
		lineAndShapeRenderer.setSeriesStroke(0, new BasicStroke(2.0F));
		plot.setRenderer(lineAndShapeRenderer);
		lineAndShapeRenderer.setDefaultLegendTextFont(new Font("", Font.BOLD, 16));
		// XAxis
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setRange(0.00, 360.00);
		domainAxis.setTickUnit(new NumberTickUnit(30.00));
		domainAxis.setTickMarkStroke(new BasicStroke(2.5F));
		domainAxis.setLabelFont(new Font("", Font.BOLD, 16));
		domainAxis.setTickLabelFont(new Font("", Font.BOLD, 12));
		// YAxis
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(0.00, upperBound);
		rangeAxis.setTickUnit(new NumberTickUnit(10.00));
		rangeAxis.setTickMarkStroke(new BasicStroke(2.5F));
		rangeAxis.setLabelFont(new Font("", Font.BOLD, 16));
		rangeAxis.setTickLabelFont(new Font("", Font.BOLD, 12));
		// Grid
		plot.setDomainGridlinesVisible(false);
		BufferedImage buff = xylineChart.createBufferedImage(640, 512);
		ImagePlus courbe = new ImagePlus("", buff);
		return courbe;
	}

	// permet de creer un dataset d'un group de donees pour un courbe
	private XYSeriesCollection createDatasetUn(double[] resX, double[] resY, String titre) {
		XYSeries courbe = new XYSeries(titre);
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int i = 0; i < resX.length; i++) {
			courbe.add(resX[i], resY[i]);
		}
		dataset.addSeries(courbe);
		return dataset;
	}

	// permet de creer un graphique avec trois courbes
	protected ImagePlus createCourbeTrois(String yAxisLabel, double[] resX, double[] resY1, Color color1, String titre1,
			double[] resY2, Color color2, String titre2, double[] resY3, Color color3, String titre3) {
		//On cree un dataset qui contient les 3 series
		XYSeriesCollection dataset=createDatasetTrois(resX, resY1, titre1, resY2, titre2, resY3, titre3);
		//On cree le graphique
		JFreeChart xylineChart = ChartFactory.createXYLineChart("", "min", yAxisLabel,
				dataset, PlotOrientation.VERTICAL, true,
				true, false);
		// Parametres de l'affichage
		XYPlot plot = (XYPlot) xylineChart.getPlot();
		// choix du Background
		plot.setBackgroundPaint(Color.WHITE);

		// XYLineAndShapeRenderer
		// reference:
		// https://stackoverflow.com/questions/28428991/setting-series-line-style-and-legend-size-in-jfreechart
		XYLineAndShapeRenderer lineAndShapeRenderer = new XYLineAndShapeRenderer();
		//on set le renderer dans le plot
		plot.setRenderer(lineAndShapeRenderer);
		//On definit les parametre du renderer
		lineAndShapeRenderer.setDefaultLegendTextFont(new Font("", Font.BOLD, 16));
		lineAndShapeRenderer.setSeriesPaint(0, color1);
		lineAndShapeRenderer.setSeriesPaint(1, color2);
		lineAndShapeRenderer.setSeriesPaint(2, color3);
		//Defini la taille de la courbes (epaisseur du trait)
		lineAndShapeRenderer.setSeriesStroke(0, new BasicStroke(2.0F));
		lineAndShapeRenderer.setSeriesStroke(1, new BasicStroke(2.0F));
		lineAndShapeRenderer.setSeriesStroke(2, new BasicStroke(2.0F));

		// XAxis
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		//Limites de l'axe X
		domainAxis.setRange(0.00, 360.00);
		//Pas de l'axe X
		domainAxis.setTickUnit(new NumberTickUnit(30.00));
		domainAxis.setTickMarkStroke(new BasicStroke(2.5F));
		domainAxis.setLabelFont(new Font("", Font.BOLD, 16));
		domainAxis.setTickLabelFont(new Font("", Font.BOLD, 12));

		// YAxis
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(0.00, 100.00);
		rangeAxis.setTickUnit(new NumberTickUnit(10.00));
		rangeAxis.setTickMarkStroke(new BasicStroke(2.5F));
		rangeAxis.setLabelFont(new Font("", Font.BOLD, 16));
		rangeAxis.setTickLabelFont(new Font("", Font.BOLD, 12));
		
		// Grid
		//On ne met pas de grille sur la courbe
		plot.setDomainGridlinesVisible(false);
		//On cree la buffered image de la courbe et on envoie dans une ImagePlus
		BufferedImage buff = xylineChart.createBufferedImage(640, 512);
		ImagePlus courbe = new ImagePlus("", buff);
		
		return courbe;
	}

	// permet de creer un dataset de trois groups de donees pour trois courbes
	private XYSeriesCollection createDatasetTrois(double[] resX, double[] resY1, String titre1, double[] resY2,
			String titre2, double[] resY3, String titre3) {
		// On initialise les 3 series avec un titre
		XYSeries courbe1 = new XYSeries(titre1);
		XYSeries courbe2 = new XYSeries(titre2);
		XYSeries courbe3 = new XYSeries(titre3);
		//On initialise un dataset
		XYSeriesCollection dataset = new XYSeriesCollection();
		// Pour chaque serie on ajouter les valeurs une a une
		for (int i = 0; i < resX.length; i++) {
			courbe1.add(resX[i], resY1[i]);
			courbe2.add(resX[i], resY2[i]);
			courbe3.add(resX[i], resY3[i]);
		}
		//On ajoute les 3 series dans le dataset
		dataset.addSeries(courbe1);
		dataset.addSeries(courbe2);
		dataset.addSeries(courbe3);
		return dataset;
	}
	
	//Pour faire un fit lineaire et extrapoler les valeurs non existante
	private double[] getParametreCourbeFit(){
		XYSeriesCollection dataset=createDatasetTrois(Modele_VG_Roi.temps,
				Modele_VG_Roi.estomacPourcent,  "Stomach", Modele_VG_Roi.fundusPourcent, 
				"Fundus", Modele_VG_Roi.antrePourcent,  "Antrum");
		//Retourne les valeurs a et b de la fonction fit y=ax+b
		double[] parametreCourbe=Regression.getOLSRegression(dataset, 0 );
		return parametreCourbe;
	}

	// permet de obtenir le temps de debut antre et debut intestin
	private int getDebut(String organe) {
		boolean trouve = false;
		double res = 0.0;
		if(organe=="Antre"){
		for (int i = 0; i < Modele_VG_Roi.antrePourcent.length && !trouve; i++) {
			//si le pourcentage de l'antre > 0
			if (Modele_VG_Roi.antrePourcent[i] > 0) {
				trouve = true;
				res = Modele_VG_Roi.temps[i];
			}
		}
		}
		if(organe=="Intestin"){
			for (int i = 0; i < Modele_VG_Roi.estomacPourcent.length && !trouve; i++) {
				//si le pourcentage de l'estomac est < 100, c'est a dire le pourcentage de l'intestin  > 0
				if (Modele_VG_Roi.estomacPourcent[i] < 100.0) {
					trouve = true;
					res = Modele_VG_Roi.temps[i];
				}
			}
			}
		return (int) Math.round(res);
	}

	// permet de obtenir la valeur X de la courbe selon la valeur Y donee
	private int getX(double valueY) {
		trouve = false;
		double valueX = 0.0;
		for (int i = 0; i < Modele_VG_Roi.estomacPourcent.length && !trouve; i++) {
			if (Modele_VG_Roi.estomacPourcent[i] <= valueY) {
				trouve = true;
				double x1 = Modele_VG_Roi.temps[i - 1];
				double x2 = Modele_VG_Roi.temps[i];
				double y1 = Modele_VG_Roi.estomacPourcent[i - 1];
				double y2 = Modele_VG_Roi.estomacPourcent[i];
				valueX = x2 - (y2 - valueY) * ((x2 - x1) / (y2 - y1));
			}
		}
		if(trouve==false){
			//Si les valeurs n'existent pas on realise le fit
			double[] parametres=this.getParametreCourbeFit();
			double a=parametres[1];
			double b=parametres[0];
			valueX=(valueY-b)/a;
		}
		return (int) Math.round(valueX);
	}

	// permet de obtenir la valeur Y de la courbe selon la valeur X donee
	private double getY(double valueX) {
		trouve = false;
		double valueY = 0.0;
		for (int i = 0; i < Modele_VG_Roi.temps.length && !trouve; i++) {
			if (Modele_VG_Roi.temps[i] >= valueX) {
				trouve = true;
				double x1 = Modele_VG_Roi.temps[i - 1];
				double x2 = Modele_VG_Roi.temps[i];
				double y1 = Modele_VG_Roi.estomacPourcent[i - 1];
				double y2 = Modele_VG_Roi.estomacPourcent[i];
				valueY = y2 - (x2 - valueX) * ((y2 - y1) / (x2 - x1));
			}
		}
		if(trouve==false){
			//Si les valeurs n'existent pas on realise le fit
			double[] parametres=this.getParametreCourbeFit();
			double a=parametres[1];
			double b=parametres[0];
			valueY=a*valueX+b;
			//Si valeur negative on met 0;
			if (valueY<=0) valueY=0;
		}
		return (double) (Math.round(valueY * 10) / 10.0);
	}

}