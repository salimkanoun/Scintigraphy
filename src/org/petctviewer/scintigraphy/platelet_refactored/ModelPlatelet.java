package org.petctviewer.scintigraphy.platelet_refactored;

import ij.ImagePlus;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.gastric.Region;
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
		//TODO
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
			Region region = this.regionsPost.get(regionName);
			if (region == null) region = this.regionsAnt.get(regionName);
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
			Region region = this.regionsPost.get(regionName);
			if (region == null) region = this.regionsAnt.get(regionName);
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
	}
}
