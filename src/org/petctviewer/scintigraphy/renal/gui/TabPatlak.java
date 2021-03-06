package org.petctviewer.scintigraphy.renal.gui;

import org.petctviewer.scintigraphy.renal.Model_Renal;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import javax.swing.*;
import java.awt.*;

class TabPatlak extends TabResult {

	public TabPatlak(FenResults parent) {
		super(parent, "Patlak", true);

		this.reloadDisplay();
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

		double[] patlak = ((Model_Renal) this.getParent().getModel()).getPatlakPente();
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
		return ((Model_Renal) this.parent.getModel()).getPatlakChart();
	}

}
