package org.petctviewer.scintigraphy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ij.Prefs;

public class RenalSettings extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4940052155300552623L;
	
	private JButton btn_ok, btn_cancel;
	private JCheckBox ckb_bld, ckb_plv, ckb_utr;
	private JTextField txt_lasilix;

	public RenalSettings(Component parentComponent) {		
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
		ckb_plv = new JCheckBox("Pelvis");
		boxLeft.add(ckb_plv);
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

	private void autoFillSettings() {
		this.ckb_bld.setSelected(Prefs.get("renal.bladder.preferred", true));
		this.ckb_plv.setSelected(Prefs.get("renal.pelvis.preferred", true));
		this.ckb_utr.setSelected(Prefs.get("renal.ureter.preferred", true));
		
		this.txt_lasilix.setText("" + Prefs.get("renal.lasilix.preferred", 20.0));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton b = (JButton) e.getSource();

		if (b == RenalSettings.this.btn_ok) {
			Prefs.set("renal.bladder.preferred", this.ckb_bld.isSelected());
			Prefs.set("renal.pelvis.preferred", this.ckb_plv.isSelected());
			Prefs.set("renal.ureter.preferred", this.ckb_utr.isSelected());
			Prefs.set("renal.lasilix.preferred", Double.parseDouble(this.txt_lasilix.getText()));
			Prefs.savePreferences();
		}

		RenalSettings.this.dispose();
	}
}
