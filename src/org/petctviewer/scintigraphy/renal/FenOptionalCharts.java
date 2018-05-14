package org.petctviewer.scintigraphy.renal;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

public class FenOptionalCharts extends FenResultatSidePanel {

	private static final long serialVersionUID = -2647720655737610538L;

	public FenOptionalCharts(VueScin vue) {
		super("Optional graphs", vue, null, "");
		JPanel grid = new JPanel(new GridLayout(2, 2));

		// on recupere le modele et les series
		Modele_Renal modele = (Modele_Renal) vue.getFen_application().getControleur().getModele();
		List<XYSeries> listSeries = modele.getSeries();

		// recuperation des chart panel avec association
		String[][] asso = { { "Bladder" }, { "Blood Pool" } };

		ChartPanel[] cPanels = ModeleScin.associateSeries(asso, listSeries);
		
		for(ChartPanel c : cPanels) {
			grid.add(c);
			c.setPreferredSize(new Dimension(300, 300));
		}

		this.add(grid, BorderLayout.WEST);

		this.finishBuildingWindow();
	}

	@Override
	public Component[] getSidePanelContent() {
		return null;
	}

}
