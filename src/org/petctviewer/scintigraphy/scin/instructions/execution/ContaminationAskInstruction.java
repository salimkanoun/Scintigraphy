package org.petctviewer.scintigraphy.scin.instructions.execution;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.petctviewer.scintigraphy.cardiac.ControllerWorkflowCardiac;
import org.petctviewer.scintigraphy.cardiac.FenApplication_Cardiac;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalLoopInstruction;

public class ContaminationAskInstruction extends ExecutionInstruction implements ActionListener {

	private static final long serialVersionUID = 1L;

	private transient Button btn_yes, btn_no;

	private transient Workflow workflow;

	private transient ImageState state;

	private String nameInstructionDrawLoop;

	private boolean instructionFlying;

	public ContaminationAskInstruction(Workflow workflow, ImageState state,
									   String nameInstructionDrawLoop) {

		this.state = state;
		this.nameInstructionDrawLoop = nameInstructionDrawLoop;

		this.workflow = workflow;
		this.instructionFlying = false;
	}

	private void createPanel() {
		
		this.workflow.getController().getVue().getPanel_Instructions_btns_droite().remove(1);
		
		JPanel panel = new JPanel(new GridLayout(1,3));
		
		panel.add(this.workflow.getController().getVue().getBtn_precedent());
		this.btn_yes = new Button("Yes");
		panel.add(this.btn_yes);
		this.btn_yes.addActionListener(this);
		this.btn_no = new Button("No");
		panel.add(this.btn_no);
		this.btn_no.addActionListener(this);
		
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
		if(this.instructionFlying) {
			this.workflow.getController().getVue().getPanel_Instructions_btns_droite().remove(1);
			this.workflow.getController().getVue().getPanel_Instructions_btns_droite().add(this.workflow.getController().getVue().createPanelInstructionsBtns());
			this.workflow.getController().getVue().pack();
			if(this.workflow.getController() instanceof ControllerWorkflowCardiac) {
				((ControllerWorkflowCardiac)this.workflow.getController()).endContamination();
			}
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
		return new DrawSymmetricalLoopInstruction(this.workflow, null, this.state, null,
										   this.nameInstructionDrawLoop);
	}
	
	public void setInstructionValidated() {
		this.instructionFlying = true;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btn_yes) {
			this.setInstructionValidated();
			this.workflow.addInstructionOnTheFly(this.getInstructionToGenerate());
			((FenApplication_Cardiac)this.workflow.getController().getVue()).startContaminationMode();
			this.workflow.getController().clickNext();
		}else 
			((ControllerWorkflowCardiac)this.workflow.getController()).clicEndCont();	
	}
	
	@Override
	public String toString() {
		return "ContaminationAskInstruction  [ isFlying : "+this.instructionFlying+", isExpectingUserInput : "+this.isExpectingUserInput()+"]";
	}

}
