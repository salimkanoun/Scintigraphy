package org.petctviewer.scintigraphy.hepatic.dynRefactored.tab;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.petctviewer.scintigraphy.hepatic.dynRefactored.ModelHepaticDynamic;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.SecondHepaticScintigraphy;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabOtherMethod extends TabResult implements ActionListener {

	private boolean examDone;

	private SecondHepaticScintigraphy vueBasic;

	private JButton btn_addImp;

	private JTabbedPane tabPane;
	private List<TabResult> tabsResult;

	public TabOtherMethod(FenResults parent, String title) {
		super(parent, title, true);
		// TODO Auto-generated constructor stub
		this.setSidePanelTitle("Other Method");
		((ModelHepaticDynamic) this.parent.getModel()).setResultTab(this);

		this.tabPane = new JTabbedPane();
		this.tabsResult = new ArrayList<>();

		this.getPanel().add(tabPane);

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		// TODO Auto-generated method stub
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
			// TabVasculaire tabVasculaire = new TabVasculaire(this.resultTab.getParent());
			this.tabPane.addTab(tabTac.getTitle(), tabTac.getPanel());
			// this.tabPane.addTab(tabVasculaire.getTitle(),tabVasculaire.getPanel());

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
	}

	public Scintigraphy getVueBasic() {
		return this.vueBasic;
	}

	public JTabbedPane getTabPane() {
		return this.tabPane;
	}

	public SecondHepaticScintigraphy getFenApplication() {
		return this.vueBasic;
	}
}
