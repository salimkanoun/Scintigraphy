package org.petctviewer.scintigraphy.renal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.general.DatasetUtils;

public class ValueSelector extends CrosshairOverlay implements ChartMouseListener {

	/**
	* 
	*/
	private static final long serialVersionUID = 6794595703667698248L;
	private Crosshair crossX, crossY;
	private boolean xLocked;
	private int series;
	private ChartPanel chartPanel;

	public ValueSelector(String nom, double startX, int series, RectangleAnchor anchor) {
		this.series = series;
		this.xLocked = true;

		this.crossX = new Crosshair(startX, Color.GRAY, new BasicStroke(0f));
		
		this.crossX.setLabelOutlineVisible(false);
		this.crossX.setLabelAnchor(anchor);
		
		this.crossX.setLabelGenerator(new CrosshairLabelGenerator() {
			@Override
			public String generateLabel(Crosshair crosshair) {
				if(nom.equals("") || nom == null) {
					//cause une exception lors du premier affichage
					crossX.setLabelVisible(false);
				}
				return nom;
			}
		});

		this.crossX.setLabelVisible(true);

		this.crossY = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
		this.crossY.setLabelVisible(true);

		this.addDomainCrosshair(crossX);
		this.addRangeCrosshair(crossY);
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
			this.xLocked = !this.xLocked;
			this.crossY.setVisible(!this.xLocked);
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		XYPlot plot = (XYPlot) event.getChart().getPlot();		
		
		if (!this.xLocked) {			
			ValueAxis xAxis = plot.getDomainAxis();
			
			double x = xAxis.java2DToValue(event.getTrigger().getX(), chartPanel.getScreenDataArea(),
					org.jfree.chart.ui.RectangleEdge.BOTTOM);
			
			double y = Double.NaN;
			if(series != -1) {
				y = DatasetUtils.findYValue(plot.getDataset(), this.series, x);
			}			

			this.crossX.setValue(x);
			this.crossY.setValue(y);
		}
	}
	
	public double getXValue() {
		return this.crossX.getValue();
	}

	public void setChartPanel(ChartPanel chartPanel) {
		this.chartPanel = chartPanel;
		
//		XYPlot plot = chartPanel.getChart().getXYPlot();		
//		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
//		for (int c = 0; c < plot.getDataset().getSeriesCount(); c++) {
//			renderer.setSeriesShapesVisible(c, false);
//		}
//		plot.setRenderer(series, renderer);
	}

	public void setPaint(Paint seriesPaint) {
		this.crossX.setPaint(seriesPaint);
	}
	
	public int getSeries() {
		return this.series;
	}
}