package org.petctviewer.scintigraphy.generic.statics;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.DrawLoopInstruction;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.generator.DefaultGenerator;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;

import ij.ImagePlus;
import ij.gui.Roi;

public class ControllerWorkflow_ScinStatic extends ControllerWorkflow {

	private FenResults fenResult;

	private int nbOrganes = 0;
	private boolean over;
	private ImagePlus impProjetee;
	private int indexRoi;


	public ControllerWorkflow_ScinStatic(Scintigraphy main, FenApplication vue, ImageSelection[] selectedImages,
			String studyName) {
		super(main, vue, new ModeleScinStatic(selectedImages, studyName));

		ImageState statePost = new ImageState(Orientation.POST, 2, true, ImageState.ID_NONE);
		setOverlay(statePost);

		this.generateInstructions();
		this.start();

		this.fenResult = new FenResults(this);
		this.fenResult.setVisible(false);
	}

	public String getNomOrgane(int index) {
		return this.vue.getTextfield_instructions().getText();
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];
		DefaultGenerator dri_1 = null;

		ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		dri_1 = new DrawLoopInstruction(this.workflows[0], stateAnt);


		this.workflows[0].addInstructionOnTheFly(dri_1);

		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

	public void end() {
		ImagePlus imp = this.model.getImagePlus();

		// pour la ant
		imp.setSlice(1);

		for (int i = 0; i < this.model.getRoiManager().getCount(); i++) {
			Roi roi = this.model.getRoiManager().getRoi(i);
			imp.setRoi(roi);
			((ModeleScinStatic) this.model).enregistrerMesureAnt(roi.getName(), imp);
		}

		// pour la post
		imp.setSlice(2);

		for (int i = 0; i < this.model.getRoiManager().getCount(); i++) {
			Roi roi = this.model.getRoiManager().getRoi(i);
			imp.setRoi(roi);
			((ModeleScinStatic) this.model).enregistrerMesurePost(roi.getName(), imp);
		}

		Thread t = new DoubleImageThread("test", this.main, this.model);
		t.start();

	}

	@Override
	public void clicSuivant() {
		boolean sameName = false;
		for (Instruction instruction : this.workflows[this.indexCurrentImage].getInstructions())
			if (instruction instanceof DrawLoopInstruction)
				if (((DrawLoopInstruction) instruction) != this.workflows[this.indexCurrentImage].getCurrentInstruction())
					if (this.workflows[this.indexCurrentImage].getController().getVue().getTextfield_instructions()
							.getText().equals(((DrawLoopInstruction) instruction).getInstructionRoiName()))
						sameName = true;
		if (sameName) {
			int retour = JOptionPane.OK_OPTION;
			if (this.model.getRoiManager()
					.getRoi(indexRoi) != null /* && indexRoiToSave > this.model.getRoiManager().getCount() */) {
				JOptionPane d = new JOptionPane();
				retour = d.showConfirmDialog(getVue(), "A Roi already have this name. Do you want to continue ?",
						"Duplicate Roi Name", JOptionPane.YES_NO_CANCEL_OPTION);
			}
			if (retour != JOptionPane.OK_OPTION)
				return;
		}

		super.clicSuivant();
		System.out.println(this.model.getRoiManager().getRoi(indexRoi).getName());
	}



}
