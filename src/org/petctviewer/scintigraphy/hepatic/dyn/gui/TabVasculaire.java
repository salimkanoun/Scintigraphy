package org.petctviewer.scintigraphy.hepatic.dyn.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.hepatic.dyn.Modele_HepaticDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

public class TabVasculaire extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TabVasculaire(Scintigraphy scin, int width, int height) {
		super(new BorderLayout());
		
		Modele_HepaticDyn modele = (Modele_HepaticDyn) scin.getModele();
		List<XYSeries> series = modele.getSeries();
		ChartPanel chartVasculaire = Library_JFreeChart.associateSeries(new String[] {"Blood pool"}, series);
		
		SidePanel border = new SidePanel(null, "Renal Scintigraphy", scin.getImp());
		border.addCaptureBtn(scin, "Blood Pool");
		
		this.add(chartVasculaire, BorderLayout.CENTER);
		
		this.setPreferredSize(new Dimension(width, height));
	}

}