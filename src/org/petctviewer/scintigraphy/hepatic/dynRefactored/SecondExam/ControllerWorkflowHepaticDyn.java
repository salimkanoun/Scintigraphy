package org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.tab.TabOtherMethod;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.tab.TabTAC;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.instructions.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;

public class ControllerWorkflowHepaticDyn extends ControllerWorkflow {

	private final int NBORGAN = 6;

	public static String[] organes = { "R. Liver", "L. Liver", "Hilium", "CBD", "Duodenom", "Blood pool" };

	private List<ImagePlus> captures;

	private TabResult resultTab;

	public ControllerWorkflowHepaticDyn(Scintigraphy main, FenApplication vue, ModeleScin model, TabResult resultTab) {
		super(main, vue, model);
		// TODO Auto-generated constructor stub
		this.resultTab = resultTab;

		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {

		this.workflows = new Workflow[1];

		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null, dri_5 = null, dri_6 = null;
		ScreenShotInstruction dri_capture = null;
		this.captures = new ArrayList<>();

		this.workflows[0] = new Workflow(this, ((ModelSecondMethodHepaticDynamic) this.model).getImageSelection()[0]);

		ImageState stateAnt = new ImageState(Orientation.ANT, 1, true, ImageState.ID_NONE);

		dri_1 = new DrawRoiInstruction("Right Liver", stateAnt, null);
		dri_2 = new DrawRoiInstruction("Left Liver", stateAnt, null);
		dri_3 = new DrawRoiInstruction("Hilium", stateAnt, null);
		dri_4 = new DrawRoiInstruction("CBD", stateAnt, null);
		dri_5 = new DrawRoiInstruction("Duodenom", stateAnt, null);
		dri_6 = new DrawRoiInstruction("Blood pool", stateAnt, null);
		dri_capture = new ScreenShotInstruction(captures, this.getVue());

		this.workflows[0].addInstruction(dri_1);
		this.workflows[0].addInstruction(dri_2);
		this.workflows[0].addInstruction(dri_3);
		this.workflows[0].addInstruction(dri_4);
		this.workflows[0].addInstruction(dri_5);
		this.workflows[0].addInstruction(dri_6);
		this.workflows[0].addInstruction(dri_capture);

		this.workflows[0].addInstruction(new EndInstruction());

	}

	@Override
	public void end() {
		SecondHepaticScintigraphy scin = (SecondHepaticScintigraphy) this.main;

		ModelSecondMethodHepaticDynamic modele = (ModelSecondMethodHepaticDynamic) this.model;

		ImagePlus imp = modele.getImageSelection()[0].getImagePlus();
		BufferedImage capture = Library_Capture_CSV.captureImage(imp, 512, 0).getBufferedImage();

		modele.setLocked(false);

		// on copie les roi sur toutes les slices
		modele.saveValues();

		// TODO remove start
		List<Double> bp = modele.getData("Blood pool");
		List<Double> rliver = modele.getData("Right Liver");
		System.out.println("bp.size() : " + bp.size());
		System.out.println("rliver.size() : " + rliver.size());

		Double[] deconv = new Double[bp.size()];
		for (int i = 0; i < bp.size(); i++) {
			deconv[i] = rliver.get(i) / bp.get(i);
		}

		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(modele.createSerie(Arrays.asList(deconv), "deconv"));
		data.addSeries(modele.getSerie("Blood pool"));
		data.addSeries(modele.getSerie("Right Liver"));

		JFreeChart chart = ChartFactory.createXYLineChart("", "x", "y", data);

		ChartPanel chartpanel = new ChartPanel(chart);
		JFrame frame = new JFrame();
		frame.add(chartpanel);
		frame.pack();
		frame.setVisible(true);

		// remove finish
		((TabOtherMethod) this.resultTab).setExamDone(true);
		this.resultTab.reloadDisplay();
		this.main.getFenApplication().dispose();
	}

	/**
	 * Creates an ImagePlus with 2 captures.
	 * 
	 * @param captures
	 *            ImageStack with 2 captures
	 * @return ImagePlus with the 2 captures on 1 slice
	 */
	private ImagePlus montage2Images(ImageStack captures) {
		MontageMaker mm = new MontageMaker();
		// TODO: patient ID
		String patientID = "NO_ID_FOUND";
		ImagePlus imp = new ImagePlus("Resultats Pelvis -" + this.model.getStudyName() + " -" + patientID, captures);
		imp = mm.makeMontage2(imp, 1, 2, 0.50, 1, 2, 1, 10, false);
		return imp;
	}

	public ModeleScin getModel() {
		return this.model;
	}

}
