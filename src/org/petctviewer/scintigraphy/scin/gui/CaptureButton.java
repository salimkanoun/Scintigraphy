package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.JButton;
import javax.swing.JLabel;

public class CaptureButton extends JButton {
	
	private static final long serialVersionUID = 1L;
	private TabResult tabResult;
	private JLabel lbl_credits;

	public CaptureButton(String name, TabResult tab, JLabel lbl_credits) {
		super(name == null ? "" : name);
		this.tabResult = tab;
		this.lbl_credits = lbl_credits;
	}
	
	public TabResult getTabResult() {
		return this.tabResult;
	}
	
	public JLabel getLabelCredits() {
		return this.lbl_credits;
	}


}
