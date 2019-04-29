package org.petctviewer.scintigraphy.gastric_refactored.tabs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.gastric_refactored.Model_Gastric;
import org.petctviewer.scintigraphy.gastric_refactored.dynamic.DynGastricScintigraphy;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabDynamic extends TabResult {

	private ControllerWorkflow controller;
	
	public TabDynamic(FenResults parent, ControllerWorkflow controller) {
		super(parent, "Dynamic result");
		this.controller = controller;
		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		return null;
	}

	@Override
	public JPanel getResultContent() {
		JPanel panel = new JPanel();
		JButton btn = new JButton("Launch dynamic acquisition");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Finish gastric
				controller.getVue().setVisible(false);
				
				// Start scintigraphy
				new DynGastricScintigraphy(((Model_Gastric) parent.getModel()));
			}
		});
		panel.add(btn);
		return panel;
	}

}
