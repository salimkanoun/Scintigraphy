package org.petctviewer.scintigraphy.renal;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An horizontal selector, showing the X coordinate associated to the Y step.
 * Extends {@link Selector}
 *
 */
public class YSelector extends Selector implements ChartMouseListener {
	private static final long serialVersionUID = 6794595703667698248L;
	private Crosshair crossY;
	private List<Crosshair> crossXs;
	private Crosshair crossDemieX;
	private int currentLabelVisible;

	// si le selecteur n'est pas selectionne
	private boolean yLocked;

	// serie sur laquelle se situe le selecteur
	private int series;

	private Comparable key;
	private JValueSetter jValueSetter;

	/**
	 * Permet de creer un selecteur deplacable sur un courbe
	 * 
	 * @param nom
	 *            studyName du selecteur (null accepte)
	 * @param startY
	 *            position de depart du selecteur
	 * @param series
	 *            series observee (-1 si aucune)
	 * @param anchor
	 *            position du label
	 */
	public YSelector(String nom, double startY, int series, RectangleAnchor anchor) {
		super(nom, startY, series, anchor);
		this.series = series;
		this.yLocked = true;
		this.crossXs = new ArrayList<>();
		this.removeDomainCrosshair(this.getCrossX());

		// on intialise le selecteur vertical
		this.crossY = new Crosshair(startY, Color.GRAY, new BasicStroke(0f));

		// on place le label a l'endroit demande
		this.crossY.setLabelOutlineVisible(false);

		// on rend le label invisible si le studyName est null ou si c'est un espace
		this.crossY.setLabelGenerator(new CrosshairLabelGenerator() {
			@Override
			public String generateLabel(Crosshair crosshair) {
				if (nom == null || nom.trim().equals("")) {
					YSelector.this.crossY.setLabelVisible(false);
				}
				return nom;
			}
		});

		// le selecteur vertical n'est pour l'instant pas affiche
		this.crossY.setLabelVisible(true);
		this.crossY.setLabelAnchor(RectangleAnchor.RIGHT);

		this.addRangeCrosshair(this.crossY);
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// on bloque, ou debloque le selecteur vertical
		this.yLocked = !this.yLocked;

		// on rend visible le selecteur horizontal si le selecteur horizontal est
		// debloque
		for (Crosshair ch : this.crossXs)
			ch.setVisible(!this.yLocked);

		for (Crosshair domain : this.getDomainCrosshairs())
			this.removeDomainCrosshair(domain);

		// Calculate and display the t1/2 of the ltas hited point of crossY
		if (this.yLocked) {
			XYPlot plot = (XYPlot) event.getChart().getPlot();
			ValueAxis yAxis = plot.getRangeAxis();

			// Getting the y value of the selector
			double y = yAxis.java2DToValue(event.getTrigger().getY(), this.jValueSetter.getScreenDataArea(),
					RectangleEdge.LEFT);

			// The futur x value
			Number xValue = 0;
			// The y graph value, before the current value (default to the y value on index
			// 0)
			double previousYValue = (double) plot.getDataset().getY(this.series, 0);
			// Index of the selected value
			int lastIndex = 0;

			// For every y of the graph
			for (int itemIndex = 0; itemIndex < plot.getDataset().getItemCount(this.series); itemIndex++) {
				Number yValue = (double) plot.getDataset().getY(this.series, itemIndex);
				if ((((double) yValue) <= y && y <= previousYValue)) {
					lastIndex = itemIndex - 1;
				}
				previousYValue = (double) yValue;
			}
			double demieLastValue = y / 2;

			previousYValue = (double) plot.getDataset().getY(this.series, lastIndex);
			for (int itemIndex = lastIndex; itemIndex < plot.getDataset().getItemCount(this.series); itemIndex++) {
				Number yValue = Math.max(0.0d, (double) plot.getDataset().getY(this.series, itemIndex));
				if (((double) yValue) == demieLastValue || demieLastValue == previousYValue
						|| (((double) yValue) <= demieLastValue && demieLastValue <= previousYValue)
						|| (((double) yValue) >= demieLastValue && demieLastValue >= previousYValue)) {
					xValue = findYValue(plot.getDataset(), this.series, demieLastValue, previousYValue, (double) yValue,
							itemIndex - 1);
					break;

				}
				previousYValue = (double) yValue;
			}
			this.crossDemieX = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
			this.crossDemieX.setValue(xValue.doubleValue());
			this.crossDemieX.setLabelVisible(true);
			this.addDomainCrosshair(this.crossDemieX);
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		XYPlot plot = (XYPlot) event.getChart().getPlot();
		
		ValueAxis yAxis = plot.getRangeAxis();

		// on calcule la nouvelle valeur du selecteur horizontal
		double y = yAxis.java2DToValue(event.getTrigger().getY(), this.jValueSetter.getScreenDataArea(),
				RectangleEdge.LEFT);
		
		// This method won't be use ... because it's useless. But it's cool so, I can't delete it.
		// si le selecteur horizontal n'est pas bloque
		if (false) {
			if (!this.yLocked) {
				

				for (Crosshair ch : this.crossXs)
					this.removeDomainCrosshair(ch);
				this.crossXs.clear();

				// Futur value of x (interpolated)
				Number xValue;
				// The value of the previous point
				double previousYValue = (double) plot.getDataset().getY(this.series, 0);
				// FOr every y point of our value
				for (int itemIndex = 0; itemIndex < plot.getDataset().getItemCount(this.series); itemIndex++) {
					// The actual y value of the selector, on the graph.
					Number yValue = (double) plot.getDataset().getY(this.series, itemIndex);
					// If the value of the selector match the current value of the graph
					if (((double) yValue) == y || y == previousYValue || (((double) yValue) <= y && y <= previousYValue)
							|| (((double) yValue) >= y && y >= previousYValue)) {
						// Interpolation of x
						xValue = findYValue(plot.getDataset(), this.series, y, previousYValue, (double) yValue,
								itemIndex - 1);
						// Add the interpoled x to the value to display, and display it
						this.crossXs.add(new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f)));
						this.crossXs.get(this.crossXs.size() - 1).setValue(xValue.doubleValue());
						this.crossXs.get(this.crossXs.size() - 1).setLabelVisible(true);
						this.addDomainCrosshair(this.crossXs.get(this.crossXs.size() - 1));
					}
					previousYValue = (double) yValue;
				}
				
				this.currentLabelVisible = -1;
			}
		}
		this.crossY.setValue(y);
	}

	public double getXValue() {
		return this.crossY.getValue();
	}

	public void setXValue(double x) {
		this.crossY.setValue(x);
	}

	public void setPaint(Paint seriesPaint) {
		this.crossY.setPaint(seriesPaint);
	}

	public int getSeries() {
		return this.series;
	}

	public boolean isXLocked() {
		return this.yLocked;

	}

	public Comparable getKey() {
		return this.key;
	}

	public void setKey(Comparable key) {
		this.key = key;
	}

	public void setJValueSetter(JValueSetter jValueSetter) {
		this.jValueSetter = jValueSetter;
	}

	public List<Crosshair> getCrossXs() {
		return this.crossXs;
	}

	/**
	 * Returns the interpolated value of x that corresponds to the specified y-value
	 * in the given series.
	 * 
	 * @param dataset
	 *            the dataset ({@code null} not permitted).
	 * @param series
	 *            the series index.
	 * @param y
	 *            the y-value on the graph.
	 * @param previousValue
	 *            the previousValue in the dataset.
	 * @param nextValue
	 *            the next value in the dataset.
	 * @param itemIndex
	 *            the index of the previous value.
	 * 
	 * @return The x value.
	 */
	public static double findYValue(XYDataset dataset, int series, double y, double previousValue, double nextValue,
			int itemIndex) {

		if (y == previousValue)
			return dataset.getXValue(series, itemIndex);
		if (y == nextValue)
			return dataset.getXValue(series, itemIndex + 1);

		int[] indices = { itemIndex, itemIndex + 1 };

		double y0 = dataset.getYValue(series, indices[0]);
		double y1 = dataset.getYValue(series, indices[1]);
		double x0 = dataset.getXValue(series, indices[0]);
		double x1 = dataset.getXValue(series, indices[1]);
		return x0 + (x1 - x0) * (y - y0) / (y1 - y0);
	}

	public void setCurrentLabelVisible(int crossXIndex) {
		this.currentLabelVisible = crossXIndex;
	}

	public int getCurrentLabelVisible() {
		return this.currentLabelVisible;
	}

}
