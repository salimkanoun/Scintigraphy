package org.petctviewer.scintigraphy.scin.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

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

	private JComboBox<Isotope> comboBox;

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
		okBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		this.add(okBtn, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(parent);
	}

	/**
	 * Instantiates the dialog.
	 * 
	 * @param parent
	 * @param codeFound
	 */
	public IsotopeDialog(Frame parent, String codeFound) {
		super(parent, "Choose isotope", true);

		JPanel panel = new JPanel(new GridLayout(0, 1));

		panel.add(new JLabel("The code " + codeFound + " was found but is not regognized as a Isotope Code."));
		panel.add(new JLabel("Please enter the Isotope used for this acquisition:"));

		comboBox = new JComboBox<>(Isotope.values());
		panel.add(comboBox);

		JButton okBtn = new JButton("Validate");
		okBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

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
