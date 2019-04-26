package org.petctviewer.scintigraphy.lympho.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.lympho.FenApplicationLympho;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;

public class TabTest extends TabResult implements ActionListener{

	private ImagePlus imp;
	
	private JLabel label;

	public TabTest(FenResults parent, String title, boolean captureBtn, ImagePlus captures) {
		super(parent, title, captureBtn);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Component getSidePanelContent() {
		JPanel container = new JPanel(new BorderLayout());
		this.label = new JLabel();
		JButton button = new JButton("Get counts");
		button.addActionListener(this);
		container.add(this.label,BorderLayout.CENTER);
		container.add(button, BorderLayout.NORTH);
		return container;
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
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		this.label.setText("Nombre de coups : "+Library_Quantif.getCounts(this.imp));
	}

	

}
