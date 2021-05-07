package org.petctviewer.scintigraphy.salivaryGlands;

import ij.ImagePlus;
import ij.gui.Overlay;
import org.petctviewer.scintigraphy.salivaryGlands.gui.FenSelectLemonInjection;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

public class FenApplication_SalivaryGlands extends FenApplicationWorkflow {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final SalivaryGlandsScintigraphy main;
	private boolean dyn;
	private final ImagePlus impProj;
	private final ImageSelection imsProj;
	private final Button btn_start;

	public FenApplication_SalivaryGlands(ImageSelection ims, String nom, SalivaryGlandsScintigraphy main) {
		super(ims, nom);
		// Keep default visualisation
		this.setVisualizationEnable(false);
		
		this.main = main;
		this.imsProj = ims;
		

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

		
		this.impProj = imp;
		
		this.pack();

	}

	@Override
	public void setController(ControllerScin ctrl) {
		super.setController(ctrl);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.btn_start) {
			FenSelectLemonInjection dialogFen = new FenSelectLemonInjection();
			dialogFen.setLocationRelativeTo(this);
			dialogFen.setAlwaysOnTop(true);
			dialogFen.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			((ControllerWorkflowSalivaryGlands) this.getController()).setLemonJuiceInjection(dialogFen.getLemonJuiceInjection());

			this.getBtn_reverse().setEnabled(true);
			this.getPanel_bttns_droit().removeAll();
			this.getPanel_bttns_droit().add(this.createPanelInstructionsBtns());
			this.getBtn_drawROI().setEnabled(true);
			this.setAlwaysOnTop(false);
			this.setImage(impProj);
			this.updateSliceSelector();
			resizeCanvas();
		}

		super.actionPerformed(e);
	}
}