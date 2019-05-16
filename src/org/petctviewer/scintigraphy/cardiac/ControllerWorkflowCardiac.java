package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import org.apache.commons.lang.ArrayUtils;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalLoopInstruction;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalRoiInstruction.Organ;
import org.petctviewer.scintigraphy.scin.instructions.generator.DefaultGenerator;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

public class ControllerWorkflowCardiac extends ControllerWorkflow {
	
	/**
	 * This command signals that the instruction should not generate a next
	 * instruction.<br>
	 * This is only used for {@link ControllerWorkflowCardiac}.
	 */
	public static final String COMMAND_CONTINUE = "command.continue";
	
	private boolean finContSlice1;
	private boolean finContSlice2;
	private String[] organes = { "Bladder", "Kidney R", "Kidney L", "Heart", "Bkg noise" };

	public ControllerWorkflowCardiac(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);
		// TODO Auto-generated constructor stub
		
		// on declare si il y a deux prises
				((Modele_Cardiac) this.model).setDeuxPrise(this.model.getImageSelection()[0].getImagePlus().getImageStackSize() > 1);

				((Modele_Cardiac) this.model).calculerMoyGeomTotale();
		
		this.generateInstructions();
		((FenApplication_Cardiac) this.main.getFenApplication()).startContaminationMode();
		this.start();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[3];
		
		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);
		this.workflows[1] = new Workflow(this, this.model.getImageSelection()[0]);
		this.workflows[2] = new Workflow(this, this.model.getImageSelection()[0]);
		
		DefaultGenerator dri_1 = null, dri_2 = null;
		ImageState state_1, state_2; 
		
		
		DrawRoiInstruction dri_3 = null, dri_4 = null, dri_5 = null, dri_6 = null, dri_7 = null, dri_8 = null, dri_9 = null, dri_10 = null, dri_11 = null, dri_12 = null;

		state_1 = new ImageState(Orientation.ANT, 1, true, ImageState.ID_CUSTOM_IMAGE);
		state_1.specifieImage(this.workflows[0].getImageAssociated());
		
		state_2 = new ImageState(Orientation.ANT, 2, true, ImageState.ID_CUSTOM_IMAGE);
		state_2.specifieImage(this.workflows[0].getImageAssociated());
		

		dri_1 = new DrawSymmetricalLoopInstruction(this.workflows[0], null, state_1, model, null, "ContE");
		dri_2 = new DrawSymmetricalLoopInstruction(this.workflows[0], null, state_2, model, null, "ContL");

		this.workflows[0].addInstructionOnTheFly(dri_1);
		this.workflows[1].addInstructionOnTheFly(dri_2);
		
		
		
		// Organs to delimit
		dri_3 = new DrawRoiInstruction("Bladder A", state_2);
		dri_4 = new DrawSymmetricalRoiInstruction("Bladder P", state_2, dri_3, null, model, Organ.OTHER);
		
		dri_5 = new DrawRoiInstruction("Kidney R A", state_2);
		dri_6 = new DrawSymmetricalRoiInstruction("Kidney R P", state_2, dri_5, null, model, Organ.OTHER);
		
		dri_7 = new DrawRoiInstruction("Kidney L A", state_2);
		dri_8 = new DrawSymmetricalRoiInstruction("Kidney L P", state_2, dri_7, null, model, Organ.OTHER);
		
		dri_9 = new DrawRoiInstruction("Heart A", state_2);
		dri_10 = new DrawSymmetricalRoiInstruction("Heart P", state_2, dri_9, null, model, Organ.OTHER);
		
		dri_11 = new DrawSymmetricalRoiInstruction("Bkg noise A", state_2, dri_9, null, model, Organ.HEART);
		dri_12 = new DrawSymmetricalRoiInstruction("Bkg noise P", state_2, dri_10, null, model, Organ.HEART);
		
		
		
		this.workflows[2].addInstruction(dri_3);
		this.workflows[2].addInstruction(dri_4);
		this.workflows[2].addInstruction(dri_5);
		this.workflows[2].addInstruction(dri_6);
		this.workflows[2].addInstruction(dri_7);
		this.workflows[2].addInstruction(dri_8);
		this.workflows[2].addInstruction(dri_9);
		this.workflows[2].addInstruction(dri_10);
		this.workflows[2].addInstruction(dri_11);
		this.workflows[2].addInstruction(dri_12);
		
		
		this.workflows[2].addInstruction(new EndInstruction());
		
	}

	
	private void clicNewCont() {
		
		if (this.position % 2 != 0) {
			FenApplication_Cardiac fac = (FenApplication_Cardiac) this.main.getFenApplication();
			fac.getBtn_continue().setEnabled(true);
			fac.getBtn_newCont().setLabel("Next");
		} else {
			FenApplication_Cardiac fac = (FenApplication_Cardiac) this.main.getFenApplication();
			fac.getBtn_continue().setEnabled(false);
			fac.getBtn_newCont().setLabel("Save");
		}
		this.clicSuivant();
	}

	private void clicEndCont() {
		// on set la slice
		if ((this.model.getImageSelection()[0].getImagePlus().getCurrentSlice() == 1 && this.model.getImageSelection()[0].getImagePlus().getImageStackSize() > 1)) {
			// on relance le mode decontamination, cette fois ci pour la deuxieme slice
			this.finContSlice1 = true;
			((FenApplication_Cardiac) this.main.getFenApplication()).startContaminationMode();
			// TODO demander confirmation à Titouan
			this.position--;

		} else { // on a trait� toutes les contaminations
			this.finContSlice2 = true;
			((FenApplication_Cardiac) this.main.getFenApplication()).stopContaminationMode();
			String[] conts = new String[this.position];
			for (int i = 0; i < conts.length; i++) {
				conts[i] = "Cont";
			}
			// on ajoute de nouvelles cases dans le tableau organes pour ne pas modifier
			// l'indexRoi
			this.setOrganes((String[]) ArrayUtils.addAll(conts, this.getOrganes()));
		}
		((DrawSymmetricalLoopInstruction) this.workflows[this.indexCurrentWorkflow].getCurrentInstruction()).stop();
		this.clicSuivant();
	}
	
	public String[] getOrganes() {
		return this.organes;
	}
	
	public void setOrganes(String[] organes) {
		this.organes = organes;
	}
	
	
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		super.actionPerformed(arg0);
		// permet d'appeller les methodes correspondant au clic des deux nouveaux
		// boutons
		Button b = (Button) arg0.getSource();
		if (b == ((FenApplication_Cardiac) this.main.getFenApplication()).getBtn_newCont()) {
			this.clicNewCont();
		} else if (b == ((FenApplication_Cardiac) this.main.getFenApplication()).getBtn_continue()) {
			this.clicEndCont();
		}
	}
	
	
	@Override
	public void end() {
		// suppression du controleur de l'imp
		// this.removeImpListener();
		((Modele_Cardiac) this.model).getResults();
		((Modele_Cardiac) this.model).calculerResultats();
		
		BufferedImage capture = Library_Capture_CSV.captureImage(this.main.getFenApplication().getImagePlus(), 512, 0).getBufferedImage();
		new FenResultat_Cardiac(this.main, capture, this);
	}
	
	
	
	
	public int getSliceNumberByRoiIndex() {
		// changement de slice si la prise contient une precoce
		if (this.model.getImageSelection()[0].getImagePlus().getImageStackSize() > 1) {
			if (this.finContSlice1) {
				return 2;
			}
		}
		return 1;
	}
}
