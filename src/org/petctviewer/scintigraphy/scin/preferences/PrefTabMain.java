package org.petctviewer.scintigraphy.scin.preferences;

import ij.IJ;
import ij.Prefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class PrefTabMain extends PrefTab {

	private static final long serialVersionUID = 1L;
	private final JLabel lut;
	private final JLabel dir;
	private final JButton btn_choixLut;
	private final JButton btn_dir;
	private final JButton btn_displut;
	private JFileChooser fc;
	private final JComboBox comboDate;
	private final JCheckBox experimentalMode;

	public PrefTabMain(PrefWindow parent) {
		super(parent, "Main");

		this.setTitle("Main settings");

		String plut = Prefs.get("lut.preferred", null) == null ? "Preferred LUT" : Prefs.get("lut.preferred", null);
		this.lut = new JLabel(plut);
		this.lut.setEnabled(false);
		this.btn_choixLut = new JButton("Open...");
		this.btn_choixLut.addActionListener(this);

		this.btn_displut = new JButton("Show LUTs");
		this.btn_displut.addActionListener(this);

		String pdir = Prefs.get("dir.preferred", null) == null ? "Save Directory" : Prefs.get("dir.preferred", null);
		this.dir = new JLabel(pdir);
		this.dir.setEnabled(false);
		this.btn_dir = new JButton("Browse");
		this.btn_dir.addActionListener(this);
		this.fc = new JFileChooser();

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
		pnl_formatDate.add(new JLabel("Date format :"));
		this.comboDate = new JComboBox<>(new String[] { "MM/dd/yyyy", "dd/MM/yyyy" });
		this.comboDate.setSelectedItem(Prefs.get("dateformat.preferred", "MM/dd/yyyy"));
		this.comboDate.addActionListener(this);
		pnl_formatDate.add(comboDate);
		this.mainPanel.add(pnl_formatDate);

		// Check box simple method
		this.experimentalMode = new JCheckBox("Try experimental methods");
		this.experimentalMode.addActionListener(this);
		this.experimentalMode.setSelected(Prefs.get("petctviewer.scin.experimental", false));
		this.experimentalMode.setToolTipText("This methods are currently : { Deconvolution }");
		this.mainPanel.add(this.experimentalMode);

		this.add(this.mainPanel, BorderLayout.CENTER);

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btn_choixLut) {
			this.fc.setCurrentDirectory(new File("./luts"));
			this.fc.setDialogTitle("Choose Preferred LUT");
			int returnVal = fc.showOpenDialog(PrefTabMain.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				this.lut.setText(file.getPath());
				Prefs.set("lut.preferred", this.lut.getText() + "");
			}
		}

		else if (arg0.getSource() == this.btn_dir) {
			this.fc.setDialogTitle("Export directory");
			this.fc.setCurrentDirectory(this.fc.getFileSystemView().getDefaultDirectory());
			this.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			this.fc.setAcceptAllFileFilterUsed(false);
			int rval = fc.showOpenDialog(PrefTabMain.this);
			if (rval == JFileChooser.APPROVE_OPTION) {
				this.dir.setText(fc.getSelectedFile().getAbsoluteFile().toString());
				Prefs.set("dir.preferred", this.dir.getText() + "");
			}
		}

		else if (arg0.getSource() == this.btn_displut) {
			IJ.run("Display LUTs");
		}

		else if (arg0.getSource() == this.comboDate) {
			Prefs.set("dateformat.preferred", (String) this.comboDate.getSelectedItem());
		}
		else if (arg0.getSource() == this.experimentalMode) {
			// Save value in prefs
			Prefs.set("petctviewer.scin.experimental", this.experimentalMode.isSelected());
		}

		this.parent.displayMessage("Please close the window to save the preferences", PrefWindow.DURATION_SHORT);
		this.fc = new JFileChooser();
	}

}
