package org.petctviewer.scintigraphy.generic.statics.tab;

import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TabMainStaticScin  extends TabResult {
    private final JPanel panAnt;
    private final JPanel panPost;
    private final JPanel panAvgGeo;
    private final BufferedImage capture;

    public TabMainStaticScin(FenResults parent, String title, BufferedImage capture) {
        super(parent, title, true);
        this.setAdditionalInfo("cc");

        this.capture = capture;
        this.panAnt = new JPanel();
        this.panPost = new JPanel();
        this.panAvgGeo = new JPanel();

        this.reloadDisplay();
    }

    @Override
    public Component getSidePanelContent() {
        Box box = new Box(BoxLayout.PAGE_AXIS);
        if (panAnt != null)
            box.add(panAnt);
        if (panPost != null)
            box.add(panPost);
        if (panAvgGeo != null)
            box.add(panAvgGeo);
        return box;
    }

    public void addAntTab(Object[][] data) {
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
    public JPanel getResultContent() {
        return new DynamicImage(capture);
    }

}