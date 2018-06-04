package org.petctviewer.scintigraphy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.renal.JValueSetter;

import ij.Prefs;

public class RenalSettings extends JFrame implements ActionListener {

	private static String DEFAULT_SETTINGS = "Bladder:true\nPelvis:true\nUreter:true\nLasilix:19.0\nDateFormat:MMM dd yyyy";
	
	private JButton btn_ok, btn_cancel;
	private JCheckBox ckb_bld, ckb_ctl, ckb_utr;
	private JTextField txt_lasilix;
	private JComboBox comboDate;

	public RenalSettings(Component parentComponent) {
		this.checkSettingsFormat();
		
		this.btn_ok = new JButton("Save");
		this.btn_ok.addActionListener(this);

		this.btn_cancel = new JButton("Cancel");
		this.btn_cancel.addActionListener(this);

		this.setLayout(new BorderLayout());

		this.setTitle("Renal scintigraphy settings");
		JPanel pnl_titre = new JPanel();
		pnl_titre.add(new JLabel("<html><h3>Renal scintigraphy settings</h3></html>"));
		this.add(pnl_titre, BorderLayout.NORTH);


		//checkbox organs
		Box boxLeft = Box.createVerticalBox();
		boxLeft.add(new JLabel("Organs to delimit :"));
		JCheckBox ckb_kid = new JCheckBox("Kidneys");
		ckb_kid.setSelected(true);
		ckb_kid.setEnabled(false);
		boxLeft.add(ckb_kid);
		JCheckBox ckb_bp = new JCheckBox("Blood Pool");
		ckb_bp.setSelected(true);
		ckb_bp.setEnabled(false);
		boxLeft.add(ckb_bp);
		ckb_bld = new JCheckBox("Bladder");
		boxLeft.add(ckb_bld);
		ckb_ctl = new JCheckBox("Pelvis");
		boxLeft.add(ckb_ctl);
		ckb_utr = new JCheckBox("Ureter");
		boxLeft.add(ckb_utr);
		
		//panel lasilix
		Box boxRight = Box.createVerticalBox();
		JLabel lbl_lasilix = new JLabel("Lasilix injection time :");
		txt_lasilix = new JTextField("9999");
		JPanel pnl_lasilix = new JPanel();
		pnl_lasilix.add(lbl_lasilix);
		pnl_lasilix.add(txt_lasilix);
		pnl_lasilix.add(new JLabel("min"));
		
		boxRight.add(pnl_lasilix);
		
		JPanel pnl_formatDate = new JPanel();
		pnl_formatDate.add(new JLabel("Date format :"));
		this.comboDate = new JComboBox(new String[] { "MM/dd/yyyy", "dd/MM/yyyy" });
		pnl_formatDate.add(comboDate);
		
		boxRight.add(pnl_formatDate);

		JPanel pnl_btns = new JPanel();
		pnl_btns.add(this.btn_ok);
		pnl_btns.add(this.btn_cancel);

		JPanel pnl_center = new JPanel();
		pnl_center.add(boxLeft);
		pnl_center.add(boxRight);

		this.add(pnl_center, BorderLayout.CENTER);
		this.add(pnl_btns, BorderLayout.SOUTH);

		this.autoFillSettings();

		this.setLocationRelativeTo(parentComponent);
		this.pack();
		this.setResizable(false);
		this.setVisible(true);
	}

	private void checkSettingsFormat() {
		int n1 = Prefs.get("renal.preferred", DEFAULT_SETTINGS).split("\n").length;
		int n2 = DEFAULT_SETTINGS.split("\n").length;
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
		
		this.comboDate.setSelectedItem(this.getDateFormat());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton b = (JButton) e.getSource();

		if (b == RenalSettings.this.btn_ok) {
			String settings = "";
			settings += "Bladder:" + this.ckb_bld.isSelected();
			settings += "\n";
			settings += "Pelvis:" + this.ckb_ctl.isSelected();
			settings += "\n";
			settings += "Ureter:" + this.ckb_utr.isSelected();
			settings += "\n";
			settings += "Lasilix:" + Double.parseDouble(this.txt_lasilix.getText());
			settings += "\n";
			settings += "DateFormat:" + this.comboDate.getSelectedItem();

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

		String[] splitted = settingsString.split("\n");
		int cpt = 0;
		for (int i = 0; i < 3; i ++) {
			String bool = splitted[i].split(":")[1];
			ret[cpt] = Boolean.parseBoolean(bool);
			cpt++;
		}

		return ret;
	}
	
	public static Double getLasilixTime() {
		String settingsString = Prefs.get("renal.preferred", DEFAULT_SETTINGS);
		String[] splitted = settingsString.split("\n");
		return Double.valueOf(splitted[3].split(":")[1]);
	}

	public static String getDateFormat() {
		String settingsString = Prefs.get("renal.preferred", DEFAULT_SETTINGS);
		return settingsString.split("\n")[4].split(":")[1];
	}
	
}
