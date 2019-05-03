package org.petctviewer.scintigraphy.gastric_refactored;

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
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
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.FitType;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.LinearFit;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.MontageMaker;

public class Model_Gastric extends ModeleScin {
	public static Font italic = new Font("Arial", Font.ITALIC, 8);

	/**
	 * Results available for the {@link Model_Gastric} model.
	 * 
	 * @author Titouan QUÉMA
	 *
	 */
	public enum Result {
		START_ANTRUM("Start antrum", "min"),
		START_INTESTINE("Start intestine", "min"),
		LAG_PHASE("Lag phase", "min"),
		T_HALF("T 1/2", "%"),
		RETENTION("Retention", "%");

		private String s;
		private String unit;

		private Result(String s, String unit) {
			this.s = s;
			this.unit = unit;
		}

		/**
		 * @return unit of this result
		 */
		public String getUnit() {
			return this.unit;
		}

		/**
		 * @return name of this result
		 */
		public String getName() {
			return this.s;
		}
	}

	/**
	 * Result returned by the {@link Model_Gastric} model.
	 * 
	 * @author Titouan QUÉMA
	 *
	 */
	public class ResultValue {
		public Result type;
		public double value;
		public FitType extrapolation;

		public ResultValue(Result type, double value, FitType extrapolation) {
			this.type = type;
			this.value = value;
			this.extrapolation = extrapolation;
		}

		/**
		 * @return TRUE if the value is extrapolated from the current fit and FALSE if
		 *         the value is linearly extrapolated between two known points
		 */
		public boolean isExtrapolated() {
			return this.extrapolation != null;
		}

		/**
		 * Returns the value of this result rounded at 2 decimals and set to 0 if
		 * negative.<br>
		 * If this result is extrapolated, then a star '(*)' is added at the end of the
		 * result.
		 * 
		 * @return rounded value for this result (2 decimals) restrained to 0 if
		 *         negative
		 */
		public String notNegative() {
			return BigDecimal.valueOf(Math.max(0, value)).setScale(2, RoundingMode.HALF_UP).toString()
					+ (isExtrapolated() ? "(*)" : "");
		}
	}

	/**
	 * Results calculated for each image.
	 */
	public static final int RES_TIME = 0, RES_STOMACH = 1, RES_FUNDUS = 2, RES_ANTRUM = 3;

	// == STATIC ACQUISITION ==
	private HashMap<String, Double> coups;// pour enregistrer les coups dans chaque organe sur chaque image
	private HashMap<String, Double> mgs;// pour enregistrer le MG dans chaque organe pour chaque serie

	private double[] temps;// pour enregistrer l'horaire où on recupere chaque serie
	private double[] estomacPourcent;// pour enregistrer le pourcentage de l'estomac(par rapport a total) pour
										// chaque serie
	private double[] fundusPourcent;// pour enregistrer le pourcentage du fundus(par rapport a total) pour
									// chaque serie
	private double[] antrePourcent;// pour enregistrer le pourcentage de l'antre(par rapport a total) pour
									// chaque serie
	private double[] funDevEsto;// pour enregistrer le rapport fundus/estomac pour chaque serie
	private double[] estoInter;// pour enregistrer le rapport fundus/estomac pour chaque serie
	private double[] tempsInter;// pour enregistrerla derivee de la courbe de variation de l’estomac
	private boolean logOn;// signifie si log est ouvert
	private double[] intestinPourcent;// pour enregistrer le pourcentage de l'intestin(par rapport a total)
										// pour chaque serie

	private ImageSelection[] staticImages;

	// == DYNAMIC ACQUISITION ==

	// == BOTH ACQUISITIONS ==

	private String[] organes = { "Estomac", "Intestin", "Fundus", "Antre" };

	private boolean trouve;// signifie si la valeur qu'on veut est trouvee sur la courbe

	private Date timeIngestion;

	private Fit extrapolation;

	public Model_Gastric(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
		this.coups = new HashMap<>();
		this.mgs = new HashMap<>();
		Prefs.useNamesAsLabels = true;
		this.logOn = false;
	}

	private void mgs(int indexImage) {
		// on calcule cet de l'antre et de l'intestin
		for (String roi : this.organes)
			this.moyenneGeo(roi, indexImage);
		// on calcule les MGs de l'estomac
		this.mgs.put("Estomac_MG" + indexImage,
				this.mgs.get("Fundus_MG" + indexImage) + this.mgs.get("Antre_MG" + indexImage));
		// on calcule la somme des MGs
		this.mgs.put("Total" + indexImage,
				1 * this.mgs.get("Estomac_MG" + indexImage) + 1 * this.mgs.get("Intestin_MG" + indexImage));
	}

	// Calcule la moyenne geometrique pour un organe specifique
	private void moyenneGeo(String organe, int indexImage) {
		double[] coupsa = new double[2];
		String[] asuppr = new String[2];
		int index = 0;
		for (Entry<String, Double> entry : this.coups.entrySet()) {
			if (entry.getKey().contains(organe) && entry.getKey().contains(Integer.toString(indexImage))) {
				coupsa[index] = entry.getValue();
				asuppr[index] = entry.getKey();
				index++;
			}
		}
		this.mgs.put(organe + "_MG" + indexImage, Library_Quantif.moyGeom((double) coupsa[0], (double) coupsa[1]));
	}

	/**
	 * Generates a graphic image with the specified arguments.
	 * 
	 * @param yAxisLabel Label of the Y axis
	 * @param color      Color of the line
	 * @param titre      Title of the graph
	 * @param resX       Values of the points for the X axis
	 * @param resY       Values of the points for the Y axis
	 * @param upperBound The upper axis limit
	 * @return ImagePlus containing the graphic
	 */
	private static ImagePlus createGraph(String yAxisLabel, Color color, String titre, double[] resX, double[] resY,
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
	private static XYSeriesCollection createDatasetUn(double[] resX, double[] resY, String titre) {
		XYSeries courbe = new XYSeries(titre);
		for (int i = 0; i < resX.length; i++)
			courbe.add(resX[i], resY[i]);
		return new XYSeriesCollection(courbe);
	}

	// permet de creer un dataset de trois groups de donees pour trois courbes
	private XYSeriesCollection createDatasetTrois(double[] resX, double[] resY1, String titre1, double[] resY2,
			String titre2, double[] resY3, String titre3) {
		// On initialise les 3 series avec un titre
		XYSeries courbe1 = new XYSeries(titre1);
		XYSeries courbe2 = new XYSeries(titre2);
		XYSeries courbe3 = new XYSeries(titre3);
		// On initialise un dataset
		XYSeriesCollection dataset = new XYSeriesCollection();
		// Pour chaque serie on ajouter les valeurs une a une
		for (int i = 0; i < resX.length; i++) {
			courbe1.add(resX[i], resY1[i]);
			courbe2.add(resX[i], resY2[i]);
			courbe3.add(resX[i], resY3[i]);
		}
		// On ajoute les 3 series dans le dataset
		dataset.addSeries(courbe1);
		dataset.addSeries(courbe2);
		dataset.addSeries(courbe3);
		return dataset;
	}

	// permet de obtenir le temps de debut antre et debut intestin
	private double getDebut(String organe) {
		boolean trouve = false;
		double res = 0.0;
		if (organe == "Antre") {
			for (int i = 0; i < this.antrePourcent.length && !trouve; i++) {
				// si le pourcentage de l'antre > 0
				if (this.antrePourcent[i] > 0) {
					trouve = true;
					res = this.temps[i];
				}
			}
		}
		if (organe == "Intestin") {
			for (int i = 0; i < this.estomacPourcent.length && !trouve; i++) {
				// si le pourcentage de l'estomac est < 100, c'est a dire le pourcentage de
				// l'intestin > 0
				if (this.estomacPourcent[i] < 100.0) {
					trouve = true;
					res = this.temps[i];
				}
			}
		}
		return res;
	}

	/**
	 * Gets the X value based upon the specified Y value of the graph.<br>
	 * The value X returned is calculated with a linear interpolation as the point
	 * between x1 and x2 with <code>x1 <= X <= x2</code>. If x1 or x2 could not be
	 * found, then the result is null.<br>
	 * 
	 * @return X value or null if none found
	 * @see #extrapolateX(double, Fit)
	 */
	private Double getX(double valueY) {
		for (int i = 1; i < this.estomacPourcent.length && !trouve; i++) {
			if (this.estomacPourcent[i] <= valueY) {
				double x1 = this.temps[i - 1];
				double x2 = this.temps[i];
				double y1 = this.estomacPourcent[i - 1];
				double y2 = this.estomacPourcent[i];
				return x2 - (y2 - valueY) * ((x2 - x1) / (y2 - y1));
			}
		}
		return null;
	}

	/**
	 * Extrapolates the X value based upon the specified Y value of the graph.<br>
	 * The value X returned is extrapolated with the specified fit.
	 * 
	 * @param valueY Y value
	 * @param fit    Fit the interpolation must rely on
	 * @return X value interpolated
	 */
	private Double extrapolateX(double valueY, Fit fit) {
		return fit.extrapolateX(valueY);
	}

	/**
	 * Gets the Y value based upon the specified X value of the graph.<br>
	 * The value Y returned is calculated with a linear interpolation as the point
	 * between y1 and y2 with <code>y1 <= Y <= y2</code>. If y1 or y2 could not be
	 * found, then the result is null.<br>
	 * 
	 * @return Y value or null if none found
	 * @see #extrapolateY(double, Fit)
	 */
	private Double getY(double valueX) {
		for (int i = 0; i < this.temps.length && !trouve; i++) {
			if (this.temps[i] >= valueX) {
				double x1 = this.temps[i - 1];
				double x2 = this.temps[i];
				double y1 = this.estomacPourcent[i - 1];
				double y2 = this.estomacPourcent[i];
				return y2 - (x2 - valueX) * ((y2 - y1) / (x2 - x1));
			}
		}
		return null;
	}

	/**
	 * Extrapolates the Y value based upon the specified X value of the graph.<br>
	 * The value Y returned is extrapolated with the specified fit.
	 * 
	 * @param valueX X value
	 * @param fit    Fit the interpolation must rely on
	 * @return Y value interpolated
	 */
	private Double extrapolateY(double valueX, Fit fit) {
		return fit.extrapolateY(valueX);
	}

	/**
	 * Generates the dataset of the graph.
	 * 
	 * @return array in the form:
	 *         <ul>
	 *         <li><code>[i][0] -> x</code></li>
	 *         <li><code>[i][1] -> y</code></li>
	 *         </ul>
	 */
	public double[][] generateDataset() {
		double[][] dataset = new double[temps.length][2];
		for (int i = 0; i < temps.length; i++) {
			dataset[i][0] = temps[i];
			dataset[i][1] = estomacPourcent[i];
		}
		return dataset;
	}

	/**
	 * @return series for the stomach (used for the graph)
	 */
	public XYSeries getStomachSeries() {
		XYSeries serie = new XYSeries("Stomach");
		for (int i = 0; i < estomacPourcent.length; i++)
			serie.add(temps[i], estomacPourcent[i]);
		return serie;
	}

	/**
	 * @return series for the selected fit of the graph
	 */
	public XYSeries getFittedSeries() {
		double[] y = this.extrapolation.generateOrdinates(temps);
		XYSeries fittedSeries = new XYSeries(this.extrapolation.toString());
		for (int i = 0; i < temps.length; i++)
			fittedSeries.add(temps[i], y[i]);
		return fittedSeries;
	}

	/**
	 * @return number of points on the chart
	 */
	public int nbAcquisitions() {
		// number of images + the starting point
		return this.selectedImages.length + 1;
	}

	/**
	 * Sets the time when the patient ingested the food.
	 * 
	 * @param timeIngestion Time of ingestion
	 */
	public void setTimeIngestion(Date timeIngestion) {
		this.timeIngestion = timeIngestion;
	}

	/**
	 * Sets the interpolation the graph and the result must follow.
	 * 
	 * @param fit Interpolation to follow
	 */
	public void setExtrapolation(Fit fit) {
		this.extrapolation = fit;
	}

	/**
	 * @return interpolation defined
	 */
	public Fit getExtrapolation() {
		return this.extrapolation;
	}

	/**
	 * Swaps this model to be ready for the dynamic acquisition.
	 */
	public void swapToDynamic() {
		this.staticImages = Arrays.copyOf(this.selectedImages, this.selectedImages.length);
		this.selectedImages = null;
		this.roiManager.reset();
	}

	// calcule le coups du ROI
	public void calculerCoups(String roi, int indexImage, ImagePlus imp) {
		this.coups.put(roi + indexImage, Library_Quantif.getCounts(imp));
	}

	public double getCoups(String roi, int indexImage) {
		return this.coups.get(roi + indexImage);
	}

	public void setCoups(String roi, int indexImage, double d) {
		this.coups.put(roi + indexImage, d);
	}

	/**
	 * Calculates the acquisition time of the image.
	 * 
	 * @param indexImage Index of the image worked on
	 */
	public void tempsImage(int indexImage, ImagePlus imp) {
		double diff = Library_Dicom.getDateAcquisition(imp).getTime() - this.timeIngestion.getTime();
		double day = diff / (24 * 60 * 60 * 1000);
		double hour = (diff / (60 * 60 * 1000) - day * 24);
		double min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
		this.temps[indexImage + 1] = day * 24 * 60 + hour * 60 + min;
		System.out.println("Time[" + (indexImage + 1) + "] = " + this.temps[indexImage + 1]);
	}

	// pour chaque serie, on calcule le pourcentage de l'estomac, le fundus, l'antre
	// et l'intestin par rapport au total du repas
	// calcule le rapport fundus/estomac et la derivee de la courbe de variation de
	// l’estomac
	public void pourcVGImage(int indexImage) {
		this.mgs(indexImage);

		double fundusPour = ((double) this.mgs.get("Fundus_MG" + indexImage)
				/ (double) this.mgs.get("Total" + indexImage)) * 100;
		this.fundusPourcent[indexImage + 1] = fundusPour;
		double antrePour = ((double) this.mgs.get("Antre_MG" + indexImage)
				/ (double) this.mgs.get("Total" + indexImage)) * 100;
		this.antrePourcent[indexImage + 1] = antrePour;
		double estomacPour = ((double) this.mgs.get("Estomac_MG" + indexImage)
				/ (double) this.mgs.get("Total" + indexImage)) * 100;
		this.estomacPourcent[indexImage + 1] = estomacPour;
		double intestinPour = ((double) this.mgs.get("Intestin_MG" + indexImage)
				/ (double) this.mgs.get("Total" + indexImage)) * 100;
		this.intestinPourcent[indexImage + 1] = intestinPour;
		double funDevEsto = (this.fundusPourcent[indexImage + 1] / this.estomacPourcent[indexImage + 1]) * 100;
		this.funDevEsto[indexImage + 1] = funDevEsto;

		this.tempsInter[indexImage] = this.temps[indexImage + 1];
		double estoInter = ((this.estomacPourcent[indexImage] - this.estomacPourcent[indexImage + 1])
				/ (this.temps[indexImage + 1] - this.temps[indexImage])) * 30.;
		this.estoInter[indexImage] = estoInter;

		System.out.println("Result #" + indexImage);
		System.out.println(
				"EstoInter: (" + (this.estomacPourcent[indexImage]) + " - " + (this.estomacPourcent[indexImage + 1])
						+ ") / (" + (this.temps[indexImage + 1] + " - " + (this.temps[indexImage]) + ")"));
		System.out.println("EstoInter: " + estoInter);
		System.out.println("temps[(" + indexImage + "+1)" + (indexImage + 1) + "] = " + this.temps[indexImage + 1]);

		if (this.logOn) {
			IJ.log("image " + (indexImage) + ": " + " Stomach " + this.estomacPourcent[indexImage + 1] + " Intestine "
					+ this.intestinPourcent[indexImage + 1] + " Fundus " + this.fundusPourcent[indexImage + 1]
					+ " Antre " + this.antrePourcent[indexImage + 1]);
		}

	}

	// initialisation des tables de resultats
	public void initResultat() {
		this.temps = new double[this.nbAcquisitions()];
		this.estomacPourcent = new double[this.nbAcquisitions()];
		this.fundusPourcent = new double[this.nbAcquisitions()];
		this.antrePourcent = new double[this.nbAcquisitions()];
		this.intestinPourcent = new double[this.nbAcquisitions()];
		this.funDevEsto = new double[this.nbAcquisitions()];
		// -1 because the derivative is not calculated for the starting point
		this.estoInter = new double[this.nbAcquisitions() - 1];
		this.tempsInter = new double[this.nbAcquisitions() - 1];

		// Starting point is supposed to be 100 % in the stomach (fundus)
		this.temps[0] = 0.;
		this.estomacPourcent[0] = 100.;
		this.fundusPourcent[0] = 100.;
		this.antrePourcent[0] = 0.;
		this.intestinPourcent[0] = 0.;
		this.funDevEsto[0] = 100.;
	}

	/**
	 * Converts the index of the result into a readable String.
	 * 
	 * @param result Result to convert
	 * @return String corresponding to the specified result or 'Unknown' if none was
	 *         found
	 */
	public String valueOfResult(int result) {
		switch (result) {
		case RES_TIME:
			return "Time";
		case RES_STOMACH:
			return "Stomach";
		case RES_FUNDUS:
			return "Fundus";
		case RES_ANTRUM:
			return "Antrum";
		default:
			return "Unknown";
		}
	}

	/**
	 * Delivers the requested result for the specified image.<br>
	 * If the result requested is not recognized, then 0 is returned.
	 * 
	 * @param result  ID of the result to get. It must be one of {@link #RES_TIME},
	 *                {@link #RES_STOMACH}, {@link #RES_FUNDUS} and
	 *                {@link #RES_ANTRUM}
	 * @param idImage Image to get the result from
	 * @return result found<br>
	 *         or<br>
	 *         0 if the result is not recognized
	 * @see Model_Gastric#getResult(Result)
	 * @throws IllegalArgumentException if the image ID is incorrect
	 */
	public double getImageResult(int result, int idImage) throws IllegalArgumentException {
		if (idImage >= this.nbAcquisitions())
			throw new IllegalArgumentException(
					"The id (" + idImage + ") is out of bounds [" + 0 + ";" + this.nbAcquisitions() + "]");

		System.out.println("Get image result " + this.valueOfResult(result) + " with " + this.extrapolation.toString()
				+ " interpolation");

		switch (result) {
		case RES_TIME:
			return BigDecimal.valueOf(this.temps[idImage]).setScale(2, RoundingMode.HALF_UP).doubleValue();
		case RES_STOMACH:
			return BigDecimal.valueOf(this.estomacPourcent[idImage]).setScale(2, RoundingMode.HALF_UP).doubleValue();
		case RES_FUNDUS:
			return BigDecimal.valueOf(this.fundusPourcent[idImage]).setScale(2, RoundingMode.HALF_UP).doubleValue();
		case RES_ANTRUM:
			return BigDecimal.valueOf(this.antrePourcent[idImage]).setScale(2, RoundingMode.HALF_UP).doubleValue();
		default:
			return 0.;
		}
	}

	/**
	 * Delivers the requested result.<br>
	 * This method can only be used for the results that are calculated for all of
	 * the images. If you need a result specific for an image, use
	 * {@link #getImageResult(int, int)}<br>
	 * This method should only be called once all of the data was incorporated in
	 * this model.<br>
	 * 
	 * @param result Result to get
	 * @return ResultValue containing the result requested
	 * @see Model_Gastric#getImageResult(Result, int)
	 */
	public ResultValue getResult(Result result) {
		System.out.println("Get result " + result + " with " + this.extrapolation.toString() + " interpolation");
		FitType extrapolationType = null;
		switch (result) {
		case START_ANTRUM:
			return new ResultValue(result, this.getDebut("Antre"), null);
		case START_INTESTINE:
			return new ResultValue(result, this.getDebut("Intestin"), null);
		case LAG_PHASE:
			extrapolationType = null;
			Double valX = this.getX(95.);
			if (valX == null) {
				// Interpolate
				valX = this.extrapolateX(95., this.extrapolation);
				extrapolationType = this.extrapolation.getType();
			}
			return new ResultValue(result, valX, extrapolationType);
		case T_HALF:
			extrapolationType = null;
			Double valY = this.getY(50.);
			if (valY == null) {
				// Interpolate
				valY = this.extrapolateY(50., this.extrapolation);
				extrapolationType = this.extrapolation.getType();
			}
			return new ResultValue(result, valY, extrapolationType);
		default:
			throw new UnsupportedOperationException("This result is not available here!");
		}
	}

	/**
	 * Returns the retention percentage at the specified time.<br>
	 * The result might be interpolated.
	 * 
	 * @param time Time to observe in minutes
	 * @return retention time
	 */
	public ResultValue retentionAt(double time) {
		Double res = this.getY(time);
		if (res == null)
			return new ResultValue(Result.RETENTION, this.extrapolateY(time, this.extrapolation),
					this.extrapolation.getType());
		return new ResultValue(Result.RETENTION, res, null);
	}

	// permet de transferer toutes les resultats en une tableau de chaine
	public String[] resultats() {
		String[] retour = new String[4 * this.nbAcquisitions() + 12];
		// on enregistre la premiere partie des resultats
		retour[0] = "Time(min)";
		retour[1] = "Stomach(%)";
		retour[2] = "Fundus(%)";
		retour[3] = "Antrum(%)";
		for (int i = 0; i < this.nbAcquisitions(); i++) {
			retour[i * 4 + 4] = BigDecimal.valueOf(this.temps[i]).setScale(2, RoundingMode.HALF_UP).toString();
			retour[i * 4 + 5] = BigDecimal.valueOf(this.estomacPourcent[i]).setScale(2, RoundingMode.HALF_UP)
					.toString();
			retour[i * 4 + 6] = BigDecimal.valueOf(this.fundusPourcent[i]).setScale(2, RoundingMode.HALF_UP).toString();
			retour[i * 4 + 7] = BigDecimal.valueOf(this.antrePourcent[i]).setScale(2, RoundingMode.HALF_UP).toString();
		}
		// on enregistre la deuxime partie des resultats
		int j = this.nbAcquisitions() * 4 + 4;
		retour[j++] = "Start Antrum : " + this.getDebut("Antre") + " min";
		retour[j++] = "Start Intestine : " + this.getDebut("Intestin") + " min";
		// SI valeur interpolee on ajoute * a la string
		retour[j++] = "Lag Phase : " + this.getX(95.0) + (trouve ? " min" : " min*");
		retour[j++] = "T1/2 : " + this.getX(50.0) + (trouve ? " min" : " min*");
		retour[j++] = "Retention at 1h : " + this.getY(60.0) + (trouve ? "%" : "%*");
		retour[j++] = "Retention at 2h : " + this.getY(120.0) + (trouve ? "%" : "%*");
		retour[j++] = "Retention at 3h : " + this.getY(180.0) + (trouve ? "%" : "%*");
		retour[j] = "Retention at 4h : " + this.getY(240.0) + (trouve ? "%" : "%*");
		return retour;
	}

	// permet de obtenir un courbe
	public ImagePlus createGraph_1() {
		return createGraph("Fundus/Stomach (%)", new Color(0, 100, 0), "Intragastric Distribution", temps, funDevEsto,
				100.0);
	}

	public ImagePlus createGraph_2() {
		return createGraph("% meal in the interval", Color.RED, "Gastrointestinal flow", tempsInter, estoInter, 50.0);
	}

	// permet de creer un graphique avec trois courbes
	public ImagePlus createGraph_3() {
		// On cree un dataset qui contient les 3 series
		XYSeriesCollection dataset = createDatasetTrois(temps, estomacPourcent, "Stomach", fundusPourcent, "Fundus",
				antrePourcent, "Antrum");
		// On cree le graphique
		JFreeChart xylineChart = ChartFactory.createXYLineChart("", "min", "Retention (% meal)", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		// Parametres de l'affichage
		XYPlot plot = (XYPlot) xylineChart.getPlot();
		// choix du Background
		plot.setBackgroundPaint(Color.WHITE);

		// XYLineAndShapeRenderer
		// reference:
		// https://stackoverflow.com/questions/28428991/setting-series-line-style-and-legend-size-in-jfreechart
		XYLineAndShapeRenderer lineAndShapeRenderer = new XYLineAndShapeRenderer();
		// on set le renderer dans le plot
		plot.setRenderer(lineAndShapeRenderer);
		// On definit les parametre du renderer
		lineAndShapeRenderer.setDefaultLegendTextFont(new Font("", Font.BOLD, 16));
		lineAndShapeRenderer.setSeriesPaint(0, Color.RED);
		lineAndShapeRenderer.setSeriesPaint(1, new Color(0, 100, 0));
		lineAndShapeRenderer.setSeriesPaint(2, Color.BLUE);
		// Defini la taille de la courbes (epaisseur du trait)
		lineAndShapeRenderer.setSeriesStroke(0, new BasicStroke(2.0F));
		lineAndShapeRenderer.setSeriesStroke(1, new BasicStroke(2.0F));
		lineAndShapeRenderer.setSeriesStroke(2, new BasicStroke(2.0F));

		// XAxis
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		// Limites de l'axe X
		domainAxis.setRange(0.00, 360.00);
		// Pas de l'axe X
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
		// On ne met pas de grille sur la courbe
		plot.setDomainGridlinesVisible(false);
		// On cree la buffered image de la courbe et on envoie dans une ImagePlus
		BufferedImage buff = xylineChart.createBufferedImage(640, 512);
		ImagePlus courbe = new ImagePlus("", buff);

		return courbe;
	}

	public ImagePlus montage(ImageStack stack) {
		MontageMaker mm = new MontageMaker();
		ImagePlus imp = new ImagePlus("Resultats Vidange Gastrique", stack);
		imp = mm.makeMontage2(imp, 2, 2, 0.5, 1, 4, 1, 10, false);
		imp.setTitle("Resultats " + this.studyName);
		return imp;
	}

	@Override
	public void calculerResultats() {
		// Set fit
		this.setExtrapolation(new LinearFit(this.generateDataset()));
	}
}