package org.petctviewer.scintigraphy.hepatic.scintivol;

import ij.gui.Overlay;
import ij.util.DicomTools;
import org.apache.commons.lang.StringUtils;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FenApplication_Scintivol extends FenApplicationWorkflow {


    private static final long serialVersionUID = 1L;
    private final ScintivolScintigraphy main;
    private final Button btn_start;

    public FenApplication_Scintivol(ImageSelection ims, String nom, ScintivolScintigraphy main) {
        super(ims, nom);
        // Keep default visualisation
        this.setVisualizationEnable(false);

        this.main = main;



        Overlay overlay = Library_Gui.initOverlay(this.getImagePlus(), 12);
        this.getImagePlus().setOverlay(overlay);
        Library_Gui.setOverlayDG(this.getImagePlus(), Color.YELLOW);

        btn_start = new Button("Start");
        btn_start.addActionListener(this);

        this.getPanel_bttns_droit().removeAll();
        this.getPanel_bttns_droit().add(btn_start);

        this.getBtn_drawROI().setEnabled(false);
        this.getBtn_reverse().setEnabled(false);


        this.setDefaultSize();

        this.pack();

    }

    @Override
    public void setController(ControllerScin ctrl) {
        super.setController(ctrl);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.btn_start) {
            double size, weight;
            if (!StringUtils.isEmpty(DicomTools.getTag(imp, "0010,1020")))
                size = Double.parseDouble(DicomTools.getTag(imp, "0010,1020"));
            else
                size = Double.parseDouble(JOptionPane.showInputDialog(this,
                        "Input the patient size in cm", 160));

            if (!StringUtils.isEmpty(DicomTools.getTag(imp, "0010,1030")))
                weight = Double.parseDouble(DicomTools.getTag(imp, "0010,1030"));
            else
                weight = Double.parseDouble(JOptionPane.showInputDialog(this,
                        "Input the patient weight in kg", 70));


            ((Model_Scintivol) this.getController().getModel()).setSize(size);
            ((Model_Scintivol) this.getController().getModel()).setWeight(weight);

            this.getBtn_reverse().setEnabled(true);
            this.getPanel_bttns_droit().removeAll();
            this.getPanel_bttns_droit().add(this.createPanelInstructionsBtns());
            this.getBtn_drawROI().setEnabled(true);
            this.setAlwaysOnTop(false);
            this.setImage(this.imp);
            this.updateSliceSelector();
            resizeCanvas();
        }

        super.actionPerformed(e);
    }
}
