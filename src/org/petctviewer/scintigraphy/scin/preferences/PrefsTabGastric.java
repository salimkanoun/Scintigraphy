package org.petctviewer.scintigraphy.scin.preferences;

import ij.Prefs;
import org.petctviewer.scintigraphy.scin.model.Unit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class PrefsTabGastric extends JPanel implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L;

	public static final String PREF_HEADER = PrefsWindows.PREF_HEADER + ".gastric";
	public static final String PREF_SIMPLE_METHOD = PREF_HEADER + ".simple_method", PREF_UNIT_USED =
			PREF_HEADER + "unit_used", PREF_FRAME_DURATION_TOLERANCE = PREF_HEADER + ".frame_duration_tolerance";
	private final PrefsWindows parent;

	public PrefsTabGastric(PrefsWindows parent) {
		// Set variable
		this.parent = parent;

		this.setLayout(new BorderLayout());
		JPanel pnl_titre = new JPanel();
		pnl_titre.add(new JLabel("<html><h3>Gastric Scintigraphy settings</h3></html>"));
		this.add(pnl_titre, BorderLayout.NORTH);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// Check box simple method
		panel.add(this.createCheckbox(PREF_SIMPLE_METHOD, "Only use simple method", false));

		// Unit used for simple method
		panel.add(this.createUnitChooser(PREF_UNIT_USED, new Unit[]{Unit.COUNTS, Unit.KCOUNTS}));

		// Time tolerance
		panel.add(this.createTextInput(PREF_FRAME_DURATION_TOLERANCE, "Frame durations delta tolerance: ", "sec", 3));

		this.add(panel, BorderLayout.CENTER);
	}

	private JCheckBox createCheckbox(String prefName, String text, boolean defaultValue) {
		JCheckBox checkBox = new JCheckBox(text);
		checkBox.setActionCommand(prefName);
		checkBox.addActionListener(this);
		// Get state
		boolean state = Prefs.get(prefName, defaultValue);
		checkBox.setSelected(state);
		return checkBox;
	}

	private JPanel createUnitChooser(String prefName, Unit[] possibleUnits) {
		JPanel panUnit = new JPanel();
		JComboBox<Unit> unitUsed = new JComboBox<>(possibleUnits);
		unitUsed.setActionCommand(prefName);
		// Get state
		String prefValue = Prefs.get(prefName, possibleUnits[0].name());
		Unit unitState = Unit.valueOf(prefValue);
		unitUsed.setSelectedItem(unitState);
		unitUsed.addItemListener(this);
		panUnit.add(new JLabel("Unit used: "));
		panUnit.add(unitUsed);
		return panUnit;
	}

	private JPanel createTextInput(String prefName, String text, String afterText, int length) {
		JPanel panel = new JPanel();

		String defaultValue = Integer.toString((int) Prefs.get(PREF_FRAME_DURATION_TOLERANCE, 1));

		JLabel label = new JLabel(text);
		panel.add(label);

		JTextField textField = new JTextField(defaultValue, length);
		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				savePref();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				savePref();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				savePref();
			}

			private void savePref() {
				try {
					Prefs.set(PREF_FRAME_DURATION_TOLERANCE, Integer.parseInt(textField.getText()));
					parent.displayMessage(null);
				} catch (NumberFormatException e) {
					Prefs.set(PREF_FRAME_DURATION_TOLERANCE, 1);
					parent.displayMessage("Cannot save (" + textField.getText() + ") -> not a number");
				}
			}
		});
		panel.add(textField);

		if (afterText != null) {
			JLabel afterLabel = new JLabel(afterText);
			panel.add(afterLabel);
		}

		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			JCheckBox check = (JCheckBox) e.getSource();
			// Save value in prefs
			Prefs.set(check.getActionCommand(), check.isSelected());
			this.parent.displayMessage("Please close the window to save the preferences", PrefsWindows.DURATION_SHORT);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getSource() instanceof JComboBox) {
				@SuppressWarnings("unchecked") JComboBox<Unit> source = (JComboBox<Unit>) e.getSource();
				// Save new unit
				Prefs.set(source.getActionCommand(), ((Unit) e.getItem()).name());
				this.parent.displayMessage("Please close the window to save the preferences",
										   PrefsWindows.DURATION_SHORT);
			}
		}
	}

}
