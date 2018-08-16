package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

import ij.Prefs;

class TabOther extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TabOther(Scintigraphy scin) {
		super(new BorderLayout());
		SidePanel side = new SidePanel(null, "Renal scintigraphy", scin.getImp());
		side.addCaptureBtn(scin, "_other");
		
		String[][] asso = new String[][] {{"Blood Pool"} , {"Bladder"}};
		List<XYSeries> series = ((Modele_Renal) scin.getFenApplication().getControleur().getModele()).getSeries();
		ChartPanel[] cPanels = ModeleScinDyn.associateSeries(asso, series);
		
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
		this.add(side, BorderLayout.EAST);
	}

}
