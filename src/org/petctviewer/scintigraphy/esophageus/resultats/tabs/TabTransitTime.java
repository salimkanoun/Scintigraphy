package org.petctviewer.scintigraphy.esophageus.resultats.tabs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.esophageus.resultats.Model_Resultats_EsophagealTransit;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import javax.swing.*;
import java.awt.*;

public class TabTransitTime extends TabResult {

	private JFreeChart graphTransitTime;

	private JValueSetter valueSetterTransit;

	private Selector startSelector, endSelector;

	private double[][] selectorsTransitValue;

	private JLabel[] labelsMesureTempsSelectorTransit;

	private JRadioButton[] radioButtonTransitTime;

	private static int numSeriesSelectors = 0;

	private final Integer nbAcquisition;

	private final Model_Resultats_EsophagealTransit modeleApp;

	public TabTransitTime(int nbAcquisition, FenResults parent, Model_Resultats_EsophagealTransit model) {
		super(parent, "Transit Time", true);
		this.modeleApp = model;
		this.nbAcquisition = nbAcquisition;

//		this.createCaptureButton("TransitTimes");
		this.setAdditionalInfo("TransitTimes");

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
		// Panel de selection des acquisitions (side panel)
		JPanel selectionAcquiTransitPanel = new JPanel();
		selectionAcquiTransitPanel.setLayout(new GridLayout(nbAcquisition, 1));

		ButtonGroup buttonGroupTransit = new ButtonGroup();
		labelsMesureTempsSelectorTransit = new JLabel[nbAcquisition];
		radioButtonTransitTime = new JRadioButton[nbAcquisition];

		for (int i = 0; i < nbAcquisition; i++) {
			// un selecteur pour tous les acqui
			JPanel un = new JPanel();
			un.setLayout(new FlowLayout());

			radioButtonTransitTime[i] = new JRadioButton("Acquisition " + (i + 1));
			radioButtonTransitTime[i].addItemListener(e -> {
				TabTransitTime tab = TabTransitTime.this;

				for (int i1 = 0; i1 < tab.radioButtonTransitTime.length; i1++) {
					if (e.getSource().equals(radioButtonTransitTime[i1])) {
						tab.setVisibilitySeriesGraph(tab.graphTransitTime, i1, true);
						numSeriesSelectors = i1;
						tab.startSelector.setXValue(selectorsTransitValue[i1][0]);
						tab.endSelector.setXValue(selectorsTransitValue[i1][1]);
						tab.valueSetterTransit.updateAreas();

					} else {
						tab.setVisibilitySeriesGraph(tab.graphTransitTime, i1, false);
					}
				}
			});
			buttonGroupTransit.add(radioButtonTransitTime[i]);
			un.add(radioButtonTransitTime[i]);

			labelsMesureTempsSelectorTransit[i] = new JLabel("measure = ?");

			selectionAcquiTransitPanel.add(un);
		}

		// pour quil soit regroupe
		JPanel selectionAcquiTransitPanelFlow = new JPanel(new FlowLayout());
		selectionAcquiTransitPanelFlow.add(selectionAcquiTransitPanel);

		Box sidePanel = Box.createVerticalBox();
		SidePanel sidePanelScin = new SidePanel(null, modeleApp.esoPlugIn.getStudyName(), modeleApp.getImagePlus());
		sidePanel.add(sidePanelScin);

		sidePanel.add(selectionAcquiTransitPanelFlow);

		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new GridLayout(nbAcquisition + 1, 1));

		resultPanel.add(new JLabel("Time Measure :"));
		for (int i = 0; i < selectorsTransitValue.length; i++) {
			double delta = Math.abs(selectorsTransitValue[i][0] - selectorsTransitValue[i][1]);
			labelsMesureTempsSelectorTransit[i] = new JLabel("Acquisition " + (i + 1) + " : " + delta + " sec");
			resultPanel.add(labelsMesureTempsSelectorTransit[i]);
		}

		JPanel resultPanelFlow = new JPanel();
		resultPanelFlow.setLayout(new FlowLayout());
		resultPanelFlow.add(resultPanel);

		sidePanel.add(resultPanelFlow);

		radioButtonTransitTime[0].setSelected(true);

		return sidePanel;
	}

	@Override
	public JPanel getResultContent() {
		if (nbAcquisition == null)
			return null;

		// graph
		graphTransitTime = ChartFactory.createXYLineChart("Transit Time", "s", "Count/s", null);
		// Changes background color et grid color
		graphTransitTime.getXYPlot().setRangeGridlinePaint(Color.black);
		graphTransitTime.getXYPlot().setDomainGridlinePaint(Color.black);

		XYSeries[][] datasetModele = modeleApp.getDataSetTransitTime();
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (XYSeries[] xySeries : datasetModele) {
			for (XYSeries aXYSeries : xySeries) {
				dataset.addSeries(aXYSeries);
			}
		}
		// Changes background color
//		XYPlot plott = (XYPlot) graphTransitTime.getPlot();

		graphTransitTime.getXYPlot().setDataset(dataset);

		// Hide every curves exept the first one
		for (int i = 1; i < nbAcquisition; i++) {
			this.setVisibilitySeriesGraph(graphTransitTime, i, false);
		}

		// graph avec les selecteur
		valueSetterTransit = new JValueSetter(graphTransitTime);
		valueSetterTransit.addChartMouseListener(new ChartMouseListener() {

			/*
			 * actualise la valeur a chaque mouvement de la souris sur le graphe meme si on
			 * n'a pas selectionné un selecteur(non-Javadoc) consomme trop de ressource
			 */
			@Override
			public void chartMouseMoved(ChartMouseEvent event) {

			}

			/*
			 * au clic sur un selecteur(non-Javadoc)
			 */
			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				TabTransitTime tab = TabTransitTime.this;
				tab.selectorsTransitValue[numSeriesSelectors][0] = tab.startSelector.getXValue();
				tab.selectorsTransitValue[numSeriesSelectors][1] = tab.endSelector.getXValue();

				double delta = Math.abs(tab.startSelector.getXValue() - tab.endSelector.getXValue());
				tab.labelsMesureTempsSelectorTransit[numSeriesSelectors].setText(
						"Acquisition " + (numSeriesSelectors + 1) + " : " + Library_Quantif.round(delta, 2) + " sec");

				// on l'envoi au modele pour le csv
				modeleApp.setTimeMeasure(numSeriesSelectors, delta);
			}
		});

		startSelector = new Selector("start", 0, -1, RectangleAnchor.TOP_LEFT);
		valueSetterTransit.addSelector(startSelector, "start");
		endSelector = new Selector("end", 1, -1, RectangleAnchor.TOP_LEFT);
		valueSetterTransit.addSelector(endSelector, "end");
		valueSetterTransit.addArea("start", "end", "area", null);

		// liste contenant les couples de valeurs des selecteurs
		selectorsTransitValue = new double[nbAcquisition][2];
		for (int i = 0; i < selectorsTransitValue.length; i++) {
			// couple des valeurs d'un slecteur
			selectorsTransitValue[i][0] = 0;
			selectorsTransitValue[i][1] = 2;
		}

		return valueSetterTransit;
	}

}
