package org.petctviewer.scintigraphy.gastric;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.Window;

public class InstructionTooltip extends Window {
	private static final long serialVersionUID = 1L;

	private Label label;
	
	public InstructionTooltip(Frame parent) throws HeadlessException {
		super(parent);
		
		this.label = new Label();
		this.add(label);
	}
	
	public void setText(String msg) {
		this.label.setText(msg);
	}

}
