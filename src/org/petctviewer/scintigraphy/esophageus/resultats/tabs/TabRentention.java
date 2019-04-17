package org.petctviewer.scintigraphy.esophageus.resultats.tabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.petctviewer.scintigraphy.esophageus.application.Modele_EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.resultats.Modele_Resultats_EsophagealTransit;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.shunpo.FenResults;
import org.petctviewer.scintigraphy.shunpo.TabResult;

public class TabRentention extends TabResult {

	private JFreeChart graphRetention;

	private Selector selectorRentention;
	private double selectorRetentionValue[];

	private JRadioButton[] radioButtonRetention;

	private JLabel[] retention10sLabel;
	private static int numAcquisitionRetention = 0;

	private Modele_EsophagealTransit modeleApp;

	private Integer nbAcquisition;

	public TabRentention(int nbAcquisition, FenResults parent, Modele_EsophagealTransit modeleApp) {
		super(parent, "Retention");
		this.modeleApp = modeleApp;
		this.nbAcquisition = nbAcquisition;

		this.createCaptureButton("Retention");

	}

	private void setVisibilitySeriesGraph(JFreeChart graph, int numSerie, boolean visibility) {
		XYItemRenderer renderer = graph.getXYPlot().getRenderer();
		renderer.setSeriesVisible(numSerie, visibility);
		renderer.setSeriesPaint(numSerie, Color.red);

	}

	@Override
	public Component getSidePanelContent() {
		if (nbAcquisition == null)
			return null;

		graphRetention.getXYPlot()
				.setDataset(((Modele_Resultats_EsophagealTransit) this.parent.getModel()).retentionForGraph());

		JPanel radioButtonRetentionPanel = new JPanel();
		radioButtonRetentionPanel.setLayout(new GridLayout(nbAcquisition, 1));

		ButtonGroup buttonGroupRetention = new ButtonGroup();
		radioButtonRetention = new JRadioButton[nbAcquisition];
		for (int i = 0; i < nbAcquisition; i++) {
			radioButtonRetention[i] = new JRadioButton("Acquisition " + (i + 1));
			radioButtonRetention[i].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					TabRentention tab = TabRentention.this;
					for (int i = 0; i < tab.radioButtonRetention.length; i++) {
						if (((JRadioButton) e.getSource()).equals(radioButtonRetention[i])) {
							tab.setVisibilitySeriesGraph(tab.graphRetention, i, true);
							numAcquisitionRetention = i;
							tab.selectorRentention.setXValue(selectorRetentionValue[i]);

						} else {
							tab.setVisibilitySeriesGraph(tab.graphRetention, i, false);
						}
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
		SidePanel sidePanelScin = new SidePanel(null, modeleApp.esoPlugIn.getExamType(), modeleApp.getImagePlus());
		sidePanel.add(sidePanelScin);

		sidePanel.add(radioButtonRetentionPanelFlow);

		JPanel retentionResultPanel = new JPanel();
		retentionResultPanel.setLayout(new GridLayout(nbAcquisition + 1, 1));

		retentionResultPanel.add(new JLabel("Decrease 10s after peak"));
		double[] retention10s = ((Modele_Resultats_EsophagealTransit) this.parent.getModel()).retentionAllPoucentage();
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
		XYLineAndShapeRenderer rendererTransit = new XYLineAndShapeRenderer();
		// monter les formes des points
		rendererTransit.setSeriesShapesVisible(0, true);

		// pour avoir les infos des points quand on passe la souris dessus
		rendererTransit.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		graphRetention.getXYPlot().setRenderer(rendererTransit);

		this.graphRetention.getXYPlot().setBackgroundPaint(new Color(255, 255, 255));

		// grille en noir
		this.graphRetention.getXYPlot().setRangeGridlinePaint(Color.black);
		this.graphRetention.getXYPlot().setDomainGridlinePaint(Color.black);

		selectorRetentionValue = ((Modele_Resultats_EsophagealTransit) this.parent.getModel()).retentionAllX();

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

				double retention = ((Modele_Resultats_EsophagealTransit) tab.getParent().getModel())
						.retentionPoucentage(tab.selectorRentention.getXValue(), numAcquisitionRetention);
				retention10sLabel[numAcquisitionRetention]
						.setText("Acquisition " + (numAcquisitionRetention + 1) + " : " + retention + "%");

				// on l'envoi au modele pour le csv
				((Modele_Resultats_EsophagealTransit) tab.getParent().getModel())
						.setRetentionDecrease(numAcquisitionRetention, retention);
			}
		});

		selectorRentention = new Selector("max", 1, -1, RectangleAnchor.TOP_RIGHT);
		valueSetterRetention.addSelector(selectorRentention, "max");
		return valueSetterRetention;
	}

}
