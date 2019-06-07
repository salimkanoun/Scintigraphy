package org.petctviewer.scintigraphy.scin.preferences;

import ij.IJ;
import ij.Prefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class PrefTabBone extends PrefTab {
	public static final String PREF_HEADER = PrefWindow.PREF_HEADER + ".bone", PREF_LUT = PREF_HEADER + ".lut",
			PREF_DEFAULT_LUT = PREF_HEADER + ".default_lut";

	private static final long serialVersionUID = 1L;
	private final JLabel lut;
	private final JButton btn_choixLut;
	private final JButton btn_displut;
	private JFileChooser fc;

	public PrefTabBone(PrefWindow parent) {
		super(parent, "Bone");

		this.setTitle("Bone Scintigraphy settings");

		this.fc = new JFileChooser();
		JPanel pan = new JPanel();

		String plut = Prefs.get(PrefTabMain.PREF_LUT, null) == null ? "Preferred LUT" : Prefs.get(PrefTabMain.PREF_LUT,
																								  null);
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
		boxLeft.add(this.createCheckbox(PREF_DEFAULT_LUT, "Use the default Lut ?", true));

		pan.add(boxLeft);
		this.add(pan, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.btn_choixLut) {
			this.fc.setCurrentDirectory(new File("./luts"));
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
