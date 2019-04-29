package org.petctviewer.scintigraphy.lympho.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.lympho.FenApplicationLympho;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;

public class TabTest3 extends TabResult implements ActionListener {

	private ImagePlus imp;

	private JLabel label;

	public TabTest3(FenResults parent, String title, boolean captureBtn, ImagePlus captures) {
		super(parent, title, captureBtn);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Component getSidePanelContent() {
		JPanel container = new JPanel(new BorderLayout());
		this.label = new JLabel();
		JButton button = new JButton("Get counts");
		button.addActionListener(this);
		container.add(this.label, BorderLayout.CENTER);
		container.add(button, BorderLayout.NORTH);
		return container;
	}

	@Override
	public JPanel getResultContent() {
		this.imp = parent.getModel().getImagePlus();

		JPanel borderLayout = new JPanel(new BorderLayout());

//		JPanel pan_center = new JPanel();
//		ImageCanvas ic = new ImageCanvas(imp);
////		ic = imp.getCanvas();
//		ic.setSize((int)(imp.getWidth()), (int)(imp.getHeight()));
////		ic.zoomIn(0, 0);
////		ic.updateImage(imp);
//		ic.setImageUpdated();
//		ic.repaint();
//		pan_center.add(ic);

		
		
		ImageCanvas canvas = TabTest3.getConnectedImageCanvas(imp);
		JPanel pan_center = new JPanel();
		pan_center.add(canvas);
		borderLayout.add(pan_center, BorderLayout.CENTER);
		return borderLayout;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.label.setText("Nombre de coups : " + Library_Quantif.getCounts(this.imp));
	}
	
	
	
	public static ImageCanvas getConnectedImageCanvas (ImagePlus imp) {
		
		ImagePlus impDuplicate = imp.duplicate();
		
		FenApplication fenApplication = new FenApplication(impDuplicate, "Test");
		fenApplication.setVisible(false);
		// TODO Auto-generated constructor stub
		Component[] compo = fenApplication.getComponents();
		for(int i = 0 ; i < fenApplication.getComponentCount(); i++) {
			Component encours = fenApplication.getComponent(1);
			encours = null;
			fenApplication.remove(1);
			fenApplication.revalidate();
		}
		fenApplication = null;
		
		return ((ImageCanvas)compo[0]);
	}

}
