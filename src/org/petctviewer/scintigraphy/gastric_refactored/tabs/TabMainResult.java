package org.petctviewer.scintigraphy.gastric_refactored.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.gastric_refactored.ControllerWorkflow_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.Result;
import org.petctviewer.scintigraphy.gastric_refactored.ResultValue;
import org.petctviewer.scintigraphy.gastric_refactored.Unit;
import org.petctviewer.scintigraphy.gastric_refactored.dynamic.DynGastricScintigraphy;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.FitType;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import ij.ImagePlus;
import ij.ImageStack;

public class TabMainResult extends TabResult implements ItemListener, ChartMouseListener {

	private ImagePlus capture;
	private Date timeIngestion;

	private ControllerWorkflow controller;

	private XYSeriesCollection data;
	private JValueSetter valueSetter;

	private JComboBox<FitType> fitsChoices;
	private JLabel labelInterpolation, labelError;

	private Fit currentFit;

	private static final Unit UNIT = Unit.PERCENTAGE;

	public TabMainResult(FenResults parent, ImagePlus capture, ControllerWorkflow_Gastric controller) {
		super(parent, "Result");

		// Instantiate variables
		fitsChoices = new JComboBox<>(FitType.values());
		fitsChoices.addItemListener(this);

		this.labelInterpolation = new JLabel();
		this.labelInterpolation.setVisible(false);

		this.labelError = new JLabel();
		this.labelError.setForeground(Color.RED);

		this.capture = capture;
		this.controller = controller;

		this.currentFit = new Fit.NoFit(UNIT);

		this.createGraph();

		Component[] hide = new Component[] { fitsChoices };
		Component[] show = new Component[] { this.labelInterpolation };
		this.createCaptureButton(hide, show, null);

		this.reloadDisplay();
	}

	/**
	 * Generates the table with all of the results to display for this scintigraphy.
	 * 
	 * @return table containing the results
	 */
	private JTable tablesResultats() {
		Result[] results = new Result[] { Model_Gastric.RES_TIME, Model_Gastric.RES_STOMACH, Model_Gastric.RES_FUNDUS,
				Model_Gastric.RES_ANTRUM };
		Unit[] unitsUsed = new Unit[] { Unit.TIME, UNIT, UNIT, UNIT };

		Model_Gastric model = (Model_Gastric) this.parent.getModel();

		// Create table
		JTable table = new JTable(0, results.length);
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		String[] arr = new String[tableModel.getColumnCount()];

		// Change column titles
		for (int i = 0; i < tableModel.getColumnCount(); i++)
			table.getColumnModel().getColumn(i).setHeaderValue(results[i].getName() + " (" + unitsUsed[i] + ")");

		// Fill table with data
		for (int i = 0; i < ((Model_Gastric) this.parent.getModel()).nbAcquisitions(); i++) {
			for (int j = 0; j < tableModel.getColumnCount(); j++) {
				ResultValue res = model.getImageResult(results[j], i);
				if (res == null)
					arr[j] = "--";
				else {
					res.convert(unitsUsed[j]);
					arr[j] = res.value();
				}
			}
			tableModel.addRow(arr);
		}

		// Customize table
		table.setRowHeight(30);
		MatteBorder border = new MatteBorder(1, 1, 1, 1, Color.BLACK);
		table.setBorder(border);

		return table;
	}

	/**
	 * Utility method to print on the panel the specified result.
	 * 
	 * @param infoRes Panel on which to place the result in
	 * @param result  Result of the model to display
	 */
	private void displayResult(JPanel infoRes, ResultValue result) {
		infoRes.add(new JLabel(result.getResultType().getName() + ":"));
		JLabel lRes = new JLabel(result.value() + " " + result.getUnit());
		if (result.getExtrapolation() == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);
	}

	/**
	 * Utility method to print the result for the retention at a certain time.
	 * 
	 * @param infoRes Panel on which to place the result in
	 * @param time    Time of the retention in minutes
	 * @param result  Result of the model to display
	 * @throws IllegalArgumentException if the result is not a
	 *                                  {@link Model_Gastric#RETENTION} type
	 */
	private void displayRetentionResult(JPanel infoRes, double time, ResultValue result)
			throws IllegalArgumentException {
		if (result.getResultType() != Model_Gastric.RETENTION)
			throw new IllegalArgumentException("Result type must be " + Model_Gastric.RETENTION);

		infoRes.add(new JLabel(result.getResultType().getName() + " at " + (int) (time / 60) + "h:"));

		JLabel lRes = new JLabel(result.value() + " " + result.getUnit());
		if (result.getExtrapolation() == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);
	}

	/**
	 * Utility method that generates a panel containing all of the results needed
	 * for that scintigraphy.
	 * 
	 * @return panel containing the results
	 */
	private JPanel infoResultats() {
		JPanel panel = new JPanel(new BorderLayout());

		boolean hasExtrapolatedValue = false;

		JPanel infoRes = new JPanel();
		infoRes.setLayout(new GridLayout(0, 2));

		// Data
		double[] data = getModel().getStomachValues();

		ResultValue result = getModel().getResult(data, Model_Gastric.START_ANTRUM, this.currentFit);
		hasExtrapolatedValue = result.getExtrapolation() != null;
		this.displayResult(infoRes, result);

		result = getModel().getResult(data, Model_Gastric.START_INTESTINE, this.currentFit);
		hasExtrapolatedValue = result.getExtrapolation() != null;
		this.displayResult(infoRes, result);

		result = getModel().getResult(data, Model_Gastric.LAG_PHASE, this.currentFit);
		hasExtrapolatedValue = result.getExtrapolation() != null;
		this.displayResult(infoRes, result);

		result = getModel().getResult(data, Model_Gastric.T_HALF, this.currentFit);
		hasExtrapolatedValue = result.getExtrapolation() != null;
		this.displayResult(infoRes, result);

		for (double time = 60.; time <= 240.; time += 60.) {
			result = getModel().retentionAt(getModel().getStomachValues(), time, this.currentFit);
			hasExtrapolatedValue = result.getExtrapolation() != null;
			this.displayRetentionResult(infoRes, time, result);
		}

		panel.add(infoRes, BorderLayout.CENTER);
		if (hasExtrapolatedValue) {
			JLabel l = null;
			if (getSelectedFit() == FitType.NONE) {
				l = new JLabel("(*) No fit has been selected to extrapolate the values!");
				l.setForeground(Color.RED);
			} else {
				l = new JLabel("(*) The results are calculated with a " + getSelectedFit() + " extrapolation");
			}
			panel.add(l, BorderLayout.SOUTH);
		}
		return panel;
	}

	/**
	 * Detects the fit selected by the combo box and draw the fit on the tab.
	 */
	private void reloadFit() {
		try {
			// Create fit
			XYSeries series = ((XYSeriesCollection) this.getValueSetter().retrieveValuesInSpan()).getSeries(0);
			this.currentFit = Fit.createFit(getSelectedFit(), Library_JFreeChart.invertArray(series.toArray()), UNIT);

			this.drawFit();
			this.setErrorMessage(null);
			this.reloadSidePanelContent();
		} catch (IllegalArgumentException error) {
			System.err.println("Not enough data");
			this.setErrorMessage("Not enough data to fit the graph");
		}
	}

	/**
	 * Displays additional results on the tab:
	 * <ul>
	 * <li>time of ingestion</li>
	 * </ul>
	 * 
	 * @return panel containing the additional results
	 */
	private JPanel additionalResults() {
		JPanel panel = new JPanel();

		if (this.timeIngestion != null) {
			panel.add(new JLabel("Ingestion Time: " + new SimpleDateFormat("HH:mm:ss").format(timeIngestion)));
		}

		return panel;
	}

	/**
	 * Removes all previous fits.
	 */
	private void clearFits() {
		for (int i = 1; i < this.data.getSeriesCount(); i++)
			this.data.removeSeries(i);
	}

	/**
	 * Creates the panel with the images of the graphs
	 */
	private JPanel createPanelResults() {
		ImageStack ims = Library_Capture_CSV.captureToStack(new ImagePlus[] { capture, getModel().createGraph_3(),
				getModel().createGraph_1(), getModel().createGraph_2() });

		JButton btn = new JButton("Launch dynamic acquisition");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Finish gastric
				controller.getVue().setVisible(false);

				// Start scintigraphy
				new DynGastricScintigraphy(getModel(), parent);
			}
		});

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new DynamicImage(getModel().montage(ims).getImage()), BorderLayout.CENTER);
		panel.add(btn, BorderLayout.SOUTH);

		return panel;
	}

	private Model_Gastric getModel() {
		return (Model_Gastric) this.parent.getModel();
	}

	/**
	 * Creates the panel with the graph fitted.
	 */
	private JPanel createPanelFit() {
		JPanel panel = new JPanel(new BorderLayout());

		panel.add(this.valueSetter, BorderLayout.CENTER);

		JPanel panSouth = new JPanel(new GridLayout(1, 0));
		panSouth.add(this.fitsChoices);
		panSouth.add(this.labelInterpolation);
		panSouth.add(this.labelError);
		if (this.timeIngestion != null)
			panSouth.add(new JLabel("Ingestion Time: " + new SimpleDateFormat("HH:mm:ss").format(this.timeIngestion)));
		panel.add(panSouth, BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * Generates the graph for the fit of this tab.
	 */
	public void createGraph() {
		// Create chart
		this.data = new XYSeriesCollection();
		XYSeries stomachSeries = getModel().getStomachSeries();
		this.data.addSeries(stomachSeries);

		JFreeChart chart = ChartFactory.createXYLineChart("Stomach retention", "Time (min)", "Stomach retention (%)",
				data, PlotOrientation.VERTICAL, true, true, true);

		// Set bounds
		XYPlot plot = chart.getXYPlot();
		// X axis
		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setRange(-10., stomachSeries.getMaxX() + 10.);
		// Y axis
		NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		yAxis.setRange(-10., stomachSeries.getMaxY() + 10.);

		// Create value setter
		double startX = stomachSeries.getMinX() + .1 * (stomachSeries.getMaxX() - stomachSeries.getMinX());
		double endX = stomachSeries.getMinX() + .7 * (stomachSeries.getMaxX() - stomachSeries.getMinX());
		valueSetter = new JValueSetter(chart);
		valueSetter.addSelector(new Selector(" ", startX, -1, RectangleAnchor.TOP_LEFT), "start");
		valueSetter.addSelector(new Selector(" ", endX, -1, RectangleAnchor.TOP_LEFT), "end");
		valueSetter.addArea("start", "end", "area", null);
		valueSetter.addChartMouseListener(this);
	}

	/**
	 * @return value setter containing the graph of this tab
	 */
	public JValueSetter getValueSetter() {
		return this.valueSetter;
	}

	/**
	 * Sets the time of ingestion to display. If set to null, then deletes the
	 * previous time displayed.
	 * 
	 * @param timeIngestion Time of ingestion to display
	 */
	public void displayTimeIngestion(Date timeIngestion) {
		this.timeIngestion = timeIngestion;
		this.reloadSidePanelContent();
	}

	/**
	 * Gets the fit selected by the user with the combo box.
	 * 
	 * @return type of the fit selected by the user
	 */
	public FitType getSelectedFit() {
		return (FitType) this.fitsChoices.getSelectedItem();
	}

	/**
	 * Displays the fit selected by the user and removes the previous fit if
	 * existing.
	 * 
	 * @see #getSelectedFit()
	 */
	public void drawFit() {
		this.clearFits();

		this.data.addSeries(this.currentFit.getFittedSeries(getModel().getTimes()));
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
	 * Changes the error message. This message is displayed in red. If null is
	 * passed, then the previous message is erased.
	 * 
	 * @param msg message to show or null to erase the last message
	 */
	public void setErrorMessage(String msg) {
		this.labelError.setText(msg);
	}

	@Override
	public JPanel getResultContent() {
		JTabbedPane tab = new JTabbedPane(JTabbedPane.LEFT);

		// Results
		tab.add("Results", this.createPanelResults());

		// Fit
		tab.add("Fit", this.createPanelFit());

		JPanel panel = new JPanel();
		panel.add(tab);
		return panel;
	}

	@Override
	public Component getSidePanelContent() {
		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(this.additionalResults());
		panel.add(tablesResultats());
		panel.add(this.infoResultats());
		return panel;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			this.reloadFit();

			this.changeLabelInterpolation(e.getItem().toString() + " extrapolation");
		}
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// Does nothing
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		if (this.getValueSetter().getGrabbedSelector() != null) {
			this.reloadFit();
		}
	}

}
