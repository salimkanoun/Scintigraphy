package org.petctviewer.scintigraphy.gastric.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import org.petctviewer.scintigraphy.gastric.ControllerWorkflow_Gastric;
import org.petctviewer.scintigraphy.gastric.Model_Gastric;
import org.petctviewer.scintigraphy.gastric.Result;
import org.petctviewer.scintigraphy.gastric.ResultRequest;
import org.petctviewer.scintigraphy.gastric.ResultValue;
import org.petctviewer.scintigraphy.gastric.Unit;
import org.petctviewer.scintigraphy.gastric.dynamic.DynGastricScintigraphy;
import org.petctviewer.scintigraphy.gastric.gui.Fit;
import org.petctviewer.scintigraphy.gastric.gui.Fit.FitType;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import com.itextpdf.text.Font;

import ij.ImagePlus;

public class TabMainResult extends TabResult implements ItemListener, ChartMouseListener {

	private ImagePlus capture;
	private Date timeIngestion;

	private XYSeriesCollection data;
	private JValueSetter valueSetter;

	private JComboBox<FitType> fitsChoices;
	private JLabel labelInterpolation, labelError;
	private JButton btnAutoFit, btnDynAcquisition;

	private ResultRequest request;

	private Unit unitDefault, unitTime;

	public TabMainResult(FenResults parent, ImagePlus capture, ControllerWorkflow_Gastric controller) {
		super(parent, "Intragastric Distribution");

		// Declare attributes
		this.unitDefault = Unit.PERCENTAGE;
		this.unitTime = Unit.TIME;

		// Prepare request
		this.request = new ResultRequest(null);
		this.request.setFit(new Fit.NoFit());

		// Instantiate components
		fitsChoices = new JComboBox<>(FitType.values());
		fitsChoices.addItemListener(this);

		// - Label interpolation
		this.labelInterpolation = new JLabel();
		this.labelInterpolation.setVisible(false);

		// - Label error
		this.labelError = new JLabel();
		this.labelError.setForeground(Color.RED);

		// - Button to auto-fit the graph
		btnAutoFit = new JButton("Auto-fit");
		btnAutoFit.addActionListener(controller);
		btnAutoFit.setActionCommand(ControllerWorkflow_Gastric.COMMAND_FIT_BEST_1);

		// - Button to launch the dynamic acquisition
		this.btnDynAcquisition = new JButton("Launch dynamic acquisition");
		this.btnDynAcquisition.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Finish gastric
				controller.getVue().setVisible(false);

				// Stop this button
				btnDynAcquisition.setEnabled(false);

				// Start scintigraphy
				new DynGastricScintigraphy(getModel(), parent);
			}
		});

		// Set variables
		this.capture = capture;

		this.createGraph();

		Component[] hide = new Component[] { fitsChoices, btnAutoFit };
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
		Unit[] unitsUsed = new Unit[] { this.unitTime, this.unitDefault, this.unitDefault, this.unitDefault };

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
				ResultRequest request = new ResultRequest(results[j]);
				request.setUnit(unitsUsed[j]);
				request.setIndexImage(i);
				ResultValue res = model.getResult(request);
				if (res == null)
					arr[j] = "--";
				else {
					res.convert(unitsUsed[j]);
					arr[j] = res.formatValue();
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
		JLabel lRes = new JLabel(result.formatValue() + " " + result.getUnit());
		if (result.getExtrapolation() == FitType.NONE)
			lRes.setForeground(Color.RED);
		infoRes.add(lRes);
	}

	/**
	 * Checks if the value obtained for the specified time is a normal value.<br>
	 * The <i>normal</i> value is based upon <code>tech.snmjournals.org</code>.<br>
	 * The only times accepted are <code>30min</code>, <code>60min</code>,
	 * <code>120min</code>, <code>180min</code> and <code>240min</code>.
	 * 
	 * @param time  Time for retention in minutes
	 * @param value Value obtained in percentage
	 * @return TRUE if the result is normal and FALSE if the result is abnormal
	 */
	private boolean isRetentionNormal(double time, double value) {
		if (time == 30.)
			return value >= 70.;
		if (time == 60.)
			return value >= 30. && value <= 90.;
		if (time == 120.)
			return value <= 60.;
		if (time == 180.)
			return value <= 30.;
		if (time == 240.)
			return value <= 10.;

		throw new IllegalArgumentException("No information for this time (" + time + ")");
	}

	/**
	 * Returns a string indicating the range of the normal values for the specified
	 * time.<br>
	 * The <i>normal</i> value is based upon <code>tech.snmjournals.org</code>.<br>
	 * The only times accepted are <code>30min</code>, <code>60min</code>,
	 * <code>120min</code>, <code>180min</code> and <code>240min</code>.
	 * 
	 * @param time Time for retention in minutes
	 * @return readable string indicating the normal range
	 */
	private String retentionNormalArea(double time) {
		if (time == 30.)
			return ">= 70%";
		if (time == 60.)
			return ">= 30% and <= 90%";
		if (time == 120.)
			return "<= 60%";
		if (time == 180.)
			return "<= 30%";
		if (time == 240.)
			return "<= 10%";

		throw new IllegalArgumentException("No information for this time (" + time + ")");
	}

	/**
	 * Returns a string indicating the grade of the retention values after 4h.<br>
	 * Based upon <code>tech.snmjournals.org</code>.
	 * 
	 * @param value Value of the retention after 4h
	 * @return <code>string[0]</code> = grade and <code>string[1]</code> = readable
	 *         value
	 */
	private JLabel[] gradeOfRetention(double value) {
		JLabel l1, l2;
		if (value < 11.) {
			l1 = new JLabel("--");
			l2 = new JLabel("Result is OK");

			l1.setForeground(Color.GREEN);
			l2.setForeground(Color.GREEN);
		} else {
			if (value >= 11. && value <= 20.) {
				l1 = new JLabel("Grade 1");
				l2 = new JLabel("Mild");
			} else if (value > 20. && value <= 35.) {
				l1 = new JLabel("Grade 2");
				l2 = new JLabel("Moderate");
			} else if (value > 35. && value <= 50.) {
				l1 = new JLabel("Grade 3");
				l2 = new JLabel("Severe");
			} else {
				l1 = new JLabel("Grade 4");
				l2 = new JLabel("Very severe");
			}

			l1.setForeground(Color.RED);
			l2.setForeground(Color.RED);
		}

		l1.setFont(l1.getFont().deriveFont(Font.BOLD));
		l2.setFont(l2.getFont().deriveFont(12f));

		return new JLabel[] { l1, l2 };
	}

	/**
	 * Utility method to print the result for the retention at a certain time.
	 * 
	 * @param infoRes Panel on which to place the result in
	 * @param time    Time of the retention in minutes
	 * @param result  Result of the model to display
	 */
	private void displayRetentionResult(JPanel infoRes, double time, ResultValue result) {
		// Format time string
		String timeString;
		if (time < 60.)
			timeString = (int) time + "min";
		else
			timeString = (int) (time / 60) + "h";

		infoRes.add(new JLabel(result.getResultType().getName() + " at " + timeString + ":"));

		JLabel lRes = new JLabel(result.formatValue() + " " + result.getUnit());
		if (result.getExtrapolation() == FitType.NONE) {
			lRes.setForeground(Color.RED);
		}
		if (!this.isRetentionNormal(time, result.getValue())) {
			lRes.setForeground(Color.RED);
			lRes.setToolTipText("The result is abnormal! Value should be " + this.retentionNormalArea(time));
		}
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

		Result[] resultsRequested = new Result[] { Model_Gastric.START_ANTRUM, Model_Gastric.START_INTESTINE,
				Model_Gastric.LAG_PHASE_PERCENTAGE, Model_Gastric.T_HALF_PERCENTAGE };
		Unit[] unitsRequested = new Unit[] { this.unitDefault, this.unitDefault, this.unitTime, this.unitTime };

		for (int i = 0; i < resultsRequested.length; i++) {
			request.changeResultOn(resultsRequested[i]);
			request.setUnit(unitsRequested[i]);

			ResultValue result = getModel().getResult(request);
			hasExtrapolatedValue = result.isExtrapolated();
			this.displayResult(infoRes, result);
		}

		// Retention 30min
		request.changeResultOn(Model_Gastric.RETENTION_PERCENTAGE);
		request.setUnit(unitTime);
		ResultValue result = getModel().getRetentionResult(request, 30.);
		hasExtrapolatedValue = result.isExtrapolated();
		this.displayRetentionResult(infoRes, 30., result);
		// Retention from 1h to 4h
		for (double time = 60.; time <= 240.; time += 60.) {
			result = getModel().getRetentionResult(request, time);
			hasExtrapolatedValue = result.isExtrapolated();
			this.displayRetentionResult(infoRes, time, result);
		}
		// Grade of retention at 4h
		JLabel[] grade = this.gradeOfRetention(result.getValue());
		infoRes.add(grade[0]);
		infoRes.add(grade[1]);

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
			this.request.setFit(Fit.createFit(getSelectedFit(), Library_JFreeChart.invertArray(series.toArray())));

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
	 * <li>button to launch dyn acquisition</li>
	 * </ul>
	 * 
	 * @return panel containing the additional results
	 */
	private JPanel additionalResults() {
		JPanel panel = new JPanel(new GridLayout(0, 1));

		// Time of ingestion
		if (this.timeIngestion != null) {
			panel.add(new JLabel("Ingestion Time: " + new SimpleDateFormat("HH:mm:ss").format(timeIngestion)));
		}

		// Button dyn acquisition
		panel.add(this.btnDynAcquisition);

		return panel;
	}

	/**
	 * Removes all previous fits.
	 */
	private void clearFits() {
		for (int i = 1; i < this.data.getSeriesCount(); i++)
			this.data.removeSeries(i);
	}

	private Model_Gastric getModel() {
		return (Model_Gastric) this.parent.getModel();
	}

	/**
	 * Creates the panel with the images of the graphs
	 */
	private JPanel createPanelResults() {
		JPanel panel = new JPanel(new GridLayout(2, 2));

		panel.add(new DynamicImage(capture.getImage()));
		panel.add(getModel().createGraph_3());
		panel.add(getModel().createGraph_1());
		panel.add(getModel().createGraph_2());

		return panel;
	}

	/**
	 * Creates the panel with the graph fitted.
	 */
	private JPanel createPanelFit() {
		JPanel panel = new JPanel(new BorderLayout());

		panel.add(this.valueSetter, BorderLayout.CENTER);

		JPanel panSouth = new JPanel();
		panSouth.setLayout(new BoxLayout(panSouth, BoxLayout.LINE_AXIS));

		panSouth.add(this.fitsChoices);
		panSouth.add(this.btnAutoFit);
		panSouth.add(this.labelInterpolation);
		panSouth.add(this.labelError);

		panel.add(panSouth, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createPanelCustomRetention() {
		JTextField fieldCustomRetention = new JTextField(3);
		JLabel resultRetention = new JLabel("--");
		fieldCustomRetention.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateResult();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateResult();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateResult();
			}

			public void updateResult() {
				// Calculate result
				try {
					request.changeResultOn(Model_Gastric.RETENTION_PERCENTAGE);
					request.setUnit(unitTime);
					ResultValue result = getModel().getRetentionResult(request,
							Double.parseDouble(fieldCustomRetention.getText()));
					// Update result
					resultRetention.setText(result.formatValue() + result.getUnit().abrev());
				} catch (NumberFormatException exception) {
					resultRetention.setText("--");
				}
			}
		});

		JPanel panRetention = new JPanel(new GridLayout(1, 2));

		JPanel panWest = new JPanel(new FlowLayout());
		panWest.add(new JLabel("Calculate retention at"));
		panWest.add(fieldCustomRetention);
		panWest.add(new JLabel("min"));
		panWest.add(new JLabel(" ="));
		panRetention.add(panWest);

		panRetention.add(resultRetention);

		return panRetention;
	}

	/**
	 * Finds the best fit matching the graph. Only the values in the area specified
	 * by the user are taken into account.<br>
	 * The best fit is determined by the method of least squares.
	 * 
	 * @return best fit for this graph
	 */
	public FitType findBestFit() {
		double bestScore = Double.MAX_VALUE;
		FitType bestFit = null;
		for (FitType type : FitType.values()) {
			double[][] dataset = ((XYSeriesCollection) this.valueSetter.retrieveValuesInSpan()).getSeries(0).toArray();

			try {
				// Create fit
				Fit fit = Fit.createFit(type, Library_JFreeChart.invertArray(dataset));

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

	/**
	 * Selects the specified type for the fit.
	 * 
	 * @param type Type of fit to select
	 */
	public void selectFit(FitType type) {
		this.fitsChoices.setSelectedItem(type);
	}

	/**
	 * Generates the graph for the fit of this tab.
	 */
	public void createGraph() {
		// Create chart
		this.data = new XYSeriesCollection();
		XYSeries stomachSeries = getModel().generateStomachSeries(this.unitDefault);
		this.data.addSeries(stomachSeries);

		JFreeChart chart = ChartFactory.createXYLineChart("Stomach retention", "Time (" + this.unitTime.abrev() + ")",
				"Stomach retention (" + this.unitDefault.abrev() + ")", data, PlotOrientation.VERTICAL, true, true,
				true);

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

		this.data.addSeries(this.request.getFit().generateFittedSeries(getModel().getTimes()));
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
	public Container getResultContent() {
		JTabbedPane tab = new JTabbedPane(JTabbedPane.LEFT);

		// Results
		tab.add("Results", this.createPanelResults());

		// Fit
		tab.add("Fit", this.createPanelFit());

		return tab;
	}

	@Override
	public Component getSidePanelContent() {
		JPanel panel = new JPanel(new BorderLayout());

		// North
		panel.add(this.additionalResults(), BorderLayout.NORTH);

		// Center
		JPanel panCenter = new JPanel(new BorderLayout());

		// - Table
		JPanel panTable = new JPanel(new BorderLayout());
		JTable table = tablesResultats();
		panTable.add(table.getTableHeader(), BorderLayout.PAGE_START);
		panTable.add(table, BorderLayout.CENTER);
		panCenter.add(panTable, BorderLayout.CENTER);

		panCenter.add(this.infoResultats(), BorderLayout.SOUTH);
		panel.add(panCenter, BorderLayout.CENTER);

		// - Custom retention
		JPanel panCustomRetention = this.createPanelCustomRetention();
		panel.add(panCustomRetention, BorderLayout.SOUTH);

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
