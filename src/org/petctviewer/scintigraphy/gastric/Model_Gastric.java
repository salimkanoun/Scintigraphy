package org.petctviewer.scintigraphy.gastric;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import org.apache.commons.lang.ArrayUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.platelet_refactored.Data;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.*;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.*;

/**
 * Model of the Gastric Scintigraphy.
 *
 * @author Xie PING
 * @author Titouan QUÉMA - refactoring, JavaDoc
 */
public class Model_Gastric extends ModelWorkflow {

	public static final int SERIES_STOMACH_PERCENTAGE = 0, SERIES_DECAY_FUNCTION = 1;

	public static final Result RES_TIME = new Result("Time"), RES_STOMACH = new Result("Stomach"), RES_FUNDUS =
			new Result("Fundus"), RES_ANTRUM = new Result("Antrum"), RES_STOMACH_COUNTS = new Result("Stomach"),
			START_ANTRUM = new Result("Start antrum"), START_INTESTINE = new Result("Start intestine"),
			LAG_PHASE_PERCENTAGE = new Result("Lag phase"), LAG_PHASE_GEOAVG = new Result("Lag phase"),
			T_HALF_PERCENTAGE = new Result("T 1/2"), T_HALF_GEOAVG = new Result("T 1/2"), RETENTION_PERCENTAGE =
			new Result("Retention"), RETENTION_GEOAVG = new Result("Retention");

	public static final String REGION_STOMACH = "Stomach", REGION_ANTRE = "Antre", REGION_FUNDUS = "Fundus",
			REGION_INTESTINE = "Intestine", REGION_ALL = "Total";
	private final Map<Integer, Data> results;
	private ImageSelection firstImage;
	/**
	 * Fictional data representing the first acquisition.
	 */
	private Data time0;
	/**
	 * Time when the ingestion started.
	 */
	private Date timeIngestion;

	private Region bkgNoise_antre, bkgNoise_intestine, bkgNoise_stomach, bkgNoise_fundus;

	public Model_Gastric(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
		Prefs.useNamesAsLabels = true;

		this.results = new HashMap<>();
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
	 * Computes the geometrical average of the region stored in the specified data.<br> The Ant and Post counts values
	 * are used for this operation.
	 *
	 * @param data   Data where the values are taken from
	 * @param region Region on which to apply the calculation
	 */
	private void computeAverage(Data data, String region) {
		if (data.hasRegion(region)) {
			Double valueAnt = data.getAntValue(region, Data.DATA_ANT_COUNTS);
			Double valuePost = data.getPostValue(region, Data.DATA_POST_COUNTS);
			if (valueAnt != null && valuePost != null) {
				data.setAntValue(region, Data.DATA_GEO_AVG, Library_Quantif.moyGeom(valueAnt, valuePost));
			}
		}
	}

	/**
	 * Computes the geometrical average of each region of the data found.<br> The average is made with the {@link
	 * Data#DATA_ANT_COUNTS} and the {@link Data#DATA_POST_COUNTS} data and will generate the {@link Data#DATA_GEO_AVG}
	 * for every region.
	 * <p>
	 * So the data ANT_COUNTS and POST_COUNTS <b>must</b> be defined for all regions (except REGION_ALL).
	 * </p>
	 * Be careful: this method assumes that the specified state is correct and will not throw any exception
	 *
	 * @param state ImageState from which the data will be retrieved
	 * @throws NoSuchElementException if no data is linked to the specified state
	 */
	private void computeGeometricalAverages(ImageState state) throws NoSuchElementException {
		Data data = this.results.get(hashState(state));

		// Antre
		this.computeAverage(data, REGION_ANTRE);

		// Intestine
		this.computeAverage(data, REGION_INTESTINE);

		// Fundus
		this.computeAverage(data, REGION_FUNDUS);

		// Stomach
		Double valueFundus = data.getAntValue(REGION_FUNDUS, Data.DATA_GEO_AVG);
		Double valueAntre = data.getAntValue(REGION_ANTRE, Data.DATA_GEO_AVG);
		Double geoStomach;
		if (valueFundus != null && valueAntre != null) {
			geoStomach = valueFundus + valueAntre;
			data.setAntValue(REGION_STOMACH, Data.DATA_GEO_AVG, geoStomach);
		} else {
			this.computeAverage(data, REGION_STOMACH);
			geoStomach = data.getAntValue(REGION_STOMACH, Data.DATA_GEO_AVG);
		}

		// Total
		Double geoIntestine = data.getAntValue(REGION_INTESTINE, Data.DATA_GEO_AVG);
		if (geoStomach != null && geoIntestine != null) data.setAntValue(REGION_ALL, Data.DATA_GEO_AVG,
																		 geoStomach + geoIntestine, null, null);
	}

	/**
	 * Finds the starting time for the Antre or Intestine region (only).
	 *
	 * @param region Region to find the starting time
	 * @return starting time for the region
	 * @throws IllegalArgumentException if the region is different than ANTRE or INTESTINE
	 * @throws NoSuchElementException   if no data was found for the specified region
	 */
	private double getDebut(String region) throws IllegalArgumentException, NoSuchElementException {
		if (!region.equals(REGION_ANTRE) && !region.equals(REGION_INTESTINE)) throw new IllegalArgumentException(
				"The region " + region + " is not supported here!");

		for (Data data : this.generatesDataOrdered())
			if (data.getAntValue(region, Data.DATA_PERCENTAGE) > 0) return data.getMinutes();

		throw new NoSuchElementException("No data found, please first use the calculateCounts method before!");
	}

	/**
	 * Generates a list of all data from this model ordered by time.<br> This method also includes the fictional time 0
	 * if necessary.
	 *
	 * @return all data ordered by time
	 */
	private List<Data> generatesDataOrdered() {
		List<Data> orderedData = new ArrayList<>(this.results.values());
		Collections.sort(orderedData, Comparator.comparing(o -> o.getAssociatedImage().getDateAcquisition()));
		if (time0 != null) orderedData.add(0, time0);
		return orderedData;
	}

	/**
	 * Creates an array with all of the requested results ordered chronologically. The array may contain NaN values if
	 * no value was found for a data.
	 *
	 * @param regionName Region to get the result from
	 * @param key        Key of the results to place in the array
	 * @param unit       Unit of the output
	 * @return array of all data for the requested key result
	 */
	private double[] getAllResultsAsArray(String regionName, int key, Unit unit) {
		// Get all results
		double[] results = new double[this.nbAcquisitions()];
		Iterator<Data> it = this.generatesDataOrdered().iterator();
		int i = 0;
//		int resultsIgnored = 0;
		while (it.hasNext()) {
			Data data = it.next();
			try {
				double value;
				if (key == Data.DATA_POST_COUNTS) value = data.getPostValue(regionName, key);
				else value = data.getAntValue(regionName, key);
				Unit unitValue = data.unitForKey(key);
				results[i] = unitValue.convertTo(value, unit);
			} catch (NullPointerException e) {
				// No data found for this point
				// Ignore value
//				resultsIgnored++;
				results[i] = Double.NaN;
			}
			i++;
		}

		return results;
	}

	/**
	 * Creates an array with all of the requested results ordered chronologically.
	 *
	 * @param regionName Region to get the result from
	 * @param key        Key of the results to place in the array
	 * @param unit       Unit of output
	 * @return array of all data for the requested key result
	 */
	private double[] getResultAsArray(String regionName, int key, Unit unit) {
		// Get all results
		double[] results = getAllResultsAsArray(regionName, key, unit);

		// Count results to ignore
		int resultsIgnored = (int) Arrays.stream(results).filter(Double::isNaN).count();

		// Create array with right dimensions
		double[] goodResults = new double[this.nbAcquisitions() - resultsIgnored];

		// Fill array
		int j = 0;
		for (double result : results) {
			if (!Double.isNaN(result)) {
				goodResults[j] = result;
				j++;
			}
		}

		return goodResults;
	}

	/**
	 * Calculates the difference between the time of the data associated with the specified state and the ingestion's
	 * time of this model.
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
	 * @return difference of time expressed in minutes (negative value if the specified time is before the ingestion's
	 * time)
	 */
	private double calculateDeltaTime(Date time) {
		return Library_Quantif.calculateDeltaTime(this.timeIngestion, time);
	}

	/**
	 * Change a data value.<br> This method is designed to be used by public methods to allow controlled modifications
	 * of the data.
	 *
	 * @param region Region to edit
	 * @param key    Key of the value to set
	 * @param value  Value to force
	 */
	private void forceDataValue(Region region, int key, double value) {
		ImageState state = region.getState();
		Data data = this.createOrRetrieveData(state);

		if (key == Data.DATA_POST_COUNTS) data.setPostValue(region.getName(), key, value);
		else data.setAntValue(region.getName(), key, value);
	}

	/**
	 * Calculates the percentage of the specified region for the specified data using the values responding to the
	 * specified key.
	 *
	 * @return percentage using key values
	 */
	private Double calculatePercentage(Data data, String region, int key) {
		Double valueRegion, valueAll;
		if (key == Data.DATA_POST_COUNTS) {
			valueRegion = data.getPostValue(region, key);
			valueAll = data.getPostValue(REGION_ALL, key);
		} else {
			valueRegion = data.getAntValue(region, key);
			valueAll = data.getAntValue(REGION_ALL, key);
		}
		if (valueRegion != null && valueAll != null) return valueRegion / valueAll * 100.;
		return null;
	}

	/**
	 * Gets the data with the specified image state. If no data could be retrieved with this image state, then the data
	 * is created.
	 *
	 * @param state Image state associated with the data
	 * @return data created or retrieved (always not null)
	 */
	private Data createOrRetrieveData(ImageState state) {
		// Set the image in the state
		// This is important: the getImage() of a state in a data must return something
		// different than null
		if (state.getIdImage() != ImageState.ID_CUSTOM_IMAGE) state.specifieImage(
				this.selectedImages[state.getIdImage()]);

		// Retrieve data
		Data data = this.results.get(hashState(state));

		// Create data if not existing
		if (data == null) {
			data = new Data(state, this.calculateDeltaTime(state));
			this.results.put(hashState(state), data);
		}

		return data;
	}

	/**
	 * Calculates the DATA_DERIVATIVE of the REGION_STOMACH if necessary.
	 *
	 * @param data          Data on which the derivative will be added
	 * @param state         State of the image analyzed
	 * @param previousState State of the image before the image analyzed
	 */
	private void computeDerivative(Data data, ImageState state, ImageState previousState) {
		Data previousData, dataToInflate;
		if (previousState == null) {
			previousData = this.time0;
		} else previousData = this.results.get(hashState(previousState));

		if (data.getAssociatedImage().getImageOrientation().isDynamic()) dataToInflate = previousData;
		else dataToInflate = data;

		if (previousData != null) {
			Double prevPercentageStomach = previousData.getAntValue(REGION_STOMACH, Data.DATA_PERCENTAGE);
			Double percentageStomach = data.getAntValue(REGION_STOMACH, Data.DATA_PERCENTAGE);
			if (prevPercentageStomach != null && percentageStomach != null) {
				double stomachDerivative = (prevPercentageStomach - percentageStomach) / (this.calculateDeltaTime(
						Library_Dicom.getDateAcquisition(state.getImage().getImagePlus())) -
						previousData.getMinutes()) * 30.;
				dataToInflate.setAntValue(REGION_STOMACH, Data.DATA_DERIVATIVE, stomachDerivative);
			}
		} else {
			System.err.println("Warning: no data found");
		}
	}

	/**
	 * Generates the decay function for the specified data.
	 *
	 * @param data Data for which the decay function will be calculated
	 */
	private void computeDecayFunction(Data data) {
		int delayMs = (int) (data.getMinutes() * 60. * 1000.);
		Double stomachAverage = data.getAntValue(REGION_STOMACH, Data.DATA_GEO_AVG);
		if (stomachAverage != null) {
			double value = Library_Quantif.calculer_countCorrected(delayMs, stomachAverage, isotope);
			data.setAntValue(REGION_STOMACH, Data.DATA_DECAY_CORRECTED, value);
		}
	}

	/**
	 * Calculates the number of counts of the specified region from the given image state.
	 *
	 * @param regionName Region for which the counts will be calculated
	 * @param state      State of the image to do the calculations on
	 * @param roi        ROI on the image where the calculations will be made
	 */
	private void calculateCountsFromImage(String regionName, ImageState state, Roi roi) {
		ImageSelection ims = imageFromState(state);
		ImagePlus imp = ims.getImagePlus();

		// Create data if not existing
		Data data = this.createOrRetrieveData(state);

		// Save value
		imp.setSlice(state.getSlice());
		imp.setRoi(roi);
		if (state.getFacingOrientation() == Orientation.ANT) {
			data.setAntValue(regionName, Data.DATA_ANT_COUNTS, Math.max(0, Library_Quantif.getCounts(imp)), state,
							 roi);
			data.setAntValue(regionName, Data.DATA_PIXEL_COUNTS, imp.getStatistics().pixelCount);
		} else {
			data.setPostValue(regionName, Data.DATA_POST_COUNTS, Math.max(0, Library_Quantif.getCounts(imp)), state,
							  roi);
			data.setPostValue(regionName, Data.DATA_PIXEL_COUNTS, imp.getStatistics().pixelCount);
		}
	}

	/**
	 * Calculates the number of counts for the specified region using existing data.<br> This method must only be
	 * called
	 * when all of the data required are available.
	 *
	 * @param regionName Region for which the counts will be calculated
	 * @param state      State of the image to retrieved the data
	 */
	private void calculateCountsFromData(String regionName, ImageState state) {
		// Create data if not existing
		Data data = this.createOrRetrieveData(state);

		// Calculate value
		double counts, pixels;
		if (regionName.equals(REGION_FUNDUS)) {
			if (state.getFacingOrientation() == Orientation.ANT) {
				counts = data.getAntValue(REGION_STOMACH, Data.DATA_ANT_COUNTS) - data.getAntValue(REGION_ANTRE,
																								   Data.DATA_ANT_COUNTS);
				pixels = data.getAntValue(REGION_STOMACH, Data.DATA_PIXEL_COUNTS) - data.getAntValue(REGION_ANTRE,
																									 Data.DATA_PIXEL_COUNTS);
			} else {
				counts = data.getPostValue(REGION_STOMACH, Data.DATA_POST_COUNTS) - data.getPostValue(REGION_ANTRE,
																									  Data.DATA_POST_COUNTS);
				pixels = data.getPostValue(REGION_STOMACH, Data.DATA_PIXEL_COUNTS) - data.getPostValue(REGION_ANTRE,
																									   Data.DATA_PIXEL_COUNTS);
			}
		} else if (regionName.equals(REGION_INTESTINE)) {
			if (state.getFacingOrientation() == Orientation.ANT) {
				counts = data.getAntValue(REGION_INTESTINE, Data.DATA_ANT_COUNTS) - data.getAntValue(REGION_ANTRE,
																									 Data.DATA_ANT_COUNTS);
				pixels = data.getAntValue(REGION_INTESTINE, Data.DATA_PIXEL_COUNTS) - data.getAntValue(REGION_ANTRE,
																									   Data.DATA_PIXEL_COUNTS);
			} else {
				counts = data.getPostValue(REGION_INTESTINE, Data.DATA_POST_COUNTS) - data.getPostValue(REGION_ANTRE,
																										Data.DATA_POST_COUNTS);
				pixels = data.getPostValue(REGION_INTESTINE, Data.DATA_PIXEL_COUNTS) - data.getPostValue(REGION_ANTRE,
																										 Data.DATA_PIXEL_COUNTS);
			}
		} else throw new UnsupportedOperationException("The region " + regionName + " is not supported here!");

		// Save value
		if (state.getFacingOrientation() == Orientation.ANT) {
			data.setAntValue(regionName, Data.DATA_ANT_COUNTS, Math.max(0, counts), state, null);
			data.setAntValue(regionName, Data.DATA_PIXEL_COUNTS, pixels);
		} else {
			data.setPostValue(regionName, Data.DATA_POST_COUNTS, Math.max(0, counts), state, null);
			data.setPostValue(regionName, Data.DATA_PIXEL_COUNTS, pixels);
		}
	}

	/**
	 * Generates the dataset for the graph of the stomach retention.
	 *
	 * @param unit Unit of the Y axis
	 * @return array in the form:
	 * <ul>
	 * <li><code>[i][0] -> x</code></li>
	 * <li><code>[i][1] -> y</code></li>
	 * </ul>
	 */
	private double[][] generateStomachDataset(Unit unit) {
		return this.generateDatasetFromKey(REGION_STOMACH, Data.DATA_PERCENTAGE, unit);
	}

	/**
	 * Generates the dataset for the graph of the decay function.
	 *
	 * @return array in the form:
	 * <ul>
	 * <li><code>[i][0] -> x</code></li>
	 * <li><code>[i][1] -> y</code></li>
	 * </ul>
	 */
	private double[][] generateDecayFunctionDataset(Unit unit) {
		return this.generateDatasetFromKey(REGION_STOMACH, Data.DATA_DECAY_CORRECTED, unit);
	}

	/**
	 * Generates the series from the specified array.<br> The array must be of the form:
	 * <ul>
	 * <li><code>[i][0] = x</code></li>
	 * <li><code>[i][1] = y</code></li>
	 * </ul>
	 *
	 * @param seriesName Name of the series to generate (used for display)
	 * @param dataset    Data to generate the series
	 * @return Series based on the dataset
	 */
	private XYSeries generateSeriesFromDataset(String seriesName, double[][] dataset) {
		XYSeries series = new XYSeries(seriesName);
		for (double[] doubles : dataset) series.add(doubles[0], doubles[1]);
		return series;
	}

	/**
	 * Retrieves the Y value of the specified dataset.<br> The array must be of the form:
	 * <ul>
	 * <li><code>[i][0] = x</code></li>
	 * <li><code>[i][1] = y</code></li>
	 * </ul>
	 *
	 * @param dataset Data to retrieve the Y values
	 * @return array containing only the Y values
	 */
	private double[] generateYValuesFromDataset(double[][] dataset) {
		double[] yValues = new double[dataset.length];
		int i = 0;
		for (double[] d : dataset)
			yValues[i++] = d[1];
		return yValues;
	}

	/**
	 * Generates the dataset with the values of the data for the specified key.<br> All data that have not the
	 * requested
	 * value will be ignored.
	 *
	 * @param regionName Region on which the data will be retrieved
	 * @param key        Key of the values to retrieve
	 * @param unit       Unit of the output result for the Y axis
	 * @return dataset with the values from the data associated with the region
	 */
	private double[][] generateDatasetFromKey(String regionName, int key, Unit unit) {
		// Get all Y points
		double[] yPoints = this.getAllResultsAsArray(regionName, key, unit);

		// Count results to ignore
		int nbResultsToIgnore = (int) Arrays.stream(yPoints).filter(Double::isNaN).count();

		// Create dataset with right dimensions
		double[][] dataset = new double[yPoints.length - nbResultsToIgnore][2];

		// Get times
		double[] times = this.generateTime();

		// Check dimensions
		if (times.length != yPoints.length) throw new IllegalStateException(
				"The length of the datas (" + yPoints.length + ") is different than the length of the times (" +
						times.length + ")");

		// Fill dataset
		int j = 0;
		for (int i = 0; i < yPoints.length; i++) {
			if (!Double.isNaN(yPoints[i])) {
				dataset[j][0] = times[i];
				dataset[j][1] = yPoints[i];
				j++;
			}
		}

		return dataset;
	}

	/**
	 * Adjusts the percentage with the ratio of eggs in the body.
	 *
	 * @param region         Region where the adjustment will be made
	 * @param percentage     Percentage calculated that will be adjusted
	 * @param numActualImage Number of the image to be adjusted
	 * @param nbTotalImages  Total number of images (total number of eggs ingested)
	 * @return percentage adjusted with the eggs ratio
	 */
	@SuppressWarnings("StringEquality")
	private double adjustPercentageWithEggsRatio(String region, double percentage, int numActualImage,
												 int nbTotalImages) {
		double ratioEggsInBody = (double) numActualImage / (double) nbTotalImages;
		double percentEggsNotInBody = 100. - ratioEggsInBody * 100.;

		if (region == REGION_FUNDUS) return percentEggsNotInBody + percentage * ratioEggsInBody;

		return percentage * ratioEggsInBody;
	}

	/**
	 * Calculates the counts of the specified region.<br> The region must be previously inflated with the correct
	 * state.<br> This method takes care of all necessary operations to do on the ImagePlus or the RoiManager.<br> This
	 * method will create a new data for each new ImageSelection encountered.
	 *
	 * @param regionName Region to calculate
	 * @throws IllegalArgumentException if the region is not part of the requested regions for this model
	 */
	public void calculateCounts(String regionName, ImageState state, Roi roi) throws IllegalArgumentException {
		// Check region is part of requested regions for this model
		if (!Arrays.asList(this.getAllRegionsName()).contains(regionName)) throw new IllegalArgumentException(
				"The region (" + regionName + ") is not requested in this model\nValid regions: " +
						Arrays.toString(this.getAllRegionsName()));

		if (regionName.equals(REGION_STOMACH) || regionName.equals(REGION_ANTRE) || regionName.equals(
				REGION_INTESTINE)) {
			this.calculateCountsFromImage(regionName, state, roi);
		}
		if (regionName.equals(REGION_FUNDUS) || regionName.equals(REGION_INTESTINE)) {
			this.calculateCountsFromData(regionName, state);
		}
	}

	/**
	 * Retrieves the number of counts for the specified region and orientation.<br> The orientation accepted is only
	 * Ant
	 * or Post. If any other orientation is passed, then the result will be returned as if it was a Post
	 * orientation.<br> To use this method, data must be previously entered in this model.
	 *
	 * @param region      Region to get the counts from
	 * @param orientation Orientation for the counts
	 * @return value of the counts for the specified region and orientation
	 * @throws NoSuchElementException if no data could be retrieved for the specified region
	 */
	public double getCounts(Region region, Orientation orientation) throws NoSuchElementException {
		Data data = this.results.get(hashState(region.getState()));
		if (data == null) throw new NoSuchElementException(
				"No data has been set for this image (" + region.getState().getImage().getImagePlus().getTitle() +
						")");

		if (orientation == Orientation.ANT) return data.getAntValue(region.getName(), Data.DATA_ANT_COUNTS);
		else return data.getPostValue(region.getName(), Data.DATA_POST_COUNTS);
	}

	/**
	 * Force the insertion of a percentage value in the data for the specified region.
	 *
	 * @param region Region to set this value on
	 * @param value  Value to force
	 */
	public void forcePercentageDataValue(Region region, double value) {
		this.forceDataValue(region, Data.DATA_PERCENTAGE, Math.max(0, value));
	}

	/**
	 * Force the insertion of a correlation value in the data for the specified region.
	 *
	 * @param region Region to set this value on
	 * @param value  Value to force
	 */
	public void forceCorrelationDataValue(Region region, double value) {
		this.forceDataValue(region, Data.DATA_CORRELATION, value);
	}

	/**
	 * Force the insertion of the number of counts in the data for the specified region.
	 *
	 * @param region Region to set this value on
	 * @param value  Value to force
	 */
	public void forceCountsDataValue(Region region, double value) {
		this.forceDataValue(region, region.getState().getFacingOrientation() == Orientation.ANT ?
				Data.DATA_ANT_COUNTS :
				Data.DATA_POST_COUNTS, Math.max(0, value));
	}

	/**
	 * Sets the background noise for the specified region with the given image and ROI.
	 *
	 * @param regionName Region on which the background will be set
	 * @param state      State the image should be when taking the noise
	 * @param roi        ROI where to take the noise
	 */
	public void setBkgNoise(String regionName, ImageState state, Roi roi) {
		ImagePlus imp = state.getImage().getImagePlus();
		imp.setRoi(roi);

		double bkgNoise = Library_Quantif.getAvgCounts(imp);

		Region region = new Region("Background Noise " + regionName);
		region.inflate(state, roi);
		region.setValue(Data.DATA_BKG_NOISE, bkgNoise);
		region.setValue(Data.DATA_ANT_COUNTS, Library_Quantif.getCounts(imp));
		region.setValue(Data.DATA_PIXEL_COUNTS, Library_Quantif.getPixelNumber(imp));

		switch (regionName) {
			case REGION_ANTRE:
				this.bkgNoise_antre = region;
				break;
			case REGION_INTESTINE:
				this.bkgNoise_intestine = region;
				break;
			case REGION_STOMACH:
				this.bkgNoise_stomach = region;
				double countsFundus = bkgNoise_stomach.getValue(Data.DATA_ANT_COUNTS) - bkgNoise_antre.getValue(
						Data.DATA_ANT_COUNTS);
				double pixelsFundus = bkgNoise_stomach.getValue(Data.DATA_PIXEL_COUNTS) - bkgNoise_antre.getValue(
						Data.DATA_PIXEL_COUNTS);

				this.bkgNoise_fundus = region.clone();
				this.bkgNoise_fundus.setValue(Data.DATA_ANT_COUNTS, countsFundus);
				this.bkgNoise_fundus.setValue(Data.DATA_PIXEL_COUNTS, pixelsFundus);
				this.bkgNoise_fundus.setValue(Data.DATA_BKG_NOISE, countsFundus / pixelsFundus);
				break;
			default:
				throw new IllegalArgumentException("The region (" + region + ") is not a background noise");
		}
	}

	/**
	 * Generates an array of durations for each data from the current time of ingestion.
	 */
	public double[] generateTime() {
		double[] times = new double[this.nbAcquisitions()];

		int i = 0;
		for (Data data : this.generatesDataOrdered()) {
			if (data == time0) times[i] = 0.;
			else times[i] = this.calculateDeltaTime(data.getAssociatedImage().getDateAcquisition());
			i++;
		}

		return times;
	}

	public double[] generateDerivedTime() {
		return ArrayUtils.remove(this.generateTime(), 0);
	}

	/**
	 * @return all regions required by this model
	 */
	private String[] getAllRegionsName() {
		return new String[]{REGION_STOMACH, REGION_ANTRE, REGION_FUNDUS, REGION_INTESTINE};
	}

	/**
	 * Generates the series with the specified ID. The ID must be one of {@link #SERIES_DECAY_FUNCTION} or {@link
	 * #SERIES_STOMACH_PERCENTAGE}.
	 *
	 * @param seriesId ID of the series to generate
	 * @param unit     Unit of the Y axis
	 * @return series for the specified ID
	 */
	public XYSeries generateSeries(int seriesId, Unit unit) {
		switch (seriesId) {
			case SERIES_DECAY_FUNCTION:
				return this.generateDecayFunction(unit);
			case SERIES_STOMACH_PERCENTAGE:
				return this.generateStomachSeries(unit);
			default:
				return null;
		}
	}

	/**
	 * @param unit Unit of the Y axis
	 * @return series for the stomach (used for the graph)
	 */
	public XYSeries generateStomachSeries(Unit unit) {
		return this.generateSeriesFromDataset("Stomach", this.generateStomachDataset(unit));
	}

	/**
	 * @param unit Unit of the Y axis
	 * @return Y values for the stomach
	 */
	private double[] generateStomachValues(Unit unit) {
		return this.generateYValuesFromDataset(generateStomachDataset(unit));
	}

	/**
	 * @return series for the decay function
	 */
	public XYSeries generateDecayFunction(Unit unit) {
		return this.generateSeriesFromDataset("Stomach", this.generateDecayFunctionDataset(unit));
	}

	/**
	 * @return Y values for the decay function
	 */
	private double[] generateDecayFunctionValues(Unit unit) {
		return this.generateYValuesFromDataset(generateDecayFunctionDataset(unit));
	}

	/**
	 * This method returns the number of data this model possesses. If a fictional time 0 has been activated, then it
	 * will be counted as an acquisition.
	 *
	 * @return number of data of this model
	 */
	public int nbAcquisitions() {
		// number of images + the starting point
		return this.results.size() + (this.time0 != null ? 1 : 0);
	}

	/**
	 * @return first image acquired (dynamic image)
	 */
	public ImageSelection getFirstImage() {
		return this.firstImage;
	}

	/**
	 * Sets the dynamic image corresponding to the first image of the model.
	 *
	 * @param firstImage First image acquired (time 0)
	 */
	public void setFirstImage(ImageSelection firstImage) {
		this.firstImage = firstImage;
	}

	/**
	 * @return time when the patient ingested the food
	 */
	public Date getTimeIngestion() {
		return this.timeIngestion;
	}

	/**
	 * Sets the time when the patient ingested the food.
	 *
	 * @param timeIngestion Time of ingestion
	 */
	public void setTimeIngestion(Date timeIngestion) {
		this.timeIngestion = timeIngestion;

		// Refresh all data.getMinutes()s
		for (Data data : this.results.values()) {
			data.setTime(this.calculateDeltaTime(
					Library_Dicom.getDateAcquisition(data.getAssociatedImage().getImagePlus())));
		}
	}

	/**
	 * Computes the data retrieved from the specified state. This method calculates the percentages for each region.
	 * This method should be used when the static acquisition has been made.<br> The {@link Data#DATA_GEO_AVG}
	 * <b>must</b> be defined in every region (except REGION_ALL).<br> If the previous state is not null, then the
	 * derivative is calculated for the stomach.
	 *
	 * @param state         State of the data to retrieve
	 * @param previousState State of the previous data to retrieve (in chronological order)
	 * @throws NoSuchElementException if no data could be retrieved from the specified state
	 */
	void computeStaticData(ImageState state, ImageState previousState) {
		Data data = this.results.get(hashState(state));
		if (data == null) throw new NoSuchElementException(
				"No data has been set for this image (" + state.getImage().getImagePlus().getTitle() + ")");

		this.computeGeometricalAverages(state);

		// Calculate percentages
		// - Fundus
		Double percentageFundus = calculatePercentage(data, REGION_FUNDUS, Data.DATA_GEO_AVG);
		if (percentageFundus != null) data.setAntValue(REGION_FUNDUS, Data.DATA_PERCENTAGE, percentageFundus);

		// - Antre
		Double percentageAntre = calculatePercentage(data, REGION_ANTRE, Data.DATA_GEO_AVG);
		if (percentageAntre != null) data.setAntValue(REGION_ANTRE, Data.DATA_PERCENTAGE, percentageAntre);

		// - Stomach
		Double percentageStomach = null;
		if (percentageAntre != null && percentageFundus != null) {
			percentageStomach = percentageFundus + percentageAntre;
			data.setAntValue(REGION_STOMACH, Data.DATA_PERCENTAGE, percentageStomach);
		}

		// - Intestine
		if (percentageStomach != null) data.setAntValue(REGION_INTESTINE, Data.DATA_PERCENTAGE,
														100. - percentageStomach);

		// Calculate correlation
		if (percentageFundus != null && percentageStomach != null) {
			double fundusCorrelation = percentageFundus / percentageStomach * 100.;
			data.setAntValue(REGION_FUNDUS, Data.DATA_CORRELATION, fundusCorrelation);
		}

		try {
			this.computeDerivative(data, state, previousState);

			this.computeDecayFunction(data);
		} catch (NullPointerException e) {
			// Data missing, the derivative or decay function could not be calculated
		}
	}

	/**
	 * Computes the data retrieved from the specified state. This method calculates the percentages for each region.
	 * This method should be used when the dynamic acquisition has been made.<br> The {@link Data#DATA_ANT_COUNTS}
	 * <b>must</b> be defined in every region (except REGION_ALL).<br> If the previous state is not null, then the
	 * derivative is calculated for the stomach.
	 *
	 * @param state          State of the data to retrieve
	 * @param previousState  State of the previous data to retrieve (in chronological order)
	 * @param numActualImage Image number (1 = first image in chronological order). The first images contains less
	 *                       tracer than the last images.
	 * @param nbTotalImages  Total number of dynamic images
	 * @throws NoSuchElementException if no data could be retrieved from the specified state
	 */
	public void computeDynamicData(ImageState state, ImageState previousState, int numActualImage, int nbTotalImages) {
		Data data = this.results.get(hashState(state));
		if (data == null) throw new NoSuchElementException(
				"No data has been set for this image (" + state.getImage().getImagePlus().getTitle() + ")");

		// Adjust counts with background
		for (Region region : data.getRegions()) {
			if (region.getName().equals(REGION_ALL)) continue;

			Double bkgNoise = null;
			switch (region.getName()) {
				case REGION_ANTRE:
					bkgNoise = this.bkgNoise_antre.getValue(Data.DATA_BKG_NOISE);
					break;
				case REGION_INTESTINE:
					bkgNoise = this.bkgNoise_intestine.getValue(Data.DATA_BKG_NOISE);
					break;
				case REGION_STOMACH:
					bkgNoise = this.bkgNoise_stomach.getValue(Data.DATA_BKG_NOISE);
					break;
				case REGION_FUNDUS:
					bkgNoise = this.bkgNoise_fundus.getValue(Data.DATA_BKG_NOISE);
					break;
				default:
					System.err.println(
							"Warning: The region (" + region + ") is not corrected with a background " + "noise!");
					break;
			}

			if (bkgNoise != null) {
				data.setAntValue(region.getName(), Data.DATA_ANT_COUNTS,
								 data.getAntValue(region.getName(), Data.DATA_ANT_COUNTS) -
										 (bkgNoise * data.getAntValue(region.getName(), Data.DATA_PIXEL_COUNTS)));
				if (bkgNoise == 0.) System.err.println("Warning: The background noise " + region + " is 0.");
			}
		}

		// Calculate total
		data.setAntValue(REGION_ALL, Data.DATA_ANT_COUNTS, data.getAntValue(REGION_STOMACH, Data.DATA_ANT_COUNTS) +
				data.getAntValue(REGION_INTESTINE, Data.DATA_ANT_COUNTS));

		// Adjust percentages with eggs ratio
		double percentage = this.adjustPercentageWithEggsRatio(REGION_FUNDUS, calculatePercentage(data, REGION_FUNDUS,
																								  Data.DATA_ANT_COUNTS),
															   numActualImage, nbTotalImages);
		data.setAntValue(REGION_FUNDUS, Data.DATA_PERCENTAGE, percentage);

		percentage = this.adjustPercentageWithEggsRatio(REGION_ANTRE,
														calculatePercentage(data, REGION_ANTRE, Data.DATA_ANT_COUNTS),
														numActualImage, nbTotalImages);
		data.setAntValue(REGION_ANTRE, Data.DATA_PERCENTAGE, percentage);

		percentage = data.getAntValue(REGION_FUNDUS, Data.DATA_PERCENTAGE) + data.getAntValue(REGION_ANTRE,
																							  Data.DATA_PERCENTAGE);
		data.setAntValue(REGION_STOMACH, Data.DATA_PERCENTAGE, percentage);

		percentage = 100. - data.getAntValue(REGION_STOMACH, Data.DATA_PERCENTAGE);
		data.setAntValue(REGION_INTESTINE, Data.DATA_PERCENTAGE, percentage);

		double fundusDerivative = data.getAntValue(REGION_FUNDUS, Data.DATA_PERCENTAGE) / data.getAntValue(
				REGION_STOMACH, Data.DATA_PERCENTAGE) * 100.;
		data.setAntValue(REGION_FUNDUS, Data.DATA_CORRELATION, fundusDerivative);

		// Compute derivative
		this.computeDerivative(data, state, previousState);
	}

	/**
	 * Creates the graphic for the Intragastric Distribution.
	 *
	 * @return Intragastric distribution graph
	 */
	public ChartPanel createGraph_1() {
		return Library_JFreeChart.createGraph("Time (" + Unit.MINUTES.abbrev() + ")", "Fundus/Stomach (%)",
											  new Color[]{new Color(0, 100, 0)}, "",
											  Library_JFreeChart.createDataset(this.generateTime(),
																			   this.getResultAsArray(REGION_FUNDUS,
																									 Data.DATA_CORRELATION,
																									 Unit.PERCENTAGE),
																			   "Intragastric Distribution"));
	}

	/**
	 * Creates the graphic for the Gastrointestinal flow.
	 *
	 * @return Gastrointestinal flow graph
	 */
	public ChartPanel createGraph_2() {
		double[] result = this.getResultAsArray(REGION_STOMACH, Data.DATA_DERIVATIVE, Unit.PERCENTAGE);
		return Library_JFreeChart.createGraph("Time (" + Unit.MINUTES.abbrev() + ")", "% meal in the interval",
											  new Color[]{Color.RED}, "",
											  Library_JFreeChart.createDataset(this.generateDerivedTime(), result,
																			   "Gastrointestinal flow"));
	}

	/**
	 * Creates the graphic for the Stomach, Fundus and Antrum percentages.
	 *
	 * @return graph
	 */
	public ChartPanel createGraph_3() {
		double[][] ySeries = new double[][]{this.getResultAsArray(REGION_STOMACH, Data.DATA_PERCENTAGE,
																  Unit.PERCENTAGE),
				this.getResultAsArray(REGION_FUNDUS,
																										  Data.DATA_PERCENTAGE,
																										  Unit.PERCENTAGE),
				this.getResultAsArray(REGION_ANTRE, Data.DATA_PERCENTAGE, Unit.PERCENTAGE)};
		String[] titles = new String[]{"Stomach", "Fundus", "Antrum"};
		Color[] colors = new Color[]{Color.RED, new Color(0, 255, 0), Color.BLUE};

		XYSeriesCollection dataset = Library_JFreeChart.createDataset(this.generateTime(), ySeries, titles);

		return Library_JFreeChart.createGraph("Time (" + Unit.MINUTES.abbrev() + ")", "Retention (% meal)", colors, "",
											  dataset);
	}

	/**
	 * Creates the graphic for the Stomach geometrical average.
	 *
	 * @return graph
	 */
	public ChartPanel createGraph_4(Unit unit) {
		double[] result = this.getResultAsArray(REGION_STOMACH, Data.DATA_GEO_AVG, unit);

		XYSeriesCollection dataset = Library_JFreeChart.createDataset(this.generateTime(), result,
																	  "Stomach " + "retention");

		return Library_JFreeChart.createGraph(Unit.MINUTES.abbrev(), unit.abbrev(), new Color[]{Color.GREEN}, "",
											  dataset);
	}

	/**
	 * Activates the fictional time 0 representing the moment when the fundus contains all of the food.
	 */
	public void activateTime0() {
		this.time0 = new Data(null, 0.);
		this.time0.setTime(0.);
		this.time0.setAntValue(REGION_STOMACH, Data.DATA_PERCENTAGE, 100., null, null);
		this.time0.setAntValue(REGION_FUNDUS, Data.DATA_PERCENTAGE, 100., null, null);
		this.time0.setAntValue(REGION_ANTRE, Data.DATA_PERCENTAGE, 0., null, null);
		this.time0.setAntValue(REGION_INTESTINE, Data.DATA_PERCENTAGE, 0., null, null);

		this.time0.setAntValue(REGION_FUNDUS, Data.DATA_CORRELATION, 100., null, null);
	}

	/**
	 * Deactivates the fictional time 0.<br> The ingestion time should now be set to the time of the first dynamic
	 * acquisition.
	 *
	 * @see #setTimeIngestion
	 */
	public void deactivateTime0() {
		this.time0 = null;
	}

	/**
	 * Delivers the retention percentage at the specified time.<br> The result might be interpolated.
	 *
	 * @param request Request for the result (only {@link #RETENTION_GEOAVG} or {@link #RETENTION_PERCENTAGE} allowed)
	 * @param time    Time to observe in minutes
	 * @return retention percentage
	 */
	public ResultValue getRetentionResult(ResultRequest request, double time) {
		Result result = request.getResultOn();
		if (result != RETENTION_GEOAVG && result != RETENTION_PERCENTAGE) throw new IllegalArgumentException(
				"The result " + result + " not supported here!");

		double[] yValues;
		if (result == RETENTION_PERCENTAGE) {
			yValues = generateStomachValues(request.getFit().getYUnit());
		} else {
			yValues = generateDecayFunctionValues(request.getFit().getYUnit());
		}

		Double res = Library_JFreeChart.getY(this.generateTime(), yValues, time);
		boolean isExtrapolated = false;
		if (res == null) {
			res = Library_JFreeChart.extrapolateY(time, request.getFit());
			isExtrapolated = true;
		}

		// Percentage of res
		res = res * 100. / yValues[0];

		return new ResultValue(request, res, Unit.PERCENTAGE, isExtrapolated);
	}

	/**
	 * Delivers the requested result.<br> This method must be called only when all of the data was incorporated in this
	 * model.<br>
	 *
	 * @param request Request for a result
	 * @return ResultValue containing the requested result or null if the result
	 */
	@Override
	public ResultValue getResult(ResultRequest request) {
		Data data = this.generatesDataOrdered().get(request.getIndexImage());
		Result result = request.getResultOn();
		Fit fit = request.getFit();

		if (result == START_ANTRUM) return new ResultValue(request, this.getDebut(REGION_ANTRE), Unit.TIME);
		else if (result == START_INTESTINE) return new ResultValue(request, this.getDebut(REGION_INTESTINE),
																   Unit.TIME);
		else if (result == LAG_PHASE_PERCENTAGE || result == LAG_PHASE_GEOAVG) {
			double[] yValues;
			if (result == LAG_PHASE_PERCENTAGE) yValues = generateStomachValues(fit.getYUnit());
			else yValues = generateDecayFunctionValues(fit.getYUnit());

			// Assumption: the first value is the highest (maybe do not assume that...?)
			double yValue = .95 * yValues[0];
			Double valX = Library_JFreeChart.getX(this.generateTime(), yValues, yValue);
			boolean isExtrapolated = false;
			if (valX == null) {
				// Extrapolate
				valX = Library_JFreeChart.extrapolateX(yValue, fit);
				isExtrapolated = true;
			}

			// Convert to requested unit
			valX = Unit.TIME.convertTo(valX, request.getUnit());

			return new ResultValue(request, valX, request.getUnit(), isExtrapolated);
		} else if (result == T_HALF_PERCENTAGE || result == T_HALF_GEOAVG) {
			double[] yValues;
			if (result == T_HALF_PERCENTAGE) yValues = generateStomachValues(fit.getYUnit());
			else yValues = generateDecayFunctionValues(fit.getYUnit());

			// Assumption: the first value is the highest (maybe do not assume that...?)
			double half = yValues[0] / 2.;
			boolean isExtrapolated = false;
			Double valX = Library_JFreeChart.getX(this.generateTime(), yValues, half);
			if (valX == null) {
				// Extrapolate
				valX = Library_JFreeChart.extrapolateX(half, fit);
				isExtrapolated = true;
			}

			// Convert to requested unit
			valX = Unit.TIME.convertTo(valX, request.getUnit());

			return new ResultValue(request, valX, request.getUnit(), isExtrapolated);
		} else {
			try {
				if (result == RES_TIME) return new ResultValue(request, BigDecimal.valueOf(data.getMinutes()).setScale(
						2, RoundingMode.HALF_UP).doubleValue(), Unit.TIME);
				if (result == RES_STOMACH) return new ResultValue(request, BigDecimal.valueOf(
						data.getAntValue(REGION_STOMACH, Data.DATA_PERCENTAGE)).setScale(2,
																						 RoundingMode.HALF_UP).doubleValue(),
																  Unit.PERCENTAGE);
				if (result == RES_FUNDUS) return new ResultValue(request, BigDecimal.valueOf(
						data.getAntValue(REGION_FUNDUS, Data.DATA_PERCENTAGE)).setScale(2,
																						RoundingMode.HALF_UP).doubleValue(),
																 Unit.PERCENTAGE);
				if (result == RES_ANTRUM) return new ResultValue(request, BigDecimal.valueOf(
						data.getAntValue(REGION_ANTRE, Data.DATA_PERCENTAGE)).setScale(2,
																					   RoundingMode.HALF_UP).doubleValue(),
																 Unit.PERCENTAGE);
				if (result == RES_STOMACH_COUNTS) {
					return new ResultValue(request, BigDecimal.valueOf(
							data.getAntValue(REGION_STOMACH, Data.DATA_GEO_AVG)).setScale(2,
																						  RoundingMode.HALF_UP).doubleValue(),
										   Unit.COUNTS);
				} else {
					throw new IllegalArgumentException("Result " + result + " not supported here!");
				}
			} catch (NullPointerException e) {
				// Exception = data not found
				return null;
			}
		}
	}

	@Override
	public void calculateResults() {
		this.generateTime();
	}
}