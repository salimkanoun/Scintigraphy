package org.petctviewer.scintigraphy.esophageus.resultats.tabs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.calibration.resultats.JTableCheckBox;
import org.petctviewer.scintigraphy.esophageus.resultats.Model_Resultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import javax.swing.*;
import java.awt.*;

public class TabCurves extends TabResult {

	private JFreeChart graphMain;
	private final Model_Resultats_EsophagealTransit modeleApp;
	private final Integer nbAcquisition;

	private final XYSeries[][] datasetModele;
	private final String[] titleRows;

	public TabCurves(int nbAcquisition, FenResults parent, Model_Resultats_EsophagealTransit modeleApp) {
		super(parent, "Curves");
		this.modeleApp = modeleApp;
		this.nbAcquisition = nbAcquisition;

		// set les data du graph
		datasetModele = modeleApp.getDataSetMain();
		titleRows = new String[datasetModele.length];

//		this.createCaptureButton("Curves");
		this.setadditionalInfo("Curves");

		this.reloadDisplay();
	}

	public void setVisibilitySeriesMain(int x, int y, boolean visibility) {
		XYItemRenderer renderer = this.graphMain.getXYPlot().getRenderer();
		// x+4 4: car on a 4 colonnes
		renderer.setSeriesVisible((x * 4) + y, visibility);
	}

	@Override
	public Component getSidePanelContent() {

		this.getResultContent();

		if (nbAcquisition == null || titleRows == null)
			return null;

		String[] titleCols = { "Full", "Upper", "Middle", "Lower" };

		// table de checkbox
		JTableCheckBox tableCheckbox = new JTableCheckBox(titleRows, titleCols, e -> {

			TabCurves tab = TabCurves.this;
			JCheckBox selected = (JCheckBox) e.getSource();

			tab.setVisibilitySeriesMain(Integer.parseInt(selected.getName().split("\\|")[0]),
					Integer.parseInt(selected.getName().split("\\|")[1]), selected.isSelected());
		});

		// active uniquement la premiere colonne
		tableCheckbox.setFirstColumn();

		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

		@SuppressWarnings("deprecation")
		SidePanel sidePanelScin = new SidePanel(null, modeleApp.esoPlugIn.getStudyName(), modeleApp.getImagesPlus()[0]);
		sidePanel.add(sidePanelScin);

		sidePanel.add(tableCheckbox);

		JPanel longeurEsophageResultPanel = new JPanel();
		longeurEsophageResultPanel.setLayout(new GridLayout(nbAcquisition + 1, 1));
		longeurEsophageResultPanel.add(new JLabel("Esophageal height"));
		double[] longueurEsophage = modeleApp.calculLongeurEsophage();
		for (int i = 0; i < longueurEsophage.length; i++) {
			longeurEsophageResultPanel.add(new JLabel(
					"Acquisition " + (i + 1) + " : " + (Library_Quantif.round(longueurEsophage[i], 2)) + " cm"));
		}

		JPanel longueurEsophageResultPanelFlow = new JPanel();
		longueurEsophageResultPanelFlow.setLayout(new FlowLayout());
		longueurEsophageResultPanelFlow.add(longeurEsophageResultPanel);

		sidePanel.add(longueurEsophageResultPanelFlow);
		return sidePanel;
	}

	@Override
	public JPanel getResultContent() {
		if (datasetModele == null || titleRows == null)
			return null;

		// graph
		graphMain = ChartFactory.createXYLineChart("Esophageal Transit", "s", "Count/s", null);

		XYLineAndShapeRenderer rendererMain = new XYLineAndShapeRenderer();
		rendererMain.setDefaultShapesVisible(true);

		// met la grille en noir
		graphMain.getXYPlot().setDomainGridlinePaint(Color.black);
		graphMain.getXYPlot().setRangeGridlinePaint(Color.black);

		// pour avoir les infos des points quand on passe la souris dessus
		rendererMain.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());

		graphMain.getXYPlot().setRenderer(rendererMain);

		// Changes background color et grid color

		XYSeriesCollection dataset = new XYSeriesCollection();

		for (int i = 0; i < datasetModele.length; i++) {
			titleRows[i] = "Acqui " + (i + 1);
			for (int j = 0; j < datasetModele[i].length; j++) {
				dataset.addSeries(datasetModele[i][j]);
			}
		}
		this.graphMain.getXYPlot().setDataset(dataset);

		return new ChartPanel(graphMain);
	}

}
