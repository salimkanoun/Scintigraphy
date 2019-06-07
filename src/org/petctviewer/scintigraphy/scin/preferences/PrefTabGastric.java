package org.petctviewer.scintigraphy.scin.preferences;

import ij.Prefs;
import org.petctviewer.scintigraphy.scin.model.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class PrefTabGastric extends PrefTab implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L;

	public static final String PREF_HEADER = PrefWindow.PREF_HEADER + ".gastric";
	public static final String PREF_SIMPLE_METHOD = PREF_HEADER + ".simple_method", PREF_UNIT_USED =
			PREF_HEADER + "unit_used", PREF_FRAME_DURATION_TOLERANCE = PREF_HEADER + ".frame_duration_tolerance";

	public PrefTabGastric(PrefWindow parent) {
		super(parent, "Gastric");

		this.setTitle("Gastric Scintigraphy settings");

		// Check box simple method
		this.mainPanel.add(this.createCheckbox(PREF_SIMPLE_METHOD, "Only use simple method", false));

		// Unit used for simple method
		this.mainPanel.add(this.createUnitChooser(PREF_UNIT_USED, new Unit[]{Unit.COUNTS, Unit.KCOUNTS}));

		// Time tolerance
		this.mainPanel.add(this.createTextInput(PREF_FRAME_DURATION_TOLERANCE, "Frame durations delta tolerance: ", "sec", 3));

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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			JCheckBox check = (JCheckBox) e.getSource();
			// Save value in prefs
			Prefs.set(check.getActionCommand(), check.isSelected());
			this.parent.displayMessage("Please close the window to save the preferences", PrefWindow.DURATION_SHORT);
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
										   PrefWindow.DURATION_SHORT);
			}
		}
	}

}
