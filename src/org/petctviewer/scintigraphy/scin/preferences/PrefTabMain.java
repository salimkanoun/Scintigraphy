package org.petctviewer.scintigraphy.scin.preferences;

import ij.IJ;
import ij.Prefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class PrefTabMain extends PrefTab {

	public static final String PREF_HEADER = PrefWindow.PREF_HEADER + ".main", PREF_EXPERIMENTS =
			PREF_HEADER + ".experimental", PREF_LUT = PREF_HEADER + ".lut", PREF_SAVE_DIRECTORY =
			PREF_HEADER + ".save_directory", PREF_DATE_FORMAT = PREF_HEADER + ".date_format";

	private static final long serialVersionUID = 1L;
	private final JLabel lut;
	private final JLabel dir;
	private final JButton btn_choixLut;
	private final JButton btn_dir;
	private final JButton btn_displut;
	private JFileChooser fc;
	private final JComboBox comboDate;

	public PrefTabMain(PrefWindow parent) {
		super(parent, "Main");

		this.setTitle("Main settings");

		String plut = Prefs.get(PREF_LUT, "Preferred LUT");
		this.lut = new JLabel(plut);
		this.lut.setEnabled(false);
		this.btn_choixLut = new JButton("Open...");
		this.btn_choixLut.addActionListener(this);

		this.btn_displut = new JButton("Show LUTs");
		this.btn_displut.addActionListener(this);

		String pdir = Prefs.get(PREF_SAVE_DIRECTORY, "Save Directory");
		this.dir = new JLabel(pdir);
		this.dir.setEnabled(false);
		this.btn_dir = new JButton("Browse");
		this.btn_dir.addActionListener(this);
		this.fc = new JFileChooser(new File(pdir));

		this.mainPanel.setLayout(new GridLayout(4, 1));

		JPanel pan_lut = new JPanel();
		pan_lut.add(this.lut);
		pan_lut.add(this.btn_choixLut);
		pan_lut.add(this.btn_displut);
		this.mainPanel.add(pan_lut);

		JPanel pan_dir = new JPanel();
		pan_dir.add(this.dir);
		pan_dir.add(this.btn_dir);
		this.mainPanel.add(pan_dir);

		JPanel pnl_formatDate = new JPanel();
		pnl_formatDate.add(new JLabel("Date format:"));
		this.comboDate = new JComboBox<>(new String[] { "MM/dd/yyyy", "dd/MM/yyyy" });
		this.comboDate.setSelectedItem(Prefs.get(PREF_DATE_FORMAT, "MM/dd/yyyy"));
		this.comboDate.addActionListener(this);
		pnl_formatDate.add(comboDate);
		this.mainPanel.add(pnl_formatDate);

		// Check box simple method
		JCheckBox experimentalMode = this.createCheckbox(PREF_EXPERIMENTS, "Try experimental methods", false);
		experimentalMode.setToolTipText("This methods are currently : { Deconvolution }");
		this.mainPanel.add(experimentalMode);

		this.add(this.mainPanel, BorderLayout.CENTER);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.btn_choixLut) {
			String path = Prefs.get(PREF_LUT, "./luts");
			this.fc.setCurrentDirectory(new File(path));
			this.fc.setDialogTitle("Choose Preferred LUT");
			int returnVal = fc.showOpenDialog(PrefTabMain.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				this.lut.setText(file.getPath());
				Prefs.set(PREF_LUT, this.lut.getText());
			}
		} else if (e.getSource() == this.btn_dir) {
			this.fc.setDialogTitle("Export directory");
			this.fc.setCurrentDirectory(this.fc.getFileSystemView().getDefaultDirectory());
			this.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			this.fc.setAcceptAllFileFilterUsed(false);
			int rval = fc.showOpenDialog(PrefTabMain.this);
			if (rval == JFileChooser.APPROVE_OPTION) {
				this.dir.setText(fc.getSelectedFile().getAbsoluteFile().toString());
				Prefs.set(PREF_SAVE_DIRECTORY, this.dir.getText());
			}
		} else if (e.getSource() == this.btn_displut) {
			IJ.run("Display LUTs");
		} else if (e.getSource() == this.comboDate) {
			Prefs.set(PREF_DATE_FORMAT, (String) this.comboDate.getSelectedItem());
		} else super.actionPerformed(e);

		this.fc = new JFileChooser();
	}

}
