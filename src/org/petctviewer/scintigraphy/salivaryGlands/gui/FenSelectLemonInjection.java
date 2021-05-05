package org.petctviewer.scintigraphy.salivaryGlands.gui;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class FenSelectLemonInjection extends JDialog {
    private JSpinner timeLemonInjection;
    private JButton saveBtn;

    public FenSelectLemonInjection() {
        this.setTitle("Lemon Juice Injection");

        JPanel container = new JPanel(new GridLayout(3, 1, 0, 10));
        container.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        container.add(new JLabel("Indicate when lemon juice was injected"));

        JPanel input = new JPanel(new GridLayout(1, 2, 15, 0));
        SpinnerModel model = new SpinnerNumberModel(10, 1, 30, 0.1);
        this.timeLemonInjection = new JSpinner(model);
        input.add(this.timeLemonInjection);
        input.add(new JLabel("min"));
        container.add(input);

        this.saveBtn = new JButton("Save");
        this.saveBtn.addActionListener(e -> this.dispose());
        container.add(this.saveBtn);

        this.add(container);

        this.pack();
        this.setVisible(true);
    }

    /**
     * Returns the value of lemon juice injection selected by the user
     * @return the moment of lemon juice injection in minute
     */
    public double getLemonJuiceInjection() {
        return (Double) this.timeLemonInjection.getValue();
    }
}