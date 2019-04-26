package org.petctviewer.scintigraphy.lympho.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.MemoryImageSource;

import javax.swing.JPanel;

import org.petctviewer.scintigraphy.lympho.FenApplicationLympho;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import ij.ImagePlus;

public class TabTest2 extends TabResult {

	private ImagePlus imp;

	public TabTest2(FenResults parent, String title, boolean captureBtn, ImagePlus captures) {
		super(parent, title, captureBtn);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Component getSidePanelContent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void testprint() {
		Graphics g=this.getPanel().getGraphics();
		byte[] pixels =(byte[]) imp.getProcessor().getPixels();
		MemoryImageSource image = new 
				this.getPanel().creat
		g.drawBytes(pixels, 0, 16, 0, 0);
		
	}

	@Override
	public JPanel getResultContent() {
		this.imp = parent.getModel().getImagePlus();
		FenApplicationLympho fenApplication = new FenApplicationLympho(this.imp, "Test");
		fenApplication.setVisible(false);
		// TODO Auto-generated constructor stub
		Component[] compo = fenApplication.getComponents();

		JPanel borderLayout = new JPanel(new BorderLayout());

		JPanel pan_center = new JPanel();

		for (int i = 0; i < 1; i++) {
			pan_center.add(compo[i]);
		}
		borderLayout.add(pan_center, BorderLayout.CENTER);
		return borderLayout;
	}

}
