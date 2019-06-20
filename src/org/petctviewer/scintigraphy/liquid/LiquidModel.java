package org.petctviewer.scintigraphy.liquid;

import ij.ImagePlus;
import org.apache.commons.lang.ArrayUtils;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.*;

import java.util.List;

public class LiquidModel extends ModelWorkflow {

	public static final Result RES_T_HALF = new Result("T 1/2");

	private XYSeries counts;

	public LiquidModel(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);

		this.counts = new XYSeries("Stomach");
	}

	public double[] generateXValues() {
		@SuppressWarnings("unchecked") List<XYDataItem> items = counts.getItems();
		return items.stream().mapToDouble(XYDataItem::getXValue).toArray();
	}

	public double[] generateYValues() {
		@SuppressWarnings("unchecked") List<XYDataItem> items = counts.getItems();
		return items.stream().mapToDouble(XYDataItem::getYValue).toArray();
	}

	public void calculateCounts(ImagePlus image, int duration) {
		// Calculate value
		double value = Library_Quantif.applyDecayFraction(duration, Library_Quantif.getCounts(image), this.isotope);

		// Save value
		this.counts.add(new XYDataItem(duration / 1000. / 60., value));
	}

	public XYSeries getSeries() {
		return counts;
	}

	@Override
	public ResultValue getResult(ResultRequest request) {
		if (request.getResultOn() == RES_T_HALF) {
			double[] xValues = this.generateXValues();
			double[] yValues = this.generateYValues();
			// Calculate t 1/2
			// Find max
			double max = Library_JFreeChart.maxValue(yValues);
			int cutAt = ArrayUtils.indexOf(yValues, max);
			yValues = ArrayUtils.subarray(yValues, cutAt, yValues.length);
			xValues = ArrayUtils.subarray(xValues, cutAt, xValues.length);
			double half = max / 2.;
			Double result = Library_JFreeChart.getX(xValues, yValues, half);
			boolean isExtrapolated = false;
			if (result == null) {
				// Extrapolate
				result = request.getFit().extrapolateX(half);
				isExtrapolated = true;
			}
			return new ResultValue(request, result, Unit.TIME, isExtrapolated);
		}
		return null;
	}

	@Override
	public void calculateResults() {
	}
}
