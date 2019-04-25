package org.petctviewer.scintigraphy.gastric_refactored.gui;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Fit {

	public static final Fit LINEAR_FIT = new Fit("Linear",
			new ImageIcon("/Scintigraphy/src/org/petctviewer/scintigraphy/gastric_refactored/gui/fit_linear.png")),
			POWER_FIT = new Fit("Exponential",
					new ImageIcon(
							"/Scintigraphy/src/org/petctviewer/scintigraphy/gastric_refactored/gui/fit_linear.png")),
			POLYNOMIAL_FIT = new Fit("Polynomial", new ImageIcon(
					"/Scintigraphy/src/org/petctviewer/scintigraphy/gastric_refactored/gui/fit_linear.png")),
			TEST_FIT = new Fit("Testing...", null);

	public static Fit[] allFits() {
		return new Fit[] { LINEAR_FIT, POWER_FIT, POLYNOMIAL_FIT, TEST_FIT };
	}

	private String name;
	private Icon icon;

	public Fit(String name, Icon icon) {
		this.name = name;
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public Icon getIcon() {
		return icon;
	}

}
