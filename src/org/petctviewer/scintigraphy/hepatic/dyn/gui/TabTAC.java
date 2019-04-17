package org.petctviewer.scintigraphy.hepatic.dyn.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.hepatic.dyn.Modele_HepaticDyn;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.shunpo.FenResults;
import org.petctviewer.scintigraphy.shunpo.TabResult;

class TabTAC extends TabResult {

	public TabTAC(Scintigraphy scin, int width, int height, FenResults parent) {
		super(parent, "TAC", true);

		this.getPanel().setPreferredSize(new Dimension(width, height));
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		Modele_HepaticDyn modele = (Modele_HepaticDyn) this.parent.getModel();

		JPanel pnl_center = new JPanel(new GridLayout(2, 2));

		List<XYSeries> series = modele.getSeries();
		ChartPanel chartDuodenom = Library_JFreeChart.associateSeries(new String[] { "Duodenom" }, series);
		JValueSetter setterDuodenom = new JValueSetter(chartDuodenom.getChart());
		setterDuodenom.addSelector(new Selector("start", 10, -1, RectangleAnchor.BOTTOM_RIGHT), "start");
		pnl_center.add(setterDuodenom);

		ChartPanel chartCBD = Library_JFreeChart.associateSeries(new String[] { "CBD" }, series);
		pnl_center.add(chartCBD);

		ChartPanel chartHilium = Library_JFreeChart.associateSeries(new String[] { "Hilium" }, series);
		JValueSetter setterHilium = new JValueSetter(chartHilium.getChart());
		setterHilium.addSelector(new Selector("start", 10, -1, RectangleAnchor.BOTTOM_RIGHT), "start");
		pnl_center.add(setterHilium);

		return pnl_center;
	}

}
