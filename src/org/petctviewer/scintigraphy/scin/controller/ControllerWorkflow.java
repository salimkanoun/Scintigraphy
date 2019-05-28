package org.petctviewer.scintigraphy.scin.controller;


import com.google.gson.*;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.gui.*;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Instruction.DrawInstructionType;
import org.petctviewer.scintigraphy.scin.instructions.LastInstruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawLoopInstruction;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalLoopInstruction;
import org.petctviewer.scintigraphy.scin.instructions.generator.DefaultGenerator;
import org.petctviewer.scintigraphy.scin.instructions.generator.GeneratorInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This controller is used when working with a flow of instructions.<br>
 * In order to use this type of controller, you need to redefine the
 * {@link #generateInstructions()} method to create the workflow.<br>
 * Then, the constructor must call the {@link #generateInstructions()} and the
 * {@link #start()} methods (in that order).
 *
 * @author Titouan QUÉMA
 */
public abstract class ControllerWorkflow extends ControllerScin implements AdjustmentListener, MouseWheelListener {

	/**
	 * This command signals that the instruction should not generate a next
	 * instruction.<br>
	 * This is only used for {@link GeneratorInstruction}.<br>
	 * To use this command on a button, use the {@link Button#setActionCommand(String)} method on the desired
	 * button.
	 * This command should not be used on the 'Next' button present by default in the view since it will
	 * duplicate calls to {@link #clickNext()}.
	 */
	public static final String COMMAND_END = "command.end";

	/**
	 * Workflows containing the instructions to execute by this controller
	 */
	protected Workflow[] workflows;

	/**
	 * Index of the current workflow
	 */
	protected int indexCurrentWorkflow;
	/**
	 * Current state the image must be in
	 */
	protected ImageState currentState;

	/**
	 * Index of the ROI to store in the RoiManager.
	 */
	protected int indexRoi;

	/**
	 * If set to TRUE then the next call to {@link #clickNext()} or {@link #clickPrevious()} will be repeated.
	 */
	private boolean skipInstruction;

	/**
	 * Window used to display the results.
	 */
	protected FenResults fenResults;

	/**
	 * @param main  Reference to the main class
	 * @param vue   View of the MVC pattern
	 * @param model Model of the MVC pattern
	 */
	public ControllerWorkflow(Scintigraphy main, FenApplicationWorkflow vue, ModelScin model) {
		super(main, vue, model);

		this.skipInstruction = false;
	}

	/**
	 * This method must instantiate the workflow and fill it with the instructions
	 * for this model.<br>
	 * Typically, this will look like a repetition of:<br>
	 *
	 * <pre>
	 * this.workflow[0].addInstruction(new DrawRoiInstruction(...));
	 * ...
	 * this.workflow[0].addInstruction(new EndInstruction());
	 * </pre>
	 * <p>
	 * Only the last workflow generated MUST end with a {@link LastInstruction}.
	 */
	protected abstract void generateInstructions();

//	private void DEBUG(String s) {
//		System.out.println("=== " + s + " ===");
//		System.out.println("Current position: " + this.position);
//		System.out.println("Current workflow: " + this.indexCurrentWorkflow);
//		String currentInstruction = "-No instruction-";
//		if (this.indexCurrentWorkflow >= 0 && this.indexCurrentWorkflow < this.workflows.length) {
//			Instruction i = this.workflows[this.indexCurrentWorkflow].getCurrentInstruction();
//			if (i != null)
//				currentInstruction = i.getMessage();
//			else
//				currentInstruction = "-No Message-";
//		}
//		System.out.println("Current instruction: " + currentInstruction);
//		System.out.println("Index ROI: " + this.indexRoi);
//		System.out.println();
//	}

	/**
	 * This method displays the ROI to edit (if necessary).
	 */
	private void editOrgan(int roiToCopy) {
		if (!this.editRoi(this.indexRoi)) this.editCopyRoi(roiToCopy);
	}

	/**
	 * @return array of ROI indexes to display for the current instruction
	 */
	private int[] currentRoisToDisplay() {
		return this.roisToDisplay(this.indexCurrentWorkflow, this.currentState, this.indexRoi);
	}

	/**
	 * Returns an array containing all indexes of the ROIs to display for a specified state until a certain index. The
	 * array may then be passed to the {@link #displayRois(int[])} method.
	 *
	 * @param indexWorkflow Index of the workflow of the ROIs
	 * @param state         State the image is in (used for orientation)
	 * @param indexRoi      Index of the last ROI to save (not included)
	 * @return array of indexes of ROIs to display
	 */
	private int[] roisToDisplay(int indexWorkflow, ImageState state, int indexRoi) {
		List<Instruction> dris = new ArrayList<>();
		for (Instruction i :
				this.workflows[indexWorkflow].getInstructionsWithOrientation(state.getFacingOrientation()))
			if (i.getRoiIndex() >= 0 && i.getRoiIndex() < indexRoi) {
				dris.add(i);
			}
		int[] array = new int[dris.size()];
		for (int i = 0; i < dris.size(); i++)
			array[i] = dris.get(i).getRoiIndex();
		return array;
	}

	/**
	 * Returns an array containing all indexes of the ROIs to display for a specified state until a certain
	 * instruction. The array may then be passed to the {@link #displayRois(int[])} method.
	 *
	 * @param indexWorkflow Index of the workflow of the ROIs
	 * @param state         State the image is in (used for orientation)
	 * @param last          Instruction of the last ROI to save (included)
	 * @return array of indexes of ROIs to display
	 */
	private int[] roisToDisplay(int indexWorkflow, ImageState state, Instruction last) {
		List<Instruction> dris = new ArrayList<>();
		Instruction[] instructions = this.workflows[indexWorkflow]
				.getInstructionsWithOrientation(state.getFacingOrientation());
		for (Instruction i : instructions) {
			if (i.getRoiIndex() >= 0) {
				dris.add(i);
			}
			if (i == last) {
				break;
			}
		}
		int[] array = new int[dris.size()];
		for (int i = 0; i < dris.size(); i++)
			array[i] = dris.get(i).getRoiIndex();
		return array;
	}

	/**
	 * Finds the index of the workflow where the specified instruction is in. If the instruction doesn't belong to
	 * any of the workflows of this controller, then this method returns -1.
	 *
	 * @param instruction Instruction to search through the workflows
	 * @return index of the workflow or -1 if not found
	 */
	private int indexWorkflowFromInstruction(Instruction instruction) {
		for (int i = 0; i < this.workflows.length; i++)
			if (workflows[i].getInstructions().contains(instruction)) return i;
		return -1;
	}

	/**
	 * Changes the window for the results. This method should only be called once.
	 *
	 * @param fenResults Window for the results
	 */
	protected void setFenResults(FenResults fenResults) {
		this.fenResults = fenResults;
	}

	/**
	 * Returns the window used to display the results of this study.
	 *
	 * @return window for the results or null if no window was set yet
	 */
	protected FenResults getFenResults() {
		return this.fenResults;
	}

	/**
	 * Updates the scroll bar in the view. This method should only be called if the
	 * {@link FenApplicationWorkflow#isVisualizationEnabled()} is set to TRUE.
	 *
	 * @param value New position for the scroll bar
	 */
	protected void updateScrollbar(int value) {
		List<Instruction> allInstructions = this.allInstructions();
		List<Instruction> instructions = this.allInputInstructions();
		Instruction instruction = instructions.get(value);
		int indexWorkflow = this.indexWorkflowFromInstruction(instruction);

		// Index of instructions
		int indexInstruction = allInstructions.indexOf(instruction);
		int indexCurrentInstruction = allInstructions
				.indexOf(this.workflows[this.indexCurrentWorkflow].getCurrentInstruction());

		// Color to display
		Color color;
		String btnNextTxt = FenApplicationWorkflow.BTN_TXT_RESUME;

		if (indexInstruction < indexCurrentInstruction) color = Color.GREEN;
		else if (indexInstruction == indexCurrentInstruction) {
			color = Color.YELLOW;
			btnNextTxt = FenApplicationWorkflow.BTN_TXT_NEXT;
		} else color = Color.WHITE;

		// Display title of Instruction
		getVue().displayScrollToolTip("[" + value + "] " + instruction.getMessage(), color);

		// Change to prepare image
		if (instruction.getImageState() != null) {
			this.prepareImage(instruction.getImageState(), indexWorkflow);
			int[] roisToDisplay = this.roisToDisplay(indexWorkflow, instruction.getImageState(), instruction);
			this.vue.getOverlay().clear();
			this.setOverlay(instruction.getImageState());
			this.displayRois(roisToDisplay);
		}

		// Change next button label
		this.getVue().getBtn_suivant().setLabel(btnNextTxt);
	}

	/**
	 * Prepares the image of the view to respect the specified state. This method only change the image if necessary
	 * (meaning if this method is called with the same arguments, it will not regenerate the image every time).<br>
	 * This method will also update the current state of the controller.
	 *
	 * @param imageState    State the image will be set to
	 * @param indexWorkflow Index of the workflow as reference
	 */
	private void prepareImage(ImageState imageState, int indexWorkflow) {
		if (imageState == null) return;

		boolean resetOverlay = false;

		// == FACING ORIENTATION ==
		if (imageState.getFacingOrientation() != null && imageState.getFacingOrientation() != this.currentState
				.getFacingOrientation()) {
			this.currentState.setFacingOrientation(imageState.getFacingOrientation());
			resetOverlay = true;
		}

		// == ID IMAGE ==
		if (imageState.getIdImage() == ImageState.ID_CUSTOM_IMAGE) {
			if (imageState.getImage() == null) throw new IllegalStateException(
					"The state specifies that a custom image should be used but no image " + "has been set!");
			// Use image specified in the image state
			this.currentState.setIdImage(ImageState.ID_CUSTOM_IMAGE);
			this.currentState.specifieImage(imageState.getImage());
		} else {
			if (imageState.getIdImage() == ImageState.ID_NONE || imageState.getIdImage() == ImageState.ID_WORKFLOW) {
				// Don't use the id
				this.currentState.setIdImage(indexWorkflow);
				this.currentState.specifieImage(this.workflows[indexWorkflow].getImageAssociated());
			} else if (imageState.getIdImage() >= 0) {
				// Use the specified id
				this.currentState.setIdImage(imageState.getIdImage());
			}
			// else, don't touch the previous id
		}

		// Change image only if different than the previous
		if (this.vue.getImagePlus() != this.currentState.getImage().getImagePlus()) {
			this.vue.setImage(this.currentState.getImage().getImagePlus());
			resetOverlay = true;
		}

		// == SLICE ==
		if (imageState.getSlice() > ImageState.SLICE_PREVIOUS)
			// Use the specified slice
			this.currentState.setSlice(imageState.getSlice());
		// else, don't touch the previous slice

		// Change slice only if different than the previous
		if (this.currentState.getSlice() != this.vue.getImagePlus().getCurrentSlice()) {
			this.vue.getImagePlus().setSlice(this.currentState.getSlice());
//			resetOverlay = true;
		}

		// == LATERALISATION ==
		if (imageState.getLateralisation() != this.currentState.getLateralisation()) {
			this.currentState.setLateralisation(imageState.getLateralisation());
			resetOverlay = true;
		}

		if (resetOverlay) {
			this.vue.getOverlay().clear();
			this.setOverlay(this.currentState);
		}
	}

	/**
	 * Generates a list of all the instructions of every workflow of this controller that require a user input.
	 *
	 * @return list of instruction expecting a user input
	 */
	protected List<Instruction> allInputInstructions() {
		List<Instruction> instructions = new ArrayList<>();
		for (Workflow w : this.workflows) {
			for (Instruction i : w.getInstructions())
				if (i.isExpectingUserInput()) instructions.add(i);
		}
		return instructions;
	}

	/**
	 * Generates a list of all the instructions of every workflow of this controller.
	 *
	 * @return list of all instructions
	 */
	private List<Instruction> allInstructions() {
		List<Instruction> instructions = new ArrayList<>();
		for (Workflow w : this.workflows)
			instructions.addAll(w.getInstructions());
		return instructions;
	}

	/**
	 * This method initializes the controller. It must be called <b>after</b> the
	 * {@link #generateInstructions()} method.
	 */
	protected void start() {
		this.indexCurrentWorkflow = 0;
		this.indexRoi = 0;

		Instruction i = this.workflows[0].next();
		if (i != null) {
			this.currentState = new ImageState(
					this.workflows[0].getImageAssociated().getImageOrientation().getFacingOrientation(), 1,
					ImageState.LAT_RL, ImageState.ID_NONE);
			this.setOverlay(currentState);

			this.displayInstruction(i.getMessage());
			this.prepareImage(i.getImageState());
			i.afterNext(this);
		}
	}

	/**
	 * Finds the workflow matching the specified image.
	 *
	 * @param ims Image to find
	 * @return Workflow associated with the image or null if not found
	 */
	protected Workflow getWorkflowAssociatedWithImage(ImageSelection ims) {
		for (Workflow workflow : this.workflows)
			if (workflow.getImageAssociated() == ims) return workflow;
		return null;
	}

	/**
	 * Finds the workflow containing the specified instruction.
	 *
	 * @param instruction Instruction to search
	 * @return Workflow containing the instruction
	 */
	protected Workflow getWorkflowAssociatedWithInstruction(Instruction instruction) {
		int index = this.indexWorkflowFromInstruction(instruction);
		if (index == -1) return null;
		return this.workflows[index];
	}

	/**
	 * Prepares the ImagePlus with the specified state and updates the currentState.
	 *
	 * @param imageState State the ImagePlus must complies
	 */
	private void prepareImage(ImageState imageState) {
		this.prepareImage(imageState, this.indexCurrentWorkflow);
	}

	/**
	 * @return state of the current image
	 */
	public ImageState getCurrentImageState() {
		return this.currentState;
	}

	/**
	 * @return index of the last ROI saved by this controller
	 */
	public int getIndexLastRoiSaved() {
		return this.indexRoi - 1;
	}

	public void skipInstruction() {
		this.skipInstruction = true;
	}

	@Override
	public void clickPrevious() {
		super.clickPrevious();

		// Hide result window
		if(this.fenResults != null)
			this.fenResults.setVisible(false);

		Instruction previousInstruction = this.workflows[this.indexCurrentWorkflow].getCurrentInstruction();

		Instruction currentInstruction = this.workflows[this.indexCurrentWorkflow].previous();

		// Update view
		int indexInstruction = this.allInputInstructions().indexOf(currentInstruction);
		if (indexInstruction != -1) this.getVue().currentInstruction(indexInstruction);

		if (previousInstruction instanceof LastInstruction && currentInstruction instanceof GeneratorInstruction) {
			((GeneratorInstruction) currentInstruction).activate();
		}

		if (currentInstruction == null) {
			this.indexCurrentWorkflow--;
			currentInstruction = this.workflows[this.indexCurrentWorkflow].getCurrentInstruction();
			currentInstruction.prepareAsPrevious();
		}

		if (currentInstruction.isExpectingUserInput()) {
			this.displayInstruction(currentInstruction.getMessage());
			this.vue.getOverlay().clear();
			this.setOverlay(this.currentState);
			this.prepareImage(currentInstruction.getImageState());

			if (currentInstruction.saveRoi()) this.indexRoi--;
			this.displayRois(this.currentRoisToDisplay());

			if (currentInstruction.saveRoi()) {
				this.editOrgan(currentInstruction.getRoiIndex());
			}

			currentInstruction.afterPrevious(this);
		} else {
			if (currentInstruction.saveRoi() && !currentInstruction.isRoiVisible()) {
				this.indexRoi--;
			}
			currentInstruction.afterPrevious(this);
			this.clickPrevious();
		}

		// == Skip instruction if requested ==
		if (this.skipInstruction) {
			this.skipInstruction = false;
			this.clickPrevious();
		}

//		DEBUG("PREVIOUS");
	}

	@Override
	public FenApplicationWorkflow getVue() {
		return (FenApplicationWorkflow) super.getVue();
	}

	@Override
	public void clickNext() {
		Instruction previousInstruction = this.workflows[this.indexCurrentWorkflow].getCurrentInstruction();

		// Only execute 'Next' if the instruction is not cancelled
		if (!previousInstruction.isCancelled()) {

			// Prepare next instruction
			int indexPreviousImage = this.indexCurrentWorkflow;

			// === Draw ROI of the previous instruction ===
			if (previousInstruction.saveRoi()) {
				try {
					this.saveRoiAtIndex(previousInstruction.getRoiName(), this.indexRoi);
					previousInstruction.setRoi(this.indexRoi);

					if (previousInstruction.isRoiVisible()) this.displayRoi(this.indexRoi);

					this.indexRoi++;
				} catch (NoDataException e) {
					JOptionPane.showMessageDialog(vue, e.getMessage(), "", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}

			// == Generate next instruction if necessary ==
			if (previousInstruction instanceof GeneratorInstruction) {
				GeneratorInstruction generatorInstruction = (GeneratorInstruction) previousInstruction;
				this.workflows[indexPreviousImage].addInstructionOnTheFly(generatorInstruction.generate());
			}

			// == Go to the next instruction ==
			super.clickNext();

			if (this.workflows[this.indexCurrentWorkflow].isOver()) {
				this.indexCurrentWorkflow++;
			}
			Instruction nextInstruction = this.workflows[this.indexCurrentWorkflow].next();

			// Update view
			int indexInstruction = this.allInputInstructions().indexOf(nextInstruction);
			if (indexInstruction != -1) this.getVue().currentInstruction(indexInstruction);

			if (this.isOver()) {
				nextInstruction.afterNext(this);
				getVue().setCursor(new Cursor(Cursor.WAIT_CURSOR));
				this.end();
				getVue().setCursor(Cursor.getDefaultCursor());
			}

			// == Display instruction for the user ==
			if (nextInstruction.isExpectingUserInput()) {
				this.displayInstruction(nextInstruction.getMessage());
				this.prepareImage(nextInstruction.getImageState());

				if (nextInstruction.saveRoi()) this.editOrgan(nextInstruction.getRoiIndex());

				nextInstruction.afterNext(this);
			} else {
				// TODO: might be a problem if the workflow is over: this code should not execute
				// If not displayable, go directly to the next instruction
				nextInstruction.afterNext(this);
				this.clickNext();
			}
		} else {
			// Execution cancelled
			if (!previousInstruction.isExpectingUserInput()) {
				// Since the previous instruction is not displayable, it should not be stopped
				// on, so you go back to the previous instruction
				this.clickPrevious();
			}
		}

		// == Skip instruction if requested ==
		if (this.skipInstruction) {
			this.skipInstruction = false;
			this.clickNext();
		}

//		DEBUG("NEXT");
	}

	@Override
	public boolean isOver() {
		return this.workflows[this.indexCurrentWorkflow].getCurrentInstruction() instanceof LastInstruction;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == getVue().getBtn_suivant() || e.getSource() == getVue().getBtn_precedent()) && getVue()
				.isVisualizationEnabled()) {
			int indexScrollForCurrentInstruction = this.allInputInstructions()
					.indexOf(this.workflows[this.indexCurrentWorkflow].getCurrentInstruction());
			if (indexScrollForCurrentInstruction == 0) indexScrollForCurrentInstruction = 1;
			if (getVue().getInstructionDisplayed() != indexScrollForCurrentInstruction) {
				// Update view
				this.updateScrollbar(indexScrollForCurrentInstruction);
				getVue().currentInstruction(indexScrollForCurrentInstruction);
				return; // Do nothing more
			}
		}

		super.actionPerformed(e);

		if ((e.getSource() instanceof Button)) {
			Button source = (Button) e.getSource();
			if (source.getActionCommand().contentEquals(COMMAND_END)) {
				if (this.workflows[this.indexCurrentWorkflow]
						.getCurrentInstruction() instanceof GeneratorInstruction && getVue().getImagePlus()
						.getRoi() != null) {
					((GeneratorInstruction) this.workflows[this.indexCurrentWorkflow].getCurrentInstruction()).stop();
				}
				this.clickNext();
			}
		} else if (e.getSource() instanceof CaptureButton) actionCaptureButton((CaptureButton) e.getSource());
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		this.updateScrollbar(e.getValue());
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int rotation = e.getWheelRotation();
		int value = this.getVue().getInstructionDisplayed() + rotation;
		if (value < this.getVue().getMaxInstruction()) {
			this.updateScrollbar(value);
			this.getVue().currentInstruction(value);
		}
	}


	// -------------------------------------
	// TODO: move all json to another class!
	// -------------------------------------

	public Gson saveWorkflow(String path) {
		this.getRoiManager();

		return new Gson();
	}

	public JsonElement saveWorkflowToJson(String[] label) {


		// Pretty print
		// Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
		// .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

		// Formal data sending
		Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
				.create();

		JsonObject workflowsObject = new JsonObject();
		JsonArray workflowsArray = new JsonArray();

		int indexNames = 0;
		for (Workflow workflow : this.workflows) {
			JsonObject currentWorkflow = new JsonObject();
			JsonArray instructionsArray = new JsonArray();
			for (Instruction instruction : workflow.getInstructions()) {
				if (instruction.saveRoi() || instruction.isRoiVisible()) {
					JsonObject currentInstruction = new JsonObject();
					currentInstruction.addProperty("InstructionType", instruction.getClass().getSimpleName());
					currentInstruction.addProperty("IndexRoiToEdit", instruction.getRoiIndex());
					currentInstruction.addProperty("NameOfRoi",
							this.getModel().getRoiManager().getRoi(instruction.getRoiIndex()).getName());
					if (label[indexNames].endsWith(".roi"))
						label[indexNames] = label[indexNames].substring(0, label[indexNames].length() - 4);
					currentInstruction.addProperty("NameOfRoiFile", label[indexNames]);
					// instructionsArray.add((JsonObject) gson.toJsonTree(instruction));
					instructionsArray.add(gson.toJsonTree(currentInstruction));
					indexNames++;
				}
			}
			currentWorkflow.add("Intructions", instructionsArray);
			workflowsArray.add(currentWorkflow);
		}

		workflowsObject.add("Workflows", workflowsArray);


		JsonObject patientObject = new JsonObject();

		String[] infoPatient = Library_Capture_CSV.getInfoPatient(this.getModel().getImagePlus());
		HashMap<String, String> patientInfo = Library_Capture_CSV.getPatientInfo(this.getModel().getImagePlus());

		patientObject.addProperty("Name", infoPatient[0]);
		patientObject.addProperty("ID", infoPatient[1]);
		patientObject.addProperty("Date", infoPatient[2]);
		patientObject.addProperty("AccessionNumber", infoPatient[3]);

//		patientObject.addProperty("Name", patientInfo.get(Library_Capture_CSV.PATIENT_INFO_NAME));
//		patientObject.addProperty("ID", patientInfo.get(Library_Capture_CSV.PATIENT_INFO_ID));
//		patientObject.addProperty("Date", patientInfo.get(Library_Capture_CSV.PATIENT_INFO_DATE));
//		patientObject.addProperty("AccessionNumber", patientInfo.get(Library_Capture_CSV
//		.PATIENT_INFO_ACCESSION_NUMBER));

		workflowsObject.add("Patient", patientObject);
		// System.out.println("\n\n\n --------------------------- TEST
		// --------------------------- \n");
		// System.out.println(gson.toJson(workflowsObject));
		// System.out.println("\n --------------------------- Supposed
		// --------------------------- \n");
		// System.out.println(gson.toJson(this.workflows));
		// System.out.println("\n\n\n");

		// try (FileWriter writer = new FileWriter(path)) {
		// gson.toJson(workflowsObject, writer);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new
		// FileOutputStream(path)));
		// Writer writer = new OutputStreamWriter(zip);) {
		// zip.putNextEntry(new ZipEntry("workflow.json"));
		// gson.toJson(workflowsObject, writer);
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// this.loadWorkflow(path);

		return workflowsObject;
	}

	public WorkflowsFromGson loadWorkflows(String string) {

		Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
				.create();
		WorkflowsFromGson workflowsFromGson;

		// String path = "D:\\Bureau\\IUT\\Oncopole\\workflow.json";
		// try (Reader reader = new FileReader(path)) {
		//
		// // Convert JSON to WorkflowsFromGson
		// workflowsFromGson = gson.fromJson(reader, WorkflowsFromGson.class);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		workflowsFromGson = gson.fromJson(string, WorkflowsFromGson.class);

		if (workflowsFromGson != null) {
			if (workflowsFromGson.getWorkflows().size() != this.workflows.length) {
				System.out.println("LE NOMBRE DE WORKFLOW EST DIFFERENT, IMPOSSIBLE DE CHARGER LA SAUVEGARDE");
				return null;
			}

			PatientFromGson patientFromGson = workflowsFromGson.getPatient();
			String[] currentPatient = Library_Capture_CSV.getInfoPatient(this.getModel().getImagePlus());

			Date currentPatientDate = null;
			try {
				currentPatientDate = new SimpleDateFormat("yyyyMMdd").parse(currentPatient[2]);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			int differenceNumber = 0;

			if (!patientFromGson.getAccessionNumber().equals(currentPatient[3])) differenceNumber++;
			if (!patientFromGson.getName().equals(currentPatient[0])) differenceNumber++;
			if (!patientFromGson.getID().equals(currentPatient[1])) differenceNumber++;
			if (!patientFromGson.getDate().toString().equals(currentPatientDate.toString())) differenceNumber++;

			Object[][] difference = new Object[differenceNumber][3];

			int indexDifference = 0;
			if (!patientFromGson.getAccessionNumber().equals(currentPatient[3])) {
				difference[indexDifference] = new String[]{"Accession Number",
				                                           "" + patientFromGson.getAccessionNumber(),
				                                           "" + currentPatient[3]};
				indexDifference++;
			}
			if (!patientFromGson.getName().equals(currentPatient[0])) {
				difference[indexDifference] = new String[]{"Name", "" + patientFromGson.getName(),
				                                           "" + currentPatient[0]};
				indexDifference++;
			}
			if (!patientFromGson.getID().equals(currentPatient[1])) {
				difference[indexDifference] = new String[]{"ID", "" + patientFromGson.getID(), "" + currentPatient[1]};
				indexDifference++;
			}


			if (!patientFromGson.getDate().toString().equals(currentPatientDate.toString())) {
				difference[indexDifference] = new String[]{"Date", "" + patientFromGson.getDate(),
				                                           "" + currentPatientDate};
				indexDifference++;
			}


			JTable differences = new JTable(difference, new String[]{"Conflict", "From Json, Current patient"});

			if (differenceNumber != 0) {
				WindowDifferentPatient fen = new WindowDifferentPatient(difference);
				fen.setModal(true);
				fen.setVisible(true);
				fen.setAlwaysOnTop(true);
				fen.setLocationRelativeTo(null);
			}

			// int nbDrawInstruction = 0;
			// for (Workflow workflow : this.workflows)
			// for(Instruction instruction : workflow.getInstructions())
			// if(instruction.saveRoi() || instruction.isRoiVisible())
			// nbDrawInstruction++;
			//
			// if (nbDrawInstruction != workflowsFromGson.Workflows.size()) {
			// System.out.println("NOMBRE D'INSTRUCTION DIFFERENTE, IMPOSSIBLE DE CHARGER LA
			// SAUVEGARDE");
			// return false;
			// }

			for (int index = 0; index < this.workflows.length; index++) {
				int specialIndex = 0;
				for (int j = 0; j < this.workflows[index].getInstructions().size(); j++) {
					if (this.workflows[index].getInstructionAt(j).saveRoi()) {

						InstructionFromGson intructionFromGson = workflowsFromGson.getWorkflowAt(index)
								.getInstructionAt(specialIndex);
						String typeOfIntructionFromGson = intructionFromGson.getInstructionType();

						if (!this.workflows[index].getInstructionAt(j).getClass().getSimpleName()
								.equals(typeOfIntructionFromGson)) {
							System.out.println(
									"LES INSTRUCTIONs NE SONT PAS LES MÊMES, IMPOSSIBLE DE CHARGER LA " +
											"SAUVEGARDE");
							System.out.println(this.workflows[index].getInstructionAt(j).getClass().getSimpleName());
							System.out.println(typeOfIntructionFromGson);
							return null;
						}

//						if (!this.getModel().getRoiManager().getRoi(this.workflows[index].getInstructionAt(j)
//						.roiToDisplay()).getName()
//								.equals(intructionFromGson.getNameOfRoi())) {
//							System.out.println(
//									"LES INSTRUCTIONs NE SONT PAS DU MÊME TYPE, IMPOSSIBLE DE CHARGER LA SAUVEGARDE");
//							System.out.println(this.workflows[index].getInstructionAt(j).getClass().getSimpleName());
//							System.out.println(typeOfIntructionFromGson);
//							return null;
//						}


						if ((typeOfIntructionFromGson
								.equals(DrawInstructionType.DRAW_LOOP.getName())) || typeOfIntructionFromGson
								.equals(DrawInstructionType.DRAW_SYMMETRICAL_LOOP.getName())) {
							if (workflowsFromGson.getWorkflowAt(index).getInstructions().size() > specialIndex + 1) {
								InstructionFromGson nextIntructionFromGson = workflowsFromGson.getWorkflowAt(index)
										.getInstructionAt(specialIndex + 1);
								String typeOfNextIntructionFromGson = nextIntructionFromGson.getInstructionType();

								if (typeOfNextIntructionFromGson.equals(DrawInstructionType.DRAW_LOOP.getName()))
									this.workflows[index].getInstructions().add(j + 1,
											((DrawLoopInstruction) this.workflows[index].getInstructionAt(j))
													.generate());

								else if (typeOfNextIntructionFromGson
										.equals(DrawInstructionType.DRAW_SYMMETRICAL_LOOP.getName()))
									this.workflows[index].getInstructions().add(j + 1,
											((DrawSymmetricalLoopInstruction) this.workflows[index].getInstructionAt(j))
													.generate());
								else ((DefaultGenerator) this.workflows[index].getInstructionAt(j)).stop();
							}
						}

						this.workflows[index].getInstructionAt(j).setRoi(intructionFromGson.getIndexRoiToEdit());

						specialIndex++;
					}
				}
			}

			String jsonInString = gson.toJson(workflowsFromGson);
			System.out.println(jsonInString);

			System.out.println("\n\n\n\nWorkflow : ");
			String workflowIsString = gson.toJson(this.workflows);
			System.out.println(workflowIsString);
			System.out.println("\n\n\n");

		}

		return workflowsFromGson;
	}

	public class WorkflowsFromGson {
		List<WorkflowFromGson> Workflows;
		PatientFromGson Patient;

		public List<WorkflowFromGson> getWorkflows() {
			return this.Workflows;
		}

		public WorkflowFromGson getWorkflowAt(int index) {
			return this.Workflows.get(index);
		}

		public int getNbROIs() {
			int nbROIs = 0;
			for (WorkflowFromGson workflowFromGson : this.Workflows)
				for (InstructionFromGson instructionFromGson : workflowFromGson.getInstructions())
					nbROIs++;
			return nbROIs;
		}

		public InstructionFromGson getInstructionFromGson(int indexWorkflow, int indexInstruction) {
			return this.Workflows.get(indexWorkflow).getInstructionAt(indexInstruction);
		}

		public InstructionFromGson getInstructionFromGson(String nameOfRoiFile) {

			for (WorkflowFromGson workflowFromGson : this.Workflows)
				for (InstructionFromGson instructionFromGson : workflowFromGson.getInstructions())
					if (nameOfRoiFile.equals(instructionFromGson.getNameOfRoiFile())) return instructionFromGson;

			return null;
		}

		public int getIndexRoiOfInstructionFromGson(String nameOfRoiFile) {
			System.out.println("nameOfRoiFile to found on Controller : " + nameOfRoiFile);
			for (WorkflowFromGson workflowFromGson : this.Workflows)
				for (InstructionFromGson instructionFromGson : workflowFromGson.getInstructions()) {
					System.out.println("\tName of Instruction : " + instructionFromGson.getNameOfRoiFile());
					if (nameOfRoiFile.equals(instructionFromGson.getNameOfRoiFile())) {
						System.out.println("\t Matchs !");
						return instructionFromGson.getIndexRoiToEdit();
					}
				}

			return -1;
		}

		public PatientFromGson getPatient() {
			return this.Patient;
		}

	}

	private class WorkflowFromGson {
		List<InstructionFromGson> Intructions;

		public List<InstructionFromGson> getInstructions() {
			return this.Intructions;
		}

		public InstructionFromGson getInstructionAt(int index) {
			return this.Intructions.get(index);
		}
	}

	private class InstructionFromGson {

		private String InstructionType;

		private int IndexRoiToEdit;

		private String NameOfRoi;

		private String NameOfRoiFile;


		public int getIndexRoiToEdit() {
			return this.IndexRoiToEdit;
		}

		public String getInstructionType() {
			return this.InstructionType;
		}

		public String getNameOfRoi() {
			return this.NameOfRoi;
		}

		public String getNameOfRoiFile() {
			return this.NameOfRoiFile;
		}
	}

	public class PatientFromGson {

		private String Name;

		private String ID;

		private Date Date;

		private String AccessionNumber;


		public String getName() {
			return this.Name;
		}

		public String getID() {
			return this.ID;
		}

		public Date getDate() {
			return this.Date;
		}

		public String getAccessionNumber() {
			return this.AccessionNumber;
		}

		@Override
		public String toString() {
			return "Bonjour, mon nom est  " + this.Name + ", j'ai pour ID " + this.ID + " car j'ai été admis le " + this.Date + " ce qui donne comme accessionNumber : " + this.AccessionNumber;
		}
	}

	public void actionCaptureButton(CaptureButton captureButton) {

		TabResult tab = captureButton.getTabResult();
		JLabel lbl_credits = captureButton.getLabelCredits();
		Component[] hide = tab.getComponentToHide();
		Component[] show = tab.getComponentToShow();
		String additionalInfo = tab.getAdditionalInfo();

		// generation du tag info
		String info = Library_Capture_CSV.genererDicomTagsPartie1(tab.getParent().getModel().getImagePlus(),
				tab.getParent().getModel().getStudyName(),
				tab.getParent().getModel().getUID6digits()) + Library_Capture_CSV
				.genererDicomTagsPartie2(tab.getParent().getModel().getImagePlus());

		// on ajoute le listener sur le bouton capture
		captureButton.addActionListener(e -> {

			captureButton.setVisible(false);
			for (Component comp : hide)
				comp.setVisible(false);

			lbl_credits.setVisible(true);
			for (Component comp : show)
				comp.setVisible(true);

			SwingUtilities.invokeLater(() -> {
				// Capture, nouvelle methode a utiliser sur le reste des programmes
				BufferedImage capture = new BufferedImage(tab.getPanel().getWidth(), tab.getPanel().getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				tab.getPanel().paint(capture.getGraphics());
				ImagePlus imp = new ImagePlus("capture", capture);

				captureButton.setVisible(true);
				for (Component comp : hide)
					comp.setVisible(true);

				lbl_credits.setVisible(false);
				for (Component comp : show)
					comp.setVisible(false);

				// on passe a la capture les infos de la dicom
				imp.setProperty("Info", info);
				// on affiche la capture
				imp.show();

				// on change l'outil
				IJ.setTool("hand");

				// generation du csv
				String resultats = tab.getParent().getModel().toString();

				try {
					Library_Capture_CSV.exportAll(resultats, tab.getParent().getModel().getRoiManager(),
							tab.getParent().getModel().getStudyName(), imp, additionalInfo, ControllerWorkflow.this);

					String addInfo = additionalInfo == null ? "" : additionalInfo;
					String nomFichier = Library_Capture_CSV.getInfoPatient(imp)[1] + "_" + Library_Capture_CSV
							.getInfoPatient(imp)[2] + addInfo;
					String path = Prefs.get("dir.preferred", null) + File.separator + tab.getParent().getModel()
							.getStudyName() + File.separator + Library_Capture_CSV.getInfoPatient(imp)[1];
					String pathFinal = path + File.separator + nomFichier + ".zip";
					System.out.println(path);
					System.out.println(pathFinal);

					// ((ControllerWorkflow) tab.getParent().getController()).saveWorkflow(path);

					imp.killRoi();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				// Execution du plugin myDicom
				try {
					IJ.run("myDicom...");
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				System.gc();
			});

		});

	}
}