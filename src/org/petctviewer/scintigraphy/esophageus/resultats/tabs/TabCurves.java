package org.petctviewer.scintigraphy.esophageus.resultats.tabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.calibration.resultats.JTableCheckBox;
import org.petctviewer.scintigraphy.esophageus.application.Modele_EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.resultats.Modele_Resultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.shunpo.FenResults;
import org.petctviewer.scintigraphy.shunpo.TabResult;

public class TabCurves extends TabResult {

	private JFreeChart graphMain;
	private Modele_EsophagealTransit modeleApp;
	private Integer nbAcquisition;
	
	private XYSeries[][] datasetModele;
	private String[] titleRows;

	public TabCurves(int nbAcquisition, FenResults parent, Modele_EsophagealTransit modeleApp) {
		super(parent, "Curves");
		this.modeleApp = modeleApp;
		this.nbAcquisition = nbAcquisition;

		// set les data du graph
		datasetModele = ((Modele_Resultats_EsophagealTransit) this.parent.getModel()).getDataSetMain();
		titleRows = new String[datasetModele.length];

		this.createCaptureButton("Curves");
	}

	public void setVisibilitySeriesMain(int x, int y, boolean visibility) {
		XYItemRenderer renderer = this.graphMain.getXYPlot().getRenderer();
		// x+4 4: car on a 4 colonnes
		renderer.setSeriesVisible((x * 4) + y, visibility);
	}

	@Override
	public Component getSidePanelContent() {
		if(nbAcquisition == null || titleRows == null)
			return null;
		
		
		String[] titleCols = { "Full", "Upper", "Middle", "Lower" };

		// table de checkbox
		JTableCheckBox tableCheckbox = new JTableCheckBox(titleRows, titleCols, new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {

				TabCurves tab = TabCurves.this;
				JCheckBox selected = (JCheckBox) e.getSource();

				tab.setVisibilitySeriesMain(Integer.parseInt(selected.getName().split("\\|")[0]),
						Integer.parseInt(selected.getName().split("\\|")[1]), selected.isSelected());
			}
		});

		// active uniquement la premiere colonne
		tableCheckbox.setFirstColumn();

		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

		SidePanel sidePanelScin = new SidePanel(null, modeleApp.esoPlugIn.getExamType(), modeleApp.getImagesPlus()[0]);
		sidePanel.add(sidePanelScin);

		sidePanel.add(tableCheckbox);

		JPanel longeurEsophageResultPanel = new JPanel();
		longeurEsophageResultPanel.setLayout(new GridLayout(nbAcquisition + 1, 1));
		longeurEsophageResultPanel.add(new JLabel("Esophageal height"));
		double[] longueurEsophage = ((Modele_Resultats_EsophagealTransit) this.parent.getModel())
				.calculLongeurEsophage();
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
		if(datasetModele == null || titleRows == null)
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
		this.graphMain.getXYPlot().setBackgroundPaint(new Color(255, 255, 255));
		
		XYSeriesCollection dataset = new XYSeriesCollection();

		for (int i = 0; i < datasetModele.length; i++) {
			titleRows[i] = "Acqui " + (i + 1);
			for (int j = 0; j < datasetModele[i].length; j++) {
				dataset.addSeries(datasetModele[i][j]);
			}
		}
		this.graphMain.getXYPlot().setDataset(dataset);

		ChartPanel chartPanel = new ChartPanel(graphMain);
		return chartPanel;
	}

}
