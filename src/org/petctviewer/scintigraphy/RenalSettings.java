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
import javax.swing.JTextField;

import org.petctviewer.scintigraphy.renal.JValueSetter;

import ij.Prefs;

public class RenalSettings extends JFrame implements ActionListener {

	private static String DEFAULT_SETTINGS = "Bladder: true Pelvis: true Ureter: true Lasilix: 19.0";
	
	private JButton btn_ok, btn_cancel;
	private JCheckBox ckb_bld, ckb_ctl, ckb_utr;
	private JTextField txt_lasilix;

	public RenalSettings() {
		this.checkSettingsFormat();
		
		this.btn_ok = new JButton("Save");
		this.btn_ok.addActionListener(this);

		this.btn_cancel = new JButton("Cancel");
		this.btn_cancel.addActionListener(this);

		this.setLayout(new BorderLayout());

		this.setTitle("Renal scintigraphy settings");
		JPanel pnl_titre = new JPanel();
		pnl_titre.add(new JLabel("Renal scintigraphy settings"));
		this.add(pnl_titre, BorderLayout.NORTH);


		//checkbox organs
		Box box = Box.createVerticalBox();
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
		ckb_ctl = new JCheckBox("Pelvis");
		box.add(ckb_ctl);
		ckb_utr = new JCheckBox("Ureter");
		box.add(ckb_utr);
		
		//panel lasilix
		JLabel lbl_lasilix = new JLabel("Lasilix time :");
		txt_lasilix = new JTextField("9999");
		JPanel pnl_lasilix = new JPanel();
		pnl_lasilix.add(lbl_lasilix);
		pnl_lasilix.add(txt_lasilix);
		pnl_lasilix.add(new JLabel("min"));

		JPanel pnl_btns = new JPanel();
		pnl_btns.add(this.btn_ok);
		pnl_btns.add(this.btn_cancel);

		JPanel pnl_center = new JPanel();
		pnl_center.add(box);
		pnl_center.add(pnl_lasilix);

		this.add(pnl_center, BorderLayout.CENTER);
		this.add(pnl_btns, BorderLayout.SOUTH);

		this.autoFillSettings();

		this.setLocationRelativeTo(null);
		this.pack();
		this.setResizable(false);
		this.setVisible(true);
	}

	private void checkSettingsFormat() {
		int n1 = Prefs.get("renal.preferred", DEFAULT_SETTINGS).split(" ").length;
		int n2 = DEFAULT_SETTINGS.split(" ").length;
		if(n1 != n2) {
			Prefs.set("renal.preferred", DEFAULT_SETTINGS);
			Prefs.savePreferences();
		}
		
	}

	private void autoFillSettings() {
		boolean[] b = RenalSettings.getOrganSettings();

		this.ckb_bld.setSelected(b[0]);
		this.ckb_ctl.setSelected(b[1]);
		this.ckb_utr.setSelected(b[2]);
		
		this.txt_lasilix.setText("" + RenalSettings.getLasilixTime());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton b = (JButton) e.getSource();

		if (b == RenalSettings.this.btn_ok) {
			String settings = "";
			settings += "Bladder: " + this.ckb_bld.isSelected();
			settings += " ";
			settings += "Pelvis: " + this.ckb_ctl.isSelected();
			settings += " ";
			settings += "Ureter: " + this.ckb_utr.isSelected();
			settings += " ";
			settings += "Lasilix: " + Double.parseDouble(this.txt_lasilix.getText());

			Prefs.set("renal.preferred", settings);
			Prefs.savePreferences();
		}

		RenalSettings.this.dispose();
	}

	/**
	 * Renvoie un tableau de booleen correspondant aux organes selectionnes
	 * @return tab[0] : Bladder <br> tab[1] : Pelvis <br> tab[2] : Ureter
	 */
	public static boolean[] getOrganSettings() {
		String settingsString = Prefs.get("renal.preferred", DEFAULT_SETTINGS);

		boolean[] ret = new boolean[3];

		String[] splitted = settingsString.split(" ");
		int cpt = 0;
		for (int i = 1; i < 6; i += 2) {
			ret[cpt] = splitted[i].equals(String.valueOf(true));
			cpt++;
		}

		return ret;
	}
	
	public static Double getLasilixTime() {
		String settingsString = Prefs.get("renal.preferred", DEFAULT_SETTINGS);
		String[] splitted = settingsString.split(" ");
		return Double.valueOf(splitted[7]);
	}

}
