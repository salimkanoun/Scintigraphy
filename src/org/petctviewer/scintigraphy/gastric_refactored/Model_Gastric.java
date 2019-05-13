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
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.MontageMaker;

/**
 * Model of the Gastric Scintigraphy.
 * 
 * @author Xie PING
 * @author Titouan QUÉMA - refactoring, JavaDoc
 *
 */
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

	public static final Region REGION_STOMACH = new Region("Stomach"), REGION_ANTRE = new Region("Antre"),
			REGION_FUNDUS = new Region("Fundus"), REGION_INTESTINE = new Region("Intestine"),
			REGION_ALL = new Region("Total");

	/**
	 * This class stores the data measured or calculated for each region of the
	 * model.<br>
	 * The natural order of this class depends of the chronological order of the
	 * images.
	 * 
	 * @author Titouan QUÉMA
	 *
	 */
	private class Data implements Comparable<Data> {
		private ImageState state;
		private Map<Integer, Double>[] organs;
		private double time;

		public static final int STOMACH = 0, ANTRE = 1, FUNDUS = 2, INTESTINE = 3, ALL = 4, TOTAL_ORGANS = 5;
		public static final int ANT_COUNTS = 0, POST_COUNTS = 1, GEO_AVEREAGE = 2, PERCENTAGE = 3, DERIVATIVE = 4,
				CORRELATION = 5;

		/**
		 * Instantiates a new data. The image state of a data should be unique (in this
		 * model).
		 * 
		 * @param state Unique state for this data (if null, this data is user-made)
		 * @param time  Time in minutes after the ingestion time
		 */
		public Data(ImageState state, double time) {
			this.state = state;
			this.organs = new Map[TOTAL_ORGANS];
			for (int i = 0; i < TOTAL_ORGANS; i++)
				this.organs[i] = new HashMap<>();
			this.time = time;
		}

		/**
		 * Converts a region into an index for the array.
		 * 
		 * @param region Region to convert
		 * @return index of the region in the array
		 * @throws IllegalArgumentException if the region is not part of the requested
		 *                                  regions of the {@link Model_Gastric}
		 */
		private int indexFromRegion(Region region) throws IllegalArgumentException {
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

		/**
		 * This method reset the time of this data
		 * 
		 * @param time Time elapsed in minutes since the ingestion
		 */
		public void setTime(double time) {
			this.time = time;
		}

		/**
		 * @return time in minutes for this data
		 */
		public double getTime() {
			return this.time;
		}

		/**
		 * Sets a new value for the specified key.
		 * 
		 * @param region Region on which the value will be applied
		 * @param key    Key for the value
		 * @param value  Value to insert
		 */
		public void setValue(Region region, int key, double value) {
			this.organs[indexFromRegion(region)].put(key, value);
		}

		/**
		 * Gets the value previously stored.<br>
		 * Be careful, this method will throw a NullPointerException if no data was
		 * found for the specified key.
		 * 
		 * @param region Region on which the value must be retrieved
		 * @param key    Key of the value to retrieve
		 * @return value stored for the specified key of the specified region
		 */
		public double getValue(Region region, int key) {
			return this.organs[indexFromRegion(region)].get(key);
		}

		@Override
		public int compareTo(Data o) {
			double res = this.time - o.time;
			if (res > 0)
				return 1;
			if (res < 0)
				return -1;
			return 0;
		}

		@Override
		public String toString() {
			return state.getImage().getImagePlus().getTitle();
		}
	}

	private Map<Integer, Data> results;
	/**
	 * Fictional data representing the first acquisition.
	 */
	private Data time0;

	/**
	 * Time when the ingestion started.
	 */
	private Date timeIngestion;

	/**
	 * Extrapolation used to fit the values.
	 */
	private Fit extrapolation;

	/**
	 * Times calculated.
	 */
	private double[] times, timesDerivative;

	public Model_Gastric(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
		Prefs.useNamesAsLabels = true;

		this.results = new HashMap<>();
	}

	/**
	 * Retrieves the image described by the specified image state.<br>
	 * The image can be retrieved from this model or from this image state.
	 * 
	 * @param state State describing a data
	 * @return image retrieved from the specified state (null can be returned)
	 * @throws IllegalArgumentException if the ID of the ImageState is different
	 *                                  than {@link ImageState#ID_CUSTOM_IMAGE} or a
	 *                                  positive value
	 */
	private ImageSelection imageFromState(ImageState state) {
		if (state.getIdImage() == ImageState.ID_CUSTOM_IMAGE)
			return state.getImage();

		if (state.getIdImage() >= 0)
			return this.selectedImages[state.getIdImage()];

		throw new IllegalArgumentException("ID " + state.getIdImage() + " is not applicable here");
	}

	/**
	 * Creates a hash from the specified ImageState.
	 * 
	 * @param state ImageState to hash
	 * @return hash of the state
	 */
	private int hashState(ImageState state) {
		return state.getImage().hashCode();
	}

	/**
	 * Computes the geometrical average of each region of the data found.<br>
	 * The average is made with the {@link Data#ANT_COUNTS} and the
	 * {@link Data#POST_COUNTS} data and will generate the {@link Data#GEO_AVEREAGE}
	 * for every region.
	 * <p>
	 * So the data ANT_COUNTS and POST_COUNTS <b>must</b> be defined for all regions
	 * (except REGION_ALL).
	 * </p>
	 * Be careful: this method assumes that the specified state is correct and will
	 * not throw any exception
	 * 
	 * @param state ImageState from which the data will be retrieved
	 * @throws NoSuchElementException if no data is linked to the specified state
	 */
	private void computeGeometricalAverages(ImageState state) throws NoSuchElementException {
		Data data = this.results.get(hashState(state));

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

	/**
	 * Generates a dataset of 1 series with the specified arguments.
	 * 
	 * @param resX  Values of the points for the X axis
	 * @param resY  Values of the points for the Y axis
	 * @param titre Title of the series
	 * @return generated dataset with 1 series
	 */
	private static XYSeriesCollection createDatasetUn(double[] resX, double[] resY, String titre) {
		XYSeries courbe = new XYSeries(titre);
		for (int i = 0; i < resX.length; i++)
			courbe.add(resX[i], resY[i]);
		return new XYSeriesCollection(courbe);
	}

	/**
	 * Generates a dataset of 3 series with the specified arguments.
	 * 
	 * @param resX   Values of the points for the X axis for all series
	 * @param resY1  Values of the points for the Y axis of the first series
	 * @param titre1 Title of the first series
	 * @param resY2  Values of the points for the Y axis of the second series
	 * @param titre2 Title of the second series
	 * @param resY3  Values of the points for the Y axis of the third series
	 * @param titre3 Title of the thrid series
	 * @return generated dataset with the 3 series
	 */
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
	/**
	 * Finds the starting time for the Antre or Intestine region (only).
	 * 
	 * @param region Region to find the starting time
	 * @return starting time for the region
	 * @throws IllegalArgumentException if the region is different than ANTRE or
	 *                                  INTESTINE
	 * @throws NoSuchElementException   if no data was found for the specified
	 *                                  region
	 */
	private double getDebut(Region region) throws IllegalArgumentException, NoSuchElementException {
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
	 * between x1 and x2 with <code>x1 <= X <= x2</code>.<br>
	 * If x1 or x2 could not be found, then the result is null.<br>
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

	/**
	 * Generates a list of all data from this model ordered by time.<br>
	 * This method also includes the fictional time 0 if necessary.
	 * 
	 * @return all data ordered by time
	 */
	private List<Data> generatesDataOrdered() {
		List<Data> orderedData = new ArrayList<>(this.results.values());
		Collections.sort(orderedData);
		if (time0 != null)
			orderedData.add(0, time0);
		return orderedData;
	}

	/**
	 * Creates an array with all of the requested results ordered chronologically.
	 * 
	 * @param region Region to get the result from
	 * @param key    Key of the results to place in the array
	 * @return array of all data for the requested key result
	 */
	private double[] getResultAsArray(Region region, int key) {
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

	/**
	 * Evaluates the difference between the time of the data associated with the
	 * specified state and the ingestion's time of this model.
	 * 
	 * @param state State associated with the data to retrieve
	 * @return difference of time expressed in minutes
	 */
	private double evaluateTime(ImageState state) {
		return this.calculateDeltaTime(Library_Dicom.getDateAcquisition(state.getImage().getImagePlus()));
	}

	/**
	 * Calculates the time between the specified time and the ingestion's time.
	 * 
	 * @param time Time to calculate the difference with
	 * @return difference of time expressed in minutes (negative value if the
	 *         specified time is before the ingestion's time)
	 */
	private double calculateDeltaTime(Date time) {
		return (time.getTime() - this.timeIngestion.getTime()) / 1000. / 60.;
	}

	/**
	 * Change a data value.<br>
	 * This method is designed to be used by public methods to allow controlled
	 * modifications of the data.
	 * 
	 * @param region Region to edit
	 * @param key    Key of the value to set
	 * @param value  Value to force
	 */
	private void forceDataValue(Region region, int key, double value) {
		ImageState state = region.getState();
		Data data = this.createOrRetrieveData(state);

		data.setValue(region, key, value);
	}

	/**
	 * Calculates the percentage of the specified region for the specified data
	 * using the geometrical averages.
	 * 
	 * @return percentage using geometrical averages
	 */
	private double calculateGeoPercentage(Data data, Region region) {
		return data.getValue(region, Data.GEO_AVEREAGE) / data.getValue(REGION_ALL, Data.GEO_AVEREAGE) * 100.;
	}

	/**
	 * Calculates the percentage of the specified region for the specified data
	 * using number of counts in the Ant orientation.
	 * 
	 * @return percentage using number of counts in the Ant orientation
	 */
	private double calculateAntPercentage(Data data, Region region) {
		return data.getValue(region, Data.ANT_COUNTS) / data.getValue(REGION_ALL, Data.ANT_COUNTS) * 100.;
	}

	private Data createOrRetrieveData(ImageState state) {
		// Set the image in the state
		if (state.getIdImage() != ImageState.ID_CUSTOM_IMAGE)
			state.specifieImage(this.selectedImages[state.getIdImage()]);

		// Retrieve data
		Data data = this.results.get(hashState(state));

		// Create data if not existing
		if (data == null) {
			data = new Data(state, this.evaluateTime(state));
			this.results.put(hashState(state), data);
		}

		return data;
	}

	/**
	 * @return all regions required by this model
	 */
	public Region[] getAllRegions() {
		return new Region[] { REGION_STOMACH, REGION_ANTRE, REGION_FUNDUS, REGION_INTESTINE };
	}

	/**
	 * Calculates the counts of the specified region.<br>
	 * The region must be previously inflated with the correct state.<br>
	 * This method takes care of all necessary operations to do on the ImagePlus or
	 * the RoiManager.<br>
	 * This method will create a new data for each new ImageSelection encountered.
	 * 
	 * @param region Region to calculate
	 * @throws IllegalArgumentException if the region is not part of the requested
	 *                                  regions for this model
	 */
	public void calculateCounts(Region region) throws IllegalArgumentException {
		// Check region is part of requested regions for this model
		if (!Arrays.stream(this.getAllRegions()).anyMatch(r -> r == region))
			throw new IllegalArgumentException("This region is not requested in this model");

		ImageState state = region.getState();
		ImageSelection ims = imageFromState(state);
		ImagePlus imp = ims.getImagePlus();

		System.out.println("Adding new counts for region " + region);

		// Create data if not existing
		Data data = this.createOrRetrieveData(state);

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

		System.out.println("Data inserted, now having " + this.nbAcquisitions() + " acquisitions (with "
				+ this.results.size() + " actual results)");
		System.out.println("====");
	}

	/**
	 * Generates arrays of times used by the graphs.<br>
	 * This method must be called before generating datasets for the graphs.
	 */
	public void generatesTimes() {
		this.times = new double[this.nbAcquisitions()];
		this.timesDerivative = new double[this.nbAcquisitions() - 1];

		int i = 0;
		for (Data data : this.generatesDataOrdered()) {
			times[i] = data.getTime();
			if (i > 0)
				this.timesDerivative[i - 1] = times[i];
			i++;
		}
	}

	/**
	 * Generates the dataset for the graph of the stomach retention.
	 * 
	 * @return array in the form:
	 *         <ul>
	 *         <li><code>[i][0] -> x</code></li>
	 *         <li><code>[i][1] -> y</code></li>
	 *         </ul>
	 */
	public double[][] generateStomachDataset() {
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
		double[][] dataset = this.generateStomachDataset();
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
	 * This method returns the number of data this model possesses. If a fictional
	 * time 0 has been activated, then it will be counted as an acquisition.
	 * 
	 * @return number of data of this model
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

		// Refresh all data times
		for (Data data : this.results.values()) {
			data.setTime(this.evaluateTime(data.state));
		}
	}

	/**
	 * @return time when the patient ingested the food
	 */
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
	 * Current extrapolation defined.
	 * 
	 * @return type of the current extrapolation
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

	/**
	 * Retrieves the number of counts for the specified region and orientation.<br>
	 * The orientation accepted is only Ant or Post. If any other orientation is
	 * passed, then the result will be returned as if it was a Post orientation.<br>
	 * To use this method, data must be previously entered in this model.
	 * 
	 * @param region      Region to get the counts from
	 * @param orientation Orientation for the counts
	 * @return value of the counts for the specified region and orientation
	 * @throws NoSuchElementException if no data could be retrieved for the
	 *                                specified region
	 */
	public double getCounts(Region region, Orientation orientation) throws NoSuchElementException {
		Data data = this.results.get(hashState(region.getState()));
		if (data == null)
			throw new NoSuchElementException("No data has been set for this image ("
					+ region.getState().getImage().getImagePlus().getTitle() + ")");

		return data.getValue(region, orientation == Orientation.ANT ? Data.ANT_COUNTS : Data.POST_COUNTS);
	}

	/**
	 * Activates the fictional time 0 representing the moment when the fundus
	 * contains all of the food.
	 */
	public void activateTime0() {
		this.time0 = new Data(null, 0.);
		this.time0.time = 0.;
		this.time0.setValue(REGION_STOMACH, Data.PERCENTAGE, 100.);
		this.time0.setValue(REGION_FUNDUS, Data.PERCENTAGE, 100.);
		this.time0.setValue(REGION_ANTRE, Data.PERCENTAGE, 0.);
		this.time0.setValue(REGION_INTESTINE, Data.PERCENTAGE, 0.);

		this.time0.setValue(REGION_FUNDUS, Data.CORRELATION, 100.);
	}

	/**
	 * Force the insertion of a percentage value in the data for the specified
	 * region.
	 * 
	 * @param region Region to set this value on
	 * @param value  Value to force
	 */
	public void forcePercentageDataValue(Region region, double value) {
		this.forceDataValue(region, Data.PERCENTAGE, value);
	}

	/**
	 * Force the insertion of a correlation value in the data for the specified
	 * region.
	 * 
	 * @param region Region to set this value on
	 * @param value  Value to force
	 */
	public void forceCorrelationDataValue(Region region, double value) {
		this.forceDataValue(region, Data.CORRELATION, value);
	}

	/**
	 * Force the insertion of the number of counts in the data for the specified
	 * region.
	 * 
	 * @param region Region to set this value on
	 * @param value  Value to force
	 */
	public void forceCountsDataValue(Region region, double value) {
		this.forceDataValue(region,
				region.getState().getFacingOrientation() == Orientation.ANT ? Data.ANT_COUNTS : Data.POST_COUNTS,
				value);
	}

	/**
	 * Computes the data retrieved from the specified state. This method calculates
	 * the percentages for each region. This method should be used once the dynamic
	 * acquisition has been made.<br>
	 * The {@link Data#ANT_COUNTS} <b>must</b> be defined in every region (except
	 * REGION_ALL).<br>
	 * If the previous state is not null, then the derivative is calculated for the
	 * stomach.
	 * 
	 * @param state         State of the data to retrieve
	 * @param previousState State of the previous data to retrieve (in chronological
	 *                      order)
	 * @throws NoSuchElementException if no data could be retrieved from the
	 *                                specified state
	 */
	public void computeDynamicData(ImageState state, ImageState previousState) throws NoSuchElementException {
		Data data = this.results.get(hashState(state));
		if (data == null)
			throw new NoSuchElementException(
					"No data has been set for this image (" + state.getImage().getImagePlus().getTitle() + ")");

		// Compute first
		// - Stomach
		Double valueFundus = data.getValue(REGION_FUNDUS, Data.ANT_COUNTS);
		Double valueAntre = data.getValue(REGION_ANTRE, Data.ANT_COUNTS);
		Double antStomach = valueFundus + valueAntre;
		data.setValue(REGION_STOMACH, Data.ANT_COUNTS, antStomach);
		// - Total
		data.setValue(REGION_ALL, Data.ANT_COUNTS, antStomach + data.getValue(REGION_INTESTINE, Data.ANT_COUNTS));

		// Compute second
		data.setValue(REGION_FUNDUS, Data.PERCENTAGE, calculateAntPercentage(data, REGION_FUNDUS));

		data.setValue(REGION_ANTRE, Data.PERCENTAGE, calculateAntPercentage(data, REGION_ANTRE));

		data.setValue(REGION_STOMACH, Data.PERCENTAGE, calculateAntPercentage(data, REGION_STOMACH));

		data.setValue(REGION_INTESTINE, Data.PERCENTAGE, calculateAntPercentage(data, REGION_INTESTINE));

		double fundusCorrelation = data.getValue(REGION_FUNDUS, Data.PERCENTAGE)
				/ data.getValue(REGION_STOMACH, Data.PERCENTAGE) * 100.;
		data.setValue(REGION_FUNDUS, Data.CORRELATION, fundusCorrelation);

		if (previousState != null) {
			Data previousData = this.results.get(hashState(previousState));
			if (previousData != null) {
				double stomachDerivative = (previousData.getValue(REGION_STOMACH, Data.PERCENTAGE)
						- data.getValue(REGION_STOMACH, Data.PERCENTAGE))
						/ (this.calculateDeltaTime(Library_Dicom.getDateAcquisition(state.getImage().getImagePlus()))
								- this.calculateDeltaTime(
										Library_Dicom.getDateAcquisition(previousState.getImage().getImagePlus())))
						* 30.;
				previousData.setValue(REGION_STOMACH, Data.DERIVATIVE, stomachDerivative);
			} else {
				System.err.println("Careful: no data found for the previous image specified ("
						+ previousState.getImage().getImagePlus().getTitle() + ")");
			}
		}
	}

	/**
	 * Computes the data retrieved from the specified state. This method calculates
	 * the percentages for each region. This method should be used when the static
	 * acquisition has been made.<br>
	 * The {@link Data#GEO_AVEREAGE} <b>must</b> be defined in every region (except
	 * REGION_ALL).<br>
	 * If the previous state is not null, then the derivative is calculated for the
	 * stomach.
	 * 
	 * @param state         State of the data to retrieve
	 * @param previousState State of the previous data to retrieve (in chronological
	 *                      order)
	 * @throws NoSuchElementException if no data could be retrieved from the
	 *                                specified state
	 */
	public void computeData(ImageState state, ImageState previousState) {
		Data data = this.results.get(hashState(state));
		if (data == null)
			throw new NoSuchElementException(
					"No data has been set for this image (" + state.getImage().getImagePlus().getTitle() + ")");

		this.computeGeometricalAverages(state);

		data.setValue(REGION_FUNDUS, Data.PERCENTAGE, calculateGeoPercentage(data, REGION_FUNDUS));

		data.setValue(REGION_ANTRE, Data.PERCENTAGE, calculateGeoPercentage(data, REGION_ANTRE));

		data.setValue(REGION_STOMACH, Data.PERCENTAGE, calculateGeoPercentage(data, REGION_STOMACH));

		data.setValue(REGION_INTESTINE, Data.PERCENTAGE, calculateGeoPercentage(data, REGION_INTESTINE));

		double fundusDerivative = data.getValue(REGION_FUNDUS, Data.PERCENTAGE)
				/ data.getValue(REGION_STOMACH, Data.PERCENTAGE) * 100.;
		data.setValue(REGION_FUNDUS, Data.CORRELATION, fundusDerivative);

		if (previousState != null) {
			Data previousData = this.results.get(hashState(previousState));
			if (previousData != null) {
				double stomachDerivative = (previousData.getValue(REGION_STOMACH, Data.PERCENTAGE)
						- data.getValue(REGION_STOMACH, Data.PERCENTAGE))
						/ (this.calculateDeltaTime(Library_Dicom.getDateAcquisition(state.getImage().getImagePlus()))
								- this.calculateDeltaTime(
										Library_Dicom.getDateAcquisition(previousState.getImage().getImagePlus())))
						* 30.;
				previousData.setValue(REGION_STOMACH, Data.DERIVATIVE, stomachDerivative);
			} else {
				System.err.println("Careful: no data found for the previous image specified ("
						+ previousState.getImage().getImagePlus().getTitle() + ")");
			}
		}
	}

	/**
	 * Delivers the requested result for the specified image
	 * 
	 * @param result     Result to get, it must be one of RES_TIME, RES_STOMACH,
	 *                   RES_FUNDUS, RES_ANTRUM
	 * @param indexImage Index of the image (in chronological order) to get the
	 *                   result from
	 * @return result found
	 * @see Model_Gastric#getResult(Result)
	 * @throws UnsupportedOperationException if the requested result is different
	 *                                       than RES_TIME or RES_STOMACH or
	 *                                       RES_FUNDU or RES_ANTRUM
	 * @throws NoSuchElementException        if no data could not be retrieved from
	 *                                       the specified indexImage
	 */
	public ResultValue getImageResult(Result result, int indexImage)
			throws UnsupportedOperationException, NoSuchElementException {
		Data data = this.generatesDataOrdered().get(indexImage);
		if (data == null)
			throw new NoSuchElementException("No data has been set for this image#" + indexImage);

		switch (result) {
		case RES_TIME:
			return new ResultValue(result,
					BigDecimal.valueOf(data.time).setScale(2, RoundingMode.HALF_UP).doubleValue(), null);
		case RES_STOMACH:
			return new ResultValue(result, BigDecimal.valueOf(data.getValue(REGION_STOMACH, Data.PERCENTAGE))
					.setScale(2, RoundingMode.HALF_UP).doubleValue(), null);
		case RES_FUNDUS:
			return new ResultValue(result, BigDecimal.valueOf(data.getValue(REGION_FUNDUS, Data.PERCENTAGE))
					.setScale(2, RoundingMode.HALF_UP).doubleValue(), null);
		case RES_ANTRUM:
			return new ResultValue(result, BigDecimal.valueOf(data.getValue(REGION_ANTRE, Data.PERCENTAGE))
					.setScale(2, RoundingMode.HALF_UP).doubleValue(), null);
		default:
			throw new UnsupportedOperationException("The result " + result + " is not available here!");
		}
	}

	/**
	 * Delivers the requested result.<br>
	 * This method can only be used for the results that are calculated for all of
	 * the images (meaning START_ANTRUM, START_INTESTINE, LAG_PHASE and T_HALF). Any
	 * other request will throw an UnsupportedOperationException<br>
	 * If you need a result for a specific image, use
	 * {@link #getImageResult(int, int)} instead.<br>
	 * This method must be called only when all of the data was incorporated in this
	 * model.<br>
	 * 
	 * @param result Result to get
	 * @return ResultValue containing the result requested
	 * @see Model_Gastric#getImageResult(Result, int)
	 * @throws UnsupportedOperationException if the requested result is different
	 *                                       than START_ANTRUM or START_INTESTINE or
	 *                                       LAG_PHASE or T_HALF
	 */
	public ResultValue getResult(Result result) throws UnsupportedOperationException {
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

	/**
	 * Creates the graphic for the Intragastric Distribution.
	 * 
	 * @return Intragastric distribution graph as an image
	 */
	public ImagePlus createGraph_1() {
		return createGraph("Fundus/Stomach (%)", new Color(0, 100, 0), "Intragastric Distribution", times,
				this.getResultAsArray(REGION_FUNDUS, Data.CORRELATION), 100.0);
	}

	/**
	 * Creates the graphic for the Gastrointestinal flow.
	 * 
	 * @return Gastrointestinal flow graph as an image
	 */
	public ImagePlus createGraph_2() {
		return createGraph("% meal in the interval", Color.RED, "Gastrointestinal flow", timesDerivative,
				this.getResultAsArray(REGION_STOMACH, Data.DERIVATIVE), 50.0);
	}

	/**
	 * Creates the graphic for the Stomach, Fundus and Antrum percentages.
	 * 
	 * @return graph as an image
	 */
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

	/**
	 * Deactivates the fictional time 0.<br>
	 * The ingestion time should now be set to the time of the first dynamic
	 * acquisition.
	 * 
	 * @see #setTimeIngestion
	 */
	public void deactivateTime0() {
		this.time0 = null;
	}

	/**
	 * Makes a montage from the specified stack.
	 * 
	 * @param stack Stack to make the image from
	 * @return image created from the specified stack
	 */
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
		this.setExtrapolation(new LinearFit(this.generateStomachDataset()));
	}
}