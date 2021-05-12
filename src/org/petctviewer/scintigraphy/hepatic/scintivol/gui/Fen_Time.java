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

public class Fen_Time extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private final JButton btn_ok;
    private final JValueSetter jvaluesetter;
    private final Model_Scintivol model;
    private JValueSetter debut;
    private JValueSetter fin;

    public Fen_Time(ChartPanel cp, Component parentComponent, Model_Scintivol model){
        super();
        this.model = model;

        //creation du panel du bas
        this.btn_ok = new JButton("Ok");
        this.btn_ok.addActionListener(this);

        this.setLayout(new BorderLayout());
        this.setLayout(new BorderLayout());
        this.setTitle("Adjust the time");

        //cr&ation des Jvalues setters
        this.jvaluesetter = prepareValueSetters(cp);
        this.add(jvaluesetter, BorderLayout.CENTER);
        this.pack();
        this.setLocationRelativeTo(parentComponent);

    }

    private JValueSetter prepareValueSetters(ChartPanel cp) {
        XYPlot plot = cp.getChart().getXYPlot();
        cp.getChart().getPlot().setBackgroundPaint(null);
        JValueSetter jvs = new JValueSetter(cp.getChart());

        Selector start = new Selector("", 1, -1, RectangleAnchor.TOP_LEFT);
        Selector end = new Selector("",3,-1,RectangleAnchor.BOTTOM_RIGHT);

        jvs.addSelector(start, "start");
        jvs.addSelector(start, "end");

        XYSeriesCollection dataset = ((XYSeriesCollection) cp.getChart().getXYPlot().getDataset());
        //dataset.getSeries("Final liver").setKey("Liver");
        //dataset.getSeries("Final heart").setKey("Heart");

        return jvs;
    }

    private void clickOk(){
        this.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent arg0){
            JButton b = (JButton) arg0.getSource();
            if (b == this.btn_ok){
                this.clickOk();
            }
    }
    public JValueSetter getValueSetter() {
        return this.jvaluesetter;
    }


}
