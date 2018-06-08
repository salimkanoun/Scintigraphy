package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.renal.Vue_Renal;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

class TabPatlak extends FenResultatSidePanel {

	private JValueSetter patlak;

	public TabPatlak(Vue_Renal vue, int w, int h) {
		super("Renal scintigraphy", vue, null, "patlak");

		this.pack();
		this.add(vue.getPatlakChart(), BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(w, h));
		this.finishBuildingWindow(true);
	}

	@Override
	public Component getSidePanelContent() {
		Modele_Renal modele = (Modele_Renal) this.getVue().getFenApplication().getControleur().getModele();

		JPanel pnl_sep = new JPanel(new GridLayout(2, 3));
		pnl_sep.add(new JLabel("Relative function"));
		
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		pnl_sep.add(lbl_L);
		pnl_sep.add(lbl_R);
		
		double[] patlak = modele.getPatlakPente();
		pnl_sep.add(new JLabel("patlak"));
		JLabel lbl_pd = new JLabel(patlak[0] + " %");
		lbl_pd.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_sep.add(lbl_pd);

		JLabel lbl_pg = new JLabel(patlak[1] + " %");
		lbl_pg.setHorizontalAlignment(SwingConstants.CENTER);
		pnl_sep.add(lbl_pg);
		
		pnl_sep.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		JPanel flow = new JPanel();
		flow.add(pnl_sep);
		
		return flow;
	}

}
