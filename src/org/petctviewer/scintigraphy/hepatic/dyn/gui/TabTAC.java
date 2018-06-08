package org.petctviewer.scintigraphy.hepatic.dyn.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.hepatic.dyn.Modele_HepaticDyn;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

class TabTAC extends FenResultatSidePanel {

	public TabTAC(VueScin vue, int width, int height) {
		super("Biliary scintigraphy", vue, null, "");
		Modele_HepaticDyn modele = (Modele_HepaticDyn) this.getVue().getFenApplication().getControleur().getModele();
		
		JPanel pnl_center = new JPanel(new GridLayout(2,2));
		
		List<XYSeries> series = modele.getSeries();
		ChartPanel chartDuodenom = ModeleScinDyn.associateSeries(new String[] {"Duodenom"}, series);
		JValueSetter setterDuodenom = new JValueSetter(chartDuodenom.getChart());
		setterDuodenom.addSelector(new Selector("start", 10, -1, RectangleAnchor.BOTTOM_RIGHT), "start");
		pnl_center.add(setterDuodenom);
		
		ChartPanel chartHilium = ModeleScinDyn.associateSeries(new String[] {"Hilium"}, series);
		JValueSetter setterHilium = new JValueSetter(chartHilium.getChart());
		setterHilium.addSelector(new Selector("start", 10, -1, RectangleAnchor.BOTTOM_RIGHT), "start");
		pnl_center.add(setterHilium);
		
		ChartPanel chartCBD = ModeleScinDyn.associateSeries(new String[] {"CBD"}, series);
		pnl_center.add(chartCBD);
		
		this.add(pnl_center, BorderLayout.CENTER);
		
		this.finishBuildingWindow(true);
		this.setPreferredSize(new Dimension(width, height));
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

}
