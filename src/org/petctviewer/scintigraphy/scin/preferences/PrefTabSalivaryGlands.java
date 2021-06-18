package org.petctviewer.scintigraphy.scin.preferences;

import ij.Prefs;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PrefTabSalivaryGlands extends PrefTab implements DocumentListener {
    public static final String PREF_HEADER = PrefWindow.PREF_HEADER + ".salivary glands", PREF_CITRUS_INJECT_TIME =
            PREF_HEADER + ".citrus_inject_time";

    private static final long serialVersionUID = 1L;
    private final JTextField textField;


    public PrefTabSalivaryGlands(PrefWindow parent) {
        super("Salivary Glands", parent);

        this.setTitle("Salivary Glands scintigraphy settings");


        //panel lasilix
        Box boxRight = Box.createVerticalBox();
        JPanel pan = new JPanel();
        pan.add(new JLabel("Citrus injection time:"));
        this.textField = new JTextField(Prefs.get(PREF_CITRUS_INJECT_TIME, "10"), 4);
        this.textField.getDocument().addDocumentListener(this);
        pan.add(this.textField);
        pan.add(new JLabel("min"));
        boxRight.add(pan);

        this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.X_AXIS));
        this.mainPanel.add(boxRight);

    }

    private void savePref() {
        try {
            Prefs.set(PREF_CITRUS_INJECT_TIME, Double.parseDouble(textField.getText()));
            if (this.parent != null && this.parent instanceof PrefWindow) ((PrefWindow) parent).displayMessage(null);
        } catch (NumberFormatException e) {
            Prefs.set(PREF_CITRUS_INJECT_TIME, 1.);
            if (this.parent != null && this.parent instanceof PrefWindow) ((PrefWindow) parent).displayMessage(
                    "Cannot save (" + textField.getText() + ") -> not a number");
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

