package org.petctviewer.scintigraphy;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ij.Prefs;

public class RenalSettings extends JFrame implements ActionListener {

	private JButton btn_ok, btn_cancel;
	private JCheckBox ckb_bld, ckb_ctl, ckb_utr;

	public RenalSettings() {
		this.btn_ok = new JButton("Save");
		this.btn_ok.addActionListener(this);

		this.btn_cancel = new JButton("Cancel");
		this.btn_cancel.addActionListener(this);

		this.setLayout(new BorderLayout());

		Box box = Box.createVerticalBox();

		this.setTitle("Renal scintigraphy settings");
		JPanel pnl_titre = new JPanel();
		pnl_titre.add(new JLabel("Select the organs"));

		this.add(pnl_titre, BorderLayout.NORTH);

		JCheckBox ckb_kid = new JCheckBox("Kidneys");
		ckb_kid.setSelected(true);
		ckb_kid.setEnabled(false);
		box.add(ckb_kid);

		JCheckBox ckb_bp = new JCheckBox("Blood Pool");
		ckb_bp.setSelected(true);
		ckb_bp.setEnabled(false);
		box.add(ckb_bp);

		ckb_bld = new JCheckBox("Bladder");
		box.add(ckb_bld);

		ckb_ctl = new JCheckBox("Cortical");
		box.add(ckb_ctl);

		ckb_utr = new JCheckBox("Ureter");
		box.add(ckb_utr);

		JPanel pnl_btns = new JPanel();
		pnl_btns.add(this.btn_ok);
		pnl_btns.add(this.btn_cancel);

		JPanel pnl_box = new JPanel();
		pnl_box.add(box);

		this.add(pnl_box, BorderLayout.CENTER);
		this.add(pnl_btns, BorderLayout.SOUTH);

		this.enableCheckBoxes();

		this.setLocationRelativeTo(null);
		this.pack();
		this.setResizable(false);
		this.setVisible(true);
	}

	private void enableCheckBoxes() {
		boolean[] b = RenalSettings.getSettings();

		this.ckb_bld.setSelected(b[0]);
		this.ckb_ctl.setSelected(b[1]);
		this.ckb_utr.setSelected(b[2]);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton b = (JButton) e.getSource();

		if (b == RenalSettings.this.btn_ok) {
			String settings = "";
			settings += "Bladder: " + this.ckb_bld.isSelected();
			settings += " ";
			settings += "Cortical: " + this.ckb_ctl.isSelected();
			settings += " ";
			settings += "Ureter: " + this.ckb_utr.isSelected();

			Prefs.set("renal.preferred", settings);
			Prefs.savePreferences();
		}

		RenalSettings.this.dispose();
	}

	/**
	 * Renvoie un tableau de booleen correspondant aux organes selectionnes
	 * @return tab[0] : Bladder <br> tab[1] : Cortical <br> tab[2] : Ureter
	 */
	public static boolean[] getSettings() {
		String settingsString = Prefs.get("renal.preferred", "Bladder: true Cortical: true Ureter: true");

		boolean[] ret = new boolean[3];

		String[] splitted = settingsString.split(" ");
		int cpt = 0;
		for (int i = 1; i < splitted.length; i += 2) {
			ret[cpt] = splitted[i].equals(String.valueOf(true));
			cpt++;
		}

		return ret;
	}

}
