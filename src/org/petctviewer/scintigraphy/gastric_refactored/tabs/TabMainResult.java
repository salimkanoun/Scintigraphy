package org.petctviewer.scintigraphy.gastric_refactored.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.gastric_refactored.ControllerWorkflow_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric.Result;
import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric.ResultValue;
import org.petctviewer.scintigraphy.gastric_refactored.dynamic.DynGastricScintigraphy;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.FitType;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.NoFit;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;

public class TabMainResult extends TabResult {

	private ImagePlus capture;
	private Date timeIngestion;

	private ControllerWorkflow controller;

	private XYSeriesCollection data;
	private JValueSetter valueSetter;

	private JComboBox<FitType> fitsChoices;
	private JLabel labelInterpolation, labelError;

	public TabMainResult(FenResults parent, ImagePlus capture, ControllerWorkflow_Gastric controller) {
		super(parent, "Result", true);

		// Instantiate variables
		fitsChoices = new JComboBox<>(FitType.values());
		fitsChoices.addItemListener(controller);

		this.labelInterpolation = new JLabel();
		this.labelInterpolation.setVisible(false);

		this.labelError = new JLabel();
		this.labelError.setForeground(Color.RED);

		this.capture = capture;
		this.controller = controller;

		this.createGraph();

		this.reloadDisplay();
	}

	private JTable tablesResultats() {
		Model_Gastric model = (Model_Gastric) this.parent.getModel();
		JTable table = new JTable(0, 4);
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		String[] arr = new String[tableModel.getColumnCount()];
		tableModel.addRow(new String[] { "Time (min)", "Stomach (%)", "Fundus (%)", "Antrum (%)" });
		for (int i = 0; i < ((Model_Gastric) this.parent.getModel()).nbAcquisitions(); i++) {
			for (int j = 0; j < tableModel.getColumnCount(); j++) {
				ResultValue res = model.getImageResult(Result.imageResults()[j], i);
				arr[j] = res.value();
			}
			tableModel.addRow(arr);
		}
		table.setRowHeight(30);
		MatteBorder border = new MatteBorder(1, 1, 1, 1, Color.BLACK);
		table.setBorder(border);
		return table;
	}

	/**
	 * Displays the result.
	 * 
	 * @param infoRes Panel on which to place the result in
	 * @param result  Result of the model to display
	 * @return
	 */
	private void displayResult(JPanel infoRes, ResultValue result) {
		infoRes.add(new JLabel(result.type.getName() + ":"));
		JLabel lRes = new JLabel(result.value() + " " + result.type.getUnit());
		if (result.extrapolation == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);
	}

	// TODO: maybe combine this method with displayResult()
	/**
	 * Displays the result for the retention at a certain time.
	 * 
	 * @param infoRes Panel on which to place the result in
	 * @param time    Time of the retention in minutes
	 * @param result  Result of the model to display
	 * @throws IllegalArgumentException if the result is not a RETENTION type
	 */
	private void displayRetentionResult(JPanel infoRes, double time, ResultValue result)
			throws IllegalArgumentException {
		if (result.type != Result.RETENTION)
			throw new IllegalArgumentException("Result type must be " + Result.RETENTION);

		infoRes.add(new JLabel(result.type.getName() + " at " + (int) (time / 60) + "h:"));
		// The value cannot be negative, so we restrain it with Math.max
		JLabel lRes = new JLabel(result.value() + " " + result.type.getUnit());
		if (result.extrapolation == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);
	}

	private JPanel infoResultats() {
		JPanel panel = new JPanel(new BorderLayout());

		boolean hasExtrapolatedValue = false;

		JPanel infoRes = new JPanel();
		infoRes.setLayout(new GridLayout(0, 2));

		ResultValue result = getModel().getResult(Result.START_ANTRUM);
		hasExtrapolatedValue = result.extrapolation != null;
		this.displayResult(infoRes, result);

		result = getModel().getResult(Result.START_INTESTINE);
		hasExtrapolatedValue = result.extrapolation != null;
		this.displayResult(infoRes, result);

		result = getModel().getResult(Result.LAG_PHASE);
		hasExtrapolatedValue = result.extrapolation != null;
		this.displayResult(infoRes, result);

		result = getModel().getResult(Result.T_HALF);
		hasExtrapolatedValue = result.extrapolation != null;
		this.displayResult(infoRes, result);

		for (double time = 60.; time <= 240.; time += 60.) {
			result = getModel().retentionAt(time);
			hasExtrapolatedValue = result.extrapolation != null;
			this.displayRetentionResult(infoRes, time, result);
		}

		panel.add(infoRes, BorderLayout.CENTER);
		if (hasExtrapolatedValue) {
			JLabel l = null;
			if (getModel().getExtrapolation() instanceof NoFit) {
				l = new JLabel("(*) No fit has been selected to extrapolate the values!");
				l.setForeground(Color.RED);
			} else {
				l = new JLabel("(*) The results are calculated with a " + getModel().getExtrapolation().toString()
						+ " extrapolation");
			}
			panel.add(l, BorderLayout.SOUTH);
		}
		return panel;
	}

	public void displayTimeIngestion(Date timeIngestion) {
		this.timeIngestion = timeIngestion;
		this.reloadSidePanelContent();
	}

	private JPanel additionalResults() {
		JPanel panel = new JPanel();

		if (this.timeIngestion != null) {
			panel.add(new JLabel("Ingestion Time: " + new SimpleDateFormat("HH:mm:ss").format(timeIngestion)));
		}

		return panel;
	}

	@Override
	public Component getSidePanelContent() {
		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(this.additionalResults());
		panel.add(new JScrollPane(tablesResultats()));
		panel.add(this.infoResultats());
		return panel;
	}

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
		valueSetter.addChartMouseListener((ControllerWorkflow_Gastric) this.parent.getController());

		// By default, no extrapolation
		getModel().setExtrapolation(new NoFit());
		this.drawFit(getModel().getFittedSeries());
	}

	/**
	 * Removes all previous fits.
	 */
	private void clearFits() {
		for (int i = 1; i < this.data.getSeriesCount(); i++)
			this.data.removeSeries(i);
	}

	public JValueSetter getValueSetter() {
		return this.valueSetter;
	}

	public FitType getSelectedFit() {
		return (FitType) this.fitsChoices.getSelectedItem();
	}

	/**
	 * Displays the specified fit and removes the previous fit if existing.
	 * 
	 * @param series Fit to draw
	 */
	public void drawFit(XYSeries series) {
		this.clearFits();

		this.data.addSeries(series);
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

}
