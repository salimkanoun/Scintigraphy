/**
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

import ij.plugin.filter.Analyzer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JTable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;

public class Modele_Plaquettes extends ModeleScin {

	// Stockage des objets mesures, chaque image est identifee par sa date
	// d'aqcisition
	// et l'objet contient toutes les mesusres realisee sur image ant(si presente)
	// et post
	HashMap<Date, MesureImage> mesures = new HashMap<>();
	private Date dateHeureDebut;

	public Modele_Plaquettes(Date dateHeureDebut) {
		this.dateHeureDebut = dateHeureDebut;
	}

	@Override
	public void enregistrerMesure(String roi, ImagePlus imp) {
		Date dateAcquisition = Library_Dicom.getDateAcquisition(imp);

		// Recupere la somme des coups dans la ROI (integrated Density) et la valeur
		// moyenne de la Roi (mean)
		Analyzer.setMeasurement(Measurements.INTEGRATED_DENSITY, true);
		Analyzer.setMeasurement(Measurements.MEAN, true);
		Analyzer analyser = new Analyzer(imp);
		analyser.measure();
		ResultsTable density = Analyzer.getResultsTable();

		double counts = density.getValueAsDouble(ResultsTable.RAW_INTEGRATED_DENSITY, 0);
		double mean = density.getValueAsDouble(ResultsTable.MEAN, 0);

		// Si premiere fois qu'on traite l'image on cree l'objet et on l'ajoute dans la
		// hashMap
		if (!this.mesures.containsKey(dateAcquisition)) {
			MesureImage mesure = new MesureImage(dateAcquisition);
			// on calcule le delai par rapport a la premiere image et on enregistre la
			// valeur en heure (utile pour le trie et les courbes ensuite)
			mesure.setDelayFromStart((dateAcquisition.getTime() - this.dateHeureDebut.getTime()) / (1000 * 60 * 60));
			this.mesures.put(dateAcquisition, mesure);
		}

		// On calcule les valeurs et on l'ajoute dans l'objet adHoc
		if (this.mesures.containsKey(dateAcquisition)) {

			if (roi.contains("Spleen P")) {
				double[] spleen = new double[2];
				spleen[0] = counts;
				spleen[1] = mean;
				this.mesures.get(dateAcquisition).setSpleenValue(spleen);
			}

			else if (roi.contains("Liver P")) {
				double[] liver = new double[2];
				liver[0] = counts;
				liver[1] = mean;
				this.mesures.get(dateAcquisition).setLiverValue(liver);
			}

			else if (roi.contains("Heart P")) {
				double[] heart = new double[2];
				heart[0] = counts;
				heart[1] = mean;
				this.mesures.get(dateAcquisition).setHeartValue(heart);
			}

			else if (roi.contains("Spleen A")) {
				double[] spleen = new double[2];
				spleen[0] = counts;
				spleen[1] = mean;
				this.mesures.get(dateAcquisition).setSpleenAntValue(spleen);
			}

			else if (roi.contains("Liver A")) {
				double[] liver = new double[2];
				liver[0] = counts;
				liver[1] = mean;
				this.mesures.get(dateAcquisition).setliverAntValue(liver);
			}

			else if (roi.contains("Heart A")) {
				double[] heart = new double[2];
				heart[0] = counts;
				heart[1] = mean;
				this.mesures.get(dateAcquisition).setHeartAntValue(heart);
			}

		}

		if (Controleur_Plaquettes.showLog) {
			IJ.log(roi + "counts= " + String.valueOf(counts));
			IJ.log(roi + "mean= " + String.valueOf(mean));
		}
	}

	@SuppressWarnings("null")
	protected JTable getResults() {
		// On boule la hashmap pour recuperer les resultats
		Date[] mapDate = new Date[this.mesures.size()];
		mapDate = this.mesures.keySet().toArray(mapDate);
		// On trie les donnees par date
		Arrays.sort(mapDate);

		String[] titreColonne = new String[this.mesures.size() + 1];
		titreColonne[0] = "Time (Hours)";

		String[][] data = null;

		// pour l'arrondi des resultats
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		// DateFormat dateFormat=new SimpleDateFormat("HH:mm");

		// On traite chaque acquisition
		for (int i = 0; i < this.mesures.size(); i++) {
			// On ajoute le temps de mesure dans les titre de colonne
			int tempsHeures = (int) Math.round(this.mesures.get(mapDate[i]).getDelayFromStart());
			titreColonne[i + 1] = String.valueOf(tempsHeures);
			HashMap<String, Double> resultsImage = this.mesures.get(mapDate[i]).calculateandGetResults();

			// On set les data avec sa taille a la premiere boucle
			if (i == 0) {
				data = new String[resultsImage.size()][this.mesures.size() + 1];
			}

			String[] resultsLabel = new String[resultsImage.size()];
			// on traite touts les resultats d'une acquisition
			resultsLabel = resultsImage.keySet().toArray(resultsLabel);
			for (int j = 0; j < resultsLabel.length; j++) {

				if (i == 0)
					data[j][0] = resultsLabel[j];
				double valeur;
				// On file les data ligne par ligne pour chaque colonne
				if (resultsLabel[j].equals("Corrected SpleenPosterior")) {
					// Si coup corrige on divise par nombre de coups iniitiaux de la 1ere image
					double[] spleenInit = this.mesures.get(this.dateHeureDebut).getSpleenValue();
					valeur = resultsImage.get(resultsLabel[j]) / spleenInit[0];
				} else
					valeur = resultsImage.get(resultsLabel[j]);

				data[j][i + 1] = decimalFormat.format(valeur);

			}

		}

		JTable table = new JTable(data, titreColonne);
		return table;

	}

	/**
	 * collecte les valeurs, appelle la methode de creation de courbes et renvoie
	 * les courbes dans un tableau d'ImagePlus
	 * 
	 * @param table
	 * @return
	 */
	protected ImagePlus[] createDataset(JTable table) {
		XYSeriesCollection datasetPost = new XYSeriesCollection();
		XYSeriesCollection datasetGM = new XYSeriesCollection();
		XYSeriesCollection datasetJ0Ratio = new XYSeriesCollection();

		for (int i = 0; i < table.getRowCount(); i++) {
			String name = table.getValueAt(i, 0).toString();
			// Cree une courbe avec son titre
			XYSeries courbe = new XYSeries(name);

			// On ajoute les valeurs
			for (int j = 1; j < table.getColumnCount(); j++) {
				double x = Double.parseDouble(table.getColumnName(j).toString().replaceAll(",", "."));
				double y = Double.parseDouble(table.getValueAt(i, j).toString().replaceAll(",", "."));
				courbe.add(x, y);
			}

			if (name.contains(" Post")) {
				datasetPost.addSeries(courbe);
			} else if (name.contains(" GM ")) {
				datasetGM.addSeries(courbe);
			} else if (name.contains("Corrected")) {
				datasetJ0Ratio.addSeries(courbe);

			}
		}
		IJ.log("avant list ImagePlus");
		// On cree le tableau d'ImagePlus qui recoit les courbes
		List<ImagePlus> courbes = new ArrayList<>();
		if (datasetPost.getSeriesCount() != 0)
			courbes.add(makeGraph(datasetPost, "Posterior"));
		if (datasetJ0Ratio.getSeriesCount() != 0)
			courbes.add(makeGraph(datasetJ0Ratio, "J0 Ratio"));
		// Si on est en ANT/Post On ajoute la courbe moyenne Geometrique
		if (datasetGM.getSeriesCount() != 0)
			courbes.add(makeGraph(datasetGM, "Geometrical Mean"));

		// On passe la liste en tableau
		ImagePlus[] courbesTableau = new ImagePlus[courbes.size()];
		courbes.toArray(courbesTableau);
		IJ.log(String.valueOf(courbesTableau.length));

		return courbesTableau;
	}

	/**
	 * Cree les courbes en une image de 640*512 � partir d'un dataset de valeur (un
	 * ou plusieurs courbes)
	 * 
	 * @param dataset
	 * @param title
	 * @return
	 */
	private static ImagePlus makeGraph(XYSeriesCollection dataset, String title) {
		JFreeChart xylineChart = ChartFactory.createXYLineChart(title, "Hours", "Value", dataset,
				PlotOrientation.VERTICAL, true, true, true);

		XYPlot plot = (XYPlot) xylineChart.getPlot();

		// Background
		plot.setBackgroundPaint(Color.WHITE);

		// XYLineAndShapeRenderer
		// reference:
		// https://stackoverflow.com/questions/28428991/setting-series-line-style-and-legend-size-in-jfreechart
		XYLineAndShapeRenderer lineAndShapeRenderer = new XYLineAndShapeRenderer();
		lineAndShapeRenderer.setSeriesPaint(0, Color.red);
		lineAndShapeRenderer.setSeriesStroke(0, new BasicStroke(2.0F));
		plot.setRenderer(lineAndShapeRenderer);
		lineAndShapeRenderer.setDefaultLegendTextFont(new Font("", Font.BOLD, 16));
		// XAxis
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setRange(dataset.getSeries(0).getMinX() - 5, dataset.getSeries(0).getMaxX() + 5);
		domainAxis.setTickUnit(new NumberTickUnit(24.00));
		domainAxis.setTickMarkStroke(new BasicStroke(2.5F));
		domainAxis.setLabelFont(new Font("", Font.BOLD, 16));
		domainAxis.setTickLabelFont(new Font("", Font.BOLD, 12));
		// YAxis
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRange(true);
		rangeAxis.setAutoTickUnitSelection(true);
		rangeAxis.setTickMarkStroke(new BasicStroke(2.5F));
		rangeAxis.setLabelFont(new Font("", Font.BOLD, 16));
		rangeAxis.setTickLabelFont(new Font("", Font.BOLD, 12));
		// Grid
		plot.setDomainGridlinesVisible(false);
		BufferedImage buff = xylineChart.createBufferedImage(640, 512);
		ImagePlus courbe = new ImagePlus(title, buff);
		return courbe;
	}

	@Override
	public void calculerResultats() {
		//todo TODO calculer resultats plaquettes
	}

	@Override
	public String toString() {
		return "";
	}

}
