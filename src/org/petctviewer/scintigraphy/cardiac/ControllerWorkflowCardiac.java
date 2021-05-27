package org.petctviewer.scintigraphy.cardiac;

import ij.ImagePlus;
import org.apache.commons.lang.ArrayUtils;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiBackgroundSymmetrical;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalLoopInstruction;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalRoiInstruction.Organ;
import org.petctviewer.scintigraphy.scin.instructions.execution.ContaminationAskInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControllerWorkflowCardiac extends ControllerWorkflow {

	/**
	 * This command signals that the instruction should not generate a next
	 * instruction.<br>
	 * This is only used for {@link ControllerWorkflowCardiac}.
	 */
	public static final String COMMAND_CONTINUE = "command.continue";

	// private int nbConta1;
	// private int nbConta2;
	public List<ImagePlus> captures;
	private boolean finContSlice1, finContSlice2;
	private String[] organes = { "Bladder", "Kidney R", "Kidney L", "Heart", "Bkg noise" };
	private final List<String> roiList;
	private final int onlyThoraxImage;

	private final int fullBodyImages;

	public static String simpleName = "ControllerWorkflowCardiac";

	public ControllerWorkflowCardiac(FenApplicationWorkflow vue, ModelScin model, int fullBodyImages,
									 int onlyThoraxImage) {
		super(vue, model);

		this.fullBodyImages = fullBodyImages;
		this.onlyThoraxImage = onlyThoraxImage;

		this.roiList = new ArrayList<>();

		((Model_Cardiac) this.model).setFullBodyImages(fullBodyImages);
		((Model_Cardiac) this.model).setOnlyThoraxImage(onlyThoraxImage);

		// on declare si il y a deux prises
		((Model_Cardiac) this.model).setDeuxPrise(this.fullBodyImages > 1);

		((Model_Cardiac) this.model).calculerMoyGeomTotale();

		this.finContSlice1 = false;

		this.captures = new ArrayList<>();

		this.generateInstructions();
		this.start();
	}

	private void clicNewCont() {

		if (this.getVue().getImagePlus().getRoi() != null && !this.finContSlice2 && this.fullBodyImages > 0) {
			System.out.println();
			if (this.workflows[indexCurrentWorkflow].getCurrentInstructionIndex() % 2 != 0) {
				FenApplication_Cardiac fac = (FenApplication_Cardiac) this.getVue();
				fac.getBtn_continue().setEnabled(false);
				fac.getBtn_suivant().setLabel("Next");
			} else {
				FenApplication_Cardiac fac = (FenApplication_Cardiac) this.getVue();
				fac.getBtn_continue().setEnabled(true);
				fac.getBtn_suivant().setLabel("Save");
			}
		}
	}

	private void checkPanelInstruction_BtnRight() {
		// if (this.workflows[indexCurrentWorkflow].getCurrentInstruction() ==
		// this.workflows[indexCurrentWorkflow]
		// .getInstructions().get(0))
		if (this.workflows[indexCurrentWorkflow].getInstructions().get(0) instanceof ContaminationAskInstruction)
			if (this.workflows[indexCurrentWorkflow].getInstructions().size() > 1)
				if (((DrawSymmetricalLoopInstruction) this.workflows[indexCurrentWorkflow].getInstructionAt(
						this.workflows[indexCurrentWorkflow].getInstructions().size() - 1)).isStopped()) {
					((FenApplication_Cardiac) this.getVue()).stopContaminationMode();
				} else if (this.workflows[indexCurrentWorkflow].getInstructions().size() > 2)
					if (this.workflows[indexCurrentWorkflow].getCurrentInstruction() ==
							this.workflows[indexCurrentWorkflow].getInstructions().get(2))
						((FenApplication_Cardiac) this.getVue()).startContaminationMode();

	}

	public void clicEndCont() {

		// on set la slice
		if ((this.fullBodyImages > 1 && !finContSlice1))
			this.finContSlice1 = true;

		else if (this.fullBodyImages > 1 && finContSlice1) {
			// on a trait� toutes les contaminations
			((FenApplication_Cardiac) this.getVue()).stopContaminationMode();
			String[] conts = new String[this.position];
			Arrays.fill(conts, "Cont");

			// on ajoute de nouvelles cases dans le tableau organes pour ne pas modifier
			// l'indexRoi
			this.setOrganes((String[]) ArrayUtils.addAll(conts, this.getOrganes()));
			this.endContamination();
		}

		if (this.workflows[indexCurrentWorkflow].getCurrentInstruction() instanceof DrawSymmetricalLoopInstruction)
			((DrawSymmetricalLoopInstruction) this.workflows[this.indexCurrentWorkflow].getCurrentInstruction()).stop();

		this.getVue().getBtn_suivant().setLabel("Next");
		this.clickNext();

		this.vue.pack();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		System.out.println("finContSlice1 : " + this.finContSlice1);
		System.out.println("finContSlice2 : " + this.finContSlice2);

		if (arg0.getSource() instanceof Button) {
			Button b = (Button) arg0.getSource();
			if (b == this.getVue().getBtn_suivant()) {
				this.clicNewCont();
				this.checkPanelInstruction_BtnRight();
			} else if (b == ((FenApplication_Cardiac) this.getVue()).getBtn_continue()) {
				this.clicEndCont();
				((FenApplication_Cardiac) this.getVue()).stopContaminationMode();
			} else if (b == this.getVue().getBtn_precedent())
				if (this.workflows[indexCurrentWorkflow].getCurrentInstruction() instanceof DrawSymmetricalLoopInstruction) {
					this.checkPanelInstruction_BtnRight();
				}

		}
		super.actionPerformed(arg0);
	}

	@Override
	public void end() {
		super.end();

		((Model_Cardiac) this.model).getResults();
		this.model.calculateResults();

		FenResultat_Cardiac fen = new FenResultat_Cardiac(captures, this, this.fullBodyImages, this.onlyThoraxImage);
		fen.setVisible(true);
	}

	@Override
	public void setOverlay(ImageState state) throws IllegalArgumentException {

		if (this.indexCurrentWorkflow <= this.fullBodyImages) {
			((FenApplication_Cardiac) this.getVue()).setMultipleTitle(Color.yellow, state.getSlice());
		} else {
			Library_Gui.setOverlayDG(this.vue.getImagePlus(), Color.YELLOW);
			Library_Gui.setOverlayTitle("Ant", this.vue.getImagePlus(), Color.YELLOW, 1);
		}
		this.updateROIColor();
	}

	public String[] getOrganes() {
		return this.organes;
	}

	public void setOrganes(String[] organes) {
		this.organes = organes;
	}

	public int getImageNumberByRoiIndex() {
		// changement de slice si la prise contient une precoce
		if (this.fullBodyImages > 1) {
			if (this.finContSlice1) {
				return 1;
			}
		}
		return 0;
	}

	@Override
	protected void generateInstructions() {
		int nbWorkflow = this.getModel().getImageSelection().length + 1;
		if (this.fullBodyImages == 0)
			nbWorkflow = 1;

		this.workflows = new Workflow[nbWorkflow];

		int index = 0, indexCapture = 0;

		ImageState state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_WORKFLOW);

		if (this.fullBodyImages > 0) {
			this.workflows = new Workflow[this.getModel().getImageSelection().length + 1];
			this.workflows[index] = new Workflow(this, this.model.getImageSelection()[index]);
			this.workflows[index]
					.addInstruction(new ContaminationAskInstruction(this.workflows[index], state, "ContE", 0));
			index++;

			if (this.fullBodyImages > 1) {
				this.workflows[index] = new Workflow(this, this.model.getImageSelection()[index]);
				this.workflows[index]
						.addInstruction(new ContaminationAskInstruction(this.workflows[index], state, "ContL", 1));
				index++;
			}

			// Create +1 workflow for full body image
			this.workflows[index] = new Workflow(this, this.model.getImageSelection()[this.fullBodyImages - 1]);
			// Organs to delimit
			DrawRoiInstruction dri_3 = new DrawSymmetricalRoiInstruction("Bladder", state, null, null,
					this.workflows[index], Organ.DEMIE, false);
			this.roiList.add("Bladder A");
			DrawRoiInstruction driBackground_1 = new DrawRoiBackgroundSymmetrical("Bladder Background", state, dri_3,
					this.workflows[index], "");
			this.roiList.add("Bladder Background A");
			DrawRoiInstruction dri_4 = new DrawSymmetricalRoiInstruction("Bladder", state, dri_3, null,
					this.workflows[index], Organ.DEMIE, false);
			this.roiList.add("Bladder P");
			DrawRoiInstruction driBackground_2 = new DrawRoiBackgroundSymmetrical("Bladder Background", state, dri_4,
					this.workflows[index], "");
			this.roiList.add("Bladder Background P");


			DrawRoiInstruction dri_5 = new DrawSymmetricalRoiInstruction("Kidney R", state, null, null,
					this.workflows[index], Organ.DEMIE, false);
			this.roiList.add("Kidney R A");
			DrawRoiInstruction driBackground_3 = new DrawRoiBackgroundSymmetrical("Kidney R Background", state, dri_5,
					this.workflows[index], "");
			this.roiList.add("Kidney R Background A");
			DrawRoiInstruction dri_6 = new DrawSymmetricalRoiInstruction("Kidney R", state, dri_5, null,
					this.workflows[index], Organ.DEMIE, false);
			this.roiList.add("Kidney R P");
			DrawRoiInstruction driBackground_4 = new DrawRoiBackgroundSymmetrical("Kidney R Background", state, dri_6,
					this.workflows[index], "");
			this.roiList.add("Kidney R Background P");


			DrawRoiInstruction dri_7 = new DrawSymmetricalRoiInstruction("Kidney L", state, null, null,
					this.workflows[index], Organ.DEMIE, false);
			this.roiList.add("Kidney L A");
			DrawRoiInstruction driBackground_5 = new DrawRoiBackgroundSymmetrical("Kidney L Background", state, dri_7,
					this.workflows[index], "");
			this.roiList.add("Kidney L Background A");
			DrawRoiInstruction dri_8 = new DrawSymmetricalRoiInstruction("Kidney L", state, dri_7, null,
					this.workflows[index], Organ.DEMIE, false);
			this.roiList.add("Kidney L P");
			DrawRoiInstruction driBackground_6 = new DrawRoiBackgroundSymmetrical("Kidney L Background", state, dri_8,
					this.workflows[index], "");
			this.roiList.add("Kidney L Background P");


			DrawRoiInstruction dri_9 = new DrawSymmetricalRoiInstruction("Heart", state, null, null,
					this.workflows[index], Organ.DEMIE);
			this.roiList.add("Heart A");
			DrawRoiInstruction dri_10 = new DrawSymmetricalRoiInstruction("Bkg noise", state, dri_9, "",
					this.workflows[index], Organ.QUART);
			this.roiList.add("Bkg noise A");
			DrawRoiInstruction dri_11 = new DrawSymmetricalRoiInstruction("Heart", state, dri_9, null,
					this.workflows[index], Organ.DEMIE, false);
			this.roiList.add("Heart P");
			DrawRoiInstruction dri_12 = new DrawSymmetricalRoiInstruction("Bkg noise", state, dri_10, "",
					this.workflows[index], Organ.DEMIE, false);
			this.roiList.add("Bkg noise P");

			this.workflows[index].addInstruction(dri_3);
			this.workflows[index].addInstruction(driBackground_1);
			this.workflows[index].addInstruction(dri_4);
			this.workflows[index].addInstruction(driBackground_2);
			this.workflows[index].addInstruction(dri_5);
			this.workflows[index].addInstruction(driBackground_3);
			this.workflows[index].addInstruction(dri_6);
			this.workflows[index].addInstruction(driBackground_4);
			this.workflows[index].addInstruction(dri_7);
			this.workflows[index].addInstruction(driBackground_5);
			this.workflows[index].addInstruction(dri_8);
			this.workflows[index].addInstruction(driBackground_6);
			this.workflows[index].addInstruction(dri_9);
			this.workflows[index].addInstruction(dri_10);
			this.workflows[index].addInstruction(dri_11);
			this.workflows[index].addInstruction(dri_12);
			this.workflows[index].addInstruction(new ScreenShotInstruction(this.captures, this.getVue(), indexCapture++));
			index++;
		}

		// Thorax image
		if (this.onlyThoraxImage > 0) {
			this.workflows[index] = new Workflow(this, this.model.getImageSelection()[this.fullBodyImages]);

			ScreenShotInstruction captureThorax = new ScreenShotInstruction(this.captures, this.getVue(),
					indexCapture++);

			// Organs to delimit
			DrawRoiInstruction dri_onlyThorax1 = new DrawRoiInstruction("Heart Thorax A", state);
			this.roiList.add("Heart Thorax A");
			DrawRoiInstruction dri_onlyThorax2 = new DrawSymmetricalRoiInstruction("CL Thorax", state, dri_onlyThorax1,
					null, this.workflows[index], Organ.QUART);
			this.roiList.add("CL Thorax A");

			this.workflows[index].addInstruction(dri_onlyThorax1);
			this.workflows[index].addInstruction(dri_onlyThorax2);
			this.workflows[index].addInstruction(captureThorax);
		}

		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());
	}

	public void endContamination() {
		if (this.finContSlice1) this.finContSlice2 = true;
		this.finContSlice1 = true;
	}

	public int getFullBodyImagesCount() {
		return this.fullBodyImages;
	}

	public List<String> getRoiList() {
		return roiList;
	}
}