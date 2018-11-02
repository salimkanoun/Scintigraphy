package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

class TabUreter extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TabUreter(Scintigraphy scin) {
		super(new BorderLayout());
		
		SidePanel side = new SidePanel(null, "Renal scintigraphy", scin.getImp());
		side.addCaptureBtn(scin, "ureter");
		
		String[][] asso = new String[][] {{"L. Ureter" , "R. Ureter"}};
		List<XYSeries> series = ((Modele_Renal) scin.getModele()).getSeries();
		ChartPanel[] cPanels = ModeleScinDyn.associateSeries(asso, series);
		
		cPanels[0].getChart().setTitle("Ureters");
		
		this.add(side, BorderLayout.EAST);
		this.add(cPanels[0], BorderLayout.CENTER);
	}
}
