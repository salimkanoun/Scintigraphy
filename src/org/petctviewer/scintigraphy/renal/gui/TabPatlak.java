package org.petctviewer.scintigraphy.renal.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

class TabPatlak extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TabPatlak(RenalScintigraphy scin) {
		super(new BorderLayout());
		
		Modele_Renal modele = (Modele_Renal) scin.getModele();
		SidePanel side = new SidePanel(this.getSidePanelContent(modele), "Renal Scintigraphy", scin.getImp());
		side.addCaptureBtn(scin, "_patlak");

		this.add(scin.getPatlakChart(), BorderLayout.CENTER);
		this.add(side, BorderLayout.EAST);
	}

	public Component getSidePanelContent(Modele_Renal modele) {
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
