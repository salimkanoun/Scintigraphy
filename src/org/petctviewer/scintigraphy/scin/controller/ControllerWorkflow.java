package org.petctviewer.scintigraphy.scin.controller;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.gui.CaptureButton;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.LastInstruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.generator.GeneratorInstruction;
import org.petctviewer.scintigraphy.scin.json.SaveAndLoad;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import ij.IJ;
import ij.ImagePlus;

/**
 * This controller is used when working with a flow of instructions.<br> In order to use this type of controller, you
 * need to redefine the {@link #generateInstructions()} method to create the workflow.<br> Then, the constructor must
 * call the {@link #generateInstructions()} and the {@link #start()} methods (in that order).
 * <p>
 * A visualization of the workflow:
 * <pre>
 *                    NEXT                       NEXT
 * +-------------+   - - ->   +-------------+   - - ->   +-------------+
 * | Instruction |            | Instruction |            | Instruction |
 * +-------------+   <- - -   +-------------+   <- - -   +-------------+
 *                  PREVIOUS                   PREVIOUS
 * </pre>
 * </p>
 * For the NEXT operation, a simplified representation might look like:
 * <ol>
 * <li>Call the {@link Instruction#prepareAsNext()} on the NextInstruction</li>
 * <li>Save the ROI present on the screen into the Current Instruction</li>
 * <li>Go to the Next Instruction</li>
 * <li>Check if this is the end <i>(you don't have to hold your breath though..)</i></li>
 * <li>Display instruction's message</li>
 * <li>Call the {@link Instruction#afterNext(ControllerWorkflow)} on the Next Instruction (that is become the
 * current one now)</li>
 * </ol>
 *
 * @author Titouan QUÉMA
 */
public abstract class ControllerWorkflow extends ControllerScin implements AdjustmentListener, MouseWheelListener {

	/**
	 * This command signals that the instruction should not generate a next instruction.<br> This is only used for
	 * {@link GeneratorInstruction}.<br> To use this command on a button, use the {@link
	 * Button#setActionCommand(String)} method on the desired button. This command should not be used on the 'Next'
	 * button present by default in the view since it will duplicate calls to {@link #clickNext()}.
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
	 * @param main  Reference to the main class
	 * @param vue   View of the MVC pattern
	 * @param model Model of the MVC pattern
	 */
	public ControllerWorkflow(FenApplicationWorkflow vue, ModelScin model) {
		super(vue, model);

		this.getModel().addController(this);
		this.skipInstruction = false;
	}

	/**
	 * This method displays the ROI to edit (if necessary).
	 */
	private void editOrgan(int roiToCopy) {
		if (!this.editRoi(this.indexRoi)) this.editCopyRoi(roiToCopy);
	}

	// private void DEBUG(String s) {
	// System.out.println("=== " + s + " ===");
	// System.out.println("Current position: " + this.position);
	// System.out.println("Current workflow: " + this.indexCurrentWorkflow);
	// String currentInstruction = "-No instruction-";
	// if (this.indexCurrentWorkflow >= 0 && this.indexCurrentWorkflow <
	// this.workflows.length) {
	// Instruction i =
	// this.workflows[this.indexCurrentWorkflow].getCurrentInstruction();
	// if (i != null)
	// currentInstruction = i.getMessage();
	// else
	// currentInstruction = "-No Message-";
	// }
	// System.out.println("Current instruction: " + currentInstruction);
	// System.out.println("Index ROI: " + this.indexRoi);
	// System.out.println();
	// }

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
	 * instruction.
	 * The array may then be passed to the {@link #displayRois(int[])} method.
	 *
	 * @param indexWorkflow Index of the workflow of the ROIs
	 * @param state         State the image is in (used for orientation)
	 * @param last          Instruction of the last ROI to save (included)
	 * @return array of indexes of ROIs to display
	 */
	private int[] roisToDisplay(int indexWorkflow, ImageState state, Instruction last) {
		List<Instruction> dris = new ArrayList<>();
		Instruction[] instructions = this.workflows[indexWorkflow].getInstructionsWithOrientation(
				state.getFacingOrientation());
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
	 * Finds the index of the workflow where the specified instruction is in. If the instruction doesn't belong to any
	 * of the workflows of this controller, then this method returns -1.
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
	 * Prepares the image of the view to respect the specified state. This method only change the image if necessary
	 * (meaning if this method is called with the same arguments, it will not regenerate the image every time).<br>
	 *     This
	 * method will also update the current state of the controller.
	 *
	 * @param imageState    State the image will be set to
	 * @param indexWorkflow Index of the workflow as reference
	 */
	private void prepareImage(ImageState imageState, int indexWorkflow) {
		if (imageState == null) return;

		boolean resetOverlay = false;

		// == FACING ORIENTATION ==
		if (imageState.getFacingOrientation() != null &&
				imageState.getFacingOrientation() != this.currentState.getFacingOrientation()) {
			this.currentState.setFacingOrientation(imageState.getFacingOrientation());
			resetOverlay = true;
		}

		// == ID IMAGE ==
		if (imageState.getIdImage() == ImageState.ID_CUSTOM_IMAGE) {
			if (imageState.getImage() == null) throw new IllegalStateException(
					"The state specifies that a custom image should be used but no image has been set!");
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
			// resetOverlay = true;
		}

		// == LATERALISATION ==
		if (imageState.getLateralisation() != this.currentState.getLateralisation()) {
			this.currentState.setLateralisation(imageState.getLateralisation());
			resetOverlay = true;
		}

		if (resetOverlay) {
			this.vue.getImagePlus().getOverlay().clear();
			this.setOverlay(this.currentState);
		}
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
	 * Prepares the ImagePlus with the specified state and updates the currentState.
	 *
	 * @param imageState State the ImagePlus must complies
	 */
	protected void prepareImage(ImageState imageState) {
		this.prepareImage(imageState, this.indexCurrentWorkflow);
	}

	/**
	 * Locks the 'Previous' button if there is no instruction expecting a user input before the current one.
	 *
	 * @param currentInstruction Current instruction from which the search will be made
	 */
	private void lockPreviousButton(Instruction currentInstruction) {
		if(this.allInputInstructions().indexOf(currentInstruction) == 0)
			this.getVue().getBtn_precedent().setEnabled(false);
	}

	/**
	 * Generates a list of all the instructions of every workflow of this controller that require a user input.
	 *
	 * @return list of instruction expecting a user input
	 */
	public List<Instruction> allInputInstructions() {
		List<Instruction> instructions = new ArrayList<>();
		for (Workflow w : this.workflows) {
			for (Instruction i : w.getInstructions())
				if (i.isExpectingUserInput()) instructions.add(i);
		}
		return instructions;
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

	/**
	 * Signals that the next instruction should not be stopped on.
	 */
	public void skipInstruction() {
		this.skipInstruction = true;
	}

	@Override
	public boolean isOver() {
		return this.workflows[this.indexCurrentWorkflow].getCurrentInstruction() instanceof LastInstruction;
	}

	@Override
	public FenApplicationWorkflow getVue() {
		return (FenApplicationWorkflow) super.getVue();
	}

	@Override
	public void clickPrevious() {
		super.clickPrevious();

		Instruction previousInstruction = this.workflows[this.indexCurrentWorkflow].getCurrentInstruction();

		Instruction currentInstruction = this.workflows[this.indexCurrentWorkflow].previous();

		// Ensure that there is a previous instruction expecting a user input
		// Otherwise, the previous will go to a null instruction
		this.lockPreviousButton(currentInstruction);

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
			this.vue.getImagePlus().getOverlay().clear();
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

		// DEBUG("PREVIOUS");
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

			// Ensure that there is a previous instruction expecting a user input
			// Otherwise, the previous will go to a null instruction
			this.lockPreviousButton(nextInstruction);

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
				// TODO: might be a problem if the workflow is over: this code should not
				// execute
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

		// DEBUG("NEXT");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == getVue().getBtn_suivant() || e.getSource() == getVue().getBtn_precedent()) &&
				getVue().isVisualizationEnabled()) {
			int indexScrollForCurrentInstruction = this.allInputInstructions().indexOf(
					this.workflows[this.indexCurrentWorkflow].getCurrentInstruction());
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
				if (this.workflows[this.indexCurrentWorkflow].getCurrentInstruction() instanceof GeneratorInstruction &&
						getVue().getImagePlus().getRoi() != null) {
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
		if (!e.isControlDown()) {
			int rotation = e.getWheelRotation();
			int value = this.getVue().getInstructionDisplayed() + rotation;
			if (value < this.getVue().getMaxInstruction() && value >= 0) {
				this.updateScrollbar(value);
				this.getVue().currentInstruction(value);
			}
		}
	}

	public Workflow[] getWorkflows() {
		return this.workflows;
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
																  tab.getParent().getModel().getUID6digits()) +
				Library_Capture_CSV.genererDicomTagsPartie2(tab.getParent().getModel().getImagePlus());

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
				SaveAndLoad saveAndLoad = new SaveAndLoad();

				saveAndLoad.exportAllWithWorkflow(resultats, tab.getParent().getModel().getStudyName(), imp,
												  additionalInfo, this.getModel().getControllers());


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

	}

	/**
	 * This method must instantiate the workflow and fill it with the instructions for this model.<br> Typically, this
	 * will look like a repetition of:<br>
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

	/**
	 * Updates the scroll bar in the view. This method should only be called if the {@link
	 * FenApplicationWorkflow#isVisualizationEnabled()} is set to TRUE.
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
		int indexCurrentInstruction = allInstructions.indexOf(
				this.workflows[this.indexCurrentWorkflow].getCurrentInstruction());

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
			this.vue.getImagePlus().getOverlay().clear();
			this.setOverlay(instruction.getImageState());
			this.displayRois(roisToDisplay);
			// Refresh display
			this.getVue().repaint();
		}

		// Change next button label
		this.getVue().getBtn_suivant().setLabel(btnNextTxt);
	}

	/**
	 * This method initializes the controller. It must be called <b>after</b> the {@link #generateInstructions()}
	 * method.
	 */
	public void start() {
		this.indexCurrentWorkflow = 0;
		this.indexRoi = 0;

		// Update view
		getVue().setNbInstructions(this.allInputInstructions().size());

		Instruction i = this.workflows[0].next();
		if (i != null) {
			this.currentState = new ImageState(
					this.workflows[0].getImageAssociated().getImageOrientation().getFacingOrientation(), 1,
					ImageState.LAT_RL, ImageState.ID_NONE);
			this.setOverlay(currentState);

			this.displayInstruction(i.getMessage());
			this.prepareImage(i.getImageState());
			i.afterNext(this);

			if (!i.isExpectingUserInput()) this.clickNext();
		}
	}

//	/**
//	 * Finds the workflow matching the specified image.
//	 *
//	 * @param ims Image to find
//	 * @return Workflow associated with the image or null if not found
//	 */
//	protected Workflow getWorkflowAssociatedWithImage(ImageSelection ims) {
//		for (Workflow workflow : this.workflows)
//			if (workflow.getImageAssociated() == ims) return workflow;
//		return null;
//	}

//	/**
//	 * Finds the workflow containing the specified instruction.
//	 *
//	 * @param instruction Instruction to search
//	 * @return Workflow containing the instruction
//	 */
//	protected Workflow getWorkflowAssociatedWithInstruction(Instruction instruction) {
//		int index = this.indexWorkflowFromInstruction(instruction);
//		if (index == -1) return null;
//		return this.workflows[index];
//	}

}