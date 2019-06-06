package org.petctviewer.scintigraphy.scin.gui;

import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import javax.swing.*;
import java.awt.*;

/**
 * This dialog is used to prompt the isotope used for an acquisition to the
 * user.<br>
 * The isotopes displayed are specified in the enum {@link Isotope}.
 * 
 * @author Titouan QUÉMA
 *
 */
public class IsotopeDialog extends Dialog {
	private static final long serialVersionUID = 1L;

	private final JComboBox<Isotope> comboBox;

	/**
	 * Instantiates the dialog. This is used to prompt the isotope.
	 * 
	 * @param parent Frame where this dialog will be placed relative to
	 */
	public IsotopeDialog(Frame parent) {
		super(parent, "Choose isotope", true);

		JPanel panel = new JPanel();

		panel.add(new JLabel("Please enter the Isotope used for this acquisition:"));

		comboBox = new JComboBox<>(Isotope.values());
		panel.add(comboBox);

		JButton okBtn = new JButton("Validate");
		okBtn.addActionListener(e -> dispose());

		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		this.add(okBtn, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(parent);
	}

	/**
	 * Instantiates the dialog.
	 *
	 * @param parent Frame where this dialog will be placed relative to
	 * @param codeFound Code found in the DICOM header
	 */
	public IsotopeDialog(Frame parent, String codeFound) {
		super(parent, "Choose isotope", true);

		JPanel panel = new JPanel(new GridLayout(0, 1));

		panel.add(new JLabel("The code " + codeFound + " was found but is not recognized as an Isotope Code."));
		panel.add(new JLabel("Please enter the Isotope used for this acquisition:"));

		comboBox = new JComboBox<>(Isotope.values());
		panel.add(comboBox);

		JButton okBtn = new JButton("Validate");
		okBtn.addActionListener(e -> dispose());

		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		this.add(okBtn, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(parent);
	}

	public Isotope getIsotope() {
		return (Isotope) this.comboBox.getSelectedItem();
	}

}
