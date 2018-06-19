package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

class TabCort extends FenResultatSidePanel {

	private static final long serialVersionUID = -2324369375150642778L;

	public TabCort(Scintigraphy scin, int w, int h) {
		super("Renal scintigraphy", scin, null, "cort");
		
		ModeleScinDyn modele = (ModeleScinDyn) scin.getFenApplication().getControleur().getModele();
		
		List<XYSeries> listSeries = modele.getSeries();
		// recuperation des chart panel avec association
		String[][] asso = { { "L. Cortical", "L. Pelvis" }, {"R. Cortical" , "R. Pelvis" } };
		ChartPanel[] cPanels = ModeleScinDyn.associateSeries(asso, listSeries);
		
		cPanels[0].getChart().setTitle("Left kidney");
		cPanels[1].getChart().setTitle("Right kidney");
		//on change la couleur des courbes
		cPanels[0].getChart().getXYPlot().getRenderer().setSeriesPaint(0, Color.RED);
		cPanels[0].getChart().getXYPlot().getRenderer().setSeriesPaint(1, Color.RED);
		cPanels[1].getChart().getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE);
		cPanels[1].getChart().getXYPlot().getRenderer().setSeriesPaint(1, Color.BLUE);
		
		JPanel grid = new JPanel(new GridLayout(2,1));
		cPanels[0].setPreferredSize(new Dimension(w, h/2));
		grid.add(cPanels[0]);
		
		cPanels[1].setPreferredSize(new Dimension(w, h/2));
		grid.add(cPanels[1]);

		this.add(new JPanel(), BorderLayout.WEST);
		this.add(grid, BorderLayout.CENTER);
		
		this.setPreferredSize(new Dimension(w, h));
		
		this.finishBuildingWindow(true);
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

}
