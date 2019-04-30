package org.petctviewer.scintigraphy.generic.dynamic;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.DrawLoopInstruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.generator.DefaultGenerator;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.ZProjector;

public class ControllerWorkflowScinDynamic extends ControllerWorkflow {

	private FenResults fenResult;

	private int nbOrganes = 0;
	private boolean over;
	private ImagePlus impProjetee;
	private int indexRoi;

	public ControllerWorkflowScinDynamic(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);

		this.fenResult = new FenResults(this.model);
		this.fenResult.setVisible(false);
	}

	@Override
	protected void generateInstructions() {
		DefaultGenerator dri_1 = null;

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0].getImagePlus());
		dri_1 = new DrawLoopInstruction(this.workflows[0]);

		this.workflows[0].addInstructionOnTheFly(dri_1);

		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

	@Override
	public void end() {
		// on sauvegarde l'imp projetee pour la reafficher par la suite
		this.impProjetee = this.model.getImagePlus().duplicate();
		this.over = true;
		this.nbOrganes = this.model.getRoiManager().getCount();
		GeneralDynamicScintigraphy scindyn = (GeneralDynamicScintigraphy) this.main;

		ImagePlus imp = this.model.getImagePlus();
		BufferedImage capture;

		boolean postExists = false;

		String[] roiNames = new String[this.nbOrganes];
		for (int i = 0; i < this.model.getRoiManager().getCount(); i++) {
			roiNames[i] = this.model.getRoiManager().getRoi(i).getName();
		}

		FenGroup_GeneralDyn fenGroup = new FenGroup_GeneralDyn(roiNames);
		fenGroup.setModal(true);
		fenGroup.setLocationRelativeTo(this.main.getFenApplication());
		fenGroup.setVisible(true);
		String[][] asso = fenGroup.getAssociation();

		this.fenResult = new FenResultat_GeneralDyn((ModeleScinDyn) this.model, asso);

		if (scindyn.getImpAnt() != null) {
			capture = Library_Capture_CSV.captureImage(imp, 300, 300).getBufferedImage();
			saveValues(scindyn.getImpAnt());
			this.fenResult.addTab(new TabAntPost(capture, "Ant", this.fenResult));
		}

		if (scindyn.getImpPost() != null) {
			postExists = true;
			ImagePlus imp2 = ZProjector.run(scindyn.getImpPost(), "sum");
			imp2.setOverlay(imp.getOverlay());

			imp2.setProperty("Info", this.model.getImagePlus().getInfoProperty());

			// scindyn.setImp(imp2);
			this.model.getImagesPlus()[0] = imp2;
			scindyn.getFenApplication().setImage(imp2);
			scindyn.getFenApplication().resizeCanvas();
			scindyn.getFenApplication().toFront();

			Thread th = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					BufferedImage c = Library_Capture_CSV.captureImage(imp, 300, 300).getBufferedImage();

					saveValues(scindyn.getImpPost());
					ControllerWorkflowScinDynamic.this.fenResult
							.addTab(new TabAntPost(c, "Post", ControllerWorkflowScinDynamic.this.fenResult));

					ControllerWorkflowScinDynamic.this.finishDrawingResultWindow();
				}
			});
			th.start();
		}

		if (!postExists) {
			this.finishDrawingResultWindow();
		}

	}

	private void finishDrawingResultWindow() {
		GeneralDynamicScintigraphy vue = (GeneralDynamicScintigraphy) this.main;
		this.indexRoi = this.nbOrganes;
		this.over = false;

		vue.getFenApplication().setImage(this.impProjetee);
		// vue.setImp(this.impProjetee);
		this.model.getImagesPlus()[0] = this.impProjetee;

		vue.getFenApplication().resizeCanvas();
	}

	private void saveValues(ImagePlus imp) {
		this.model.getImagesPlus()[0] = imp;
		// this.getScin().setImp(imp);
		this.indexRoi = 0;

		HashMap<String, List<Double>> mapData = new HashMap<String, List<Double>>();
		// on copie les roi sur toutes les slices
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (int j = 0; j < this.nbOrganes; j++) {
				imp.setRoi(getOrganRoi(this.indexRoi));
				String name = this.getNomOrgane(this.indexRoi);

				// String name = nom.substring(0, nom.lastIndexOf(" "));
				// on cree la liste si elle n'existe pas
				if (mapData.get(name) == null) {
					mapData.put(name, new ArrayList<Double>());
				}
				// on y ajoute le nombre de coups
				mapData.get(name).add(Library_Quantif.getCounts(imp));

				this.indexRoi++;
			}
		}
		// set data to the model
		((ModeleScinDyn) this.model).setData(mapData);
		this.model.calculerResultats();

	}

	public Roi getOrganRoi(int lastRoi) {
		if (this.isOver()) {
			return this.model.getRoiManager().getRoi(this.indexRoi % this.nbOrganes);
		}
		return null;
	}

	public String getNomOrgane(int index) {
		if (!isOver()) {
			return this.main.getFenApplication().getTextfield_instructions().getText();
		}
		System.out.println(this.model.getRoiManager().getRoi(index % this.nbOrganes).getName());
		return this.model.getRoiManager().getRoi(index % this.nbOrganes).getName();
	}

}
