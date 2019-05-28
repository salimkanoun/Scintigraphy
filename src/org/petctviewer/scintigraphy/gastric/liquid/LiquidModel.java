package org.petctviewer.scintigraphy.gastric.liquid;

import ij.gui.Roi;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.gastric.ResultRequest;
import org.petctviewer.scintigraphy.gastric.ResultValue;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelWorkflow;

import java.util.LinkedList;
import java.util.List;

public class LiquidModel extends ModelWorkflow {

	private List<XYDataItem> antCounts, postCounts;

	public LiquidModel(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);

		this.antCounts = new LinkedList<>();
		this.postCounts = new LinkedList<>();
	}

	public void calculateCounts(ImageState state, Roi roi) {
		// Set image on state
		if (state.getIdImage() != ImageState.ID_CUSTOM_IMAGE)
			state.specifieImage(this.selectedImages[state.getIdImage()]);

		// Calculate delay with first image
		int delayFirstImage = (int) (state.getImage().getDateAcquisition()
				.getTime() - this.selectedImages[0].getDateAcquisition().getTime());
		System.out.println("Delay = " + delayFirstImage);
		assert delayFirstImage >= 0 : "Order of images incorrect";

		// Prepare image
		state.getImage().getImagePlus().setSlice(state.getSlice());
		state.getImage().getImagePlus().setRoi(roi);

		// Calculate value
		double value = Library_Quantif
				.applyDecayFraction(delayFirstImage, Library_Quantif.getCounts(state.getImage().getImagePlus()),
						this.isotope);

		// Save value
		if (state.getFacingOrientation() == Orientation.ANT) {
			this.antCounts.add(new XYDataItem(delayFirstImage / 1000. / 60., value));
		} else {
			this.postCounts.add(new XYDataItem(delayFirstImage / 1000. / 60., value));
		}
	}

	public XYSeries createSeries() {
		XYSeries series = new XYSeries("Stomach");

		// Add data
		for (XYDataItem item : this.antCounts)
			series.add(item);

		return series;
	}

	@Override
	public ResultValue getResult(ResultRequest request) {
		return null;
	}

	@Override
	public void calculateResults() {
		if (!postCounts.isEmpty()) {
			// Compute geometrical average
			for (int i = 0; i < this.postCounts.size(); i++) {
				double avg = Library_Quantif
						.moyGeom(this.antCounts.get(i).getYValue(), this.postCounts.get(i).getYValue());
				this.antCounts.get(i).setY(avg);
			}
		}
	}
}
