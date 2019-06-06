package org.petctviewer.scintigraphy.platelet;

import ij.ImagePlus;
import ij.gui.Roi;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.Data;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ModelPlatelet extends ModelWorkflow {

	public static final String REGION_SPLEEN = "Spleen", REGION_LIVER = "Liver", REGION_HEART = "Heart";

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

		Data data = createOrRetrieveData(state);
		if (state.getFacingOrientation() == Orientation.ANT) {
			data.setAntValue(regionName, Data.DATA_ANT_COUNTS, value, state, roi);
			data.setAntValue(regionName, Data.DATA_MEAN_ANT_COUNTS, Library_Quantif.getAvgCounts(imp));
		} else {
			data.setPostValue(regionName, Data.DATA_POST_COUNTS, value, state, roi);
			data.setPostValue(regionName, Data.DATA_MEAN_POST_COUNTS, Library_Quantif.getAvgCounts(imp));
		}
		this.datas.add(data);
	}

	public XYSeries seriesSpleenHeart(boolean mean) {
		int type;
		String title = "Ratio Spleen / Heart Post";
		if (mean) {
			type = Data.DATA_MEAN_POST_COUNTS;
			title = "Mean " + title;
		} else type = Data.DATA_POST_COUNTS;

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getPostValue(REGION_SPLEEN, type) / data.getPostValue(REGION_HEART, type);
			series.add(data.getMinutes() / 60., value);
		}
		return series;
	}

	public XYSeries seriesLiverHeart(boolean mean) {
		int type;
		String title = "Ratio Liver / Heart Post";
		if (mean) {
			type = Data.DATA_MEAN_POST_COUNTS;
			title = "Mean " + title;
		} else type = Data.DATA_POST_COUNTS;

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getPostValue(REGION_LIVER, type) / data.getPostValue(REGION_HEART, type);
			series.add(data.getMinutes() / 60., value);
		}
		return series;
	}

	public XYSeries seriesSpleenLiver(boolean mean) {
		int type;
		String title = "Ratio Spleen / Liver Post";
		if (mean) {
			type = Data.DATA_MEAN_POST_COUNTS;
			title = "Mean " + title;
		} else type = Data.DATA_POST_COUNTS;

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getPostValue(REGION_SPLEEN, type) / data.getPostValue(REGION_LIVER, type);
			series.add(data.getMinutes() / 60., value);
		}
		return series;
	}

	public XYSeries seriesSpleenPost(boolean mean) {
		int type;
		String title = "Decay Corrected Spleen Posterior";
		if (mean) {
			type = Data.DATA_MEAN_POST_COUNTS;
			title += " - Mean";
		} else {
			type = Data.DATA_POST_COUNTS;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = Library_Quantif.applyDecayFraction((int) (data.getMinutes() * 1000. * 60.),
															  data.getPostValue(REGION_SPLEEN, type), this.isotope);
			series.add(data.getMinutes() / 60., value);
		}
		return series;
	}

	public XYSeries seriesGMSpleenHeart(boolean mean) {
		int type;
		String title = "Ratio GM Spleen / Heart";
		if (mean) {
			type = Data.DATA_MEAN_GEO_AVG;
			title = "Mean " + title;
		} else {
			type = Data.DATA_GEO_AVG;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getPostValue(REGION_SPLEEN, type) / data.getPostValue(REGION_HEART, type);
			series.add(data.getMinutes() / 60., value);
		}
		return series;
	}

	public XYSeries seriesGMLiverHeart(boolean mean) {
		int type;
		String title = "Ratio GM Liver / Heart";
		if (mean) {
			type = Data.DATA_MEAN_GEO_AVG;
			title = "Mean " + title;
		} else {
			type = Data.DATA_GEO_AVG;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getPostValue(REGION_LIVER, type) / data.getPostValue(REGION_HEART, type);
			series.add(data.getMinutes() / 60., value);
		}
		return series;
	}

	public XYSeries seriesGMSpleenLiver(boolean mean) {
		int type;
		String title = "Ratio GM Spleen / Liver";
		if (mean) {
			type = Data.DATA_MEAN_GEO_AVG;
			title = "Mean " + title;
		} else {
			type = Data.DATA_GEO_AVG;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getPostValue(REGION_SPLEEN, type) / data.getPostValue(REGION_LIVER, type);
			series.add(data.getMinutes() / 60., value);
		}
		return series;
	}

	public XYSeries seriesSpleenRatio(boolean geoAvg, boolean mean) {
		int type;
		String title = "Spleen Jx / J0";
		if (mean) {
			if (geoAvg) type = Data.DATA_MEAN_GEO_AVG;
			else type = Data.DATA_MEAN_POST_COUNTS;
			title += " - Mean";
		} else {
			if (geoAvg) type = Data.DATA_GEO_AVG;
			else type = Data.DATA_POST_COUNTS;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getPostValue(REGION_SPLEEN, type) / datas.get(0).getPostValue(REGION_SPLEEN, type);
			series.add(data.getMinutes() / 60., value);
		}
		return series;
	}

	public XYSeries seriesLiverRatio(boolean geoAvg, boolean mean) {
		int type;
		String title = "Liver Jx / J0";
		if (mean) {
			if (geoAvg) type = Data.DATA_MEAN_GEO_AVG;
			else type = Data.DATA_MEAN_POST_COUNTS;
			title += " - Mean";
		} else {
			if (geoAvg) type = Data.DATA_GEO_AVG;
			else type = Data.DATA_POST_COUNTS;
		}

		XYSeries series = new XYSeries(title);
		for (Data data : this.datas) {
			double value = data.getPostValue(REGION_LIVER, type) / datas.get(0).getPostValue(REGION_LIVER, type);
			series.add(data.getMinutes() / 60., value);
		}
		return series;
	}

	@Override
	public ResultValue getResult(ResultRequest request) {
		return null;
	}

	@Override
	public void calculateResults() {
		// Compute geometrical averages if necessary
		if (this.datas.get(0).hasAntData()) {
			for (Data data : this.datas) {
				for (String regionName : this.allRegions()) {
					// Average
					double avg = Library_Quantif.moyGeom(data.getAntValue(regionName, Data.DATA_ANT_COUNTS),
														 data.getPostValue(regionName, Data.DATA_POST_COUNTS));
					double meanAvg = Library_Quantif.moyGeom(data.getAntValue(regionName, Data.DATA_MEAN_ANT_COUNTS),
															 data.getPostValue(regionName,
																			   Data.DATA_MEAN_POST_COUNTS));
					data.setPostValue(regionName, Data.DATA_GEO_AVG, avg);
					data.setPostValue(regionName, Data.DATA_MEAN_GEO_AVG, meanAvg);
				}
			}
		}
	}
}
