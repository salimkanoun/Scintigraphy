package org.petctviewer.scintigraphy.scin.preferences;

import ij.Prefs;
import org.petctviewer.scintigraphy.scin.model.Unit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class PrefTabGastric extends PrefTab implements ActionListener, ItemListener, DocumentListener {
	public static final String PREF_HEADER = PrefWindow.PREF_HEADER + ".gastric";
	public static final String PREF_SIMPLE_METHOD = PREF_HEADER + ".simple_method", PREF_UNIT_USED =
			PREF_HEADER + "unit_used", PREF_FRAME_DURATION_TOLERANCE = PREF_HEADER + ".frame_duration_tolerance";
	private static final long serialVersionUID = 1L;

	private JTextField textField;

	public PrefTabGastric(JFrame parent) {
		super("Gastric", parent);

		this.setTitle("Gastric Scintigraphy settings");

		// Check box simple method
		this.mainPanel.add(this.createCheckbox(PREF_SIMPLE_METHOD, "Only use simple method", false));

		// Unit used for simple method
		this.mainPanel.add(this.createUnitChooser(PREF_UNIT_USED, new Unit[]{Unit.COUNTS, Unit.KCOUNTS}));

		// Time tolerance
		JPanel pan = new JPanel();
		this.textField = new JTextField(Prefs.get(PREF_FRAME_DURATION_TOLERANCE, "1"), 3);
		this.textField.getDocument().addDocumentListener(this);
		this.add(new JLabel("Frame durations delta tolerance:"));
		this.add(this.textField);
		this.add(new JLabel("sec"));
		this.mainPanel.add(pan);

		this.add(this.mainPanel, BorderLayout.CENTER);
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

	private void savePref() {
		try {
			Prefs.set(PREF_FRAME_DURATION_TOLERANCE, Integer.parseInt(textField.getText()));
			if (this.parent != null && this.parent instanceof PrefWindow) ((PrefWindow) this.parent).displayMessage(
					null);
		} catch (NumberFormatException e) {
			Prefs.set(PREF_FRAME_DURATION_TOLERANCE, 1);
			if (this.parent != null && this.parent instanceof PrefWindow) ((PrefWindow) this.parent).displayMessage(
					"Cannot save (" + textField.getText() + ") -> not a number");
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getSource() instanceof JComboBox) {
				@SuppressWarnings("unchecked") JComboBox<Unit> source = (JComboBox<Unit>) e.getSource();
				// Save new unit
				Prefs.set(source.getActionCommand(), ((Unit) e.getItem()).name());
				if (this.parent != null && this.parent instanceof PrefWindow) ((PrefWindow) this.parent).displayMessage(
						"Please close the window to save the preferences", PrefWindow.DURATION_SHORT);
			}
		}
	}

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

}
