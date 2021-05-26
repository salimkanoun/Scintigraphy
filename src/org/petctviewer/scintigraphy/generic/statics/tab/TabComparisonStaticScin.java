package org.petctviewer.scintigraphy.generic.statics.tab;

import org.petctviewer.scintigraphy.generic.statics.ModelScinStatic;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

public class TabComparisonStaticScin extends TabResult {
    private final JPanel panAnt;
    private final JPanel panPost;
    private final JPanel panAvgGeo;
    private final JComboBox combo1;
    private final JComboBox combo2;
    private final BufferedImage capture;

    public TabComparisonStaticScin(FenResults parent, String title, BufferedImage capture) {
        super(parent, title, true);

        this.capture = capture;
        this.panAnt = new JPanel();
        this.panPost = new JPanel();
        this.panAvgGeo = new JPanel();

        Roi[] rois = this.getParent().getController().getModel().getRoiManager().getRoisAsArray();
        String[] rois_name = new String[rois.length];
        for (int i = 0; i < rois.length; i++) {
            rois_name[i] = rois[i].getName();
        }
        ControllerComparison ctrl = new ControllerComparison();
        combo1 = new JComboBox<>(rois_name);
        combo1.addActionListener(ctrl);
        combo2 = new JComboBox(rois_name);
        combo2.addActionListener(ctrl);

        this.reloadDisplay();
    }

    @Override
    public Component getSidePanelContent() {
        Box box = new Box(BoxLayout.PAGE_AXIS);

        //Combo
        JPanel selection = new JPanel(new GridLayout(1, 5));
        selection.add(new JLabel("ROI 1: "));
        selection.add(combo1);
        selection.add(Box.createHorizontalStrut(5));
        selection.add(new JLabel("ROI 2: "));
        selection.add(combo2);
        box.add(selection);

        //Tables
        if (panAnt != null)
            box.add(panAnt);
        if (panPost != null)
            box.add(panPost);
        if (panAvgGeo != null)
            box.add(panAvgGeo);
        return box;
    }

    private Object[][] getRatioSelectedCombos(Object[][] data) {
        Object[][] res = new Object[1][data[0].length];
        Object[] c1 = null, c2 = null;

        for (Object[] d : data) {
            if (d[0].equals(combo1.getSelectedItem()))
                c1 = d;
            if (d[0].equals(combo2.getSelectedItem()))
                c2 = d;
        }

        res[0][0] = "Ratio " + c1[0] + "/" + c2[0];
        for (int i = 1; i < c1.length; i++) {
            res[0][i] = Library_Quantif.round((Double) c1[i] / (Double) c2[i], 2);
        }
        return res;
    }

    public void addAntTab(Object[][] data) {
        this.panAnt.removeAll();
        JTable table;
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        String[] title = { "Name", "Count", "Avg", "Std" };
        table = new JTable(data, title);

        p.add(table.getTableHeader(), BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);

        JPanel fixedSize = new JPanel(new FlowLayout());
        fixedSize.add(p);

        this.panAnt.add(new JLabel("Ant"));
        this.panAnt.add(fixedSize);

        this.reloadDisplay();
    }

    public void addPostTab(Object[][] data) {
        this.panPost.removeAll();
        JTable table;
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        String[] title = { "Name", "Count", "Avg", "Std" };
        table = new JTable(data, title);

        p.add(table.getTableHeader(), BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);

        JPanel fixedSize = new JPanel(new FlowLayout());
        fixedSize.add(p);

        this.panPost.add(new JLabel("Post"));
        this.panPost.add(fixedSize);

        this.reloadDisplay();
    }

    public void addMoyGeomTab(Object[][] data) {
        this.panAvgGeo.removeAll();
        JTable table;
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        String[] title = { "Name", "Geom Mean" };
        table = new JTable(data, title);

        p.add(table.getTableHeader(), BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);

        JPanel fixedSize = new JPanel(new FlowLayout());
        fixedSize.add(p);

        this.panAvgGeo.add(new JLabel("Geom Mean"));
        this.panAvgGeo.add(fixedSize);

        this.reloadDisplay();
    }

    @Override
    public Container getResultContent() {
        return new DynamicImage(capture);
    }

    private class ControllerComparison implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ModelScinStatic model = (ModelScinStatic) getParent().getController().getModel();
            if(!model.isSingleSlice() || model.isAnt())
                addAntTab(getRatioSelectedCombos(model.calculerTableauAnt()));
            if( !model.isSingleSlice() || !model.isAnt())
                addPostTab(getRatioSelectedCombos(model.calculerTableauPost()));
            if(!model.isSingleSlice()) {
                Object[][] a = model.calculerTaleauMoyGeom();
                Object[][] b = getRatioSelectedCombos(a);
                addMoyGeomTab(b);
            }
        }
    }
}
