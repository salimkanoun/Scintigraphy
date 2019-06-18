package org.petctviewer.scintigraphy.cardiac;

import ij.ImagePlus;
import org.apache.commons.lang.ArrayUtils;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
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
	private boolean finContSlice1;
	private String[] organes = { "Bladder", "Kidney R", "Kidney L", "Heart", "Bkg noise" };
	private boolean finContSlice2;
	private int onlyThoraxImage;

	private int fullBodyImages;

	public static String simpleName = "ControllerWorkflowCardiac";

	public ControllerWorkflowCardiac(Scintigraphy main, FenApplicationWorkflow vue, ModelScin model, int fullBodyImages,
			int onlyThoraxImage) {
		super(main, vue, model);

		this.fullBodyImages = fullBodyImages;
		this.onlyThoraxImage = onlyThoraxImage;

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

	public void clicEndCont() {
		// Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
		// .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		// System.out.println("--------------------------
		// Avant--------------------------");
		// System.out.println(gson.toJson(this.workflows[this.indexCurrentWorkflow])+"\n\n");

		// on set la slice
		if ((this.fullBodyImages > 1 && !finContSlice1)) {
			// on relance le mode decontamination, cette fois ci pour la deuxieme slice
			this.finContSlice1 = true;

		} else { // on a trait� toutes les contaminations
			((FenApplication_Cardiac) this.main.getFenApplication()).stopContaminationMode();
			String[] conts = new String[this.position];
			for (int i = 0; i < conts.length; i++) {
				conts[i] = "Cont";
			}
			// on ajoute de nouvelles cases dans le tableau organes pour ne pas modifier
			// l'indexRoi
			this.setOrganes((String[]) ArrayUtils.addAll(conts, this.getOrganes()));
			this.endContamination();
		}

		// System.out.println("In da clicEndCont :
		// "+this.workflows[indexCurrentWorkflow].getCurrentInstruction());
		if (this.workflows[indexCurrentWorkflow].getCurrentInstruction() instanceof DrawSymmetricalLoopInstruction) {
			((DrawSymmetricalLoopInstruction) this.workflows[this.indexCurrentWorkflow].getCurrentInstruction()).stop();
			// ((DrawSymmetricalLoopInstruction)
			// this.workflows[this.indexCurrentWorkflow].getCurrentInstruction()).setExpectingUserInput(false);
			// this.position++;
		}

		this.getVue().getBtn_suivant().setLabel("Next");
		this.clickNext();

		this.vue.pack();

		// System.out.println("-------------------------- Apres
		// --------------------------");
		// System.out.println(gson.toJson(this.workflows[this.indexCurrentWorkflow])+"\n\n\n");

	}

	private void clicNewCont() {

		if (this.getVue().getImagePlus().getRoi() != null && !this.finContSlice2 && this.fullBodyImages > 0) {
			if (((DrawSymmetricalLoopInstruction) this.workflows[indexCurrentWorkflow].getCurrentInstruction())
					.getIndex() % 2 != 0) {
				FenApplication_Cardiac fac = (FenApplication_Cardiac) this.main.getFenApplication();
				fac.getBtn_continue().setEnabled(false);
				fac.getBtn_suivant().setLabel("Next");
			} else {
				FenApplication_Cardiac fac = (FenApplication_Cardiac) this.main.getFenApplication();
				fac.getBtn_continue().setEnabled(true);
				fac.getBtn_suivant().setLabel("Save");
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		// Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
		// .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		//
		// System.out.println(this.position);
		// System.out.println("-------------------------- Current
		// --------------------------");
		// System.out.println("In da
		// ActionPerformed"+gson.toJson(this.workflows[this.indexCurrentWorkflow])+"\n\n");

		if (arg0.getSource() instanceof Button) {
			Button b = (Button) arg0.getSource();
			if (b == this.getVue().getBtn_suivant())
				this.clicNewCont();
			else if (b == ((FenApplication_Cardiac) this.getVue()).getBtn_continue())
				this.clicEndCont();
			else if (b == this.getVue().getBtn_precedent())
				if (this.workflows[indexCurrentWorkflow]
						.getCurrentInstruction() instanceof ContaminationAskInstruction) {
					this.getVue().getPanel_Instructions_btns_droite().remove(1);
					this.getVue().getPanel_Instructions_btns_droite().add(this.getVue().createPanelInstructionsBtns());
					this.getVue().pack();
				}

		}
		super.actionPerformed(arg0);
		// System.out.println(this.position);
		// if(this.indexCurrentWorkflow - 1 >= 0) {
		// System.out.println(this.workflows[indexCurrentWorkflow -
		// 1].getCurrentInstruction());
		// System.out.println("-------------------------- Previous
		// --------------------------");
		// System.out.println(gson.toJson(this.workflows[this.indexCurrentWorkflow -
		// 1])+"\n\n");
		// }
	}

	@Override
	public void end() {
		super.end();
		// this.saveWorkflow();

		((Model_Cardiac) this.model).getResults();
		this.model.calculateResults();
		// ((Model_Cardiac) this.model).setNbConta(new int[] {this.nbConta1,
		// this.nbConta2});

		FenResultat_Cardiac fen = new FenResultat_Cardiac(captures, this, this.fullBodyImages, this.onlyThoraxImage);
		fen.setVisible(true);
	}

	@Override
	public void setOverlay(ImageState state) throws IllegalArgumentException {

		if (this.indexCurrentWorkflow < this.fullBodyImages) {
			if (state.isLateralisationRL())
				Library_Gui.setOverlayDG(this.vue.getImagePlus(), Color.YELLOW);
			else
				Library_Gui.setOverlayGD(this.vue.getImagePlus(), Color.YELLOW);

			((FenApplication_Cardiac) this.getVue()).setMultipleTitle(Color.yellow, state.getSlice());
		} else {
			Library_Gui.setOverlayDG(this.vue.getImagePlus(), Color.YELLOW);
			Library_Gui.setOverlayTitle("Ant", this.vue.getImagePlus(), Color.YELLOW, 1);
		}

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

		ImageState state = new ImageState(Orientation.ANT, 1, true, ImageState.ID_WORKFLOW);

		if (this.fullBodyImages > 0) {
			this.workflows = new Workflow[this.getModel().getImageSelection().length + 1];
			this.workflows[index] = new Workflow(this, this.model.getImageSelection()[index]);
			this.workflows[index]
					.addInstruction(new ContaminationAskInstruction(this.workflows[index], state, "ContE"));
			index++;

			if (this.fullBodyImages > 1) {
				this.workflows[index] = new Workflow(this, this.model.getImageSelection()[index]);
				this.workflows[index]
						.addInstruction(new ContaminationAskInstruction(this.workflows[index], state, "ContL"));
				index++;
			}

			// Create +1 workflow for full body image
			this.workflows[index] = new Workflow(this, this.model.getImageSelection()[this.fullBodyImages - 1]);
			// Organs to delimit
			DrawRoiInstruction dri_3 = new DrawSymmetricalRoiInstruction("Bladder", state, null, null,
					this.workflows[index], Organ.DEMIE);
			DrawRoiInstruction driBackground_1 = new DrawRoiBackgroundSymmetrical("Bladder Background", state, dri_3,
					this.workflows[index], "");
			DrawRoiInstruction dri_4 = new DrawSymmetricalRoiInstruction("Bladder", state, dri_3, null,
					this.workflows[index], Organ.DEMIE);
			DrawRoiInstruction driBackground_2 = new DrawRoiBackgroundSymmetrical("Bladder Background", state, dri_4,
					this.workflows[index], "");

			DrawRoiInstruction dri_5 = new DrawSymmetricalRoiInstruction("Kidney R", state, null, null,
					this.workflows[index], Organ.DEMIE);
			DrawRoiInstruction driBackground_3 = new DrawRoiBackgroundSymmetrical("Kidney R Background", state, dri_5,
					this.workflows[index], "");
			DrawRoiInstruction dri_6 = new DrawSymmetricalRoiInstruction("Kidney R", state, dri_5, null,
					this.workflows[index], Organ.DEMIE);
			DrawRoiInstruction driBackground_4 = new DrawRoiBackgroundSymmetrical("Kidney R Background", state, dri_6,
					this.workflows[index], "");

			DrawRoiInstruction dri_7 = new DrawSymmetricalRoiInstruction("Kidney L", state, null, null,
					this.workflows[index], Organ.DEMIE);
			DrawRoiInstruction driBackground_5 = new DrawRoiBackgroundSymmetrical("Kidney L Background", state, dri_7,
					this.workflows[index], "");
			DrawRoiInstruction dri_8 = new DrawSymmetricalRoiInstruction("Kidney L", state, dri_7, null,
					this.workflows[index], Organ.DEMIE);
			DrawRoiInstruction driBackground_6 = new DrawRoiBackgroundSymmetrical("Kidney L Background", state, dri_8,
					this.workflows[index], "");

			DrawRoiInstruction dri_9 = new DrawSymmetricalRoiInstruction("Heart", state, null, null,
					this.workflows[index], Organ.DEMIE);
			DrawRoiInstruction dri_10 = new DrawSymmetricalRoiInstruction("Heart", state, dri_9, null,
					this.workflows[index], Organ.DEMIE);

			DrawRoiInstruction dri_11 = new DrawSymmetricalRoiInstruction("Bkg noise", state, dri_9, null,
					this.workflows[index], Organ.QUART);
			DrawRoiInstruction dri_12 = new DrawSymmetricalRoiInstruction("Bkg noise", state, dri_10, null,
					this.workflows[index], Organ.QUART);

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
			this.workflows[index]
					.addInstruction(new ScreenShotInstruction(this.captures, this.getVue(), indexCapture++));
			index++;
		}

		// Thorax image
		if (this.onlyThoraxImage > 0) {
			this.workflows[index] = new Workflow(this, this.model.getImageSelection()[this.fullBodyImages]);

			ScreenShotInstruction captureThorax = new ScreenShotInstruction(this.captures, this.getVue(),
					indexCapture++);

			// Organs to delimit
			DrawRoiInstruction dri_onlyThorax1 = new DrawRoiInstruction("Heart Thorax A", state);
			DrawRoiInstruction dri_onlyThorax2 = new DrawSymmetricalRoiInstruction("CL Thorax", state, dri_onlyThorax1,
					null, this.workflows[index], Organ.QUART);

			this.workflows[index].addInstruction(dri_onlyThorax1);
			this.workflows[index].addInstruction(dri_onlyThorax2);
			this.workflows[index].addInstruction(captureThorax);
		}

		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());
	}

	public void setFullBodyImages(int fullBodyImages) {
		// TODO Auto-generated method stub
		this.fullBodyImages = fullBodyImages;
	}

	public void setOnlyThoraxImage(int onlyThoraxImage) {
		// TODO Auto-generated method stub
		this.onlyThoraxImage = onlyThoraxImage;
	}

	public void endContamination() {
		this.finContSlice1 = true;
		this.finContSlice2 = true;
	}

	public int getFullBodyImagesCount() {
		return this.fullBodyImages;
	}
}