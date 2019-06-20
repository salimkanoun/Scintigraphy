package org.petctviewer.scintigraphy.liquid;

import ij.ImagePlus;
import org.apache.commons.lang.ArrayUtils;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.*;

import java.util.List;

public class LiquidModel extends ModelWorkflow {

	public static final Result RES_T_HALF = new Result("T 1/2");

	private XYSeries countsAnt, countsPost;

	public LiquidModel(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);

		this.clearResults();
	}

	private boolean isBothOrientations() {
		return !this.countsPost.isEmpty();
	}

	private double[] computeGeoAvg(List<XYDataItem> tab1, List<XYDataItem> tab2) {
		if (tab1.size() != tab2.size()) throw new IllegalArgumentException("Tabs must be of the same length");

		double[] res = new double[tab1.size()];
		for (int i = 0; i < tab1.size(); i++) {
			res[i] = Library_Quantif.moyGeom(tab1.get(i).getYValue(), tab2.get(i).getYValue());
		}
		return res;
	}

	private XYSeries createSeriesGM() {
		XYSeries series = new XYSeries("Stomach GM");

		double[] xValues = this.generateXValues();
		double[] yValues = this.generateYValues();

		for (int i = 0; i < xValues.length; i++) {
			series.add(xValues[i], yValues[i]);
		}

		return series;
	}

	public void clearResults() {
		this.countsAnt = new XYSeries("Stomach");
		this.countsPost = new XYSeries("Stomach (Post)");
	}

	public double[] generateXValues() {
		@SuppressWarnings("unchecked") List<XYDataItem> itemsAnt = countsAnt.getItems();
		return itemsAnt.stream().mapToDouble(XYDataItem::getXValue).toArray();
	}

	public double[] generateYValues() {
		@SuppressWarnings("unchecked") List<XYDataItem> items = countsAnt.getItems();
		@SuppressWarnings("unchecked") List<XYDataItem> itemsPost = countsPost.getItems();
		if (this.isBothOrientations()) {
			return this.computeGeoAvg(items, itemsPost);
		}
		return items.stream().mapToDouble(XYDataItem::getYValue).toArray();
	}

	public void calculateCounts(ImagePlus image, int duration, Orientation orientation) {
		// Calculate value
		double value = Library_Quantif.applyDecayFraction(duration, Library_Quantif.getCounts(image), this.isotope);

		// Save value
		if (orientation == Orientation.ANT) this.countsAnt.add(new XYDataItem(duration / 1000. / 60., value));
		else this.countsPost.add(new XYDataItem(duration / 1000. / 60., value));
	}

	public XYSeries getSeries() {
		if (this.isBothOrientations()) return this.createSeriesGM();
		return countsAnt;
	}

	@Override
	public ResultValue getResult(ResultRequest request) {
		if (request.getResultOn() == RES_T_HALF) {
			double[] xValues = this.generateXValues();
			double[] yValues = this.generateYValues();
			// Calculate t 1/2
			// Find max
			double max = Library_JFreeChart.maxValue(yValues);
//			System.out.println("Max = " + max);
			int cutAt = ArrayUtils.indexOf(yValues, max);
//			System.out.println("Cut at = " + cutAt);
//			System.out.println("Previous Y: " + Arrays.toString(yValues));
			yValues = ArrayUtils.subarray(yValues, cutAt, yValues.length);
//			System.out.println("Y: " + Arrays.toString(yValues));
//			System.out.println("Previous X: " + Arrays.toString(xValues));
			xValues = ArrayUtils.subarray(xValues, cutAt, xValues.length);
//			System.out.println("X: " + Arrays.toString(xValues));
			double half = max / 2.;
//			System.out.println("Half = " + half);
			Double result = Library_JFreeChart.getX(xValues, yValues, half);
//			System.out.println("REsult = " + result);
			boolean isExtrapolated = false;
			if (result == null) {
				// Extrapolate
				result = request.getFit().extrapolateX(half);
//				System.out.println("Extrapolated = " + result);
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
