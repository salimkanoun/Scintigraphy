package org.petctviewer.scintigraphy.scin.instructions.execution;

import org.petctviewer.scintigraphy.cardiac.ControllerWorkflowCardiac;
import org.petctviewer.scintigraphy.cardiac.FenApplication_Cardiac;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalLoopInstruction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

public class ContaminationAskInstruction extends ExecutionInstruction implements ActionListener {

	private static final long serialVersionUID = 1L;

	private transient Button btn_yes;

	private final transient Workflow workflow;

	private final transient ImageState state;

	private String nameInstructionDrawLoop;

	private boolean instructionFlying;
	private boolean finishInstruction;

	private int position;

	public ContaminationAskInstruction(Workflow workflow, ImageState state,
									   String nameInstructionDrawLoop, int position) {

		this.state = state;
		this.nameInstructionDrawLoop = nameInstructionDrawLoop;

		this.workflow = workflow;
		this.instructionFlying = false;
		this.finishInstruction = false;
		this.position = position;
	}

	private void createPanel() {
		
		this.workflow.getController().getVue().getPanel_Instructions_btns_droite().remove(1);
		
		JPanel panel = new JPanel(new GridLayout(1,3));
		
		panel.add(this.workflow.getController().getVue().getBtn_precedent());
		this.btn_yes = new Button("Yes");
		panel.add(this.btn_yes);
		this.btn_yes.addActionListener(this);
		Button btn_no = new Button("No");
		panel.add(btn_no);
		btn_no.addActionListener(this);
		
		this.workflow.getController().getVue().getPanel_Instructions_btns_droite().add(panel);
		this.workflow.getController().getVue().getTextfield_instructions().setText("Do you want to perform Contamination ?");
	}

	@Override
	public void prepareAsNext() {
		if(!this.instructionFlying)
			this.createPanel();
	}
	
	@Override
	public void afterNext(ControllerWorkflow controller) {
		if(this.instructionFlying || this.finishInstruction) {
			this.workflow.getController().getVue().getPanel_Instructions_btns_droite().remove(1);
			this.workflow.getController().getVue().getPanel_Instructions_btns_droite().add(this.workflow.getController().getVue().createPanelInstructionsBtns());
			this.workflow.getController().getVue().pack();

		}
	}

	@Override
	public void prepareAsPrevious() {
		if(!this.instructionFlying)
			this.createPanel();
	}

	@Override
	public boolean isExpectingUserInput() {
		return !this.instructionFlying;
	}
	
	@Override
	public String getMessage() {
		return "Contamination Ask";
	}
	
	@Override
	public ImageState getImageState() {
		return this.state;
	}
	
	public Instruction getInstructionToGenerate() {
		System.out.println("");
		return new DrawSymmetricalLoopInstruction(this.workflow, null, this.state, null,
										   this.nameInstructionDrawLoop);
	}
	
	public void setInstructionValidated() {
		this.instructionFlying = true;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		ControllerWorkflowCardiac controller = (ControllerWorkflowCardiac) this.workflow.getController();

		if (this.btn_yes.equals(arg0.getSource())) {
			this.setInstructionValidated();
			this.workflow.addInstructionOnTheFly(this.getInstructionToGenerate());
			((FenApplication_Cardiac) controller.getVue()).startContaminationMode();
			this.workflow.getController().clickNext();
		} else {
			controller.clicEndCont();
			this.finishInstruction = true;
			this.afterNext(this.workflow.getController());
		}
	}
	
	@Override
	public String toString() {
		return "ContaminationAskInstruction  [ isFlying : "+this.instructionFlying+", isExpectingUserInput : "+this.isExpectingUserInput()+"]";
	}

}
