package org.petctviewer.scintigraphy.scin.preferences;

import ij.IJ;
import ij.Prefs;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class PrefTabBone extends PrefTab {
	public static final String PREF_HEADER = PrefWindow.PREF_HEADER + ".bone", PREF_LUT = PREF_HEADER + ".lut",
			PREF_USE_CUSTOM_LUT = PREF_HEADER + ".default_lut";

	private static final String TXT_SELECT_LUT = "Select a LUT";

	private static final long serialVersionUID = 1L;
	private final JLabel lut;
	private final JButton btn_choixLut;
	private final JButton btn_displut;
	private JFileChooser fc;
	private JCheckBox checkBoxDefaultLut;

	public PrefTabBone(PrefWindow parent) {
		super(parent, "Bone");

		this.setTitle("Bone Scintigraphy settings");

		this.fc = new JFileChooser();

		String plut = Prefs.get(PrefTabMain.PREF_LUT, TXT_SELECT_LUT);
		this.lut = new JLabel(plut);
		this.lut.setEnabled(false);
		this.btn_choixLut = new JButton("Open...");
		this.btn_choixLut.addActionListener(this);

		this.btn_displut = new JButton("Show LUTs");
		this.btn_displut.addActionListener(this);

		// Checkbox default lut
		this.checkBoxDefaultLut = this.createCheckbox(PREF_USE_CUSTOM_LUT, "Use custom LUT", false);
		this.checkBoxDefaultLut.addActionListener(e -> {
			this.lut.setVisible(this.checkBoxDefaultLut.isSelected());
			this.btn_choixLut.setVisible(this.checkBoxDefaultLut.isSelected());
		});
		this.mainPanel.add(this.checkBoxDefaultLut);

		this.mainPanel.add(this.btn_displut);
		this.mainPanel.add(this.lut);
		this.mainPanel.add(this.btn_choixLut);

		this.lut.setVisible(this.checkBoxDefaultLut.isSelected());
		this.btn_choixLut.setVisible(this.checkBoxDefaultLut.isSelected());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.btn_choixLut) {
			String path = Prefs.get(PREF_LUT, "./luts");
			this.fc.setCurrentDirectory(new File(path));
			this.fc.setDialogTitle("Choose Preferred LUT for Bone Scintigraphy");
			int returnVal = fc.showOpenDialog(PrefTabBone.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				this.lut.setText(file.getPath());
				Prefs.set(PREF_LUT, this.lut.getText() + "");
				this.fc = new JFileChooser();
			}
		} else if (e.getSource() == this.btn_displut) {
			IJ.run("Display LUTs");
			this.fc = new JFileChooser();
		} else super.actionPerformed(e);

	}

}
