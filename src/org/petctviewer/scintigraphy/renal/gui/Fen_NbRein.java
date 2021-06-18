package org.petctviewer.scintigraphy.renal.gui;

import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Fen_NbRein extends JDialog implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final boolean[] kidneys = new boolean[2];
    private final JButton btn_l;
    private final JButton btn_r;
    private final JButton btn_lr;

    public Fen_NbRein(FenApplication parent) {
        this.setLayout(new GridLayout(2, 1));

        this.setTitle("Number of kidneys");

        JPanel flow = new JPanel();
        flow.add(this.add(new JLabel("How many kidneys has the patient ?")));
        this.add(flow);

        JPanel radio = new JPanel();
        this.btn_l = new JButton("Left kidney");
        this.btn_l.addActionListener(this);
        radio.add(btn_l);
        this.btn_r = new JButton("Right kidney");
        this.btn_r.addActionListener(this);
        radio.add(btn_r);
        this.btn_lr = new JButton("Both kidney");
        this.btn_lr.addActionListener(this);
        radio.add(btn_lr);
        this.add(radio);

        this.setLocationRelativeTo(parent);

        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        JButton b = (JButton) arg0.getSource();
        if (b == this.btn_l) {
            this.kidneys[0] = true;
        } else if (b == this.btn_r) {
            this.kidneys[1] = true;
        } else if (b == this.btn_lr) {
            this.kidneys[0] = true;
            this.kidneys[1] = true;
        }

        this.dispose();
    }

    public boolean[] getKidneys() {

        return this.kidneys;
    }
}
