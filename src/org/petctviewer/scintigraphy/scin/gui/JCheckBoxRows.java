package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class JCheckBoxRows extends JPanel {
    private final JCheckBox[] checkboxes;

    public JCheckBoxRows(String[] rowsTitle, ChangeListener listener) {
        this.setLayout(new GridLayout(rowsTitle.length, 2));
        this.checkboxes = new JCheckBox[rowsTitle.length];

        for (int i = 0; i < rowsTitle.length; i++) {
            JLabel lbl = new JLabel(rowsTitle[i]);
            lbl.setOpaque(true);
            this.add(lbl);

            JCheckBox cb = new JCheckBox("", true);
            cb.setName(String.valueOf(i));
            cb.addChangeListener(listener);
            this.checkboxes[i] = cb;
            this.add(cb);
        }
    }

    public void setAllChecked(boolean state) {
        for (JCheckBox cb: checkboxes) {
            cb.setSelected(state);
        }
    }
}
