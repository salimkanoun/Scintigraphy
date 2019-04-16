package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.shunpo.FenResults;

import ij.Prefs;

class TabOther extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TabOther(Scintigraphy scin, FenResults parent) {
		super(new BorderLayout());
//		SidePanel side = new SidePanel(null, "Renal scintigraphy", parent.getImagePlus());
//		side.addCaptureBtn(scin, "_other", parent);
		parent.createCaptureButton("_other");
		
		String[][] asso = new String[][] {{"Blood Pool"} , {"Bladder"}};
		List<XYSeries> series = ((Modele_Renal) parent.getModel()).getSeries();
		ChartPanel[] cPanels = Library_JFreeChart.associateSeries(asso, series);
		
		JPanel center = new JPanel(new GridLayout(1,1));
		cPanels[0].getChart().getXYPlot().getRenderer().setSeriesPaint(0, Color.GREEN);
		center.add(cPanels[0]);
		
		//si la vessie est activee
		if(Prefs.get("renal.bladder.preferred", true)) {
			center.setLayout(new GridLayout(2,1));
			cPanels[1].getChart().getXYPlot().getRenderer().setSeriesPaint(0, Color.PINK);
			center.add(cPanels[1]);
		}
		
		this.add(center, BorderLayout.CENTER);
	}

}
