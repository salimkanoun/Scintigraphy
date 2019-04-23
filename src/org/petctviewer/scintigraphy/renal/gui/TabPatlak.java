package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

class TabPatlak extends TabResult {

	public TabPatlak(RenalScintigraphy scin, FenResults parent) {
		super(parent, "Patlak", true);
	}

	@Override
	public Component getSidePanelContent() {
		JPanel pnl_sep = new JPanel(new GridLayout(2, 3));
		pnl_sep.add(new JLabel("Relative function"));
		
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		pnl_sep.add(lbl_L);
		pnl_sep.add(lbl_R);
		
		double[] patlak = ((Modele_Renal)this.getParent().getModel()).getPatlakPente();
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

	@Override
	public JPanel getResultContent() {
		return ((Modele_Renal)this.parent.getModel()).getPatlakChart();
	}

}
