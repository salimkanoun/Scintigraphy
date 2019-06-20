
package org.petctviewer.scintigraphy.generic.statics;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.MontageMaker;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
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

import javax.swing.*;
import java.awt.*;

public class ControllerWorkflow_ScinStatic extends ControllerWorkflow {

	private FenResults fenResult;

	public ControllerWorkflow_ScinStatic(FenApplicationWorkflow vue, ImageSelection[] selectedImages,
										 String studyName) {
		super(vue, new ModelScinStatic(selectedImages, studyName));

		this.generateInstructions();
		this.start();

		this.setOverlayTitleLaterisationAndRoi();

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
		if (((ModelScinStatic) this.getModel()).isAnt())
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
		super.end();
		ImagePlus imp = this.model.getImagePlus();

		// pour la ant
		imp.setSlice(1);

		if (!((ModelScinStatic) this.getModel()).isSingleSlice() || ((ModelScinStatic) this.getModel()).isAnt()) {
			for (int i = 0; i < this.model.getRoiManager().getCount(); i++) {
				Roi roi = this.model.getRoiManager().getRoi(i);
				imp.setRoi(roi);
				((ModelScinStatic) this.model).enregistrerMesureAnt(roi.getName(), imp);
			}
		}
		// pour la post
		if (!((ModelScinStatic) this.getModel()).isSingleSlice())
			imp.setSlice(2);

		if (!((ModelScinStatic) this.getModel()).isSingleSlice() || !((ModelScinStatic) this.getModel()).isAnt()) {
			for (int i = 0; i < this.model.getRoiManager().getCount(); i++) {
				Roi roi = this.model.getRoiManager().getRoi(i);
				imp.setRoi(roi);
				((ModelScinStatic) this.model).enregistrerMesurePost(roi.getName(), imp);
			}
		}

		FenResults fenResults = new FenResultat_ScinStatic(this.takeCapture().getBufferedImage(), this);
		fenResults.setVisible(true);

	}

	private void setOverlayTitleLaterisationAndRoi() {
		vue.getImagePlus().getOverlay().clear();

		if (!((ModelScinStatic) model).isSingleSlice()) {
			Library_Gui.setOverlayTitle("Ant", vue.getImagePlus(), Color.YELLOW, 1);
			Library_Gui.setOverlayTitle("Inverted Post", vue.getImagePlus(), Color.YELLOW, 2);
		} else if (((ModelScinStatic) model).isAnt()) Library_Gui.setOverlayTitle("Ant", vue.getImagePlus(),
																				  Color.YELLOW, 1);
		else Library_Gui.setOverlayTitle("Post", vue.getImagePlus(), Color.YELLOW, 1);

		Library_Gui.setOverlayDG(vue.getImagePlus(), Color.yellow);

		for (int indexCurrentRoi = 0; indexCurrentRoi < this.indexRoi; indexCurrentRoi++) {
			Roi roi = this.getRoiManager().getRoi(indexCurrentRoi);
			roi.setPosition(0);
			this.getVue().getImagePlus().getOverlay().add(roi);
		}
	}

	@Override
	public void clickPrevious() {
		super.clickPrevious();

		this.setOverlayTitleLaterisationAndRoi();

		this.updateButtonLabel(this.indexRoi);
	}

	/**
	 * Hide the verbose. Just take capture of slice 1 & 2 image if it's an ANT/POST,
	 * or slice 1
	 * 
	 * @return The montage.
	 */
	public ImagePlus takeCapture() {
		int width = 512;

		double ratioCapture =
				this.getVue().getImagePlus().getWidth() * 1.0 / this.getVue().getImagePlus().getHeight() * 1.0;

		ImagePlus[] captures;

		if (!((ModelScinStatic) model).isSingleSlice()) {

			captures = new ImagePlus[2];

			this.getVue().getImagePlus().setSlice(1);
			captures[0] = Library_Capture_CSV.captureImage(this.getVue().getImagePlus(), width,
					(int) (width / ratioCapture));

			/*
			 * Roi to second slice
			 */

			this.getVue().getImagePlus().setSlice(2);

			for (Roi roi : this.getModel().getRoiManager().getRoisAsArray()) {
				this.getVue().getImagePlus().getOverlay().add(roi);
			}

			captures[1] = Library_Capture_CSV.captureImage(this.getVue().getImagePlus(), width,
					(int) (width / ratioCapture));
		} else {
			captures = new ImagePlus[1];
			this.getVue().getImagePlus().setSlice(1);
			captures[0] = Library_Capture_CSV.captureImage(this.getVue().getImagePlus(), width,
					(int) (width / ratioCapture));
		}

		ImageStack stackCapture = Library_Capture_CSV.captureToStack(captures);
		return this.montage(stackCapture, captures.length);
	}

	private ImagePlus montage(ImageStack captures, int nbCapture) {
		MontageMaker mm = new MontageMaker();
		// TODO: patient ID
		String patientID = "NO_ID_FOUND";
		ImagePlus imp = new ImagePlus("Results Pelvis -" + this.model.getStudyName() + " -" + patientID, captures);
		imp = mm.makeMontage2(imp, 1, nbCapture, 0.50, 1, nbCapture, 1, 10, false);
		return imp;
	}

	@Override
	public void clickNext() {
		boolean sameName = false;
		for (Instruction instruction : this.workflows[this.indexCurrentWorkflow].getInstructions())
			if (instruction instanceof DrawLoopInstruction)
				if (instruction != this.workflows[this.indexCurrentWorkflow].getCurrentInstruction())
					if (this.workflows[this.indexCurrentWorkflow].getController().getVue().getTextfield_instructions().getText().equals(
							((DrawLoopInstruction) instruction).getInstructionRoiName())) sameName = true;
		if (sameName && getVue().getImage().getImagePlus().getRoi() != null) {
			int result;
			result = JOptionPane.showConfirmDialog(getVue(), "A Roi already have this name. Do you want to continue ?",
												   "Duplicate Roi Name", JOptionPane.YES_NO_OPTION);

			if (result != JOptionPane.OK_OPTION) return;
		}

		super.clickNext();

		this.updateButtonLabel(this.indexRoi);

		this.setOverlayTitleLaterisationAndRoi();


		// TODO: still useful?
		// Update view
		int indexScroll = this.getVue().getInstructionDisplayed();
		getVue().currentInstruction(indexScroll);
	}
	

}
