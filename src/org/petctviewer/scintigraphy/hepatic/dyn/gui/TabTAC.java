package org.petctviewer.scintigraphy.hepatic.dyn.gui;

import java.awt.BorderLayout;
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
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

class TabTAC extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TabTAC(Scintigraphy scin, int width, int height) {
		this.setLayout(new BorderLayout());
		SidePanel side = new SidePanel(null, "Biliary scintigraphy", scin.getImp());
		side.addCaptureBtn(scin, "");
		
		Modele_HepaticDyn modele = (Modele_HepaticDyn) scin.getFenApplication().getControleur().getModele();
		
		JPanel pnl_center = new JPanel(new GridLayout(2,2));
		
		List<XYSeries> series = modele.getSeries();
		ChartPanel chartDuodenom = ModeleScinDyn.associateSeries(new String[] {"Duodenom"}, series);
		JValueSetter setterDuodenom = new JValueSetter(chartDuodenom.getChart());
		setterDuodenom.addSelector(new Selector("start", 10, -1, RectangleAnchor.BOTTOM_RIGHT), "start");
		pnl_center.add(setterDuodenom);
		
		ChartPanel chartCBD = ModeleScinDyn.associateSeries(new String[] {"CBD"}, series);
		pnl_center.add(chartCBD);

		ChartPanel chartHilium = ModeleScinDyn.associateSeries(new String[] {"Hilium"}, series);
		JValueSetter setterHilium = new JValueSetter(chartHilium.getChart());
		setterHilium.addSelector(new Selector("start", 10, -1, RectangleAnchor.BOTTOM_RIGHT), "start");
		pnl_center.add(setterHilium);
		
		this.add(pnl_center, BorderLayout.CENTER);
		this.add(side, BorderLayout.EAST);
		
		this.setPreferredSize(new Dimension(width, height));
		this.setMinimumSize(side.getSize());
	}

}
