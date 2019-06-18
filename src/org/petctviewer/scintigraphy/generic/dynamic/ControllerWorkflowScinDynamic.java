
package org.petctviewer.scintigraphy.generic.dynamic;

import ij.gui.Roi;
import org.petctviewer.scintigraphy.generic.statics.FenApplication_ScinStatic;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawLoopInstruction;
import org.petctviewer.scintigraphy.scin.instructions.generator.DefaultGenerator;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ControllerWorkflowScinDynamic extends ControllerWorkflow {

	private FenResults fenResult;

	public ControllerWorkflowScinDynamic(Scintigraphy main, FenApplicationWorkflow vue, ModelScin model) {
		super(main, vue, model);

		this.generateInstructions();
		this.start();

		this.fenResult = new FenResults(this);
		
		
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[1];
		DefaultGenerator dri_1;
		ImageState state; 
		if(((Model_GeneralDyn) model).getImpAnt() != null) {
			state = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);
			if(((Model_GeneralDyn) model).getImpPost() != null)
				Library_Gui.setOverlayTitle("Inverted Post", this.vue.getImagePlus(), Color.yellow, 2);
		}
		else
			state = new ImageState(Orientation.POST, 1, true, ImageState.ID_NONE);

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		dri_1 = new DrawLoopInstruction(this.workflows[0], state);

		this.workflows[0].addInstructionOnTheFly(dri_1);

		this.workflows[0].addInstruction(new EndInstruction());
	}

	@Override
	public void end() {
		Library_Gui.initOverlay(this.vue.getImagePlus());
		if(((Model_GeneralDyn) model).getImpAnt() != null) {
			Library_Gui.setOverlayTitle("Ant", this.vue.getImagePlus(), Color.yellow, 1);
			if(((Model_GeneralDyn) model).getImpPost() != null)
				Library_Gui.setOverlayTitle("Inverted Post", this.vue.getImagePlus(), Color.yellow, 2);
			
		}
		else
			Library_Gui.setOverlayTitle("Inverted Post", this.vue.getImagePlus(), Color.yellow, 1);

		Library_Gui.setOverlayDG(this.vue.getImagePlus(),Color.yellow);
		
		for (Roi roi : this.model.getRoiManager().getRoisAsArray()) {
			roi.setPosition(0);
			this.vue.getImagePlus().getOverlay().add(roi);
		}

		GeneralDynamicScintigraphy scindyn = (GeneralDynamicScintigraphy) this.main;

		BufferedImage capture;

		String[] roiNames = ((Model_GeneralDyn) this.model).getRoiNames();

		FenGroup_GeneralDyn fenGroup = new FenGroup_GeneralDyn(roiNames);
		fenGroup.setModal(true);
		fenGroup.setLocationRelativeTo(this.vue);
		fenGroup.setVisible(true);
		String[][] asso = fenGroup.getAssociation();

		this.fenResult = new FenResultat_GeneralDyn(this, asso);

		if (scindyn.getImpAnt() != null) {
			this.vue.getImagePlus().setSlice(1);
			capture = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512, 0).getBufferedImage();
			((Model_GeneralDyn) this.model).saveValues(((Model_GeneralDyn) this.model).getImpAnt().getImagePlus());
			this.fenResult.addTab(new TabAntPost(capture, "Ant", this.fenResult));
		}

		if (scindyn.getImpPost() != null) {

			this.vue.getImagePlus().setSlice(2);

			BufferedImage c = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512, 0).getBufferedImage();

			((Model_GeneralDyn) this.model).saveValues(((Model_GeneralDyn) this.model).getImpPost().getImagePlus());
			ControllerWorkflowScinDynamic.this.fenResult
					.addTab(new TabAntPost(c, "Post", ControllerWorkflowScinDynamic.this.fenResult));

			// ControllerWorkflowScinDynamic.this.finishDrawingResultWindow();

		}

		this.fenResult.setVisible(true);
	}

	private void setOverlayTitleLaterisationAndRoi() {
		vue.getImagePlus().getOverlay().clear();

		if (((Model_GeneralDyn) model).getImpAnt() != null && ((Model_GeneralDyn) model).getImpPost() != null) {
			Library_Gui.setOverlayTitle("Ant", vue.getImagePlus(), Color.YELLOW, 1);
			Library_Gui.setOverlayTitle("Inverted Post", vue.getImagePlus(), Color.YELLOW, 2);
		} else if (((Model_GeneralDyn) model).getImpAnt() != null) Library_Gui.setOverlayTitle("Ant",
																							   vue.getImagePlus(),
																							   Color.YELLOW, 1);
		else Library_Gui.setOverlayTitle("Post", vue.getImagePlus(), Color.YELLOW, 1);

		Library_Gui.setOverlayDG(vue.getImagePlus(), Color.yellow);

		for (int indexCurrentRoi = 0; indexCurrentRoi < this.indexRoi; indexCurrentRoi++) {
			Roi roi = this.getRoiManager().getRoi(indexCurrentRoi);
			roi.setPosition(0);
			this.getVue().getImagePlus().getOverlay().add(roi);
		}
	}

	private void updateButtonLabel(int indexRoi) {
		// Check ROI is present
		Roi roi = getRoiManager().getRoi(indexRoi);
		if (roi != null) {
			getVue().getBtn_suivant().setLabel(FenApplication_ScinStatic.BTN_TEXT_NEXT);
		} else {
			getVue().getBtn_suivant().setLabel(FenApplication_ScinStatic.BTN_TEXT_NEW_ROI);
		}
	}

	@Override
	public void clickPrevious() {
		super.clickPrevious();

		this.setOverlayTitleLaterisationAndRoi();

		this.updateButtonLabel(this.indexRoi);
	}
	
	@Override
	public void clickNext() {
		boolean sameName = false;
		for (Instruction instruction : this.workflows[this.indexCurrentWorkflow].getInstructions())
			if (instruction instanceof DrawLoopInstruction)
				if (instruction != this.workflows[this.indexCurrentWorkflow]
						.getCurrentInstruction())
					if (this.workflows[this.indexCurrentWorkflow].getController().getVue().getTextfield_instructions()
							.getText().equals(((DrawLoopInstruction) instruction).getInstructionRoiName()))
						sameName = true;
		if (sameName) {
			int retour = JOptionPane.OK_OPTION;
			if (this.model.getRoiManager()
					.getRoi(indexRoi) != null /* && indexRoiToSave > this.model.getRoiManager().getCount() */) {
				retour = JOptionPane.showConfirmDialog(getVue(),
						"A Roi already have this name. Do you want to continue?", "Duplicate Roi Name",
						JOptionPane.YES_NO_OPTION);
			}
			if (retour != JOptionPane.OK_OPTION)
				return;
		}

		super.clickNext();

		this.setOverlayTitleLaterisationAndRoi();

		this.updateButtonLabel(this.indexRoi);
		// TODO: still useful?
		// Update view
		int indexScroll = this.getVue().getInstructionDisplayed();
		getVue().currentInstruction(indexScroll);
	}

}
