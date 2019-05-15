package org.petctviewer.scintigraphy.renal;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;

/**
 * A CrosshairOverlay that supports many range axis crosshairs mapped to
 * different range axes. Default CrosshairOverlay does not support this. Use
 * mapCrosshairToRangeAxis to bind range crosshairs to range axes
 */
class MyCrosshairOverlay extends CrosshairOverlay {

	private static final long serialVersionUID = -962875933696708186L;
	/*
	 * The domain and range crosshair lists in superclass are private, so we shadow
	 * them. See addDomainCrosshair and addRangeCrosshair
	 */
	private final List<Crosshair> shadowD, shadowR;
	private final int[] rangeAxes;

	MyCrosshairOverlay() {
		super();
		shadowD = new ArrayList<>();
		shadowR = new ArrayList<>();
		/*
		 * These rangeAxes all being zero initially will mean that the default range
		 * axis used for ALL yCrosshairs will be range axis 0. Use
		 * mapCrosshairToRangeAxis to alter. 16 yCrosshairs should suffice.
		 */
		rangeAxes = new int[16];
	}

	@Override
	public void addDomainCrosshair(Crosshair crosshair) {
		super.addDomainCrosshair(crosshair);
		shadowD.add(crosshair);
	}

	@Override
	public void addRangeCrosshair(Crosshair crosshair) {
		super.addRangeCrosshair(crosshair);
		shadowR.add(crosshair);

	}

	/**
	 * In the spirit of mapDatasetToRangeAxis, binding a range crosshair to a
	 * particular range axis.
	 */
	public void mapCrosshairToRangeAxis(int crosshairIndex, int rangeAxisIndex) {
		rangeAxes[crosshairIndex] = rangeAxisIndex;
	}

	/**
	 * Paints the crosshairs in the layer.
	 *
	 * @param g2
	 *            the graphics target.
	 * @param chartPanel
	 *            the chart panel.
	 */
	@Override
	public void paintOverlay(Graphics2D g2, ChartPanel chartPanel) {
		Shape savedClip = g2.getClip();
		Rectangle2D dataArea = chartPanel.getScreenDataArea();
		g2.clip(dataArea);
		JFreeChart chart = chartPanel.getChart();
		XYPlot plot = (XYPlot) chart.getPlot();
		ValueAxis dAxis = plot.getDomainAxis();
		RectangleEdge dAxisEdge = plot.getDomainAxisEdge();

		// Expected ONE domain crosshair, mapped to default domain axis
		for (Crosshair c : shadowD) {
			if (!c.isVisible())
				continue;
			double x = c.getValue();
			double xx = dAxis.valueToJava2D(x, dataArea, dAxisEdge);
			if (plot.getOrientation() == PlotOrientation.VERTICAL) {
				drawVerticalCrosshair(g2, dataArea, xx, c);
			} else {
				drawHorizontalCrosshair(g2, dataArea, xx, c);
			}
		}
		for (int i = 0; i < shadowR.size(); i++) {
			Crosshair c = shadowR.get(i);
			if (!c.isVisible())
				continue;
			int rangeAxisIndex = rangeAxes[i];
			if (rangeAxisIndex >= plot.getRangeAxisCount())
				continue;
			ValueAxis rAxis = plot.getRangeAxis(rangeAxisIndex);
			RectangleEdge rAxisEdge = plot.getRangeAxisEdge(rangeAxisIndex);
			double y = c.getValue();
			double yy = rAxis.valueToJava2D(y, dataArea, rAxisEdge);
			if (plot.getOrientation() == PlotOrientation.VERTICAL) {
				drawHorizontalCrosshair(g2, dataArea, yy, c);
			} else {
				drawVerticalCrosshair(g2, dataArea, yy, c);
			}
		}
		g2.setClip(savedClip);
	}
}