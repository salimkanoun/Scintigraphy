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

import org.apache.commons.lang.ArrayUtils;
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
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Debug;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.Roi;
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
		RES_TIME("Time"), RES_STOMACH("Stomach"), RES_FUNDUS("Fundus"), RES_ANTRUM("Antrum"),
		RES_STOMACH_COUNTS("Stomach"), START_ANTRUM("Start antrum"), START_INTESTINE("Start intestine"),
		LAG_PHASE("Lag phase"), T_HALF("T 1/2"), RETENTION("Retention");

		private String s;

		private Result(String s) {
			this.s = s;
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
		private Result type;
		private double value;
		private Unit unit;
		private FitType extrapolation;

		public ResultValue(Result type, double value, Unit unit, FitType extrapolation) {
			if (type == null)
				throw new IllegalArgumentException("Result type cannot be null");
			if (unit == null)
				throw new IllegalArgumentException("Unit cannot be null");
			this.type = type;
			this.value = value;
			this.extrapolation = extrapolation;
			this.unit = unit;
		}

		public ResultValue(Result type, double value, Unit unit) {
			this(type, value, unit, null);
		}

		public void convert(Unit newUnit) {
			this.value = this.unit.convertTo(this.value, newUnit);
			this.unit = newUnit;
		}

		public Unit getUnit() {
			return this.unit;
		}

		public Result getResultType() {
			return this.type;
		}

		public FitType getExtrapolation() {
			return this.extrapolation;
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
			if (this.unit == Unit.TIME)
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
			if (hours < 10)
				s.append(0);
			s.append(hours);
			s.append(':');

			// Minutes
			if (minutes < 10)
				s.append(0);
			s.append(minutes);
			s.append(':');

			// Seconds
			if (seconds < 10)
				s.append(0);
			s.append(seconds);

			if (this.isExtrapolated())
				s.append("(*)");

			return s.toString();
		}
	}

	public static final String REGION_STOMACH = "Stomach", REGION_ANTRE = "Antre", REGION_FUNDUS = "Fundus",
			REGION_INTESTINE = "Intestine", REGION_ALL = "Total";

	public static final int DATA_ANT_COUNTS = 0, DATA_POST_COUNTS = 1, DATA_GEO_AVERAGE = 2, DATA_PERCENTAGE = 3,
			DATA_DERIVATIVE = 4, DATA_CORRELATION = 5, DATA_PIXEL_COUNTS = 6, DATA_BKG_NOISE = 7,
			DATA_DECAY_CORRECTED = 8, DATA_TOTAL_FIELDS = 9;

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
		private Map<String, Region> regionsAnt, regionsPost;

		private double time;
		private ImageSelection associatedImage;

		/**
		 * Instantiates a new data. The image state of a data should be unique (in this
		 * model).<br>
		 * The {@link ImageState#getImage()} method of the state <b>must</b> return
		 * something different than null!
		 * 
		 * @param state Unique state for this data (null allowed only for the time 0)
		 * @param time  Time in minutes after the ingestion time
		 */
		public Data(ImageSelection associatedImage, double time) {
			this.associatedImage = associatedImage;
			this.regionsAnt = new HashMap<>();
			this.regionsPost = new HashMap<>();
			this.time = time;
		}

		public Region[] getRegions() {
			return (Region[]) ArrayUtils.addAll(this.regionsAnt.values().toArray(new Region[this.regionsAnt.size()]),
					this.regionsPost.values().toArray(new Region[this.regionsPost.size()]));
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
		public double getMinutes() {
			return this.time;
		}

		public void inflateRegion(String regionName, ImageState state, Roi roi) {
			Region storedRegion = this.regionsAnt.get(regionName);
			if (storedRegion == null) {
				storedRegion = this.regionsPost.get(regionName);

				if (storedRegion == null) {
					// Create region
					storedRegion = new Region(regionName, Model_Gastric.this);
					storedRegion.inflate(state, roi);
					if (state.getFacingOrientation() == Orientation.ANT)
						this.regionsAnt.put(regionName, storedRegion);
					else
						this.regionsPost.put(regionName, storedRegion);
				}
			}

			storedRegion.inflate(state, roi);
		}

		public void setValue(String regionName, int key, double value) {
			if (key == DATA_POST_COUNTS)
				this.setPostValue(regionName, key, value);

			this.setAntValue(regionName, key, value);
		}

		public void setAntValue(String regionName, int key, double value) {
			Region region = this.regionsAnt.get(regionName);
			if (region == null) {
				// Create region
				region = new Region(regionName, Model_Gastric.this);
				this.regionsAnt.put(regionName, region);
			}

			// Set value
			region.setValue(key, Math.max(0, value));
		}

		public void setPostValue(String regionName, int key, double value) {
			Region region = this.regionsPost.get(regionName);
			if (region == null) {
				// Create region
				region = new Region(regionName, Model_Gastric.this);
				this.regionsPost.put(regionName, region);
			}

			// Set value
			region.setValue(key, Math.max(0, value));
		}

		public double getValue(String region, int key) {
			if (key == DATA_POST_COUNTS)
				return this.getPostValue(region, key);

			return this.getAntValue(region, key);
		}

		public double getAntValue(String region, int key) {
			try {
				return this.regionsAnt.get(region).getValue(key);
			} catch (NullPointerException e) {
				throw new NullPointerException("The key " + key + "(" + nameOfDataField(key) + ") of the region ("
						+ region + ") has no data associated with it");
			}
		}

		public double getPostValue(String region, int key) {
			try {
				return this.regionsPost.get(region).getValue(key);
			} catch (NullPointerException e) {
				throw new NullPointerException("The key " + key + "(" + nameOfDataField(key) + ") of the region ("
						+ region + ") has no data associated with it");
			}
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

		private String listRegions(Orientation orientation) {
			StringBuilder res = new StringBuilder();
			if (orientation == Orientation.ANT) {
				res.append(Library_Debug.subtitle("ANT REGIONS"));
				res.append('\n');

				if (this.regionsAnt.size() == 0)
					res.append("// NO REGION //\n");
				else
					for (Region region : this.regionsAnt.values())
						res.append(region + "\n");
			} else {
				res.append(Library_Debug.subtitle("POST REGIONS"));
				res.append('\n');

				if (this.regionsPost.size() == 0)
					res.append("// NO REGION //\n");
				else
					for (Region region : this.regionsPost.values())
						res.append(region + "\n");
			}
			return res.toString();
		}

		@Override
		public String toString() {
			String s = Library_Debug.separator(0);
			String imageTitle = (this.associatedImage == null ? "// NO-IMAGE //"
					: this.associatedImage.getImagePlus().getTitle());
			s += Library_Debug.title("Data");
			s += "\n";
			s += Library_Debug.title(imageTitle);
			s += "\n";
			s += this.listRegions(Orientation.ANT);
			s += this.listRegions(Orientation.POST);
			s += Library_Debug.separator(0);
			return s;
		}

		public ImageSelection getAssociatedImage() {
			return this.associatedImage;
		}
	}

	private ImageSelection firstImage;

	private Map<Integer, Data> results;

	/**
	 * Fictional data representing the first acquisition.
	 */
	private Data time0;

	/**
	 * Time when the ingestion started.
	 */
	private Date timeIngestion;

	private Isotope isotope;

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
		Double valueAnt = data.getValue(REGION_ANTRE, DATA_ANT_COUNTS);
		Double valuePost = data.getValue(REGION_ANTRE, DATA_POST_COUNTS);
		data.setValue(REGION_ANTRE, DATA_GEO_AVERAGE, Library_Quantif.moyGeom(valueAnt, valuePost));

		// Intestine
		valueAnt = data.getValue(REGION_INTESTINE, DATA_ANT_COUNTS);
		valuePost = data.getValue(REGION_INTESTINE, DATA_POST_COUNTS);
		Double geoIntestine = Library_Quantif.moyGeom(valueAnt, valuePost);
		data.setValue(REGION_INTESTINE, DATA_GEO_AVERAGE, geoIntestine);

		// Fundus
		valueAnt = data.getValue(REGION_FUNDUS, DATA_ANT_COUNTS);
		valuePost = data.getValue(REGION_FUNDUS, DATA_POST_COUNTS);
		data.setValue(REGION_FUNDUS, DATA_GEO_AVERAGE, Library_Quantif.moyGeom(valueAnt, valuePost));

		// Stomach
		Double valueFundus = data.getValue(REGION_FUNDUS, DATA_GEO_AVERAGE);
		Double valueAntre = data.getValue(REGION_ANTRE, DATA_GEO_AVERAGE);
		Double geoStomach = valueFundus + valueAntre;
		data.setValue(REGION_STOMACH, DATA_GEO_AVERAGE, geoStomach);

		// Total
		data.setValue(REGION_ALL, DATA_GEO_AVERAGE, geoStomach + geoIntestine);
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
	private double getDebut(String region) throws IllegalArgumentException, NoSuchElementException {
		if (!region.equals(REGION_ANTRE) && !region.equals(REGION_INTESTINE))
			throw new IllegalArgumentException("The region " + region + " is not supported here!");

		for (Data data : this.generatesDataOrdered())
			if (data.getValue(region, DATA_PERCENTAGE) > 0)
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
	private Double getX(double[] yValues, double valueY) {
		for (int i = 1; i < times.length; i++) {
			// Prevent from overflow
			if (i >= yValues.length)
				return null;

			// Exact value
			if (yValues[i] == valueY)
				return times[i];

			// Approximate value
			if (yValues[i] < valueY) {
				double x1 = times[i - 1];
				double x2 = times[i];
				double y1 = yValues[i - 1];
				double y2 = yValues[i];
//				return x2 - (y2 - valueY) * ((x2 - x1) / (y2 - y1));
				return ((valueY - y1) * (x2 - x1)) / (y2 - y1) + x1;
			}
		}

		// Not found
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
	private Double getY(double[] yValues, double valueX) {
		for (int i = 0; i < times.length; i++) {
			// Prevent from overflow
			if (i >= yValues.length)
				return null;

			// Exact value
			if (times[i] == valueX)
				return yValues[i];

			// Approximate value
			if (times[i] > valueX) {
				double x1 = times[i - 1];
				double x2 = times[i];
				double y1 = yValues[i - 1];
				double y2 = yValues[i];

				return y2 - (x2 - valueX) * ((y2 - y1) / (x2 - x1));
			}
		}

		// Not found
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
	 * @param regionName Region to get the result from
	 * @param key        Key of the results to place in the array
	 * @return array of all data for the requested key result
	 */
	private double[] getResultAsArray(String regionName, int key) {
		// Get all results
		double[] results = new double[this.nbAcquisitions()];
		Iterator<Data> it = this.generatesDataOrdered().iterator();
		int i = 0;
		int resultsIgnored = 0;
		while (it.hasNext()) {
			Data data = it.next();
			try {
				double value = data.getValue(regionName, key);
				results[i] = value;
			} catch (NullPointerException e) {
				// No data found for this point
				// Ignore value
				resultsIgnored++;
				results[i] = Double.NaN;
			}
			i++;
		}

		// Create array with right dimensions
		double[] goodResults = new double[this.nbAcquisitions() - resultsIgnored];

		// Fill array
		int j = 0;
		for (i = 0; i < results.length; i++) {
			if (!Double.isNaN(results[i])) {
				goodResults[j] = results[i];
				j++;
			}
		}

		return goodResults;
	}

	/**
	 * Calculates the difference between the time of the data associated with the
	 * specified state and the ingestion's time of this model.
	 * 
	 * @param state State associated with the data to retrieve
	 * @return difference of time expressed in minutes
	 * @see #calculateDeltaTime(Date)
	 */
	private double calculateDeltaTime(ImageState state) {
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

		data.setValue(region.getName(), key, value);
	}

	/**
	 * Calculates the percentage of the specified region for the specified data
	 * using the values responding to the specified key.
	 * 
	 * @return percentage using key values
	 */
	private double calculatePercentage(Data data, String region, int key) {
		return data.getValue(region, key) / data.getValue(REGION_ALL, key) * 100.;
	}

	private Data createOrRetrieveData(ImageState state) {
		// Set the image in the state
		// This is important: the getImage() of a state in a data must return something
		// different than null
		if (state.getIdImage() != ImageState.ID_CUSTOM_IMAGE)
			state.specifieImage(this.selectedImages[state.getIdImage()]);

		// Retrieve data
		Data data = this.results.get(hashState(state));

		// Create data if not existing
		if (data == null) {
			data = new Data(state.getImage(), this.calculateDeltaTime(state));
			this.results.put(hashState(state), data);
//			System.out.println("Created data for image " + state.getImage().getImagePlus().getTitle());
		}

//		System.out.println(this.nbAcquisitions() + " data stored:");
//		for(Data d : this.results.values())
//			System.out.println(d);

		return data;
	}

	/**
	 * @return all regions required by this model
	 */
	public String[] getAllRegionsName() {
		return new String[] { REGION_STOMACH, REGION_ANTRE, REGION_FUNDUS, REGION_INTESTINE };
	}

	private void calculateCountsFromImage(String regionName, ImageState state, Roi roi) {
		ImageSelection ims = imageFromState(state);
		ImagePlus imp = ims.getImagePlus();

		// Create data if not existing
		Data data = this.createOrRetrieveData(state);

		// Find orientation (ant or post)
		int key;
		if (state.getFacingOrientation() == Orientation.ANT) {
			key = DATA_ANT_COUNTS;
		} else {
			key = DATA_POST_COUNTS;
		}

		// Save value
		imp.setSlice(state.getSlice());
		imp.setRoi(roi);
		data.setValue(regionName, key, Math.max(0, Library_Quantif.getCounts(imp)));
		data.setValue(regionName, DATA_PIXEL_COUNTS, imp.getStatistics().pixelCount);

		// Inflate region
		data.inflateRegion(regionName, state, roi);
	}

	private void calculateCountsFromData(String regionName, ImageState state) {
		// Create data if not existing
		Data data = this.createOrRetrieveData(state);

		// Find orientation (ant or post)
		int key;
		if (state.getFacingOrientation() == Orientation.ANT)
			key = DATA_ANT_COUNTS;
		else
			key = DATA_POST_COUNTS;

		// Calculate value
		double counts, pixels;
		if (regionName.equals(REGION_FUNDUS)) {
			counts = data.getValue(REGION_STOMACH, key) - data.getValue(REGION_ANTRE, key);
			pixels = data.getValue(REGION_STOMACH, DATA_PIXEL_COUNTS) - data.getValue(REGION_ANTRE, DATA_PIXEL_COUNTS);
		} else if (regionName.equals(REGION_INTESTINE)) {
			counts = data.getValue(REGION_INTESTINE, key) - data.getValue(REGION_ANTRE, key);
			pixels = data.getValue(REGION_INTESTINE, DATA_PIXEL_COUNTS)
					- data.getValue(REGION_ANTRE, DATA_PIXEL_COUNTS);
		} else
			throw new UnsupportedOperationException("The region " + regionName + " is not supported here!");

		// Save value
		data.setValue(regionName, key, Math.max(0, counts));
		data.setValue(regionName, DATA_PIXEL_COUNTS, pixels);

		// Inflate region
		data.inflateRegion(regionName, state, null);
	}

	/**
	 * Calculates the counts of the specified region.<br>
	 * The region must be previously inflated with the correct state.<br>
	 * This method takes care of all necessary operations to do on the ImagePlus or
	 * the RoiManager.<br>
	 * This method will create a new data for each new ImageSelection encountered.
	 * 
	 * @param regionName Region to calculate
	 * @throws IllegalArgumentException if the region is not part of the requested
	 *                                  regions for this model
	 */
	public void calculateCounts(String regionName, ImageState state, Roi roi) throws IllegalArgumentException {
		// Check region is part of requested regions for this model
		if (!Arrays.stream(this.getAllRegionsName()).anyMatch(r -> r.equals(regionName)))
			throw new IllegalArgumentException("The region (" + regionName
					+ ") is not requested in this model\nValid regions: " + Arrays.toString(this.getAllRegionsName()));

		if (regionName.equals(REGION_STOMACH) || regionName.equals(REGION_ANTRE)
				|| regionName.equals(REGION_INTESTINE)) {
			this.calculateCountsFromImage(regionName, state, roi);
		}
		if (regionName.equals(REGION_FUNDUS) || regionName.equals(REGION_INTESTINE)) {
			this.calculateCountsFromData(regionName, state);
		}
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
			times[i] = data.getMinutes();
			if (i > 0)
				this.timesDerivative[i - 1] = times[i];
			i++;
		}
	}

	public double[] getTimes() {
		return this.times;
	}

	public String nameOfDataField(int field) {
		switch (field) {
		case DATA_ANT_COUNTS:
			return "Nb Ant-counts";
		case DATA_POST_COUNTS:
			return "Nb Post-counts";
		case DATA_GEO_AVERAGE:
			return "Geo-avg";
		case DATA_PERCENTAGE:
			return "Percentage";
		case DATA_DERIVATIVE:
			return "Derivative";
		case DATA_CORRELATION:
			return "Correlation";
		case DATA_PIXEL_COUNTS:
			return "Pixel counts";
		case DATA_BKG_NOISE:
			return "Background Noise";
		default:
			return "???";
		}
	}

	private double[][] generateDatasetFromKey(String regionName, int key) {
		// Get all Y points
		double[] yPoints = this.getResultAsArray(regionName, key);

		// Create dataset with right dimensions
		double[][] dataset = new double[yPoints.length][2];

		// Fill dataset
		for (int i = 0; i < yPoints.length; i++) {
			if (!Double.isNaN(yPoints[i])) {
				dataset[i][0] = times[i];
				dataset[i][1] = yPoints[i];
			}
		}

		return dataset;
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
	private double[][] generateStomachDataset() {
		return this.generateDatasetFromKey(REGION_STOMACH, DATA_PERCENTAGE);
	}

	private double[][] generateDecayFunction() {
		return this.generateDatasetFromKey(REGION_STOMACH, DATA_DECAY_CORRECTED);
	}

	private XYSeries generateSeriesFromDataset(String seriesName, double[][] dataset) {
		XYSeries series = new XYSeries(seriesName);
		for (int i = 0; i < dataset.length; i++)
			series.add(dataset[i][0], dataset[i][1]);
		return series;
	}

	private double[] generateValuesFromDataset(double[][] dataset) {
		double[] yValues = new double[dataset.length];
		int i = 0;
		for (double[] d : dataset)
			yValues[i++] = d[1];
		return yValues;
	}

	/**
	 * @return series for the stomach (used for the graph)
	 */
	public XYSeries getStomachSeries() {
		return this.generateSeriesFromDataset("Stomach", this.generateStomachDataset());
	}

	public double[] getStomachValues() {
		return this.generateValuesFromDataset(generateStomachDataset());
	}

	public XYSeries getDecayFunction() {
		return this.generateSeriesFromDataset("Stomach", this.generateDecayFunction());
	}

	public double[] getDecayValues() {
		return this.generateValuesFromDataset(generateDecayFunction());
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
			data.setTime(this
					.calculateDeltaTime(Library_Dicom.getDateAcquisition(data.getAssociatedImage().getImagePlus())));
		}
	}

	public void setIsotope(Isotope isotope) {
		this.isotope = isotope;
	}

	public void setFirstImage(ImageSelection firstImage) {
		this.firstImage = firstImage;
	}

	public ImageSelection getFirstImage() {
		return this.firstImage;
	}

	/**
	 * @return time when the patient ingested the food
	 */
	public Date getTimeIngestion() {
		return this.timeIngestion;
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

		return data.getValue(region.getName(), orientation == Orientation.ANT ? DATA_ANT_COUNTS : DATA_POST_COUNTS);
	}

	/**
	 * Activates the fictional time 0 representing the moment when the fundus
	 * contains all of the food.
	 */
	public void activateTime0() {
		this.time0 = new Data(null, 0.);
		this.time0.time = 0.;
		this.time0.setValue(REGION_STOMACH, DATA_PERCENTAGE, 100.);
		this.time0.setValue(REGION_FUNDUS, DATA_PERCENTAGE, 100.);
		this.time0.setValue(REGION_ANTRE, DATA_PERCENTAGE, 0.);
		this.time0.setValue(REGION_INTESTINE, DATA_PERCENTAGE, 0.);

		this.time0.setValue(REGION_FUNDUS, DATA_CORRELATION, 100.);
	}

	/**
	 * Force the insertion of a percentage value in the data for the specified
	 * region.
	 * 
	 * @param region Region to set this value on
	 * @param value  Value to force
	 */
	public void forcePercentageDataValue(Region region, double value) {
		this.forceDataValue(region, DATA_PERCENTAGE, Math.max(0, value));
	}

	/**
	 * Force the insertion of a correlation value in the data for the specified
	 * region.
	 * 
	 * @param region Region to set this value on
	 * @param value  Value to force
	 */
	public void forceCorrelationDataValue(Region region, double value) {
		this.forceDataValue(region, DATA_CORRELATION, value);
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
				region.getState().getFacingOrientation() == Orientation.ANT ? DATA_ANT_COUNTS : DATA_POST_COUNTS,
				Math.max(0, value));
	}

	/**
	 * Adjusts the percentage with the ratio of eggs in the body.
	 * 
	 * @param region
	 * @param percentage
	 * @param numActualImage
	 * @param nbTotalImages
	 * @return
	 */
	private double adjustPercentageWithEggsRatio(String region, double percentage, int numActualImage,
			int nbTotalImages) {
//		System.out.println("Inputs: region=" + region + ", percentage=" + percentage + ", image=" + numActualImage
//				+ ", totImgs=" + nbTotalImages);
		double ratioEggsInBody = (double) numActualImage / (double) nbTotalImages;
//		System.out.println("Ratio eggs in body: " + (double)numActualImage + " / " + (double)nbTotalImages + " = " + ratioEggsInBody);
		double percentEggsNotInBody = 100. - ratioEggsInBody * 100.;
//		System.out.println("Percent eggs not in body: 100 - " + ratioEggsInBody + " * 100 = " + percentEggsNotInBody);

		if (region == REGION_FUNDUS) {
//			System.out.println("Returned value: " + percentEggsNotInBody + " + " + percentage + " * " + ratioEggsInBody
//					+ " = " + (percentEggsNotInBody + percentage * ratioEggsInBody));
			return percentEggsNotInBody + percentage * ratioEggsInBody;
		}
//		System.out.println(
//				"Returned value: " + percentage + " * " + ratioEggsInBody + " = " + (percentage * ratioEggsInBody));
		return percentage * ratioEggsInBody;
	}

	private Region bkgNoise_antre, bkgNoise_intestine, bkgNoise_stomach, bkgNoise_fundus;

	public void setBkgNoise(String regionName, ImageState state, Roi roi) {
		ImagePlus imp = state.getImage().getImagePlus();
		imp.setRoi(roi);

		double bkgNoise = Library_Quantif.getAvgCounts(imp);

		Region region = new Region("Background Noise " + regionName, this);
		region.inflate(state, roi);
		region.setValue(DATA_BKG_NOISE, bkgNoise);
		region.setValue(DATA_ANT_COUNTS, Library_Quantif.getCounts(imp));
		region.setValue(DATA_PIXEL_COUNTS, Library_Quantif.getPixelNumber(imp));

		if (regionName.equals(REGION_ANTRE)) {
			this.bkgNoise_antre = region;
		} else if (regionName.equals(REGION_INTESTINE)) {
			this.bkgNoise_intestine = region;
		} else if (regionName.equals(REGION_STOMACH)) {
			this.bkgNoise_stomach = region;
			double countsFundus = bkgNoise_stomach.getValue(DATA_ANT_COUNTS) - bkgNoise_antre.getValue(DATA_ANT_COUNTS);
			double pixelsFundus = bkgNoise_stomach.getValue(DATA_PIXEL_COUNTS)
					- bkgNoise_antre.getValue(DATA_PIXEL_COUNTS);

			this.bkgNoise_fundus = region.clone();
			this.bkgNoise_fundus.setValue(DATA_ANT_COUNTS, countsFundus);
			this.bkgNoise_fundus.setValue(DATA_PIXEL_COUNTS, pixelsFundus);
			this.bkgNoise_fundus.setValue(DATA_BKG_NOISE, countsFundus / pixelsFundus);
		} else
			throw new IllegalArgumentException("The region (" + region + ") is not a background noise");

		System.out.println("The background noise for the " + region + " is set at " + bkgNoise + "!");
	}

	private void computeDerivative(Data data, ImageState state, ImageState previousState) {
		Data previousData = null, dataToInflate = null;
		if (previousState == null) {
			previousData = this.time0;
		} else
			previousData = this.results.get(hashState(previousState));

		if (data.associatedImage.getImageOrientation().isDynamic())
			dataToInflate = previousData;
		else
			dataToInflate = data;

		if (previousData != null) {
			double stomachDerivative = (previousData.getValue(REGION_STOMACH, DATA_PERCENTAGE)
					- data.getValue(REGION_STOMACH, DATA_PERCENTAGE))
					/ (this.calculateDeltaTime(Library_Dicom.getDateAcquisition(state.getImage().getImagePlus()))
							- previousData.getMinutes())
					* 30.;
			dataToInflate.setValue(REGION_STOMACH, DATA_DERIVATIVE, stomachDerivative);
		} else {
			System.err.println("Warning: no data found");
		}
	}

	private void computeDecayFunction(Data data) {
		int delayMs = (int) (data.time * 60. * 1000.);
		double value = Library_Quantif.calculer_countCorrected(delayMs, data.getValue(REGION_STOMACH, DATA_GEO_AVERAGE),
				isotope);
		data.setValue(REGION_STOMACH, DATA_DECAY_CORRECTED, value);
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
	public void computeStaticData(ImageState state, ImageState previousState) {
		Data data = this.results.get(hashState(state));
		if (data == null)
			throw new NoSuchElementException(
					"No data has been set for this image (" + state.getImage().getImagePlus().getTitle() + ")");

		int key = DATA_GEO_AVERAGE;

		this.computeGeometricalAverages(state);

		// Calculate percentages
		data.setValue(REGION_FUNDUS, DATA_PERCENTAGE, calculatePercentage(data, REGION_FUNDUS, key));

		data.setValue(REGION_ANTRE, DATA_PERCENTAGE, calculatePercentage(data, REGION_ANTRE, key));

		data.setValue(REGION_STOMACH, DATA_PERCENTAGE,
				data.getValue(REGION_FUNDUS, DATA_PERCENTAGE) + data.getValue(REGION_ANTRE, DATA_PERCENTAGE));

		data.setValue(REGION_INTESTINE, DATA_PERCENTAGE, 100. - data.getValue(REGION_STOMACH, DATA_PERCENTAGE));

		double fundusDerivative = data.getValue(REGION_FUNDUS, DATA_PERCENTAGE)
				/ data.getValue(REGION_STOMACH, DATA_PERCENTAGE) * 100.;
		data.setValue(REGION_FUNDUS, DATA_CORRELATION, fundusDerivative);

		this.computeDerivative(data, state, previousState);

		this.computeDecayFunction(data);
	}

	/**
	 * Computes the data retrieved from the specified state. This method calculates
	 * the percentages for each region. This method should be used when the dynamic
	 * acquisition has been made.<br>
	 * The {@link Data#ANT_COUNTS} <b>must</b> be defined in every region (except
	 * REGION_ALL).<br>
	 * If the previous state is not null, then the derivative is calculated for the
	 * stomach.
	 * 
	 * @param state          State of the data to retrieve
	 * @param previousState  State of the previous data to retrieve (in
	 *                       chronological order)
	 * @param numActualImage Image number (1 = first image in chronological order).
	 *                       The first images contains less tracer than the last
	 *                       images.
	 * @param nbTotalImages  Total number of dynamic images
	 * @throws NoSuchElementException if no data could be retrieved from the
	 *                                specified state
	 */
	public void computeDynamicData(ImageState state, ImageState previousState, int numActualImage, int nbTotalImages) {
		Data data = this.results.get(hashState(state));
		if (data == null)
			throw new NoSuchElementException(
					"No data has been set for this image (" + state.getImage().getImagePlus().getTitle() + ")");

		int key = DATA_ANT_COUNTS;

//		System.out.println();
//		System.out.println("BEFORE ADJUSTMENTS\n" + data);

		// Adjust counts with background
		for (Region region : data.getRegions()) {
			if (region.getName().equals(REGION_ALL))
				continue;

			Double bkgNoise = null;
			if (region.getName().equals(REGION_ANTRE)) {
				bkgNoise = this.bkgNoise_antre.getValue(DATA_BKG_NOISE);
			} else if (region.getName().equals(REGION_INTESTINE)) {
				bkgNoise = this.bkgNoise_intestine.getValue(DATA_BKG_NOISE);
			} else if (region.getName().equals(REGION_STOMACH)) {
				bkgNoise = this.bkgNoise_stomach.getValue(DATA_BKG_NOISE);
			} else if (region.getName().equals(REGION_FUNDUS)) {
				bkgNoise = this.bkgNoise_fundus.getValue(DATA_BKG_NOISE);
			} else
				// TODO: correct with a bkg noise
				System.err.println("Warning: The region (" + region + ") is not corrected with a background noise!");

			if (bkgNoise != null) {
//				System.out.println("=== Adjusting region " + region.getName() + " with background noise ===");
//				System.out.println("Background noise found: " + bkgNoise);
//				System.out.println("Pixels count of the region: " + data.getValue(region.getName(), DATA_PIXEL_COUNTS));
//				System.out.println(
//						"Adjusting value " + data.getValue(region.getName(), key) + " with background noise (- "
//								+ (bkgNoise * data.getValue(region.getName(), DATA_PIXEL_COUNTS) + ") = "
//										+ (data.getValue(region.getName(), key)
//												- (bkgNoise * data.getValue(region.getName(), DATA_PIXEL_COUNTS)))));
//				System.out.println();

				data.setValue(region.getName(), key, data.getValue(region.getName(), key)
						- (bkgNoise * data.getValue(region.getName(), DATA_PIXEL_COUNTS)));
				if (bkgNoise == 0.)
					System.err.println("Warning: The background noise " + region + " is 0.");
			}
		}

		// Calculate total
		data.setValue(REGION_ALL, DATA_ANT_COUNTS,
				data.getValue(REGION_STOMACH, DATA_ANT_COUNTS) + data.getValue(REGION_INTESTINE, DATA_ANT_COUNTS));

//		System.out.println();
//		System.out.println("AFTER ADJUSTING BKG\n" + data);

		// Adjust percentages with eggs ratio
		double percentage = this.adjustPercentageWithEggsRatio(REGION_FUNDUS,
				calculatePercentage(data, REGION_FUNDUS, key), numActualImage, nbTotalImages);
		data.setValue(REGION_FUNDUS, DATA_PERCENTAGE, percentage);

		percentage = this.adjustPercentageWithEggsRatio(REGION_ANTRE, calculatePercentage(data, REGION_ANTRE, key),
				numActualImage, nbTotalImages);
		data.setValue(REGION_ANTRE, DATA_PERCENTAGE, percentage);

		percentage = data.getValue(REGION_FUNDUS, DATA_PERCENTAGE) + data.getValue(REGION_ANTRE, DATA_PERCENTAGE);
		data.setValue(REGION_STOMACH, DATA_PERCENTAGE, percentage);

		percentage = 100. - data.getValue(REGION_STOMACH, DATA_PERCENTAGE);
		data.setValue(REGION_INTESTINE, DATA_PERCENTAGE, percentage);

		double fundusDerivative = data.getValue(REGION_FUNDUS, DATA_PERCENTAGE)
				/ data.getValue(REGION_STOMACH, DATA_PERCENTAGE) * 100.;
		data.setValue(REGION_FUNDUS, DATA_CORRELATION, fundusDerivative);

		// Compute derivative
		this.computeDerivative(data, state, previousState);

//		System.out.println();
//		System.out.println("AFTER ADJUSTING PERCENTAGES\n" + data);
//		System.out.println();
//		System.out.println();
	}

	/**
	 * Delivers the requested result for the specified image
	 * 
	 * @param result     Result to get, it must be one of RES_TIME, RES_STOMACH,
	 *                   RES_FUNDUS, RES_ANTRUM
	 * @param indexImage Index of the image (in chronological order) to get the
	 *                   result from
	 * @return result found or null if no data was found
	 * @see Model_Gastric#getResult(Result)
	 * @throws UnsupportedOperationException if the requested result is different
	 *                                       than RES_TIME or RES_STOMACH or
	 *                                       RES_FUNDU or RES_ANTRUM
	 */
	public ResultValue getImageResult(Result result, int indexImage) throws UnsupportedOperationException {
		Data data = this.generatesDataOrdered().get(indexImage);

		switch (result) {
		case RES_TIME:
			return new ResultValue(result,
					BigDecimal.valueOf(data.time).setScale(2, RoundingMode.HALF_UP).doubleValue(), Unit.TIME);
		case RES_STOMACH:
			return new ResultValue(result, BigDecimal.valueOf(data.getValue(REGION_STOMACH, DATA_PERCENTAGE))
					.setScale(2, RoundingMode.HALF_UP).doubleValue(), Unit.PERCENTAGE);
		case RES_FUNDUS:
			return new ResultValue(result, BigDecimal.valueOf(data.getValue(REGION_FUNDUS, DATA_PERCENTAGE))
					.setScale(2, RoundingMode.HALF_UP).doubleValue(), Unit.PERCENTAGE);
		case RES_ANTRUM:
			return new ResultValue(result, BigDecimal.valueOf(data.getValue(REGION_ANTRE, DATA_PERCENTAGE))
					.setScale(2, RoundingMode.HALF_UP).doubleValue(), Unit.PERCENTAGE);
		case RES_STOMACH_COUNTS:
			if (data == time0)
				return null;
			return new ResultValue(result, BigDecimal.valueOf(data.getValue(REGION_STOMACH, DATA_GEO_AVERAGE))
					.setScale(2, RoundingMode.HALF_UP).doubleValue(), Unit.COUNTS);
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
	public ResultValue getResult(double[] yValues, Result result, Fit fit) throws UnsupportedOperationException {
		FitType extrapolationType = null;
		switch (result) {
		case START_ANTRUM:
			return new ResultValue(result, this.getDebut(REGION_ANTRE), Unit.TIME);
		case START_INTESTINE:
			return new ResultValue(result, this.getDebut(REGION_INTESTINE), Unit.TIME);
		case LAG_PHASE:
			extrapolationType = null;
			Double valX = this.getX(yValues, 95.);
			if (valX == null) {
				// Extrapolate
				valX = this.extrapolateX(95., fit);
				extrapolationType = fit.getType();
			}
			return new ResultValue(result, valX, Unit.TIME, extrapolationType);
		case T_HALF:
			// Assumption: the first value is the highest (maybe do not assume that...)
			double half = yValues[0] / 2.;
			extrapolationType = null;
			valX = this.getX(yValues, half);
			if (valX == null) {
				// Extrapolate
				valX = this.extrapolateX(half, fit);
				extrapolationType = fit.getType();
			}
			return new ResultValue(result, valX, Unit.TIME, extrapolationType);
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
	public ResultValue retentionAt(double[] yValues, double time, Fit fit) {
		Double res = this.getY(yValues, time);
		FitType extrapolated = null;
		if (res == null) {
			res = this.extrapolateY(time, fit);
			extrapolated = fit.getType();
		}

		// Percentage of res
		res = res * 100. / this.getY(yValues, 0.);

		return new ResultValue(Result.RETENTION, res, Unit.PERCENTAGE, extrapolated);
	}

	// TODO: change this method, the model should not decide for rendering
	/**
	 * Creates the graphic for the Intragastric Distribution.
	 * 
	 * @return Intragastric distribution graph as an image
	 */
	public ImagePlus createGraph_1() {
		return Library_JFreeChart.createGraph("Fundus/Stomach (%)", new Color(0, 100, 0), "Intragastric Distribution",
				times, this.getResultAsArray(REGION_FUNDUS, DATA_CORRELATION), 100.0);
	}

	// TODO: change this method, the model should not decide for rendering
	/**
	 * Creates the graphic for the Gastrointestinal flow.
	 * 
	 * @return Gastrointestinal flow graph as an image
	 */
	public ImagePlus createGraph_2() {
		double[] result = this.getResultAsArray(REGION_STOMACH, DATA_DERIVATIVE);
//		System.out.println("Result for Gastrointestinal flow:");
//		System.out.println(Arrays.toString(timesDerivative));
//		System.out.println(Arrays.toString(result));
		return Library_JFreeChart.createGraph("% meal in the interval", Color.RED, "Gastrointestinal flow",
				timesDerivative, result, 50.0);
	}

	/**
	 * Creates the graphic for the Stomach, Fundus and Antrum percentages.
	 * 
	 * @return graph as an image
	 */
	public ImagePlus createGraph_3() {
		// On cree un dataset qui contient les 3 series
		XYSeriesCollection dataset = createDatasetTrois(times, this.getResultAsArray(REGION_STOMACH, DATA_PERCENTAGE),
				"Stomach", this.getResultAsArray(REGION_FUNDUS, DATA_PERCENTAGE), "Fundus",
				this.getResultAsArray(REGION_ANTRE, DATA_PERCENTAGE), "Antrum");
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

	// TODO: change this method, the model should not decide for rendering
	public ImagePlus createGraph_4(Unit unit) {
		double[] result = this.getResultAsArray(REGION_STOMACH, DATA_GEO_AVERAGE);

		// TODO: convert to kcounts/min
		result = Library_JFreeChart.convert(result, Unit.COUNTS, unit);

		return Library_JFreeChart.createGraph(unit.abrev(), Color.GREEN, "Stomach retention",
				ArrayUtils.remove(times, 0), result, Library_JFreeChart.maxValue(result) * 1.1);
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
		if (stack.size() == 4)
			imp = mm.makeMontage2(imp, 2, 2, 0.5, 1, 4, 1, 10, false);
		else if (stack.size() == 2)
			imp = mm.makeMontage2(imp, 2, 1, .5, 1, 2, 1, 10, false);
		imp.setTitle("Resultats " + this.studyName);
		return imp;
	}

	@Override
	public void calculerResultats() {
		this.generatesTimes();
	}
}