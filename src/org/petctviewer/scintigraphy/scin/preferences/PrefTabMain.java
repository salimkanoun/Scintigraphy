package org.petctviewer.scintigraphy.scin.preferences;

import ij.IJ;
import ij.Prefs;
import ij.gui.Toolbar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;

public class PrefTabMain extends PrefTab {

	public static final String PREF_HEADER = PrefWindow.PREF_HEADER + ".main", PREF_EXPERIMENTS =
			PREF_HEADER + ".experimental", PREF_LUT = PREF_HEADER + ".lut", PREF_SAVE_DIRECTORY =
			PREF_HEADER + ".save_directory", PREF_DATE_FORMAT = PREF_HEADER + ".date_format", PREF_LUT_CAPTURE =
			PREF_HEADER + ".lut_capture", PREF_TOOL_ROI = PREF_HEADER + ".toolRoi";

	public static final String TXT_NO_LUT = "No LUT selected";

	private static final long serialVersionUID = 1L;
	private final JLabel lut;
	private final JLabel dir;
	private final JButton btn_choixLut, btnCaptureLut;
	private final JButton btn_dir;
	private final JButton btn_displut;
	private final JComboBox comboDate;
	private final JLabel lCaptureLut;
	private final JFrame parent;
	private JFileChooser fc;

	public PrefTabMain(JFrame parent) {
		super("Main");
		if (parent instanceof PrefWindow) this.setParent((PrefWindow) parent);
		this.parent = parent;

		this.setTitle("Main settings");

		// Save directory
		JPanel pan_dir = new JPanel();
		String pdir = Prefs.get(PREF_SAVE_DIRECTORY, "Save Directory");
		// - Label current directory
		this.dir = new JLabel(pdir);
		this.dir.setEnabled(false);
		pan_dir.add(this.dir);
		// - Button choose directory
		this.btn_dir = new JButton("Browse");
		this.btn_dir.addActionListener(this);
		pan_dir.add(this.btn_dir);
		this.mainPanel.add(pan_dir);
		// -

		this.fc = new JFileChooser(new File(pdir));

		// =====
		// LUTs
		// =====
		JPanel panelLuts = new JPanel();
		panelLuts.setLayout(new BoxLayout(panelLuts, BoxLayout.PAGE_AXIS));
		panelLuts.setBorder(BorderFactory.createTitledBorder("LUTs"));

		// Button display LUTs
		this.btn_displut = new JButton("Show LUTs");
		this.btn_displut.addActionListener(this);
		panelLuts.add(this.btn_displut);
		// -

		// Default LUT
		this.createLabel(panelLuts, "Preferred LUT");
		this.lut = new JLabel(Prefs.get(PREF_LUT, TXT_NO_LUT));
		this.lut.setEnabled(false);
		panelLuts.add(this.lut);

		this.btn_choixLut = new JButton("Choose preferred LUT...");
		this.btn_choixLut.addActionListener(this);
		panelLuts.add(this.btn_choixLut);
		// -

		// Capture LUT
		this.createLabel(panelLuts, "Capture LUT");

		JCheckBox checkBoxCaptureLut = new JCheckBox("Use custom LUT");
		checkBoxCaptureLut.setSelected(Prefs.get(PREF_LUT_CAPTURE, null) != null);
		panelLuts.add(checkBoxCaptureLut);

		lCaptureLut = new JLabel(Prefs.get(PREF_LUT_CAPTURE, TXT_NO_LUT));
		lCaptureLut.setEnabled(false);
		lCaptureLut.setVisible(checkBoxCaptureLut.isSelected());
		panelLuts.add(lCaptureLut);

		this.btnCaptureLut = new JButton("Choose capture LUT...");
		this.btnCaptureLut.setVisible(checkBoxCaptureLut.isSelected());
		this.btnCaptureLut.addActionListener(this);
		panelLuts.add(this.btnCaptureLut);

		checkBoxCaptureLut.addActionListener(e -> {
			if (!checkBoxCaptureLut.isSelected()) {
				Prefs.set(PREF_LUT_CAPTURE, null);
				this.lCaptureLut.setText(TXT_NO_LUT);
			}
			this.lCaptureLut.setVisible(checkBoxCaptureLut.isSelected());
			this.btnCaptureLut.setVisible(checkBoxCaptureLut.isSelected());
			this.parent.pack();
		});
		// -

		this.mainPanel.add(panelLuts);
		// ====================================

		// Date format
		JPanel pnl_formatDate = new JPanel();
		pnl_formatDate.add(new JLabel("Date format:"));
		this.comboDate = new JComboBox<>(new String[]{"MM/dd/yyyy", "dd/MM/yyyy"});
		this.comboDate.setSelectedItem(Prefs.get(PREF_DATE_FORMAT, "MM/dd/yyyy"));
		this.comboDate.addActionListener(this);
		pnl_formatDate.add(comboDate);
		this.mainPanel.add(pnl_formatDate);
		// -

		// Fiji tool to draw ROIs
		String[] possibleTools = new String[]{"Polygone", "Freeroi"};
		JPanel panCombo = new JPanel();
		JComboBox<String> tools = new JComboBox<>(possibleTools);
		tools.setActionCommand(PREF_TOOL_ROI);
		// Get state
		String prefValue = Prefs.get(PREF_TOOL_ROI, possibleTools[0]);
		tools.setSelectedItem(prefValue);
		tools.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				// Save new unit
				Prefs.set(PREF_TOOL_ROI, (String) tools.getSelectedItem());
				if (this.parent != null && this.parent instanceof PrefWindow) ((PrefWindow) this.parent).displayMessage(
						"Please close the window to save the preferences", PrefWindow.DURATION_SHORT);
			}
		});
		panCombo.add(new JLabel("Tool used: "));
		panCombo.add(tools);
		this.mainPanel.add(panCombo);
		// -

		// Check box simple method
		JCheckBox experimentalMode = this.createCheckbox(PREF_EXPERIMENTS, "Try experimental methods", false);
		experimentalMode.setToolTipText("This methods are currently : { Deconvolution }");
		this.mainPanel.add(experimentalMode);
		// -

		this.add(this.mainPanel, BorderLayout.CENTER);
	}

	public static int toolFromString(String str) {
		switch (str) {
			case "Freeroi":
				return Toolbar.FREEROI;
			default:
				return Toolbar.POLYGON;
		}
	}

	private JLabel createLabel(Container container, String text) {
		JLabel label = new JLabel(text);
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		label.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		container.add(label);
		return label;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.btn_choixLut) {
			String path = Prefs.get(PREF_LUT, "./luts");
			this.fc.setCurrentDirectory(new File(path));
			this.fc.setDialogTitle("Choose Preferred LUT");
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				this.lut.setText(file.getPath());
				this.parent.pack();
				Prefs.set(PREF_LUT, this.lut.getText());
			}
		} else if (e.getSource() == this.btnCaptureLut) {
			String path = Prefs.get(PREF_LUT_CAPTURE, Prefs.get(PREF_LUT, "./luts"));
			JFileChooser chooser = new JFileChooser(new File(path));
			chooser.setDialogTitle("Choose Capture LUT");
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				this.lCaptureLut.setText(chooser.getSelectedFile().getPath());
				this.parent.pack();
				Prefs.set(PREF_LUT_CAPTURE, chooser.getSelectedFile().getPath());
			}
		} else if (e.getSource() == this.btn_dir) {
			this.fc.setDialogTitle("Export directory");
			this.fc.setCurrentDirectory(this.fc.getFileSystemView().getDefaultDirectory());
			this.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			this.fc.setAcceptAllFileFilterUsed(false);
			int rval = fc.showOpenDialog(PrefTabMain.this);
			if (rval == JFileChooser.APPROVE_OPTION) {
				this.dir.setText(fc.getSelectedFile().getAbsoluteFile().toString());
				this.parent.pack();
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
