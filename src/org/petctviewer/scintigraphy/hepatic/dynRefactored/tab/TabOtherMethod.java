package org.petctviewer.scintigraphy.hepatic.dynRefactored.tab;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.hepatic.dynRefactored.ModelHepaticDynamic;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.SecondHepaticScintigraphy;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabOtherMethod extends TabResult implements ActionListener, ChangeListener  {

	private boolean examDone;

	private SecondHepaticScintigraphy vueBasic;

	private JButton btn_addImp;

	private JTabbedPane tabPane;
	// private List<TabResult> tabsResult;
	
	private TabDeconvolv deconvolvGraph;

	public TabOtherMethod(FenResults parent, String title) {
		super(parent, title, true);
		// TODO Auto-generated constructor stub
		this.setSidePanelTitle("Other Method");
		((ModelHepaticDynamic) this.parent.getModel()).setResultTab(this);

		this.tabPane = new JTabbedPane();
		// this.tabsResult = new ArrayList<>();

		this.getPanel().add(tabPane);

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		if (this.examDone) {
			JPanel resultPane = new JPanel(new GridLayout(0, 2));
			HashMap<String, String> results = ((ModelSecondMethodHepaticDynamic) this.vueBasic.getFenApplication()
					.getControleur().getModel()).getResultsHashMap();
			String[] keys = { "T1/2 Righ Liver", "T1/2 Righ Liver *", "Maximum Right Liver", "end/max Ratio Right",
					"T1/2 Left Liver", "T1/2 Left Liver *", "Maximum Left Liver", "end/max Ratio Left",
					"T1/2 Blood pool", "T1/2 Blood pool *", "Blood pool ratio 20mn/5mn" };
			for (String s : keys)
				resultPane.add(new JLabel(s + " : " + results.get(s)));
			
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
			spinner.addChangeListener(this);
			resultPane.add(spinner);
			return resultPane;
		} else
			return null;
	}

	@Override
	public JPanel getResultContent() {
		if (!this.examDone) {
			JPanel pan = new JPanel();
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());

			btn_addImp = new JButton("Choose Pelvis dicom");
			btn_addImp.addActionListener(this);
			box.add(btn_addImp);
			box.add(Box.createHorizontalGlue());

			pan.add(box);
			return pan;

		} else {
			JPanel panelDeFin = new JPanel();
			this.tabPane = new JTabbedPane();
			TabTAC tabTac = new TabTAC(this.getParent(), this);
			TabVasculaire tabVasculaire = new TabVasculaire(this.getParent(), this);
			this.deconvolvGraph = new TabDeconvolv(this.getParent(), this);
			TabThreeAsOne threeAsOne = new TabThreeAsOne(this.getParent(), this);

			this.tabPane.addTab(tabTac.getTitle(), tabTac.getPanel());
			this.tabPane.addTab(tabVasculaire.getTitle(), tabVasculaire.getPanel());
			this.tabPane.addTab(deconvolvGraph.getTitle(), deconvolvGraph.getPanel());
			this.tabPane.addTab(threeAsOne.getTitle(), threeAsOne.getPanel());
			

			panelDeFin.add(this.tabPane);
			return panelDeFin;
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.vueBasic = new SecondHepaticScintigraphy(this, ((ModelHepaticDynamic) this.parent.getModel()));
		try {
			this.vueBasic.run("");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setExamDone(boolean boobool) {
		this.examDone = boobool;
		((ModelHepaticDynamic)this.parent.getModel()).setExamDone(boobool);
	}

	public Scintigraphy getVueBasic() {
		return this.vueBasic;
	}

	public JTabbedPane getTabPane() {
		return this.tabPane;
	}

	public FenApplication getFenApplication() {
		return this.vueBasic.getFenApplication();
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		JSpinner spin = (JSpinner) arg0.getSource();
		this.deconvolvGraph.setDeconvolvFactor((int) spin.getValue());
		this.deconvolvGraph.reloadDisplay();
	}
}
