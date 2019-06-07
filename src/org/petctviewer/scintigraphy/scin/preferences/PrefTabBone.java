package org.petctviewer.scintigraphy.scin.preferences;

import ij.IJ;
import ij.Prefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class PrefTabBone extends PrefTab {

	private static final long serialVersionUID = 1L;
	private final JLabel lut;
	private final JButton btn_choixLut;
	private final JButton btn_displut;
	private JFileChooser fc;
	private final JCheckBox ckb_lut;

	public PrefTabBone(PrefWindow parent) {
		super(parent, "Bone");

		this.setTitle("Bone Scintigraphy settings");

		this.fc = new JFileChooser();
		JPanel pan = new JPanel();

		String plut = Prefs.get("lut.preferred", null) == null ? "Preferred LUT" : Prefs.get("lut.preferred", null);
		this.lut = new JLabel(plut);
		this.lut.setEnabled(false);
		this.btn_choixLut = new JButton("Open...");
		this.btn_choixLut.addActionListener(this);

		this.btn_displut = new JButton("Show LUTs");
		this.btn_displut.addActionListener(this);

		JPanel pan_lut = new JPanel();
		pan_lut.add(this.lut);
		pan_lut.add(this.btn_choixLut);
		pan_lut.add(this.btn_displut);
		pan.add(pan_lut);

		Box boxLeft = Box.createVerticalBox();
		ckb_lut = new JCheckBox("Use the default Lut ?");
		ckb_lut.setSelected(true);
		ckb_lut.setEnabled(true);
		boxLeft.add(ckb_lut);

		pan.add(boxLeft);
		this.add(pan, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btn_choixLut) {
			this.fc.setCurrentDirectory(new File("./luts"));
			this.fc.setDialogTitle("Choose Preferred LUT for Bone Scintigraphy");
			int returnVal = fc.showOpenDialog(PrefTabBone.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				this.lut.setText(file.getPath());
				Prefs.set("lut.preferredforbone", this.lut.getText() + "");
				this.fc = new JFileChooser();
			}
		} else if (arg0.getSource() == this.btn_displut) {
			IJ.run("Display LUTs");
			this.fc = new JFileChooser();
		} else if (arg0.getSource() == ckb_lut) {
			Prefs.set("bone.defaultlut.preferred", this.ckb_lut.isSelected());
			Prefs.savePreferences();
		}

	}

}
