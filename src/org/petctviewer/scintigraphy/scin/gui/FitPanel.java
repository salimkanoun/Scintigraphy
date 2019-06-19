package org.petctviewer.scintigraphy.scin.gui;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.events.FitChangeEvent;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.model.Fit;
import org.petctviewer.scintigraphy.scin.model.Fit.FitType;
import org.petctviewer.scintigraphy.scin.model.Unit;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;

public class FitPanel extends JPanel implements ChartMouseListener, ItemListener {

	private final JComboBox<Fit.FitType> fitsChoices;
	private final JLabel labelInterpolation;
	private final JLabel labelError;
	private final JLabel labelFit;
	private final JButton btnAutoFit;
	private JValueSetter valueSetterFit;
	private XYSeriesCollection data;
	private Fit fit;
	private Unit unitY;
	private List<ChangeListener> fitChangeListeners;

	public FitPanel() {
		this.setLayout(new BorderLayout());

		// South
		JPanel panSouth = new JPanel();
		panSouth.setLayout(new BoxLayout(panSouth, BoxLayout.LINE_AXIS));

		// Fit label (used to replace the combo box during capture)
		this.labelFit = new JLabel();
		this.labelFit.setVisible(false);
		panSouth.add(this.labelFit);

		// Fit choices
		this.fitsChoices = new JComboBox<>(Fit.FitType.values());
		this.fitsChoices.addItemListener(this);
		panSouth.add(this.fitsChoices);

		// Auto fit
		btnAutoFit = new JButton("Auto-fit");
		btnAutoFit.addActionListener(e -> this.selectBestFit());
		panSouth.add(btnAutoFit);

		// Interpolation label
		this.labelInterpolation = new JLabel();
		this.labelInterpolation.setVisible(false);
		panSouth.add(this.labelInterpolation);

		// Error label
		this.labelError = new JLabel();
		this.labelError.setForeground(Color.RED);
		panSouth.add(this.labelError);

		this.add(panSouth, BorderLayout.SOUTH);
		// --

		// Init variables
		this.fitChangeListeners = new LinkedList<>();
	}

	/**
	 * Detects the fit selected by the combo box and draw the fit on the tab.
	 */
	private void reloadFit() {
		try {
			// Create fit
			XYSeries series = ((XYSeriesCollection) this.valueSetterFit.retrieveValuesInSpan()).getSeries(0);
			this.fit = Fit.createFit(getSelectedFit(), Library_JFreeChart.invertArray(series.toArray()), this.unitY);

			this.drawFit();
			this.setErrorMessage(null);

			// Update extrapolation label
			this.labelFit.setText("-- " + this.fit.getType() + " extrapolation --");

			// Notify listeners
			for (ChangeListener listener : this.fitChangeListeners)
				listener.stateChanged(new FitChangeEvent(this, this.fit));
		} catch (IllegalArgumentException error) {
			this.setErrorMessage("Not enough data to fit the graph");
		}
	}

	/**
	 * Removes all previous fits.
	 */
	private void clearFits() {
		for (int i = 1; i < this.data.getSeriesCount(); i++)
			this.data.removeSeries(i);
	}

	/**
	 * Adds a listener for fit change events.
	 *
	 * @param listener Listener to add
	 */
	public void addChangeListener(ChangeListener listener) {
		this.fitChangeListeners.add(listener);
	}

	/**
	 * Removes the listener.
	 *
	 * @param listener Listener to remove
	 */
	public void removeChangeListener(ChangeListener listener) {
		this.fitChangeListeners.remove(listener);
	}

	/**
	 * Selects the best fit for the current graph.
	 *
	 * @see #findBestFit()
	 */
	public void selectBestFit() {
		this.fitsChoices.setSelectedItem(this.findBestFit());
	}

	/**
	 * Finds the best fit matching the graph. Only the values in the area specified by the user are taken into
	 * account.<br> The best fit is determined by the method of least squares.
	 *
	 * @return best fit for this graph
	 */
	public FitType findBestFit() {
		double bestScore = Double.MAX_VALUE;
		FitType bestFit = null;
		for (FitType type : FitType.values()) {
			double[][] dataset = ((XYSeriesCollection) this.valueSetterFit.retrieveValuesInSpan()).getSeries(
					0).toArray();

			try {
				// Create fit
				Fit fit = Fit.createFit(type, Library_JFreeChart.invertArray(dataset), this.unitY);

				// Get Y points
				double[] yPoints = dataset[1];
				double[] yFittedPoints = fit.generateOrdinates(dataset[0]);

				// Compute score
				double score = Library_JFreeChart.computeLeastSquares(yPoints, yFittedPoints);

				if (score < bestScore) {
					bestScore = score;
					bestFit = type;
				}
			} catch (IllegalArgumentException e) {
				// Not enough data selected in the area
			}
		}

		return bestFit;
	}

	public Fit getFit() {
		return this.fit;
	}

	/**
	 * Changes the text of the extrapolation name (used for capture).
	 *
	 * @param labelName New name of the extrapolation
	 */
	public void changeLabelInterpolation(String labelName) {
		// Change label interpolation text (for capture)
		this.labelInterpolation.setText("-- " + labelName + " --");
	}

	/**
	 * Creates the graph specified. This method do not draw the fit. Only 1 series will be displayed.
	 *
	 * @param chart  Chart to draw
	 * @param series Collection containing only <b>1</b> series
	 * @param yUnit  Unit of the Y axis
	 */
	public void createGraph(JFreeChart chart, XYSeriesCollection series, Unit yUnit) {
		// Create chart
		this.data = series;

		// Set bounds
		XYPlot plot = chart.getXYPlot();
		final boolean includeInterval = false;
		double minX = 0, minY = 0;
		double upperBoundX = series.getDomainUpperBound(includeInterval) * 1.1;
		double lowerBoundX = series.getDomainLowerBound(includeInterval);
		double upperBoundY = series.getRangeUpperBound(includeInterval) * 1.1;
		double lowerBoundY = series.getRangeLowerBound(includeInterval);

		if (minX > lowerBoundX) minX = lowerBoundX * 1.1;
		if (minY > lowerBoundY) minY = lowerBoundY * 1.1;
		// At least 1 of range
		if (upperBoundX == lowerBoundX) upperBoundX += 1.;
		if (upperBoundY == lowerBoundY) upperBoundY += 1.;
		// X axis
		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setRange(minX, upperBoundX);
		// Y axis
		NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		yAxis.setRange(minY, upperBoundY);

		// Create value setter
		double startX = lowerBoundX + .1 * (upperBoundX - lowerBoundX);
		double endX = lowerBoundX + .7 * (upperBoundX - lowerBoundX);
		valueSetterFit = new JValueSetter(chart);
		valueSetterFit.addSelector(new Selector(" ", startX, -1, RectangleAnchor.TOP_LEFT), "start");
		valueSetterFit.addSelector(new Selector(" ", endX, -1, RectangleAnchor.TOP_LEFT), "end");
		valueSetterFit.addArea("start", "end", "area", null);
		valueSetterFit.addChartMouseListener(this);

		// Units
		this.unitY = yUnit;

		this.add(this.valueSetterFit, BorderLayout.CENTER);

		this.reloadFit();
	}

	/**
	 * Changes the error message. This message is displayed in red. If null is passed, then the previous message is
	 * erased.
	 *
	 * @param msg message to show or null to erase the last message
	 */
	public void setErrorMessage(String msg) {
		this.labelError.setText(msg);
	}

	/**
	 * Displays the fit selected by the user and removes the previous fit if existing.
	 *
	 * @see #getSelectedFit()
	 */
	public void drawFit() {
		this.clearFits();

		@SuppressWarnings("unchecked") List<XYDataItem> items = (List<XYDataItem>) this.data.getSeries(0).getItems();
		this.data.addSeries(this.fit.generateFittedSeries(items.stream().mapToDouble(XYDataItem::getXValue).toArray()));
	}

	/**
	 * Gets the fit selected by the user with the combo box.
	 *
	 * @return type of the fit selected by the user
	 */
	public FitType getSelectedFit() {
		return (FitType) this.fitsChoices.getSelectedItem();
	}

	public Component[] getComponentsToHide() {
		return new Component[]{this.fitsChoices, this.btnAutoFit};
	}

	public Component[] getComponentsToShow() {
		return new Component[]{this.labelFit};
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// Does nothing
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		if (this.valueSetterFit.getGrabbedSelector() != null) {
			this.reloadFit();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			this.reloadFit();

			this.changeLabelInterpolation(e.getItem().toString() + " extrapolation");
		}
	}

}
