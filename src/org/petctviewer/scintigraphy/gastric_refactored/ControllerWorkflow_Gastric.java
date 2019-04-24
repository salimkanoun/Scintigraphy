package org.petctviewer.scintigraphy.gastric_refactored;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Label;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

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
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptInstruction;
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
	public Model_Gastric getModel() {
		return (Model_Gastric) this.model;
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
		ImagePlus imp = this.model.getImagePlus();
		this.getModel().initResultat();
		for (int i = 0; i < this.getRoiManager().getRoisAsArray().length; i += 6) {
			imp = this.model.getImagesPlus()[i / 6];
			
			System.out.println("Saving results for image#" + i / 6);
			
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i]);
			System.out.println("ROI: " + this.getRoiManager().getRoisAsArray()[i]);
			this.getModel().calculerCoups("Estomac_Ant", i / 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i+1]);
			System.out.println("ROI: " + this.getRoiManager().getRoisAsArray()[i+1].getName());
			this.getModel().calculerCoups("Intes_Ant", i / 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i+2]);
			System.out.println("ROI: " + this.getRoiManager().getRoisAsArray()[i+2].getName());
			this.getModel().calculerCoups("Antre_Ant", i / 6, imp);
			this.getModel().setCoups("Fundus_Ant", i / 6,
					this.getModel().getCoups("Estomac_Ant", i / 6) - this.getModel().getCoups("Antre_Ant", i / 6));
			this.getModel().setCoups("Intestin_Ant", i / 6,
					this.getModel().getCoups("Intes_Ant", i / 6) - this.getModel().getCoups("Antre_Ant", i / 6));
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i+3]);
			System.out.println("ROI: " + this.getRoiManager().getRoisAsArray()[i+3].getName());
			this.getModel().calculerCoups("Estomac_Post", i / 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i+4]);
			System.out.println("ROI: " + this.getRoiManager().getRoisAsArray()[i+4].getName());
			this.getModel().calculerCoups("Intes_Post", i / 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i+5]);
			System.out.println("ROI: " + this.getRoiManager().getRoisAsArray()[i+5].getName());
			this.getModel().calculerCoups("Antre_Post", i / 6, imp);
			this.getModel().setCoups("Fundus_Post", i / 6,
					this.getModel().getCoups("Estomac_Post", i / 6) - this.getModel().getCoups("Antre_Post", i / 6));
			this.getModel().setCoups("Intestin_Post", i / 6,
					this.getModel().getCoups("Intes_Post", i / 6) - this.getModel().getCoups("Antre_Post", i / 6));

			this.getModel().pourcVGImage(i / 6);
			try {
				this.getModel().tempsImage(i / 6, imp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.model.calculerResultats();

		// Display results
		this.fenResults.setMainTab(new TabResult(this.fenResults, "Result", true) {

			// TODO: change this method to have only desired data in input
			private JTable tablesResultats(String[] resultats) {
				JTable table = new JTable(0, 4);
				DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
				String[] arr = new String[tableModel.getColumnCount()];
				for (int i = 0; i < getModel().nbAcquisitions(); i++) {
					for (int j = 0; j < tableModel.getColumnCount(); j++) {
						arr[j] = resultats[i * tableModel.getColumnCount() + j];
					}
					tableModel.insertRow(i, arr);
				}
				table.setRowHeight(30);
				MatteBorder border = new MatteBorder(1, 1, 1, 1, Color.BLACK);
				table.setBorder(border);
				return table;
			}

			// TODO: change this method to have only desired data in input
			private JPanel infoResultats(String[] resultats) {
				JPanel infoRes = new JPanel();
				infoRes.setLayout(new GridLayout(14, 1));
				// la deuxime partir du resultats contient 13 ligne
				for (int i = model.getImageSelection().length * 4 + 4; i < resultats.length; i++) {
					infoRes.add(new Label(resultats[i]));
				}
				return infoRes;
			}

			@Override
			public Component getSidePanelContent() {
				JPanel panel = new JPanel(new GridLayout(0, 1));
				panel.add(this.tablesResultats(getModel().resultats()));
				panel.add(this.infoResultats(getModel().resultats()));
				return panel;
			}

			@Override
			public JPanel getResultContent() {
				ImageStack ims = Library_Capture_CSV.captureToStack(new ImagePlus[] { getModel().createGraph_1(),
						getModel().createGraph_1(), getModel().createGraph_2(), getModel().createGraph_3() });

				JPanel panel = new JPanel();
				panel.add(new DynamicImage(getModel().montage(ims).getImage()));
				return panel;
			}
		});
		this.fenResults.pack();
		this.fenResults.setVisible(true);
	}

	@Override
	protected void generateInstructions() {
		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null;

		// First instruction to get the acquisition time for the starting point
		PromptIngestionTime promptIngestionTime = new PromptIngestionTime(this);
		PromptInstruction promptTimeAcquisition = new PromptInstruction(promptIngestionTime);
		if (promptIngestionTime.isCompleted())
			this.getModel().setTimeIngestion(promptIngestionTime.getResult());

		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow();
			dri_1 = new DrawRoiInstruction("Stomach", Orientation.ANT, dri_3);
			dri_2 = new DrawRoiInstruction("Intestine", Orientation.ANT, dri_4);
			dri_3 = new DrawRoiInstruction("Stomach", Orientation.POST, dri_1);
			dri_4 = new DrawRoiInstruction("Intestine", Orientation.POST, dri_2);

			if (i == 0)
				this.workflows[i].addInstruction(promptTimeAcquisition);
			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_1, dri_2, "Antre"));
			this.workflows[i].addInstruction(dri_3);
			this.workflows[i].addInstruction(dri_4);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_3, dri_4, "Antre"));
		}
		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

}
