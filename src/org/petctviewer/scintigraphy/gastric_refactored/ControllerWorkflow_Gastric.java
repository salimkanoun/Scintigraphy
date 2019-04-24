package org.petctviewer.scintigraphy.gastric_refactored;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.instructions.CheckIntersectionInstruction;
import org.petctviewer.scintigraphy.scin.instructions.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.EndInstruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;

public class ControllerWorkflow_Gastric extends ControllerWorkflow {

	private FenResults fenResults;

	public ControllerWorkflow_Gastric(Scintigraphy main, FenApplication vue, ImageSelection[] selectedImages,
			String studyName) {
		super(main, vue, new Model_Gastric(selectedImages, studyName));

		this.fenResults = new FenResults(model);
		this.fenResults.setVisible(false);
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
		ImagePlus imp = this.model.getImagePlus();
		((Model_Gastric)model).initResultat(imp);
		for (int i = 0; i < this.getRoiManager().getRoisAsArray().length; i += 6) {
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i]);
			((Model_Gastric) this.model).calculerCoups("Estomac_Ant", i % 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i]);
			((Model_Gastric) this.model).calculerCoups("Intestin_Ant", i % 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i]);
			((Model_Gastric) this.model).calculerCoups("Antre_Ant", i % 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i]);
			((Model_Gastric) this.model).calculerCoups("Estomac_Post", i % 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i]);
			((Model_Gastric) this.model).calculerCoups("Intestin_Post", i % 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i]);
			((Model_Gastric) this.model).calculerCoups("Antre_Post", i % 6, imp);

			((Model_Gastric) model).pourcVGImage(i % 6);
			try {
				((Model_Gastric) model).tempsImage(i % 6, "010203");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.model.calculerResultats();

		// Display results
		this.fenResults.setMainTab(new TabResult(this.fenResults, "Result", true) {

			@Override
			public Component getSidePanelContent() {
				JPanel panel = new JPanel(new GridLayout(0, 1));
				for (String s : ((Model_Gastric) model).resultats(imp)) {
					panel.add(new JLabel(s));
				}
				return panel;
			}

			@Override
			public JPanel getResultContent() {
				ImageStack ims = Library_Capture_CSV.captureToStack(new ImagePlus[] {
						((Model_Gastric) model).createCourbeUn(), ((Model_Gastric) model).createCourbeTrois() });

				JPanel panel = new JPanel();
				panel.add(new DynamicImage(montage(ims).getImage()));
				return panel;
			}
		});
		this.fenResults.setVisible(true);
	}

	@Override
	protected void generateInstructions() {
		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null;

		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow();
			dri_1 = new DrawRoiInstruction("Stomach", Orientation.ANT, dri_3);
			dri_2 = new DrawRoiInstruction("Intestine", Orientation.ANT, dri_4);
			dri_3 = new DrawRoiInstruction("Stomach", Orientation.POST, dri_1);
			dri_4 = new DrawRoiInstruction("Intestine", Orientation.POST, dri_2);

			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_1, dri_2));
			this.workflows[i].addInstruction(dri_3);
			this.workflows[i].addInstruction(dri_4);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_3, dri_4));
		}
		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

}
