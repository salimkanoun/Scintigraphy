package org.petctviewer.scintigraphy.hepatic.scintivol.gui;

import org.petctviewer.scintigraphy.hepatic.scintivol.Model_Scintivol;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class TabTomo extends TabResult implements ActionListener {
    private JTextField ftTextField;
    private JTextField ffrTextField;
    private JButton computeBtn;
    public TabTomo(FenResults parent, String title) {
        super(parent, title, true);
        this.reloadDisplay();
    }

    @Override
    public Component getSidePanelContent() {
        return null;
    }

    @Override
    public Container getResultContent() {
        JPanel res = new JPanel(new GridLayout(2, 1));


        res.add(this.getInputContent());
        res.add(this.getResultPanelContent());

        return res;
    }

    private Container getInputContent() {
        JPanel res = new JPanel();
        JPanel container = new JPanel(new GridLayout(3, 1));
        container.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        JPanel inputFT = new JPanel(new GridLayout(1, 2));
        inputFT.add(new JLabel(" "));

        inputFT.add(new JLabel("Total liver's counts (TL)"));
        inputFT.add(new JLabel(" "));

        this.ftTextField = new JTextField();
        this.ftTextField.addActionListener(this);
        inputFT.add(this.ftTextField);
        inputFT.add(new JLabel(" "));
        container.add(inputFT);

        JPanel inputFFR = new JPanel(new GridLayout(1, 2));
        inputFFR.add(new JLabel(" "));

        inputFFR.add(new JLabel("Future remnant liver's counts (FRL)  "));
        inputFFR.add(new JLabel(" "));

        this.ffrTextField = new JTextField();
        this.ffrTextField.addActionListener(this);
        inputFFR.add(this.ffrTextField);
        inputFFR.add(new JLabel(" "));

        container.add(inputFFR);

        JPanel compute = new JPanel();
        this.computeBtn = new JButton("Compute");
        this.computeBtn.addActionListener(this);
        compute.add(this.computeBtn);
        container.add(compute);

        res.add(container);

        return res;
    }

    private Container getResultPanelContent() {
        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();

        JPanel res = new JPanel();
        if (!model.getResults().containsKey("Tomo"))
            return res;

        Map<String, Double> results = model.getResults().get("Intermediate values");
        JPanel container = new JPanel(new GridLayout(3, 3,0,3));
        container.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1));

        container.add(new JLabel("FRL/TL ratio : "));
        container.add(new JLabel(" "));
        container.add(new JLabel( Library_Quantif.round(results.get("FFR/FT") * 100, 2) +" %"));

        container.add(new JLabel("Clearance FRL : "));
        container.add(new JLabel(" "));

        container.add(new JLabel(Library_Quantif.round(results.get("Clairance FFR"), 2) +" %/min"));

        container.add(new JLabel("Normalized Clearance FRL : "));
        container.add(new JLabel(" "));

        container.add(new JLabel(Library_Quantif.round(results.get("Norm Clairance FFR"), 2) +" %/min.m²"));
        res.add(container);

        return res;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        if (e.getSource() == this.computeBtn) {
            Map<String, Double> tomo = new HashMap<>();
            tomo.put("FT", Double.valueOf(this.ftTextField.getText()));
            tomo.put("FFR", Double.valueOf(this.ffrTextField.getText()));
            model.setTomo(tomo);
            model.calculateResults();
            this.reloadDisplay();
        }
    }
}
