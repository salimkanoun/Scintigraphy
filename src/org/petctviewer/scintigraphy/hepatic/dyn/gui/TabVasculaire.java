package org.petctviewer.scintigraphy.hepatic.dyn.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.hepatic.dyn.Modele_HepaticDyn;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

public class TabVasculaire extends FenResultatSidePanel {

	public TabVasculaire(VueScin vue, int width, int height) {
		super("Biliary Scintigraphy", vue, null, "");
		
		Modele_HepaticDyn modele = (Modele_HepaticDyn) this.getVue().getFenApplication().getControleur().getModele();
		List<XYSeries> series = modele.getSeries();
		
		ChartPanel chartVasculaire = ModeleScinDyn.associateSeries(new String[] {"Blood pool"}, series);
		
		this.add(chartVasculaire, BorderLayout.CENTER);
		
		this.finishBuildingWindow(true);
		this.setPreferredSize(new Dimension(width, height));
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

}