package org.petctviewer.scintigraphy.scin.json;

import java.util.List;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;

/**
 * A custom list of Workflow, used to get a Workflow list ({@link Workflow})
 * from a Json file <br/>
 * This class contains the {@link WorkflowFromGson} list, and the
 * {@link PatientFromGson}. <br/>
 * <br/>
 * Used in {@link ControllerWorkflow#loadWorkflows(String)} and
 * {@link Library_Roi#getRoiFromZip(String, ControllerWorkflow)}
 *
 */
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
			nbROIs += workflowFromGson.getInstructions().size();
		return nbROIs;
	}

	public InstructionFromGson getInstructionFromGson(int indexWorkflow, int indexInstruction) {
		return this.Workflows.get(indexWorkflow).getInstructionAt(indexInstruction);
	}

	public InstructionFromGson getInstructionFromGson(String nameOfRoiFile) {

		for (WorkflowFromGson workflowFromGson : this.Workflows)
			for (InstructionFromGson instructionFromGson : workflowFromGson.getInstructions())
				if (nameOfRoiFile.equals(instructionFromGson.getNameOfRoiFile()))
					return instructionFromGson;

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