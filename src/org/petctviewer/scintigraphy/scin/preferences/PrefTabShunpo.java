package org.petctviewer.scintigraphy.scin.preferences;

import javax.swing.*;
import java.awt.*;

public class PrefTabShunpo extends PrefTab {

	private static final long serialVersionUID = 1L;
	public static final String PREF_HEADER = PrefWindow.PREF_HEADER + ".shunpo";
	public static final String PREF_WITH_KIDNEYS = PREF_HEADER + ".withKidneys";

	public PrefTabShunpo(JFrame parent) {
		super("Pulmonary Shunt", parent);

		this.setTitle("Gastric Scintigraphy settings");

		// Check box simple method
		this.mainPanel.add(this.createCheckbox(PREF_WITH_KIDNEYS, "Use kidneys", true));

		this.add(this.mainPanel, BorderLayout.CENTER);
	}
}
