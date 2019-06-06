
package org.petctviewer.scintigraphy.generic.statics;

import ij.ImagePlus;
import ij.gui.Roi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
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

import javax.swing.*;

public class ControllerWorkflow_ScinStatic extends ControllerWorkflow {

	private FenResults fenResult;

	public ControllerWorkflow_ScinStatic(Scintigraphy main, FenApplicationWorkflow vue, ImageSelection[] selectedImages,
			String studyName) {
		super(main, vue, new ModelScinStatic(selectedImages, studyName));

		ImageState statePost = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, ImageState.ID_WORKFLOW);
		setOverlay(statePost);

		this.generateInstructions();
		this.start();

		this.fenResult = new FenResults(this);
		this.fenResult.setVisible(false);
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

	public String getNomOrgane(int index) {
		return this.vue.getTextfield_instructions().getText();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[1];
		DefaultGenerator dri_1;

		ImageState state;
		if (((ModelScinStatic) this.getModel()).getIsAnt())
			state = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);
		else
			state = new ImageState(Orientation.POST, 1, false, ImageState.ID_NONE);

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		dri_1 = new DrawLoopInstruction(this.workflows[0], state);

		this.workflows[0].addInstructionOnTheFly(dri_1);

		this.workflows[0].addInstruction(new EndInstruction());

		// Update view
		getVue().setNbInstructions(this.allInputInstructions().size());
	}

	public void end() {
		ImagePlus imp = this.model.getImagePlus();

		// pour la ant
		imp.setSlice(1);

		if (!((ModelScinStatic) this.getModel()).getIsSingleSlide() || ((ModelScinStatic) this.getModel()).getIsAnt()) {
			for (int i = 0; i < this.model.getRoiManager().getCount(); i++) {
				Roi roi = this.model.getRoiManager().getRoi(i);
				imp.setRoi(roi);
				((ModelScinStatic) this.model).enregistrerMesureAnt(roi.getName(), imp);
			}
		}
		// pour la post
		if (!((ModelScinStatic) this.getModel()).getIsSingleSlide())
			imp.setSlice(2);

		if (!((ModelScinStatic) this.getModel()).getIsSingleSlide()
				|| !((ModelScinStatic) this.getModel()).getIsAnt()) {
			for (int i = 0; i < this.model.getRoiManager().getCount(); i++) {
				Roi roi = this.model.getRoiManager().getRoi(i);
				imp.setRoi(roi);
				((ModelScinStatic) this.model).enregistrerMesurePost(roi.getName(), imp);
			}
		}

		Thread t = new DoubleImageThread("test", this.main, this.model);
		t.start();

	}

	@Override
	public void clickNext() {
		boolean sameName = false;
		for (Instruction instruction : this.workflows[this.indexCurrentWorkflow].getInstructions())
			if (instruction instanceof DrawLoopInstruction)
				if (instruction != this.workflows[this.indexCurrentWorkflow].getCurrentInstruction())
					if (this.workflows[this.indexCurrentWorkflow].getController().getVue().getTextfield_instructions()
							.getText().equals(((DrawLoopInstruction) instruction).getInstructionRoiName()))
						sameName = true;
		if (sameName && getVue().getImage().getImagePlus().getRoi() != null) {
			int result;
			result = JOptionPane.showConfirmDialog(getVue(), "A Roi already have this name. Do you want to continue ?",
					"Duplicate Roi Name", JOptionPane.YES_NO_CANCEL_OPTION);

			if (result != JOptionPane.OK_OPTION)
				return;
		}

		this.updateButtonLabel(this.indexRoi);

		super.clickNext();

		// TODO: still useful?
		// Update view
		int indexScroll = this.getVue().getInstructionDisplayed();
		getVue().currentInstruction(indexScroll);
	}

	@Override
	public void clickPrevious() {
		super.clickPrevious();

		this.updateButtonLabel(this.indexRoi);
	}

}
