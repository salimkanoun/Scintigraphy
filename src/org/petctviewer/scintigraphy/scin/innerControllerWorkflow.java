package org.petctviewer.scintigraphy.scin;

import java.util.ArrayList;
import java.util.List;

public class innerControllerWorkflow {
	
	private List<innerInstruction> innerInstructions;
	
	public innerControllerWorkflow() {
		this.innerInstructions = new ArrayList<>();
	}
	
	public boolean addInnerInstruction(innerInstruction innerInstruction) {
		return this.innerInstructions.add(innerInstruction);
	}
	
	public List<innerInstruction> getInnerInstruction(){
		return this.innerInstructions;
	}

}
