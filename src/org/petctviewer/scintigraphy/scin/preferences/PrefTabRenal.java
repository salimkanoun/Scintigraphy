package org.petctviewer.scintigraphy.scin.preferences;

import ij.Prefs;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PrefTabRenal extends PrefTab implements DocumentListener {
	public static final String PREF_HEADER = PrefWindow.PREF_HEADER + ".renal", PREF_BLADDER = PREF_HEADER +
			".bladder",
			PREF_PELVIS = PREF_HEADER + ".pelvis", PREF_URETER = PREF_HEADER + ".ureter", PREF_LASILIX_INJECT_TIME =
			PREF_HEADER + ".lasilix_inject_time";

	private static final long serialVersionUID = 1L;

	private JTextField textField;

	public PrefTabRenal(PrefWindow parent) {
		super(parent, "Renal");

		this.setTitle("Renal scintigraphy settings");

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
		boxLeft.add(this.createCheckbox(PREF_BLADDER, "Bladder", true));
		boxLeft.add(this.createCheckbox(PREF_PELVIS, "Pelvis", true));
		boxLeft.add(this.createCheckbox(PREF_URETER, "Ureter", true));

		//panel lasilix
		Box boxRight = Box.createVerticalBox();
		JPanel pan = new JPanel();
		pan.add(new JLabel("Lasilix injection time:"));
		this.textField = new JTextField(Prefs.get(PREF_LASILIX_INJECT_TIME, "20"), 4);
		this.textField.getDocument().addDocumentListener(this);
		pan.add(this.textField);
		pan.add(new JLabel("min"));
		boxRight.add(pan);

		this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.X_AXIS));
		this.mainPanel.add(boxLeft);
		this.mainPanel.add(boxRight);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		savePref();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		savePref();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		savePref();
	}

	private void savePref() {
		try {
			Prefs.set(PREF_LASILIX_INJECT_TIME, Double.parseDouble(textField.getText()));
			parent.displayMessage(null);
		} catch (NumberFormatException e) {
			Prefs.set(PREF_LASILIX_INJECT_TIME, 1.);
			parent.displayMessage("Cannot save (" + textField.getText() + ") -> not a number");
		}
	}
}
