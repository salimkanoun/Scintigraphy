package org.petctviewer.scintigraphy.scin.preferences;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.gastric.Unit;

import ij.Prefs;

public class PrefsTabGastric extends JPanel implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L;

	public static final String PREF_SIMPLE_METHOD = "petctviewer.scin.gastric.simple_method",
			PREF_UNIT_USED = "petctviewer.scin.gastric.unit_used";

	private JCheckBox simpleMethod;
	private JComboBox<Unit> unitUsed;
	
	private PrefsWindows parent;

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
		this.simpleMethod = new JCheckBox("Only use simple method");
		this.simpleMethod.addActionListener(this);
		// Get state
		boolean state = Prefs.get(PREF_SIMPLE_METHOD, false);
		this.simpleMethod.setSelected(state);
		panel.add(this.simpleMethod);

		// Unit used
		JPanel panUnit = new JPanel();
		Unit[] possibleUnits = new Unit[] { Unit.COUNTS, Unit.KCOUNTS };
		this.unitUsed = new JComboBox<>(possibleUnits);
		// Get state
		String prefValue = Prefs.get(PREF_UNIT_USED, Unit.COUNTS.name());
		Unit unitState = Unit.valueOf(prefValue);
		this.unitUsed.setSelectedItem(unitState);
		this.unitUsed.addItemListener(this);
		panUnit.add(new JLabel("Unit used: "));
		panUnit.add(this.unitUsed);
		panel.add(panUnit);

		this.add(panel, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.simpleMethod) {
			// Save value in prefs
			Prefs.set(PREF_SIMPLE_METHOD, this.simpleMethod.isSelected());
			this.parent.displayMessage("Please close the window to save the preferences", PrefsWindows.DURATION_SHORT);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == this.unitUsed && e.getStateChange() == ItemEvent.SELECTED) {
			// Save new unit
			Prefs.set(PREF_UNIT_USED, ((Unit) e.getItem()).name());
			this.parent.displayMessage("Please close the window to save the preferences", PrefsWindows.DURATION_SHORT);
		}
	}

}
