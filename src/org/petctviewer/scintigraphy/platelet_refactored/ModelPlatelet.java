package org.petctviewer.scintigraphy.platelet_refactored;

import ij.ImagePlus;
import ij.gui.Roi;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.gastric.Region;
import org.petctviewer.scintigraphy.gastric.ResultRequest;
import org.petctviewer.scintigraphy.gastric.ResultValue;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Debug;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;

import java.util.*;

public class ModelPlatelet extends ModelWorkflow {

	public static final String REGION_SPLEEN = "Spleen", REGION_LIVER = "Liver", REGION_HEART = "Heart";

	public static final int DATA_ANT_COUNTS = 0, DATA_POST_COUNTS = 1, DATA_GEO_AVG = 2, DATA_MEAN_ANT_COUNTS = 3,
			DATA_MEAN_POST_COUNTS = 4, DATA_MEAN_GEO_AVG = 5;

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

	private Data createOrRetrieveData(ImageState state) {
		Data data = this.datas.stream().filter(d -> d.getAssociatedImage() == state.getImage()).findFirst().orElse(
				null);
		if (data == null) {
			Date time0 = (this.datas.size() > 0 ? this.datas.get(0).getAssociatedImage().getDateAcquisition() :
					state.getImage().getDateAcquisition());
			data = new Data(state, Library_Quantif.calculateDeltaTime(time0, state.getImage().getDateAcquisition()));
		}
		return data;
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
			data = createOrRetrieveData(state);
			data.setValue(regionName, DATA_ANT_COUNTS, value, state, roi);
			data.setValue(regionName, DATA_MEAN_ANT_COUNTS, Library_Quantif.getAvgCounts(imp));
		} else {
			data = createOrRetrieveData(state);
			data.setValue(regionName, DATA_POST_COUNTS, value, state, roi);
			data.setValue(regionName, DATA_MEAN_POST_COUNTS, Library_Quantif.getAvgCounts(imp));
		}
		this.datas.add(data);
	}

	public XYSeries seriesSpleenHeart(boolean mean) {
		int type;
		String title = "Ratio Spleen / Heart Post";
		if (mean) {
			type = DATA_MEAN_POST_COUNTS;
			title = "Mean " + title;
		} else type = DATA_POST_COUNTS;

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getValue(REGION_SPLEEN, type) / data.getValue(REGION_HEART, type);
			series.add(data.time / 60., value);
		}
		return series;
	}

	public XYSeries seriesLiverHeart(boolean mean) {
		int type;
		String title = "Ratio Liver / Heart Post";
		if (mean) {
			type = DATA_MEAN_POST_COUNTS;
			title = "Mean " + title;
		} else type = DATA_POST_COUNTS;

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getValue(REGION_LIVER, type) / data.getValue(REGION_HEART, type);
			series.add(data.time / 60., value);
		}
		return series;
	}

	public XYSeries seriesSpleenLiver(boolean mean) {
		int type;
		String title = "Ratio Spleen / Liver Post";
		if (mean) {
			type = DATA_MEAN_POST_COUNTS;
			title = "Mean " + title;
		} else type = DATA_POST_COUNTS;

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getValue(REGION_SPLEEN, type) / data.getValue(REGION_LIVER, type);
			series.add(data.time / 60., value);
		}
		return series;
	}

	public XYSeries seriesSpleenPost(boolean mean) {
		int type;
		String title = "Decay Corrected Spleen Posterior";
		if (mean) {
			type = DATA_MEAN_POST_COUNTS;
			title += " - Mean";
		} else {
			type = DATA_POST_COUNTS;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = Library_Quantif.applyDecayFraction((int) (data.time * 1000. * 60.),
															  data.getValue(REGION_SPLEEN, type), this.isotope);
			series.add(data.time / 60., value);
		}
		return series;
	}

	public XYSeries seriesGMSpleenHeart(boolean mean) {
		int type;
		String title = "Ratio GM Spleen / Heart";
		if (mean) {
			type = DATA_MEAN_GEO_AVG;
			title = "Mean " + title;
		} else {
			type = DATA_GEO_AVG;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getValue(REGION_SPLEEN, type) / data.getValue(REGION_HEART, type);
			series.add(data.time / 60., value);
		}
		return series;
	}

	public XYSeries seriesGMLiverHeart(boolean mean) {
		int type;
		String title = "Ratio GM Liver / Heart";
		if (mean) {
			type = DATA_MEAN_GEO_AVG;
			title = "Mean " + title;
		} else {
			type = DATA_GEO_AVG;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getValue(REGION_LIVER, type) / data.getValue(REGION_HEART, type);
			series.add(data.time / 60., value);
		}
		return series;
	}

	public XYSeries seriesGMSpleenLiver(boolean mean) {
		int type;
		String title = "Ratio GM Spleen / Liver";
		if (mean) {
			type = DATA_MEAN_GEO_AVG;
			title = "Mean " + title;
		} else {
			type = DATA_GEO_AVG;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getValue(REGION_SPLEEN, type) / data.getValue(REGION_LIVER, type);
			series.add(data.time / 60., value);
		}
		return series;
	}

	public XYSeries seriesSpleenRatio(boolean geoAvg, boolean mean) {
		int type;
		String title = "Spleen Jx / J0";
		if (mean) {
			if (geoAvg) type = DATA_MEAN_GEO_AVG;
			else type = DATA_MEAN_POST_COUNTS;
			title += " - Mean";
		} else {
			if (geoAvg) type = DATA_GEO_AVG;
			else type = DATA_POST_COUNTS;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getValue(REGION_SPLEEN, type) / datas.get(0).getValue(REGION_SPLEEN, type);
			series.add(data.time / 60., value);
		}
		return series;
	}

	public XYSeries seriesLiverRatio(boolean geoAvg, boolean mean) {
		int type;
		String title = "Liver Jx / J0";
		if (mean) {
			if (geoAvg) type = DATA_MEAN_GEO_AVG;
			else type = DATA_MEAN_POST_COUNTS;
			title += " - Mean";
		} else {
			if (geoAvg) type = DATA_GEO_AVG;
			else type = DATA_POST_COUNTS;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getValue(REGION_LIVER, type) / datas.get(0).getValue(REGION_LIVER, type);
			series.add(data.time / 60., value);
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
					double meanAvg = Library_Quantif.moyGeom(data.getValue(regionName, DATA_MEAN_ANT_COUNTS),
															 data.getValue(regionName, DATA_MEAN_POST_COUNTS));
					data.setValue(regionName, DATA_GEO_AVG, avg);
					data.setValue(regionName, DATA_MEAN_GEO_AVG, meanAvg);
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

		private ImageState state;
		private double time;

		Data(ImageState state, double time) {
			this.state = state;
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
			if (key == DATA_ANT_COUNTS) region = this.regionsAnt.get(regionName);
			else region = this.regionsPost.get(regionName);
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
			if (key == DATA_ANT_COUNTS) {
				region = this.regionsAnt.get(regionName);
				if (region == null) {
					// Create region
					region = new Region(regionName, ModelPlatelet.this);
					region.inflate(state, roi);
					this.regionsAnt.put(regionName, region);
				}
			} else {
				region = this.regionsPost.get(regionName);
				if (region == null) {
					// Create region
					region = new Region(regionName, ModelPlatelet.this);
					region.inflate(state, roi);
					this.regionsPost.put(regionName, region);
				}
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

		public int getHash() {
			return this.state.hashCode();
		}

		public ImageSelection getAssociatedImage() {
			return this.state.getImage();
		}

		/**
		 * Generates a string with the regions contained in this data for the specified
		 * orientation.
		 *
		 * @param orientation Ant or Post orientation to get the regions
		 * @return string with the list of the region
		 */
		private String listRegions(Orientation orientation) {
			StringBuilder res = new StringBuilder();
			if (orientation == Orientation.ANT) {
				res.append(Library_Debug.subtitle("ANT REGIONS"));
				res.append('\n');

				if (this.regionsAnt.size() == 0) res.append("// NO REGION //\n");
				else for (Region region : this.regionsAnt.values()) {
					res.append(region);
					res.append('\n');
				}
			} else {
				res.append(Library_Debug.subtitle("POST REGIONS"));
				res.append('\n');

				if (this.regionsPost.size() == 0) res.append("// NO REGION //\n");
				else for (Region region : this.regionsPost.values()) {
					res.append(region);
					res.append('\n');
				}
			}
			return res.toString();
		}

		@Override
		public String toString() {
			String s = Library_Debug.separator();
			String imageTitle = (this.getAssociatedImage() == null ? "// NO-IMAGE //" :
					this.getAssociatedImage().getImagePlus().getTitle());
			s += Library_Debug.title("Data");
			s += "\n";
			s += Library_Debug.title(imageTitle);
			s += "\n";
			s += this.listRegions(Orientation.ANT);
			s += this.listRegions(Orientation.POST);
			s += Library_Debug.separator();
			return s;
		}
	}
}
