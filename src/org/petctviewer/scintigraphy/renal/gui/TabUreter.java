package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.shunpo.FenResults;

class TabUreter extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TabUreter(Scintigraphy scin, FenResults parent) {
		super(new BorderLayout());
		
//		SidePanel side = new SidePanel(null, "Renal scintigraphy", parent.getImagePlus());
//		side.addCaptureBtn(scin, "ureter", parent);
		parent.createCaptureButton("ureter");
		
		String[][] asso = new String[][] {{"L. Ureter" , "R. Ureter"}};
		List<XYSeries> series = ((Modele_Renal) parent.getModel()).getSeries();
		ChartPanel[] cPanels = Library_JFreeChart.associateSeries(asso, series);
		
		cPanels[0].getChart().setTitle("Ureters");
		
		this.add(cPanels[0], BorderLayout.CENTER);
	}
}
