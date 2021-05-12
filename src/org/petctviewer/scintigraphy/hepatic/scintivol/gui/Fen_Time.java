package org.petctviewer.scintigraphy.hepatic.scintivol.gui;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.hepatic.scintivol.Model_Scintivol;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Fen_Time extends JDialog  {
    private JSpinner time;
    private final JButton btn_ok;

    public Fen_Time(){
    this.setTitle("Time");
        JPanel container = new JPanel(new GridLayout(3, 1, 0, 10));
        container.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        container.add(new JLabel("Adjust the time"));


        //creation du panel du bas
        JPanel input = new JPanel(new GridLayout(1, 2, 15, 0));
        SpinnerModel model = new SpinnerNumberModel(10, 1, 30, 0.1);
        this.time = new JSpinner(model);
        input.add(this.time);
        input.add(new JLabel("min"));
        container.add(input);

        this.btn_ok = new JButton("Ok");
        this.btn_ok.addActionListener(e -> this.dispose());
        container.add(this.btn_ok);

        this.add(container);

        this.pack();
        this.setVisible(true);
    }


}
