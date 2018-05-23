package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;

public class TabUreter extends FenResultatSidePanel {

	public TabUreter(VueScin vue) {
		super("Renal Scintigraphy", vue, null, "");
		String[][] asso = new String[][] {{"L. Ureter" , "R. Ureter"}};
		List<XYSeries> series = ((Modele_Renal) vue.getFen_application().getControleur().getModele()).getSeries();
		ChartPanel[] cPanels = ModeleScin.associateSeries(asso, series);
		
		cPanels[0].getChart().setTitle("Ureters");
		this.add(cPanels[0], BorderLayout.CENTER);
		
		this.add(new JPanel(), BorderLayout.WEST);
		
		finishBuildingWindow(true);
	}

	@Override
	public Component[] getSidePanelContent() {
		// TODO Auto-generated method stub
		return null;
	}

}
