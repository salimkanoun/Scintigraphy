package org.petctviewer.scintigraphy.platelet_refactored;

import ij.ImagePlus;
import ij.gui.Roi;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.gastric.Region;
import org.petctviewer.scintigraphy.gastric.Result;
import org.petctviewer.scintigraphy.gastric.ResultRequest;
import org.petctviewer.scintigraphy.gastric.ResultValue;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelPlatelet extends ModelWorkflow {

	public static final String REGION_SPLEEN = "Spleen", REGION_LIVER = "Liver", REGION_HEART = "Heart";

	public static final int DATA_ANT_COUNTS = 0, DATA_POST_COUNTS = 1, DATA_GEO_AVG = 2;

	public static final Result RES_RATIO_SPLEEN_HEART = new Result("Ratio Spleen / Heart"), RES_RATIO_LIVER_HEART =
			new Result("Ratio Liver / Heart"), RES_RATIO_SPLEEN_LIVER = new Result("Ratio Spleen / Liver"),
			RES_SPLEEN_CORRECTED = new Result("Spleen Corrected"), RES_RATIO_GEO_SPLEEN_HEART =
			new Result("Ratio GM Spleen / Heart"), RES_RATIO_GEO_LIVER_HEART = new Result("Ratio GM Liver / Heart"),
			RES_RATIO_GEO_SPLEEN_LIVER = new Result("Ratio GM Spleen / Liver");

	private List<Data> datas;

	/**
	 * @param selectedImages Images used for this study (generally those images are used in the workflows)
	 * @param studyName      Name of this study (used for display)
	 */
	public ModelPlatelet(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);

		final int length;
		if (selectedImages[0].getImageOrientation() == Orientation.ANT_POST) length = selectedImages.length * 2;
		else length = selectedImages.length;
		this.datas = new ArrayList<>(length);
	}

	private double calculateDeltaTime(ImageSelection ims) {
		if (this.datas.size() > 0) return ims.getDateAcquisition().getTime() / 1000. / 60. - this.datas.get(0).time;
		return 0;
	}

	private Data createOrRetrieveData(ImageSelection ims) {
		return this.datas.stream().filter(d -> d.associatedImage == ims).findFirst()
				.orElse(new Data(ims, this.calculateDeltaTime(ims)));
	}

	private String[] allRegions() {
		return new String[]{REGION_SPLEEN, REGION_LIVER, REGION_HEART};
	}

	/**
	 * This method takes care of all necessary operations to do on the ImagePlus or the RoiManager. This requires the
	 * state to contain all of the required data.
	 *
	 * @param regionName Region to calculate
	 * @param state      State the image must be to do the calculations
	 * @param roi        Region where the calculates must be made
	 */
	public void addData(String regionName, ImageState state, Roi roi) {
		// Save the image in the state
		state.specifieImage(this.imageFromState(state));
		state.setIdImage(ImageState.ID_CUSTOM_IMAGE);

		ImagePlus imp = state.getImage().getImagePlus();

		// Find value
		imp.setSlice(state.getSlice());
		imp.setRoi(roi);
		double value = Library_Quantif.getCounts(imp);

		Data data;
		if (state.getFacingOrientation() == Orientation.ANT) {
			data = createOrRetrieveData(state.getImage());
			data.setValue(regionName, DATA_ANT_COUNTS, value, state, roi);
		} else {
			data = createOrRetrieveData(state.getImage());
			data.setValue(regionName, DATA_POST_COUNTS, value, state, roi);
		}
		this.datas.add(data);
	}

	private XYSeries series(String name, String regionName, int key) {
		XYSeries series = new XYSeries(name);
		for (Data data : this.datas) {
			series.add(data.time, data.getValue(regionName, key));
		}
		return series;
	}

	public XYSeries seriesSpleenHeart() {
		XYSeries series = new XYSeries("Mean Ratio Spleen / Heart Post");
		for (Data data : this.datas) {
			double value =
					data.getValue(REGION_SPLEEN, DATA_POST_COUNTS) / data.getValue(REGION_HEART, DATA_POST_COUNTS);
			series.add(data.time, value);
		}
		return series;
	}

	public XYSeries seriesLiverHeart() {
		XYSeries series = new XYSeries("Mean Ratio Liver / Heart Post");
		for (Data data : this.datas) {
			double value =
					data.getValue(REGION_LIVER, DATA_POST_COUNTS) / data.getValue(REGION_HEART, DATA_POST_COUNTS);
			series.add(data.time, value);
		}
		return series;
	}

	public XYSeries seriesSpleenLiver() {
		XYSeries series = new XYSeries("Mean Ratio Spleen / Liver Post");
		for (Data data : this.datas) {
			double value =
					data.getValue(REGION_SPLEEN, DATA_POST_COUNTS) / data.getValue(REGION_LIVER, DATA_POST_COUNTS);
			series.add(data.time, value);
		}
		return series;
	}

	public XYSeries seriesSpleenPost() {
		XYSeries series = new XYSeries("Corrected Spleen Posterior");
		for (Data data : this.datas) {
			double value = Library_Quantif
					.applyDecayFraction((int) (data.time * 1000. * 60.), data.getValue(REGION_SPLEEN,
							DATA_POST_COUNTS),
							this.isotope);
			series.add(data.time, value);
		}
		return series;
	}

	public XYSeries seriesGMSpleenHeart() {
		XYSeries series = new XYSeries("Ratio GM Spleen / Heart Post");
		for (Data data : this.datas) {
			double value = data.getValue(REGION_SPLEEN, DATA_GEO_AVG) / data.getValue(REGION_HEART, DATA_GEO_AVG);
			series.add(data.time, value);
		}
		return series;
	}

	public XYSeries seriesGMLiverHeart() {
		XYSeries series = new XYSeries("Ratio GM Liver / Heart Post");
		for (Data data : this.datas) {
			double value = data.getValue(REGION_LIVER, DATA_GEO_AVG) / data.getValue(REGION_HEART, DATA_GEO_AVG);
			series.add(data.time, value);
		}
		return series;
	}

	public XYSeries seriesGMSpleenLiver() {
		XYSeries series = new XYSeries("Ratio GM Spleen / Liver Post");
		for (Data data : this.datas) {
			double value = data.getValue(REGION_SPLEEN, DATA_GEO_AVG) / data.getValue(REGION_LIVER, DATA_GEO_AVG);
			series.add(data.time, value);
		}
		return series;
	}

	@Override
	public ResultValue getResult(ResultRequest request) {
		return null;
	}

	@Override
	public String nameOfDataField(int key) {
		switch (key) {
			case DATA_ANT_COUNTS:
				return "Nb Ant-counts";
			case DATA_POST_COUNTS:
				return "Nb Post-counts";
			case DATA_GEO_AVG:
				return "Geo-avg";
			default:
				return "???";
		}
	}

	@Override
	public void calculateResults() {
		// Compute geometrical averages if necessary
		if (!this.datas.get(0).regionsAnt.isEmpty()) {
			for (Data data : this.datas) {
				for (String regionName : this.allRegions()) {
					// Average
					double avg = Library_Quantif.moyGeom(data.getValue(regionName, DATA_ANT_COUNTS),
							data.getValue(regionName, DATA_POST_COUNTS));
					data.setValue(regionName, DATA_GEO_AVG, avg);
				}
			}
		}
	}

	/**
	 * This class stores the data measured or calculated for each region of this model. It is associated with an
	 * ImageSelection.
	 *
	 * @author Titouan QUÉMA
	 */
	private class Data {
		private final Map<String, Region> regionsAnt;
		private final Map<String, Region> regionsPost;

		private ImageSelection associatedImage;
		private double time;

		Data(ImageSelection associatedImage, double time) {
			this.associatedImage = associatedImage;
			this.time = time;
			this.regionsAnt = new HashMap<>();
			this.regionsPost = new HashMap<>();
		}

		/**
		 * Associates a value to the specified key for the region defined. The region is first searched through the
		 * Post regions and then through the Ant regions. If the region doesn't exist in any of those maps, then a
		 * exception is thrown.
		 *
		 * @param regionName Name of the region to insert the value into
		 * @param key        Key to store the value
		 * @param value      Value to store
		 * @throws IllegalArgumentException if no region exists with this region name
		 */
		public void setValue(String regionName, int key, double value) throws IllegalArgumentException {
			// Find region
			Region region;
			if(key == DATA_ANT_COUNTS)
				region = this.regionsAnt.get(regionName);
			else
				region = this.regionsPost.get(regionName);
			if (region == null) throw new IllegalArgumentException("The region (" + regionName + ") doesn't exist");

			// Set value
			region.setValue(key, value);
		}

		/**
		 * Associates a value to the specified key for the region defined. The region is first searched through the
		 * Post regions and then through the Ant regions. If the region doesn't exist in any of those maps, then a
		 * new region is created.
		 *
		 * @param regionName Name of the region to insert the value into
		 * @param key        Key to store the value
		 * @param value      Value to store
		 */
		public void setValue(String regionName, int key, double value, ImageState state, Roi roi) {
			// Find region
			Region region;
			if(key == DATA_ANT_COUNTS)
				region = this.regionsAnt.get(regionName);
			else
				region = this.regionsPost.get(regionName);
			if (region == null) {
				// Create region
				region = new Region(regionName, ModelPlatelet.this);
				region.inflate(state, roi);
				// Add region in maps (default in Post)
				if (key == DATA_ANT_COUNTS) this.regionsAnt.put(regionName, region);
				else this.regionsPost.put(regionName, region);
			}

			// Set value
			region.setValue(key, value);
		}

		/**
		 * Gets the value associated with the specified key. The region will be searched
		 * in the Ant regions.<br>
		 * If the region could not be found, then returns null.
		 *
		 * @param regionName Region for which the value will be retrieved
		 * @param key        Key of the value to get
		 * @return value associated with the key for the region or null if not found
		 */
		public Double getAntValue(String regionName, int key) {
			Region region = this.regionsAnt.get(regionName);
			if (region == null) return null;
			return region.getValue(key);
		}

		/**
		 * Gets the value associated with the specified key. The region will be searched
		 * in the Post regions.<br>
		 * If the region could not be found, then returns null.
		 *
		 * @param regionName Region for which the value will be retrieved
		 * @param key        Key of the value to get
		 * @return value associated with the key for the region or null if not found
		 */
		public Double getPostValue(String regionName, int key) {
			Region region = this.regionsPost.get(regionName);
			if (region == null) return null;
			return region.getValue(key);
		}

		/**
		 * Gets the value associated with the specified key. This method will try to
		 * determine if the region is Ant or Post.<br>
		 * If the key contains 'Ant' keyword, then the value will be searched in the
		 * Ant regions. For any other key, then the value will be searched in the Post
		 * regions.<br>
		 * If the region could not be found in the Ant or Post, then this method returns
		 * null.
		 *
		 * @param regionName Region for which the value will be retrieved
		 * @param key        Key of the value to get
		 * @return value associated with the key for the region or null if not found
		 */
		public Double getValue(String regionName, int key) {
			if (key == DATA_ANT_COUNTS) return this.getAntValue(regionName, key);
			return this.getPostValue(regionName, key);
		}
	}
}
