package org.petctviewer.scintigraphy.lympho.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabPost extends TabResult implements ActionListener {
	
	private JButton btn_addImp;

	public TabPost(FenResults parent, String title, boolean captureBtn) {
		super(parent, title, captureBtn);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Component getSidePanelContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPanel getResultContent() {
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());

		btn_addImp = new JButton("Choose post-mictional dicom");
		btn_addImp.addActionListener(this);
		box.add(btn_addImp);
		box.add(Box.createHorizontalGlue());

		JPanel pan = new JPanel();
		pan.add(box);
		return pan;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
