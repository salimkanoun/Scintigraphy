package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

class TabUreter extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TabUreter(Scintigraphy scin, ModeleScin model) {
		super(new BorderLayout());
		
		SidePanel side = new SidePanel(null, "Renal scintigraphy", model.getImagePlus());
		side.addCaptureBtn(scin, "ureter", model);
		
		String[][] asso = new String[][] {{"L. Ureter" , "R. Ureter"}};
		List<XYSeries> series = ((Modele_Renal) model).getSeries();
		ChartPanel[] cPanels = Library_JFreeChart.associateSeries(asso, series);
		
		cPanels[0].getChart().setTitle("Ureters");
		
		this.add(side, BorderLayout.EAST);
		this.add(cPanels[0], BorderLayout.CENTER);
	}
}
