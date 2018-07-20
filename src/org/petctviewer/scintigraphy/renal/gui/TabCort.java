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
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

class TabCort extends JPanel {

	private static final long serialVersionUID = -2324369375150642778L;

	public TabCort(Scintigraphy scin) {
		super(new BorderLayout());
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
		grid.add(cPanels[0]);
		
		grid.add(cPanels[1]);

		SidePanel side = new SidePanel(null, "Renal scintigraphy", scin.getImp());
		
		this.add(side, BorderLayout.EAST);
		this.add(grid, BorderLayout.CENTER);
	}

}
