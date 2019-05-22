package org.petctviewer.scintigraphy.scin.preferences;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ij.IJ;
import ij.Prefs;

public class prefsTabBone extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JLabel lut;
	private JButton btn_choixLut, btn_displut;
	private JFileChooser fc;
	private JCheckBox ckb_lut;

	public prefsTabBone() {
		this.setLayout(new BorderLayout());
		JPanel pnl_titre = new JPanel();
		pnl_titre.add(new JLabel("<html><h3>Bone Scinthigraphy settings</h3></html>"));
		this.add(pnl_titre, BorderLayout.NORTH);

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
			int returnVal = fc.showOpenDialog(prefsTabBone.this);
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
