package org.petctviewer.scintigraphy.esophageus.resultats.tabs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.petctviewer.scintigraphy.esophageus.resultats.Model_Resultats_EsophagealTransit;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import javax.swing.*;
import java.awt.*;

public class TabRentention extends TabResult {

	private JFreeChart graphRetention;

	private Selector selectorRentention;
	private double[] selectorRetentionValue;

	private JRadioButton[] radioButtonRetention;

	private JLabel[] retention10sLabel;
	private static int numAcquisitionRetention = 0;

	private final Model_Resultats_EsophagealTransit modeleApp;

	private final Integer nbAcquisition;

	public TabRentention(int nbAcquisition, FenResults parent, Model_Resultats_EsophagealTransit model) {
		super(parent, "Retention");
		this.modeleApp = model;
		this.nbAcquisition = nbAcquisition;

//		this.createCaptureButton("Retention");
		this.setAdditionalInfo("Retention");

		this.reloadDisplay();
	}

	private void setVisibilitySeriesGraph(JFreeChart graph, int numSerie, boolean visibility) {
		XYItemRenderer renderer = graph.getXYPlot().getRenderer();
		renderer.setSeriesVisible(numSerie, visibility);
		renderer.setSeriesPaint(numSerie, Color.red);

	}

	@Override
	public Component getSidePanelContent() {

		this.getResultContent();

		if (nbAcquisition == null)
			return null;

		JPanel radioButtonRetentionPanel = new JPanel();
		radioButtonRetentionPanel.setLayout(new GridLayout(nbAcquisition, 1));

		ButtonGroup buttonGroupRetention = new ButtonGroup();
		radioButtonRetention = new JRadioButton[nbAcquisition];
		for (int i = 0; i < nbAcquisition; i++) {
			radioButtonRetention[i] = new JRadioButton("Acquisition " + (i + 1));
			radioButtonRetention[i].addItemListener(e -> {
				TabRentention tab = TabRentention.this;
				for (int i1 = 0; i1 < tab.radioButtonRetention.length; i1++) {
					if (e.getSource().equals(radioButtonRetention[i1])) {
						tab.setVisibilitySeriesGraph(tab.graphRetention, i1, true);
						numAcquisitionRetention = i1;
						tab.selectorRentention.setXValue(selectorRetentionValue[i1]);

					} else {
						tab.setVisibilitySeriesGraph(tab.graphRetention, i1, false);
					}
				}
			});
			buttonGroupRetention.add(radioButtonRetention[i]);
			radioButtonRetentionPanel.add(radioButtonRetention[i]);
		}

		JPanel radioButtonRetentionPanelFlow = new JPanel();
		radioButtonRetentionPanelFlow.setLayout(new FlowLayout());
		radioButtonRetentionPanelFlow.add(radioButtonRetentionPanel);

		Box sidePanel = Box.createVerticalBox();
		SidePanel sidePanelScin = new SidePanel(null, modeleApp.esoPlugIn.getStudyName(), modeleApp.getImagePlus());
		sidePanel.add(sidePanelScin);

		sidePanel.add(radioButtonRetentionPanelFlow);

		JPanel retentionResultPanel = new JPanel();
		retentionResultPanel.setLayout(new GridLayout(nbAcquisition + 1, 1));

		retentionResultPanel.add(new JLabel("Decrease 10s after peak"));
		double[] retention10s = modeleApp.retentionAllPoucentage();
		retention10sLabel = new JLabel[nbAcquisition];
		for (int i = 0; i < retention10s.length; i++) {
			retention10sLabel[i] = new JLabel("Acquisition " + (i + 1) + " : " + (retention10s[i]) + "%");
			retentionResultPanel.add(retention10sLabel[i]);
		}

		JPanel retentionResultPanelFlow = new JPanel();
		retentionResultPanelFlow.setLayout(new FlowLayout());
		retentionResultPanelFlow.add(retentionResultPanel);

		sidePanel.add(retentionResultPanelFlow);

		radioButtonRetention[0].setSelected(true);

		return sidePanel;
	}

	@Override
	public JPanel getResultContent() {
		// graph center
		graphRetention = ChartFactory.createXYLineChart("Retention", "s", "Count/s", null);

		graphRetention.getXYPlot().setDataset(modeleApp.retentionForGraph());

		XYLineAndShapeRenderer rendererTransit = new XYLineAndShapeRenderer();
		// monter les formes des points
		rendererTransit.setSeriesShapesVisible(0, true);

		// pour avoir les infos des points quand on passe la souris dessus
		rendererTransit.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		graphRetention.getXYPlot().setRenderer(rendererTransit);


		// grille en noir
		this.graphRetention.getXYPlot().setRangeGridlinePaint(Color.black);
		this.graphRetention.getXYPlot().setDomainGridlinePaint(Color.black);

		selectorRetentionValue = modeleApp.retentionAllX();

		JValueSetter valueSetterRetention = new JValueSetter(graphRetention);
		valueSetterRetention.addChartMouseListener(new ChartMouseListener() {

			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				TabRentention tab = TabRentention.this;
				tab.selectorRetentionValue[numAcquisitionRetention] = tab.selectorRentention.getXValue();

				double retention = modeleApp
						.retentionPoucentage(tab.selectorRentention.getXValue(), numAcquisitionRetention);
				retention10sLabel[numAcquisitionRetention]
						.setText("Acquisition " + (numAcquisitionRetention + 1) + " : " + retention + "%");

				// on l'envoi au modele pour le csv
				modeleApp.setRetentionDecrease(numAcquisitionRetention,
						retention);
			}
		});

		selectorRentention = new Selector("max", 1, -1, RectangleAnchor.TOP_RIGHT);
		valueSetterRetention.addSelector(selectorRentention, "max");
		
		// Hide every curves exept the first one
		for (int i = 1; i < nbAcquisition; i++) {
			this.setVisibilitySeriesGraph(graphRetention, i, false);
		}
		
		return valueSetterRetention;
	}

}
