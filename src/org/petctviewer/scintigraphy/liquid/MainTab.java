package org.petctviewer.scintigraphy.liquid;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.events.FitChangeEvent;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.FitPanel;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.model.Fit;
import org.petctviewer.scintigraphy.scin.model.ResultRequest;
import org.petctviewer.scintigraphy.scin.model.ResultValue;
import org.petctviewer.scintigraphy.scin.model.Unit;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class MainTab extends TabResult implements ChangeListener {
	private final String xLabel = "Time (" + Unit.MINUTES.abbrev() + ")";
	private final String yLabel = "Stomach retention (" + Unit.COUNTS.abbrev() + ")";

	private final FitPanel fitPanel;
	private final JLabel labelExtrapolation;
	private final JLabel labelTHalfResult;

	public MainTab(FenResults parent) {
		super(parent, "Liquid Phase", true);

		// Init variables
		this.fitPanel = new FitPanel();
		this.fitPanel.addChangeListener(this);

		this.labelExtrapolation = new JLabel();
		this.labelTHalfResult = new JLabel();

		this.createGraph();

		this.setComponentToHide(this.fitPanel.getComponentsToHide());
		this.setComponentToShow(this.fitPanel.getComponentsToShow());
		this.reloadDisplay();
	}

	private LiquidModel getModel() {
		return (LiquidModel) parent.getModel();
	}

	private void createGraph() {
		XYSeries series = getModel().getSeries();
		XYSeriesCollection data = new XYSeriesCollection(series);

		String graphTitle = "Stomach retention - Liquid phase";
		JFreeChart chart = ChartFactory.createXYLineChart(graphTitle, xLabel, yLabel, data, PlotOrientation.VERTICAL,
														  true, true, true);
		this.fitPanel.createGraph(chart, data, Unit.COUNTS);
	}

	private void updateSidePanelContent(Fit fit) {
		// T1/2
		ResultRequest request = new ResultRequest(LiquidModel.RES_T_HALF);
		request.setFit(fit);

		ResultValue resTHalf = getModel().getResult(request);
		this.labelTHalfResult.setText(resTHalf.formatValue() + " " + resTHalf.getUnit().abbrev());


		this.labelExtrapolation.setText("(*) The results are calculated with a " + fit.getType() + " extrapolation");

		this.parent.pack();

		// Update model
		getModel().setCurrentFit(fit);
	}

	@Override
	public Component getSidePanelContent() {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel panCenter = new JPanel(new GridLayout(0, 2));

		panCenter.add(new JLabel("T 1/2"));
		panCenter.add(this.labelTHalfResult);
		panel.add(panCenter, BorderLayout.CENTER);

		panel.add(this.labelExtrapolation, BorderLayout.SOUTH);

		return panel;
	}

	@Override
	public Container getResultContent() {
		return this.fitPanel;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e instanceof FitChangeEvent) {
			this.updateSidePanelContent(((FitChangeEvent) e).getChangedFit());
		}
	}
}
