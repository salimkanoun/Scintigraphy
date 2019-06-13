package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.apache.commons.lang.ArrayUtils;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiBackgroundSymmetrical;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalLoopInstruction;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalRoiInstruction.Organ;
import org.petctviewer.scintigraphy.scin.instructions.execution.ExecutionInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.generator.DefaultGenerator;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import ij.ImagePlus;

public class ControllerWorkflowCardiac extends ControllerWorkflow {

	/**
	 * This command signals that the instruction should not generate a next
	 * instruction.<br>
	 * This is only used for {@link ControllerWorkflowCardiac}.
	 */
	public static final String COMMAND_CONTINUE = "command.continue";

	// private int nbConta1;
	// private int nbConta2;

	private boolean finContSlice1;
	private String[] organes = { "Bladder", "Kidney R", "Kidney L", "Heart", "Bkg noise" };

	private boolean finContSlice2;

	private int onlyThoraxImage;

	private int fullBodyImages;

	private DrawRoiInstruction dri_onlyThorax1 = null, dri_onlyThorax2 = null;

	public List<ImagePlus> captures;

	private int indexPreviousWorkflow;

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

	@Override
	protected void generateInstructions() {

		int nbWorkflow = this.getModel().getImageSelection().length + 1;
		if (this.fullBodyImages == 0)
			nbWorkflow = 1;

		this.workflows = new Workflow[nbWorkflow];

		if (this.fullBodyImages != 0) {
			int index = 1;

			this.workflows[0] = new Workflow(this, this.model.getImageSelection()[index - 1]);
			if (this.fullBodyImages > 1) {
				index++;
				this.workflows[1] = new Workflow(this, this.model.getImageSelection()[index - 1]);
			}
			this.workflows[index] = new Workflow(this, this.model.getImageSelection()[index - 1]);

			DefaultGenerator dri_1 = null, dri_2 = null;
			ImageState state_1, state_2;

			DrawRoiInstruction dri_3 = null, dri_4 = null, dri_5 = null, dri_6 = null, dri_7 = null, dri_8 = null,
					dri_9 = null, dri_10 = null, dri_11 = null, dri_12 = null;

			DrawRoiBackgroundSymmetrical driBackground_1 = null, driBackground_2 = null, driBackground_3 = null,
					driBackground_4 = null, driBackground_5 = null, driBackground_6 = null;

			ScreenShotInstruction captureFullBody = new ScreenShotInstruction(this.captures, this.getVue(), 0);

			state_1 = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
			state_1.specifieImage(this.workflows[0].getImageAssociated());

			state_2 = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
			state_2.specifieImage(this.workflows[1].getImageAssociated());

			dri_1 = new DrawSymmetricalLoopInstruction(this.workflows[0], null, state_1, model, null, "ContE");
			dri_2 = new DrawSymmetricalLoopInstruction(this.workflows[0], null, state_2, model, null, "ContL");

			this.workflows[0].addInstructionOnTheFly(dri_1);
			if (this.fullBodyImages > 1)
				this.workflows[1].addInstructionOnTheFly(dri_2);

			// Organs to delimit
			dri_3 = new DrawSymmetricalRoiInstruction("Bladder", state_2, null, null, model, Organ.DEMIE);
			driBackground_1 = new DrawRoiBackgroundSymmetrical("Bladder Background", state_2, dri_3, model, "");
			dri_4 = new DrawSymmetricalRoiInstruction("Bladder", state_2, dri_3, null, model, Organ.DEMIE);
			driBackground_2 = new DrawRoiBackgroundSymmetrical("Bladder Background", state_2, dri_4, model, "");

			dri_5 = new DrawSymmetricalRoiInstruction("Kidney R", state_2, null, null, model, Organ.DEMIE);
			driBackground_3 = new DrawRoiBackgroundSymmetrical("Kidney R Background", state_2, dri_5, model, "");
			dri_6 = new DrawSymmetricalRoiInstruction("Kidney R", state_2, dri_5, null, model, Organ.DEMIE);
			driBackground_4 = new DrawRoiBackgroundSymmetrical("Kidney R Background", state_2, dri_6, model, "");

			dri_7 = new DrawSymmetricalRoiInstruction("Kidney L", state_2, null, null, model, Organ.DEMIE);
			driBackground_5 = new DrawRoiBackgroundSymmetrical("Kidney L Background", state_2, dri_7, model, "");
			dri_8 = new DrawSymmetricalRoiInstruction("Kidney L", state_2, dri_7, null, model, Organ.DEMIE);
			driBackground_6 = new DrawRoiBackgroundSymmetrical("Kidney L Background", state_2, dri_8, model, "");

			dri_9 = new DrawSymmetricalRoiInstruction("Heart", state_2, null, null, model, Organ.DEMIE);
			dri_10 = new DrawSymmetricalRoiInstruction("Heart", state_2, dri_9, null, model, Organ.DEMIE);

			dri_11 = new DrawSymmetricalRoiInstruction("Bkg noise", state_2, dri_9, null, model, Organ.QUART);
			dri_12 = new DrawSymmetricalRoiInstruction("Bkg noise", state_2, dri_10, null, model, Organ.QUART);

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
			this.workflows[index].addInstruction(captureFullBody);
		}

		if (this.onlyThoraxImage != 0) {

			int currentIndex = this.fullBodyImages == 0 ? 0 : this.fullBodyImages + 1;

			this.workflows[currentIndex] = new Workflow(this,
					this.model.getImageSelection()[this.fullBodyImages == 0 ? 0 : this.fullBodyImages]);

			ScreenShotInstruction captureThorax = new ScreenShotInstruction(this.captures, this.getVue(),
					this.fullBodyImages == 0 ? 0 : 1);

			ImageState state_1;

			state_1 = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
			state_1.specifieImage(this.workflows[currentIndex].getImageAssociated());

			// Organs to delimit
			this.dri_onlyThorax1 = new DrawRoiInstruction("Heart Thorax A", state_1);
			this.dri_onlyThorax2 = new DrawSymmetricalRoiInstruction("CL Thorax", state_1, dri_onlyThorax1, null, model,
					Organ.QUART);

			this.workflows[currentIndex].addInstruction(this.dri_onlyThorax1);
			this.workflows[currentIndex].addInstruction(this.dri_onlyThorax2);
			this.workflows[currentIndex].addInstruction(captureThorax);
		}

		this.workflows[this.workflows.length - 1].addInstruction(new EndInstruction());

		// Update view
		getVue().setNbInstructions(this.allInputInstructions().size());
	}

	private void clicNewCont() {
		if (this.getVue().getImagePlus().getRoi() != null && !this.finContSlice2) {
			if (this.position % 2 != 0) {
				FenApplication_Cardiac fac = (FenApplication_Cardiac) this.main.getFenApplication();
				fac.getBtn_continue().setEnabled(true);
				fac.getBtn_suivant().setLabel("Next");
			} else {
				FenApplication_Cardiac fac = (FenApplication_Cardiac) this.main.getFenApplication();
				fac.getBtn_continue().setEnabled(false);
				fac.getBtn_suivant().setLabel("Save");
			}
		}
	}

	private void clicEndCont() {
//		System.out.println("EndCont");
		// Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
		// .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		// System.out.println("-------------------------- Avant
		// --------------------------");
		// System.out.println(gson.toJson(this.workflows[this.indexCurrentWorkflow])+"\n\n");
		// on set la slice
		if ((this.fullBodyImages > 1 && !finContSlice1)) {
//			System.out.println("First End");
			// on relance le mode decontamination, cette fois ci pour la deuxieme slice
			this.finContSlice1 = true;
			for(Instruction instruction : this.workflows[this.indexCurrentWorkflow].getInstructions())
				((DefaultGenerator)instruction).stop();

			// TODO demander confirmation Ã  Titouan
//			((DrawSymmetricalLoopInstruction) this.workflows[this.indexPreviousWorkflow].getCurrentInstruction()).stop();

			// Si ce n'est pas la première instruction du workflow
//			if (!(this.workflows[this.indexPreviousWorkflow].getInstructions()
//					.get(0) == this.workflows[this.indexPreviousWorkflow].getCurrentInstruction()))
				// Si aucune Roi n'a été sauvegardée, on supprime.
//				if (this.workflows[this.indexPreviousWorkflow].getCurrentInstruction().getRoiIndex() == 1)
//					this.workflows[this.indexPreviousWorkflow].removeInstructionWithIterator();

		} else { // on a traitï¿½ toutes les contaminations
//			System.out.println("Final End");
			((FenApplication_Cardiac) this.main.getFenApplication()).stopContaminationMode();
			String[] conts = new String[this.position];
			for (int i = 0; i < conts.length; i++) {
				conts[i] = "Cont";
			}
			// on ajoute de nouvelles cases dans le tableau organes pour ne pas modifier
			// l'indexRoi
			this.setOrganes((String[]) ArrayUtils.addAll(conts, this.getOrganes()));
			
			for(Instruction instruction : this.workflows[this.indexCurrentWorkflow].getInstructions())
				((DefaultGenerator)instruction).stop();
//			((DrawSymmetricalLoopInstruction) this.workflows[this.indexCurrentWorkflow - 1].getCurrentInstruction()).stop();

			// To avoid deleting of the first Intruction if no contaminations
//			if (!(this.workflows[this.indexPreviousWorkflow].getInstructions()
//					.get(0) == this.workflows[this.indexPreviousWorkflow].getCurrentInstruction()))
//				if (this.workflows[this.indexPreviousWorkflow].getCurrentInstruction().getRoiIndex() == 1)
//					this.workflows[this.indexPreviousWorkflow].removeInstructionWithIterator();
			this.finContSlice2 = true;
		}

		this.clickNext();
		this.position--;
		this.vue.pack();
		// System.out.println("-------------------------- AprÃ¨s
		// --------------------------");
		// System.out.println(gson.toJson(this.workflows[this.indexCurrentWorkflow])+"\n\n\n");

	}

	public String[] getOrganes() {
		return this.organes;
	}

	public void setOrganes(String[] organes) {
		this.organes = organes;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
//		 Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
//				 .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
//		 System.out.println("-------------------------- Avant--------------------------");
//		 System.out.println(gson.toJson(this.workflows[this.indexCurrentWorkflow])+"\n\n");
		
		boolean needManualIncrement = false;
		 
		if(this.workflows[indexCurrentWorkflow].getCurrentInstruction().isRoiVisible() && !this.workflows[indexCurrentWorkflow].getCurrentInstruction().saveRoi()) 
			needManualIncrement = true;
		
		super.actionPerformed(arg0);
		
		if(needManualIncrement)
			this.indexRoi++;
		
//		System.out.println(" Index du workflow précédent : "+this.indexPreviousWorkflow);
//		System.out.println(" Index du workflow actuel : "+this.indexCurrentWorkflow);
		
		
//		if(indexPreviousWorkflow != indexCurrentWorkflow)
//			System.out.println("Changing workflow : indexPreviousWorkflow : "+indexPreviousWorkflow+" => indexCurrentWorkflow : "+indexCurrentWorkflow);

		if (arg0.getSource() instanceof Button) {
			Button b = (Button) arg0.getSource();
			if (b == ((FenApplication_Cardiac) this.main.getFenApplication()).getBtn_continue())
				this.clicEndCont();
			else if (b == ((FenApplication_Cardiac) this.main.getFenApplication()).getBtn_suivant())
				this.clicNewCont();
		}
//		if (this.workflows[indexCurrentWorkflow].getCurrentInstruction().getRoiIndex() != -1)
//			System.out.println(this.getRoiManager()
//					.getRoi(this.workflows[indexCurrentWorkflow].getCurrentInstruction().getRoiIndex()).getName()
//					+ " : " + this.getRoiManager()
//							.getRoi(this.workflows[indexCurrentWorkflow].getCurrentInstruction().getRoiIndex()));
		
		
//		this.indexPreviousWorkflow = this.indexCurrentWorkflow;

		
	}

	@Override
	public void end() {
		super.end();
		// this.saveWorkflow();

		((Model_Cardiac) this.model).getResults();
		((Model_Cardiac) this.model).calculateResults();
		// ((Model_Cardiac) this.model).setNbConta(new int[] {this.nbConta1,
		// this.nbConta2});

		FenResultat_Cardiac fen = new FenResultat_Cardiac(captures, this, this.fullBodyImages, this.onlyThoraxImage);
		fen.setVisible(true);
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

	public void setFullBodyImages(int fullBodyImages) {
		// TODO Auto-generated method stub
		this.fullBodyImages = fullBodyImages;
	}

	public void setOnlyThoraxImage(int onlyThoraxImage) {
		// TODO Auto-generated method stub
		this.onlyThoraxImage = onlyThoraxImage;
	}
	
	private class ContaminationAskInstruction extends ExecutionInstruction implements ActionListener {
		
		private ControllerWorkflow controller;
		
		private Button btn_yes, btn_no;
		
		private Workflow workflow;

		private ImageState state;

		private String nameInstructionDrawLoop;
		
		private boolean instructionFlying;
		
		public ContaminationAskInstruction(ControllerWorkflow controller, Workflow workflow, ImageState state, String nameInstructionDrawLoop) {
			
			this.state = state;
			this.nameInstructionDrawLoop = nameInstructionDrawLoop;
			
			this.controller = controller;
			this.workflow = workflow;
			this.instructionFlying = false;
		}
		
		@Override
		public void prepareAsNext() {
			this.createPanel();
		}
		
		@Override
		public void prepareAsPrevious() {
			this.createPanel();
		}
		
		@Override
		public boolean isExpectingUserInput() {
			return !this.instructionFlying;
		}
		
		private void createPanel() {
			this.controller.getVue().getPanel_Instructions_btns_droite().removeAll();
			JPanel panel = new JPanel();
			panel.add(this.btn_yes);
			this.btn_yes.addActionListener(this);
			panel.add(this.btn_no);
			this.btn_no.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(arg0.getSource() == this.btn_yes) {
				this.workflow.addInstructionOnTheFly(new DrawSymmetricalLoopInstruction(this.workflow, null, this.state, model, null, this.nameInstructionDrawLoop));
				this.instructionFlying = true;
			}
			
			this.workflow.getController().clickNext();
		}
		
	}
}