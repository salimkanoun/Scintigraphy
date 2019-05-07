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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

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
		RES_TIME("Time", Unit.MINUTES),
		RES_STOMACH("Stomach", Unit.PERCENTAGE),
		RES_FUNDUS("Fundus", Unit.PERCENTAGE),
		RES_ANTRUM("Antrum", Unit.PERCENTAGE),
		START_ANTRUM("Start antrum", Unit.MINUTES),
		START_INTESTINE("Start intestine", Unit.MINUTES),
		LAG_PHASE("Lag phase", Unit.MINUTES),
		T_HALF("T 1/2", Unit.PERCENTAGE),
		RETENTION("Retention", Unit.PERCENTAGE);

		public enum Unit {
			PERCENTAGE("%"), MINUTES("h:m:s");
			private String s;

			private Unit(String s) {
				this.s = s;
			}

			@Override
			public String toString() {
				return this.s;
			}
		}

		private String s;
		private Unit unit;

		private Result(String s, Unit unit) {
			this.s = s;
			this.unit = unit;
		}

		/**
		 * @return unit of this result
		 */
		public Unit getUnit() {
			return this.unit;
		}

		/**
		 * @return name of this result
		 */
		public String getName() {
			return this.s;
		}

		@Override
		public String toString() {
			return this.s;
		}

		public static Result[] imageResults() {
			return new Result[] { RES_TIME, RES_STOMACH, RES_FUNDUS, RES_ANTRUM };
		}

		public static Result[] globalResults() {
			return new Result[] { START_ANTRUM, START_INTESTINE, LAG_PHASE, T_HALF, RETENTION };
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
		 * Adjusts the value returned by this result.<br>
		 * The unit of this result is not displayed.<br>
		 * For example, if this value is a time, then this method will format the value
		 * like this: '01:20:34'.<br>
		 * 
		 * @return formatted value for this result
		 */
		public String value() {
			if (this.type.getUnit() == Result.Unit.MINUTES)
				return this.displayAsTime();
			return this.notNegative();
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

		/**
		 * Returns the value of this result rounded at 2 decimals.<br>
		 * If this result is extrapolated, then a start '(*)' is added at the end of the
		 * result.
		 * 
		 * @return rounded value for this result (2 decimals)
		 */
		public String roundedValue() {
			return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toString()
					+ (isExtrapolated() ? "(*)" : "");
		}

		/**
		 * Returns the value of this result as a time considering the value in minutes.
		 * 
		 * @return time value for this result
		 */
		public String displayAsTime() {
			int seconds = (int) ((value - (double) ((int) value)) * 60.);
			int minutes = (int) (value % 60.);
			int hours = (int) (value / 60.);

			StringBuilder s = new StringBuilder();
			// Hours
			if (hours > 0) {
				if (hours < 10)
					s.append(0);
				s.append(hours);
				s.append(':');
			}

			// Minutes
			if (minutes < 10)
				s.append(0);
			s.append(minutes);
			s.append(':');

			// Seconds
			if (seconds < 10)
				s.append(0);
			s.append(seconds);

			return s.toString();
		}
	}

	// == REFACTORING ==
	public static final Region REGION_STOMACH = new Region("Stomach"), REGION_ANTRE = new Region("Antre"),
			REGION_FUNDUS = new Region("Fundus"), REGION_INTESTINE = new Region("Intestine"),
			REGION_ALL = new Region("Total");

	private class Data implements Comparable<Data> {
		private ImageSelection ims;
		private Map<Integer, Double>[] organs;
		public Double time;

		public static final int STOMACH = 0, ANTRE = 1, FUNDUS = 2, INTESTINE = 3, ALL = 4, TOTAL_ORGANS = 5;
		public static final int ANT_COUNTS = 0, POST_COUNTS = 1, GEO_AVEREAGE = 2, PERCENTAGE = 3, DERIVATIVE = 4,
				CORRELATION = 5;

		public Data(ImageSelection ims) {
			this.ims = ims;
			this.organs = new Map[TOTAL_ORGANS];
			for (int i = 0; i < TOTAL_ORGANS; i++)
				this.organs[i] = new HashMap<>();
			this.time = null;
		}

		public void setValue(Region region, int key, double value) {
			this.organs[indexFromRegion(region)].put(key, value);
		}

		public double getValue(Region region, int key) {
			return this.organs[indexFromRegion(region)].get(key);
		}

		public int indexFromRegion(Region region) {
			if (region == REGION_STOMACH)
				return STOMACH;
			if (region == REGION_ANTRE)
				return ANTRE;
			if (region == REGION_FUNDUS)
				return FUNDUS;
			if (region == REGION_INTESTINE)
				return INTESTINE;
			if (region == REGION_ALL)
				return ALL;

			throw new IllegalArgumentException("This region is not requested in this model");
		}

		@Override
		public int compareTo(Data o) {
			return Library_Dicom.getDateAcquisition(this.ims.getImagePlus())
					.compareTo(Library_Dicom.getDateAcquisition(o.ims.getImagePlus()));
		}

		@Override
		public String toString() {
			return this.ims.getImagePlus().getTitle();
		}
	}

	private Map<Integer, Data> results;
	private Data time0;

	private Date timeIngestion;

	private Fit extrapolation;

	private double[] times, timesDerivative;

	public Model_Gastric(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
		Prefs.useNamesAsLabels = true;

		this.results = new HashMap<>();
	}

	private void computeGeometricalAverages(ImageSelection ims) {
		Data data = this.results.get(ims.hashCode());
		if (data == null)
			throw new NoSuchElementException(
					"No data has been set for this image (" + ims.getImagePlus().getTitle() + ")");

		// Antre
		Double valueAnt = data.getValue(REGION_ANTRE, Data.ANT_COUNTS);
		Double valuePost = data.getValue(REGION_ANTRE, Data.POST_COUNTS);
		data.setValue(REGION_ANTRE, Data.GEO_AVEREAGE, Library_Quantif.moyGeom(valueAnt, valuePost));

		// Intestine
		valueAnt = data.getValue(REGION_INTESTINE, Data.ANT_COUNTS);
		valuePost = data.getValue(REGION_INTESTINE, Data.POST_COUNTS);
		Double geoIntestine = Library_Quantif.moyGeom(valueAnt, valuePost);
		data.setValue(REGION_INTESTINE, Data.GEO_AVEREAGE, geoIntestine);

		// Fundus
		valueAnt = data.getValue(REGION_FUNDUS, Data.ANT_COUNTS);
		valuePost = data.getValue(REGION_FUNDUS, Data.POST_COUNTS);
		data.setValue(REGION_FUNDUS, Data.GEO_AVEREAGE, Library_Quantif.moyGeom(valueAnt, valuePost));

		// Stomach
		Double valueFundus = data.getValue(REGION_FUNDUS, Data.GEO_AVEREAGE);
		Double valueAntre = data.getValue(REGION_ANTRE, Data.GEO_AVEREAGE);
		Double geoStomach = valueFundus + valueAntre;
		data.setValue(REGION_STOMACH, Data.GEO_AVEREAGE, geoStomach);

		// Total
		data.setValue(REGION_ALL, Data.GEO_AVEREAGE, geoStomach + geoIntestine);
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
	private double getDebut(Region region) {
		if (region != REGION_ANTRE && region != REGION_INTESTINE)
			throw new IllegalArgumentException("The region " + region + " is not supported here!");

		for (Data data : this.generatesDataOrdered())
			if (data.getValue(region, Data.PERCENTAGE) > 0)
				return data.time;

		throw new NoSuchElementException("No data found, please first use the calculateCounts method before!");
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
		double[] stomachPercentage = this.getResultAsArray(REGION_STOMACH, Data.PERCENTAGE);

		for (int i = 1; i < stomachPercentage.length; i++) {
			if (stomachPercentage[i] <= valueY) {
				double x1 = times[i - 1];
				double x2 = times[i];
				double y1 = stomachPercentage[i - 1];
				double y2 = stomachPercentage[i];
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
		System.out.println("Times: " + Arrays.toString(times));
		double[] stomachPercentage = this.getResultAsArray(REGION_STOMACH, Data.PERCENTAGE);
		for (int i = 0; i < times.length; i++) {
			if (times[i] >= valueX) {
				double x1 = times[i - 1];
				double x2 = times[i];
				double y1 = stomachPercentage[i - 1];
				double y2 = stomachPercentage[i];
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

	// =============================
	// REFACTORED
	// =============================

	private List<Data> generatesDataOrdered() {
		List<Data> orderedData = new ArrayList<>(this.results.values());
		Collections.sort(orderedData);
		orderedData.add(0, time0);
		return orderedData;
	}

	public Region[] getAllRegions() {
		return new Region[] { REGION_STOMACH, REGION_ANTRE, REGION_FUNDUS, REGION_INTESTINE };
	}

	public void calculateCounts(Region region) {
		// Check region is part of requested regions for this model
		if (!Arrays.stream(this.getAllRegions()).anyMatch(r -> r == region))
			throw new IllegalArgumentException("This region is not requested in this model");

		ImageSelection ims = region.getImage();
		ImagePlus imp = ims.getImagePlus();

		// Create data if not existing
		Data data = this.results.get(ims.hashCode());
		if (data == null) {
			data = new Data(ims);
			this.results.put(ims.hashCode(), data);
		}

		// Find orientation (ant or post)
		int key;
		if (region.getState().getFacingOrientation() == Orientation.ANT)
			key = Data.ANT_COUNTS;
		else
			key = Data.POST_COUNTS;

		// Save value
		imp.setSlice(region.getState().getSlice());
		imp.setRoi(region.getRoi());
		data.setValue(region, key, Library_Quantif.getCounts(imp));
		data.time = this.calculateDeltaTime(Library_Dicom.getDateAcquisition(imp));
	}

	public double calculateDeltaTime(Date time) {
		return (time.getTime() - this.timeIngestion.getTime()) / 1000. / 60.;
	}

	public void generatesTimes() {
		this.times = new double[this.nbAcquisitions()];
		this.timesDerivative = new double[this.nbAcquisitions() - 1];

		int i = 0;
		for (Data data : this.generatesDataOrdered()) {
			if (data == time0)
				times[i] = 0.;
			else {
				Date time = Library_Dicom.getDateAcquisition(data.ims.getImagePlus());
				times[i] = this.calculateDeltaTime(time);
				if (data.time == null)
					this.calculateDeltaTime(time);
			}
			if (i > 0)
				this.timesDerivative[i - 1] = times[i];
			i++;
		}
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
		double[][] dataset = new double[times.length][2];
		Iterator<Data> it = this.generatesDataOrdered().iterator();
		int i = 0;
		while (it.hasNext()) {
			dataset[i][0] = times[i];
			dataset[i][1] = it.next().getValue(REGION_STOMACH, Data.PERCENTAGE);
			i++;
		}
		return dataset;
	}

	/**
	 * @return series for the stomach (used for the graph)
	 */
	public XYSeries getStomachSeries() {
		XYSeries serie = new XYSeries("Stomach");
		double[][] dataset = this.generateDataset();
		for (int i = 0; i < dataset.length; i++) {
			serie.add(dataset[i][0], dataset[i][1]);
		}
		return serie;
	}

	/**
	 * @return series for the selected fit of the graph
	 */
	public XYSeries getFittedSeries() {
		double[] y = this.extrapolation.generateOrdinates(times);
		XYSeries fittedSeries = new XYSeries(this.extrapolation.toString());
		for (int i = 0; i < times.length; i++)
			fittedSeries.add(times[i], y[i]);
		return fittedSeries;
	}

	/**
	 * @return number of points on the chart
	 */
	public int nbAcquisitions() {
		// number of images + the starting point
		return this.results.size() + (this.time0 != null ? 1 : 0);
	}

	/**
	 * Sets the time when the patient ingested the food.
	 * 
	 * @param timeIngestion Time of ingestion
	 */
	public void setTimeIngestion(Date timeIngestion) {
		this.timeIngestion = timeIngestion;
	}

	public Date getTimeIngestion() {
		return this.timeIngestion;
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
	 * Current extrapolation selected.
	 * 
	 * @return
	 */
	public FitType getCurrentExtrapolation() {
		return this.extrapolation.getType();
	}

	/**
	 * @return interpolation defined
	 */
	public Fit getExtrapolation() {
		return this.extrapolation;
	}

	public double getCounts(ImageSelection ims, Region region, Orientation orientation) {
		Data data = this.results.get(ims.hashCode());
		if (data == null)
			throw new NoSuchElementException(
					"No data has been set for this image (" + ims.getImagePlus().getTitle() + ")");

		return data.getValue(region, orientation == Orientation.ANT ? Data.ANT_COUNTS : Data.POST_COUNTS);
	}

	public void activateTime0() {
		this.time0 = new Data(null);
		this.time0.time = 0.;
		this.time0.setValue(REGION_STOMACH, Data.PERCENTAGE, 100.);
		this.time0.setValue(REGION_FUNDUS, Data.PERCENTAGE, 100.);
		this.time0.setValue(REGION_ANTRE, Data.PERCENTAGE, 0.);
		this.time0.setValue(REGION_INTESTINE, Data.PERCENTAGE, 0.);

		this.time0.setValue(REGION_FUNDUS, Data.CORRELATION, 100.);
	}

	private void forceDataValue(ImageSelection ims, Region region, int key, double value, Date time) {
		Data data = this.results.get(ims.hashCode());
		if (data == null) {
			data = new Data(ims);
			this.results.put(ims.hashCode(), data);
		}

		data.setValue(region, key, value);
		if (time != null)
			data.time = this.calculateDeltaTime(time);
	}

	public void forcePercentageDataValue(ImageSelection ims, Region region, double value, Date time) {
		this.forceDataValue(ims, region, Data.PERCENTAGE, value, time);
	}

	public void forceCorrelationDataValue(ImageSelection ims, Region region, double value, Date time) {
		this.forceDataValue(ims, region, Data.CORRELATION, value, time);
	}

	public void forceCountsDataValue(ImageSelection ims, Region region, double value, Date time) {
		this.forceDataValue(ims, region,
				region.getState().getFacingOrientation() == Orientation.ANT ? Data.ANT_COUNTS : Data.POST_COUNTS, value,
				time);
	}

	public void computeData(ImageSelection ims, ImageSelection previousImage) {
		this.computeGeometricalAverages(ims);

		Data data = this.results.get(ims.hashCode());
		if (data == null)
			throw new NoSuchElementException(
					"No data has been set for this image (" + ims.getImagePlus().getTitle() + ")");

		double fundusPercentage = data.getValue(REGION_FUNDUS, Data.GEO_AVEREAGE)
				/ data.getValue(REGION_ALL, Data.GEO_AVEREAGE) * 100.;
		data.setValue(REGION_FUNDUS, Data.PERCENTAGE, fundusPercentage);

		double antrePercentage = data.getValue(REGION_ANTRE, Data.GEO_AVEREAGE)
				/ data.getValue(REGION_ALL, Data.GEO_AVEREAGE) * 100.;
		data.setValue(REGION_ANTRE, Data.PERCENTAGE, antrePercentage);

		double stomachPercentage = data.getValue(REGION_STOMACH, Data.GEO_AVEREAGE)
				/ data.getValue(REGION_ALL, Data.GEO_AVEREAGE) * 100.;
		data.setValue(REGION_STOMACH, Data.PERCENTAGE, stomachPercentage);

		double intestinePercentage = data.getValue(REGION_INTESTINE, Data.GEO_AVEREAGE)
				/ data.getValue(REGION_ALL, Data.GEO_AVEREAGE) * 100.;
		data.setValue(REGION_INTESTINE, Data.PERCENTAGE, intestinePercentage);

		double fundusDerivative = data.getValue(REGION_FUNDUS, Data.PERCENTAGE)
				/ data.getValue(REGION_STOMACH, Data.PERCENTAGE) * 100.;
		data.setValue(REGION_FUNDUS, Data.CORRELATION, fundusDerivative);

		if (previousImage != null) {
			Data previousData = this.results.get(previousImage.hashCode());
			if (previousData != null) {
				double stomachDerivative = (previousData.getValue(REGION_STOMACH, Data.PERCENTAGE)
						- data.getValue(REGION_STOMACH, Data.PERCENTAGE))
						/ (this.calculateDeltaTime(Library_Dicom.getDateAcquisition(ims.getImagePlus())) - this
								.calculateDeltaTime(Library_Dicom.getDateAcquisition(previousImage.getImagePlus())))
						* 30.;
				previousData.setValue(REGION_STOMACH, Data.DERIVATIVE, stomachDerivative);
			} else {
				System.err.println("Careful: no data found for the previous image specified ("
						+ previousImage.getImagePlus().getTitle() + ")");
			}
		}
	}

	/**
	 * Delivers the requested result for the specified image
	 * 
	 * @param result  Result to get, it must be one of TIME, STOMACH, FUNDUS, ANTRUM
	 * @param idImage Image to get the result from
	 * @return result found
	 * @see Model_Gastric#getResult(Result)
	 * @throws IllegalArgumentException if the image ID or the result is incorrect
	 */
	public ResultValue getImageResult(Result result, int indexImage) throws IllegalArgumentException {
		Data data = this.generatesDataOrdered().get(indexImage);
		if (data == null)
			throw new NoSuchElementException(
					"No data has been set for this image#" + indexImage);

		switch (result) {
		case RES_TIME:
			return new ResultValue(result, BigDecimal.valueOf(data.time)
					.setScale(2, RoundingMode.HALF_UP).doubleValue(), null);
		case RES_STOMACH:
			return new ResultValue(result,
					BigDecimal.valueOf(data.getValue(REGION_STOMACH, Data.PERCENTAGE))
							.setScale(2, RoundingMode.HALF_UP).doubleValue(),
					null);
		case RES_FUNDUS:
			return new ResultValue(result,
					BigDecimal.valueOf(data.getValue(REGION_FUNDUS, Data.PERCENTAGE))
							.setScale(2, RoundingMode.HALF_UP).doubleValue(),
					null);
		case RES_ANTRUM:
			return new ResultValue(result,
					BigDecimal.valueOf(data.getValue(REGION_ANTRE, Data.PERCENTAGE))
							.setScale(2, RoundingMode.HALF_UP).doubleValue(),
					null);
		default:
			throw new IllegalArgumentException("The result " + result + " is not available here!");
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
		FitType extrapolationType = null;
		switch (result) {
		case START_ANTRUM:
			return new ResultValue(result, this.getDebut(REGION_ANTRE), null);
		case START_INTESTINE:
			return new ResultValue(result, this.getDebut(REGION_INTESTINE), null);
		case LAG_PHASE:
			extrapolationType = null;
			Double valX = this.getX(95.);
			if (valX == null) {
				// Extrapolate
				valX = this.extrapolateX(95., this.extrapolation);
				extrapolationType = this.extrapolation.getType();
			}
			return new ResultValue(result, valX, extrapolationType);
		case T_HALF:
			extrapolationType = null;
			Double valY = this.getY(50.);
			if (valY == null) {
				// Extrapolate
				valY = this.extrapolateY(50., this.extrapolation);
				extrapolationType = this.extrapolation.getType();
			}
			return new ResultValue(result, valY, extrapolationType);
		default:
			throw new UnsupportedOperationException("The result " + result + " is not available here!");
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

	public double[] getResultAsArray(Region region, int key) {
		double[] res;
		if (key == Data.DERIVATIVE)
			res = new double[this.nbAcquisitions() - 1];
		else
			res = new double[this.nbAcquisitions()];

		int i = 0;
		for (Data data : this.generatesDataOrdered())
			if (key != Data.DERIVATIVE || (key == Data.DERIVATIVE && i != 0))
				res[i++] = data.getValue(region, key);
		return res;
	}

	// permet de obtenir un courbe
	public ImagePlus createGraph_1() {
		return createGraph("Fundus/Stomach (%)", new Color(0, 100, 0), "Intragastric Distribution", times,
				this.getResultAsArray(REGION_FUNDUS, Data.CORRELATION), 100.0);
	}

	public ImagePlus createGraph_2() {
		return createGraph("% meal in the interval", Color.RED, "Gastrointestinal flow", timesDerivative,
				this.getResultAsArray(REGION_STOMACH, Data.DERIVATIVE), 50.0);
	}

	// permet de creer un graphique avec trois courbes
	public ImagePlus createGraph_3() {
		// On cree un dataset qui contient les 3 series
		XYSeriesCollection dataset = createDatasetTrois(times, this.getResultAsArray(REGION_STOMACH, Data.PERCENTAGE),
				"Stomach", this.getResultAsArray(REGION_FUNDUS, Data.PERCENTAGE), "Fundus",
				this.getResultAsArray(REGION_ANTRE, Data.PERCENTAGE), "Antrum");
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
		this.generatesTimes();
		// Set fit
		this.setExtrapolation(new LinearFit(this.generateDataset()));
	}

	public void deactivateTime0() {
		this.time0 = null;
	}
}