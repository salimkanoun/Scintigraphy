package org.petctviewer.scintigraphy.generic.dynamic;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
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
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.gui.Roi;

public class ControllerWorkflowScinDynamic extends ControllerWorkflow {

	private FenResults fenResult;

	private int nbOrganes = 0;

	private ImagePlus impProjetee;
	private int indexRoi;

	public ControllerWorkflowScinDynamic(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);

		this.generateInstructions();
		this.start();

		this.fenResult = new FenResults(this.model);
		this.fenResult.setVisible(false);
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];
		DefaultGenerator dri_1 = null;
		ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		dri_1 = new DrawLoopInstruction(this.workflows[0], stateAnt);

		this.workflows[0].addInstructionOnTheFly(dri_1);

		this.workflows[0].addInstruction(new EndInstruction());
	}

	@Override
	public void end() {
		// on sauvegarde l'imp projetee pour la reafficher par la suite
		this.impProjetee = ((Modele_GeneralDyn) this.model).getImpProjetee().getImagePlus();

		Library_Gui.initOverlay(this.vue.getImagePlus());
		Library_Gui.setOverlayTitle("Ant", this.vue.getImagePlus(), Color.yellow, 1);
		Library_Gui.setOverlayTitle("Inverted Post", this.vue.getImagePlus(), Color.yellow, 2);

		for (Roi roi : this.model.getRoiManager().getRoisAsArray()) {
			roi.setPosition(0);
			this.vue.getImagePlus().getOverlay().add(roi);
		}

		this.nbOrganes = this.model.getRoiManager().getCount();
		GeneralDynamicScintigraphy scindyn = (GeneralDynamicScintigraphy) this.main;

		BufferedImage capture;

		String[] roiNames = ((Modele_GeneralDyn) this.model).getRoiNames();

		FenGroup_GeneralDyn fenGroup = new FenGroup_GeneralDyn(roiNames);
		fenGroup.setModal(true);
		fenGroup.setLocationRelativeTo(this.vue);
		fenGroup.setVisible(true);
		String[][] asso = fenGroup.getAssociation();

		this.fenResult = new FenResultat_GeneralDyn((ModeleScinDyn) this.model, asso);

		if (scindyn.getImpAnt() != null) {
			this.vue.getImagePlus().setSlice(1);
			capture = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512, 0).getBufferedImage();
			((Modele_GeneralDyn) this.model).saveValues(((Modele_GeneralDyn) this.model).getImpAnt().getImagePlus());
			this.fenResult.addTab(new TabAntPost(capture, "Ant", this.fenResult));
		}

		if (scindyn.getImpPost() != null) {

			this.vue.getImagePlus().setSlice(2);

			BufferedImage c = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512, 0).getBufferedImage();

			((Modele_GeneralDyn) this.model).saveValues(((Modele_GeneralDyn) this.model).getImpPost().getImagePlus());
			ControllerWorkflowScinDynamic.this.fenResult
					.addTab(new TabAntPost(c, "Post", ControllerWorkflowScinDynamic.this.fenResult));

			// ControllerWorkflowScinDynamic.this.finishDrawingResultWindow();

		}

		// if (!postExists) {
		// this.finishDrawingResultWindow();
		// }

	}

	private void finishDrawingResultWindow() {
		GeneralDynamicScintigraphy vue = (GeneralDynamicScintigraphy) this.main;
		this.indexRoi = this.nbOrganes;

		vue.getFenApplication().setImage(this.impProjetee);
		// vue.setImp(this.impProjetee);
		this.model.getImagesPlus()[0] = this.impProjetee;

		vue.getFenApplication().resizeCanvas();
	}

	@Override
	public void clicSuivant() {
		boolean sameName = false;
		for (Instruction instruction : this.workflows[this.indexCurrentImage].getInstructions())
			if (instruction instanceof DrawLoopInstruction)
				if (((DrawLoopInstruction) instruction) != this.workflows[this.indexCurrentImage]
						.getCurrentInstruction())
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
	}

}
