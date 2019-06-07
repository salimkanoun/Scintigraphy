package org.petctviewer.scintigraphy.gastric.tabs;

import ij.ImagePlus;
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
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.model.*;
import org.petctviewer.scintigraphy.scin.model.Fit.FitType;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;

public abstract class TabResultDefault extends TabResult implements ItemListener, ChartMouseListener {

	final Unit unitDefault;
	final Unit unitTime;
	private final ImagePlus capture;
	private final JComboBox<FitType> fitsChoices;
	private final JLabel labelInterpolation;
	private final JLabel labelError;
	private final JButton btnAutoFit;
	private final ResultRequest request;
	private final int seriesToGenerate;
	protected Date timeIngestion;
	private XYSeriesCollection data;
	private JValueSetter valueSetterFit, valueSetterLagPhase;
	private JLabel lagPhaseValue;

	TabResultDefault(FenResults parent, ImagePlus capture, String title, Unit unitDefault, Unit unitTime,
					 int seriesToGenerate) {
		super(parent, title);

		// Declare attributes
		this.unitDefault = unitDefault;
		this.unitTime = unitTime;
		this.seriesToGenerate = seriesToGenerate;

		// Prepare request
		this.request = new ResultRequest(null);
		this.request.setFit(new Fit.NoFit(unitDefault));

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
		btnAutoFit.addActionListener(e -> this.selectFit(this.findBestFit()));

		// Set variables
		this.capture = capture;

		this.createGraph();

		this.setComponentToHide(new Component[]{fitsChoices, btnAutoFit});
		this.setComponentToShow(new Component[]{this.labelInterpolation});
		this.createCaptureButton();

		this.reloadDisplay();
	}

	/**
	 * Displays additional results on the tab like time of ingestion or button to launch dynamic acquisition...
	 *
	 * @return panel containing the additional results
	 */
	protected abstract JPanel additionalResults();

	/**
	 * Generates the table with all of the results to display for this scintigraphy.
	 *
	 * @return table containing the results
	 */
	protected JTable tableResults(Result[] results, Unit[] unitsUsed) {
		// Prepare model
		if (this.seriesToGenerate == Model_Gastric.SERIES_STOMACH_PERCENTAGE) {
			getModel().activateTime0();
			getModel().setTimeIngestion(((ControllerWorkflow_Gastric) parent.getController()).specifiedTimeIngestion);
		} else {
			getModel().deactivateTime0();
			getModel().setTimeIngestion(getModel().getFirstImage().getDateAcquisition());
		}

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
				ResultValue res = getModel().getResult(request);
				if (res == null) arr[j] = "--";
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
		if (result.getExtrapolation() == FitType.NONE) lRes.setForeground(Color.RED);
		infoRes.add(lRes);

		if (result.getResultType() == Model_Gastric.LAG_PHASE_PERCENTAGE ||
				result.getResultType() == Model_Gastric.LAG_PHASE_GEOAVG) {
			this.lagPhaseValue = lRes;
		}
	}

	/**
	 * Checks if the value obtained for the specified time is a normal value.<br> The <i>normal</i> value is based upon
	 * <code>tech.snmjournals.org</code>.<br> The only times accepted are <code>30min</code>, <code>60min</code>,
	 * <code>120min</code>, <code>180min</code> and <code>240min</code>.
	 *
	 * @param time  Time for retention in <i>minutes</i>
	 * @param value Value obtained in <i>percentage</i>
	 * @return TRUE if the result is normal and FALSE if the result is abnormal
	 */
	private boolean isRetentionNormal(double time, double value) {
		if (time == 30.) return value >= 70.;
		if (time == 60.) return value >= 30. && value <= 90.;
		if (time == 120.) return value <= 60.;
		if (time == 180.) return value <= 30.;
		if (time == 240.) return value <= 10.;

		throw new IllegalArgumentException("No information for this time (" + time + ")");
	}

	/**
	 * Returns a string indicating the range of the normal values for the specified time.<br> The <i>normal</i>
	 * value is
	 * based upon <code>tech.snmjournals.org</code>.<br> The only times accepted are <code>30min</code>,
	 * <code>60min</code>,
	 * <code>120min</code>, <code>180min</code> and <code>240min</code>.
	 *
	 * @param time Time for retention in minutes
	 * @return readable string indicating the normal range
	 */
	private String retentionNormalArea(double time) {
		if (time == 30.) return ">= 70%";
		if (time == 60.) return ">= 30% and <= 90%";
		if (time == 120.) return "<= 60%";
		if (time == 180.) return "<= 30%";
		if (time == 240.) return "<= 10%";

		throw new IllegalArgumentException("No information for this time (" + time + ")");
	}

	/**
	 * Returns a string indicating the grade of the retention values after 4h.<br> Based upon
	 * <code>tech.snmjournals.org</code>.
	 *
	 * @param value Value of the retention after 4h in <i>percentage</i>
	 * @return <code>string[0]</code> = grade and <code>string[1]</code> = readable
	 * value
	 */
	private JLabel[] gradeOfRetention(double value) {
		JLabel l1, l2;
		if (value < 11.) {
			l1 = new JLabel("--");
			l2 = new JLabel("Result normal");

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

		return new JLabel[]{l1, l2};
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
		if (time < 60.) timeString = (int) time + "min";
		else timeString = (int) (time / 60) + "h";

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
	 * Utility method that generates a panel containing all of the results needed for that scintigraphy.
	 *
	 * @return panel containing the results
	 */
	protected JPanel infoResults(Result[] resultsRequested, Unit[] unitsRequested) {
		if (resultsRequested.length != unitsRequested.length) throw new IllegalArgumentException(
				"Array length must be equals");

		JPanel panel = new JPanel(new BorderLayout());

		boolean hasExtrapolatedValue = false;

		JPanel infoRes = new JPanel();
		infoRes.setLayout(new GridLayout(0, 2));

		for (int i = 0; i < resultsRequested.length - 1; i++) {
			request.changeResultOn(resultsRequested[i]);
			request.setUnit(unitsRequested[i]);

			ResultValue result = getModel().getResult(request);
			hasExtrapolatedValue = result.isExtrapolated() || hasExtrapolatedValue;
			this.displayResult(infoRes, result);
		}

		// Retention 30min
		request.changeResultOn(resultsRequested[resultsRequested.length - 1]);
		request.setUnit(unitsRequested[unitsRequested.length - 1]);
		ResultValue result = getModel().getRetentionResult(request, 30.);
		hasExtrapolatedValue = result.isExtrapolated() || hasExtrapolatedValue;
		this.displayRetentionResult(infoRes, 30., result);
		// Retention from 1h to 4h
		for (double time = 60.; time <= 240.; time += 60.) {
			result = getModel().getRetentionResult(request, time);
			hasExtrapolatedValue = result.isExtrapolated() || hasExtrapolatedValue;
			this.displayRetentionResult(infoRes, time, result);
		}
		// Grade of retention at 4h
		JLabel[] grade = this.gradeOfRetention(result.getValue());
		infoRes.add(grade[0]);
		infoRes.add(grade[1]);

		panel.add(infoRes, BorderLayout.CENTER);
		if (hasExtrapolatedValue) {
			JLabel l;
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
			XYSeries series = ((XYSeriesCollection) this.getValueSetterFit().retrieveValuesInSpan()).getSeries(0);
			this.request.setFit(
					Fit.createFit(getSelectedFit(), Library_JFreeChart.invertArray(series.toArray()), unitDefault));

			this.drawFit();
			this.setErrorMessage(null);
			this.reloadSidePanelContent();
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

	private Model_Gastric getModel() {
		return (Model_Gastric) this.parent.getModel();
	}

	/**
	 * Creates the panel with the images of the graphs
	 */
	private JPanel createPanelResults() {
		if (this.seriesToGenerate == Model_Gastric.SERIES_STOMACH_PERCENTAGE) {
			getModel().activateTime0();
			getModel().setTimeIngestion(((ControllerWorkflow_Gastric) parent.getController()).specifiedTimeIngestion);

			JPanel panel = new JPanel(new GridLayout(2, 2));

			panel.add(new DynamicImage(capture.getImage()));
			panel.add(getModel().createGraph_3());
			panel.add(getModel().createGraph_1());

			if (((ControllerWorkflow_Gastric) parent.getController()).isDynamicStarted()) getModel().deactivateTime0();
			panel.add(getModel().createGraph_2());

			return panel;
		} else if (this.seriesToGenerate == Model_Gastric.SERIES_DECAY_FUNCTION) {
			getModel().deactivateTime0();
			getModel().setTimeIngestion(getModel().getFirstImage().getDateAcquisition());

			JPanel panel = new JPanel(new GridLayout(2, 1));

			panel.add(new DynamicImage(capture.getImage()));
			panel.add(getModel().createGraph_4(this.unitDefault));

			return panel;
		}

		// Empty
		return new JPanel();
	}

	/**
	 * Creates the panel with the graph fitted.
	 */
	private JPanel createPanelFit() {
		JPanel panel = new JPanel(new BorderLayout());

		panel.add(this.valueSetterFit, BorderLayout.CENTER);

		JPanel panSouth = new JPanel();
		panSouth.setLayout(new BoxLayout(panSouth, BoxLayout.LINE_AXIS));

		panSouth.add(this.fitsChoices);
		panSouth.add(this.btnAutoFit);
		panSouth.add(this.labelInterpolation);
		panSouth.add(this.labelError);

		panel.add(panSouth, BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * Creates the panel with the selector for the lag phase.
	 */
	private Component createPanelLagPhase() {
		JPanel panel = new JPanel(new BorderLayout());

		valueSetterLagPhase = new JValueSetter(this.valueSetterFit.getChart());
		if (this.seriesToGenerate == Model_Gastric.SERIES_STOMACH_PERCENTAGE) this.request.changeResultOn(
				Model_Gastric.LAG_PHASE_PERCENTAGE);
		else this.request.changeResultOn(Model_Gastric.LAG_PHASE_GEOAVG);
		this.request.setUnit(Unit.MINUTES);

		Selector selector = new Selector("Lag Phase", getModel().getResult(request).getValue(), 0,
										 RectangleAnchor.BOTTOM_LEFT);
		valueSetterLagPhase.addSelector(selector, "lag_phase");

		valueSetterLagPhase.addChartMouseListener(new ChartMouseListener() {
			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
			}

			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				if (valueSetterLagPhase.getGrabbedSelector() != null) {
					// Update lag phase
					lagPhaseValue.setText(ResultValue.notNegative(selector.getXValue()) + " " + Unit.MINUTES.abbrev());
				}
			}
		});

		panel.add(valueSetterLagPhase, BorderLayout.CENTER);

		return panel;
	}

	protected JPanel createPanelCustomRetention() {
		JTextField fieldCustomRetention = new JTextField(3);
		JLabel resultRetention = new JLabel("--");
		fieldCustomRetention.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateResult();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateResult();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateResult();
			}

			public void updateResult() {
				// Calculate result
				try {
					// Prepare model
					if (seriesToGenerate == Model_Gastric.SERIES_STOMACH_PERCENTAGE) {
						getModel().activateTime0();
						getModel().setTimeIngestion(
								((ControllerWorkflow_Gastric) parent.getController()).specifiedTimeIngestion);
						request.changeResultOn(Model_Gastric.RETENTION_PERCENTAGE);
					} else {
						getModel().deactivateTime0();
						getModel().setTimeIngestion(getModel().getFirstImage().getDateAcquisition());
						request.changeResultOn(Model_Gastric.RETENTION_GEOAVG);
					}

					request.setUnit(Unit.PERCENTAGE);

					ResultValue result = getModel().getRetentionResult(request, Double.parseDouble(
							fieldCustomRetention.getText()));
					// Update result
					resultRetention.setText(result.formatValue() + result.getUnit().abbrev());
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
				Fit fit = Fit.createFit(type, Library_JFreeChart.invertArray(dataset), unitDefault);

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
	 * Selects the best fit for the current graph.
	 *
	 * @see #findBestFit()
	 */
	public void selectBestFit() {
		this.fitsChoices.setSelectedItem(this.findBestFit());
	}

	/**
	 * Generates the graph for the fit of this tab.
	 */
	public void createGraph() {
		// Prepare model
		if (seriesToGenerate == Model_Gastric.SERIES_STOMACH_PERCENTAGE) {
			getModel().activateTime0();
			getModel().setTimeIngestion(((ControllerWorkflow_Gastric) parent.getController()).specifiedTimeIngestion);
		} else {
			getModel().deactivateTime0();
			getModel().setTimeIngestion(getModel().getFirstImage().getDateAcquisition());
		}

		// Create chart
		this.data = new XYSeriesCollection();
		XYSeries stomachSeries = getModel().generateSeries(this.seriesToGenerate, this.unitDefault);
		this.data.addSeries(stomachSeries);

		JFreeChart chart = ChartFactory.createXYLineChart("Stomach retention", "Time (" + this.unitTime.abbrev() + ")",
														  "Stomach retention (" + this.unitDefault.abbrev() + ")",
														  data,
														  PlotOrientation.VERTICAL, true, true, true);

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
		valueSetterFit = new JValueSetter(chart);
		valueSetterFit.addSelector(new Selector(" ", startX, -1, RectangleAnchor.TOP_LEFT), "start");
		valueSetterFit.addSelector(new Selector(" ", endX, -1, RectangleAnchor.TOP_LEFT), "end");
		valueSetterFit.addArea("start", "end", "area", null);
		valueSetterFit.addChartMouseListener(this);
	}

	/**
	 * @return value setter containing the graph of this tab
	 */
	public JValueSetter getValueSetterFit() {
		return this.valueSetterFit;
	}

	/**
	 * Sets the time of ingestion to display. If set to null, then deletes the previous time displayed.
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
	 * Displays the fit selected by the user and removes the previous fit if existing.
	 *
	 * @see #getSelectedFit()
	 */
	public void drawFit() {
		this.clearFits();

		this.data.addSeries(this.request.getFit().generateFittedSeries(getModel().generateTime()));
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
	 * Changes the error message. This message is displayed in red. If null is passed, then the previous message is
	 * erased.
	 *
	 * @param msg message to show or null to erase the last message
	 */
	public void setErrorMessage(String msg) {
		this.labelError.setText(msg);
	}

	/**
	 * Checks if the specified button is the auto-fit button of this view.
	 *
	 * @param btn Button to check
	 * @return TRUE if the button is the auto-fit button and FALSE otherwise
	 */
	public boolean isButtonAutoFit(JButton btn) {
		return this.btnAutoFit == btn;
	}

	@Override
	public Container getResultContent() {
		JTabbedPane tab = new JTabbedPane(JTabbedPane.LEFT);

		// Results
		tab.add("Results", this.createPanelResults());

		// Fit
		tab.add("Fit", this.createPanelFit());

		// Lag phase
		tab.add("Lag phase", this.createPanelLagPhase());

		tab.addChangeListener(e -> {
			valueSetterFit.updateAreas();
			valueSetterLagPhase.revalidate();
		});

		return tab;
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
		if (this.getValueSetterFit().getGrabbedSelector() != null) {
			this.reloadFit();
		}
	}

}
