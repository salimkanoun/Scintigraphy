package test;

import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;
import ij.ImagePlus;
import ij.gui.StackWindow;

public class customStackWindow extends StackWindow {

	public customStackWindow(ImagePlus imp) {
		super(imp);
		Panel panel = new Panel();
		panel.add(new Button("Button"));
		panel.add(new Label("Text is here"));
		this.add(panel);
		
		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(null);
	}
	
}
