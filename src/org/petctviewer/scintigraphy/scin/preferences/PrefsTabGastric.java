package org.petctviewer.scintigraphy.scin.preferences;

import ij.Prefs;
import org.petctviewer.scintigraphy.gastric.Unit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class PrefsTabGastric extends JPanel implements ActionListener, ItemListener {
	public static final String PREF_SIMPLE_METHOD = "petctviewer.scin.gastric.simple_method", PREF_UNIT_USED =
			"petctviewer.scin.gastric.unit_used", PREF_LIQUID_PHASE = "petctviewer.scin.gastric" + ".liquid.enabled",
			PREF_LIQUID_PHASE_UNIT = "petctviewer.scin.gastric.liquid.unit", PREF_FRAME_DURATION_TOLERANCE =
			"petctviewer.scin.gastric.frame_duration_tolerance";
	private static final long serialVersionUID = 1L;
	private final PrefsWindows parent;
	private final JPanel panelLiquidOptions;

	public PrefsTabGastric(PrefsWindows parent) {
		// Set variable
		this.parent = parent;

		this.setLayout(new BorderLayout());
		JPanel pnl_titre = new JPanel();
		pnl_titre.add(new JLabel("<html><h3>Gastric Scinthigraphy settings</h3></html>"));
		this.add(pnl_titre, BorderLayout.NORTH);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// Check box simple method
		panel.add(this.createCheckbox(PREF_SIMPLE_METHOD, "Only use simple method", false));

		// Unit used for simple method
		panel.add(this.createUnitChooser(PREF_UNIT_USED, new Unit[]{Unit.COUNTS, Unit.KCOUNTS}));

		// Time tolerance
		panel.add(this.createTextInput(PREF_FRAME_DURATION_TOLERANCE, "Frame durations delta tolerance: ", "sec", 3));

		// TODO: remove liquid prefs, not used anymore
		// Liquid phase
		JPanel panelLiquid = new JPanel();
		panelLiquid.setLayout(new BoxLayout(panelLiquid, BoxLayout.Y_AXIS));
		panelLiquid.setBorder(BorderFactory.createTitledBorder("Liquid Phase"));
		// Activate liquid phase
		JCheckBox checkLiquidPhase = this.createCheckbox(PREF_LIQUID_PHASE, "Enable liquid phase acquisition", false);
		panelLiquid.add(checkLiquidPhase);
		// Liquid options
		panelLiquidOptions = new JPanel();
		panelLiquidOptions.setLayout(new BoxLayout(panelLiquidOptions, BoxLayout.Y_AXIS));
		panelLiquidOptions.add(this.createUnitChooser(PREF_LIQUID_PHASE_UNIT,
				new Unit[]{Unit.COUNTS_PER_SECOND, Unit.KCOUNTS_PER_SECOND, Unit.COUNTS_PER_MINUTE,
						Unit.KCOUNTS_PER_MINUTE}));
		panelLiquidOptions.setVisible(checkLiquidPhase.isSelected());
		panelLiquid.add(panelLiquidOptions);
		panel.add(panelLiquid);

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
				} catch(NumberFormatException e) {
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

			// Liquid phase
			if (check.getActionCommand().equals(PREF_LIQUID_PHASE)) {
				if (check.isSelected()) {
					// Display further options
					this.panelLiquidOptions.setVisible(true);
				} else this.panelLiquidOptions.setVisible(false);
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getSource() instanceof JComboBox) {
				@SuppressWarnings("unchecked") JComboBox<Unit> source = (JComboBox<Unit>) e.getSource();
				// Save new unit
				Prefs.set(source.getActionCommand(), ((Unit) e.getItem()).name());
				this.parent
						.displayMessage("Please close the window to save the preferences",
								PrefsWindows.DURATION_SHORT);
			}
		}
	}

}
