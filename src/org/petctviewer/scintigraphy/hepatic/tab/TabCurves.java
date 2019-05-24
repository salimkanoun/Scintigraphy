package org.petctviewer.scintigraphy.hepatic.tab;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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

import org.petctviewer.scintigraphy.hepatic.ModelHepaticDynamic;
import org.petctviewer.scintigraphy.hepatic.SecondExam.ControllerWorkflowHepaticDyn;
import org.petctviewer.scintigraphy.hepatic.SecondExam.FenApplicationSecondHepaticDyn;
import org.petctviewer.scintigraphy.hepatic.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.model.ModelScin;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import ij.Prefs;
import ij.gui.Overlay;

public class TabCurves extends TabResult implements ActionListener, ChangeListener {

	private boolean examDone;

	private FenApplicationSecondHepaticDyn vueBasic;

	private JButton btn_addImp;

	private JButton buttonSwitchGraph;

	private JTabbedPane tabPane;
	// private List<TabResult> tabsResult;

	private TabDeconvolv deconvolvGraph;

	private TabTAC tabTAC;

	private JSpinner spinnerDeconvolve;

	private JSpinner spinnerConvolve;

	private JPanel sidePanelDeconvolve;
	private JPanel sidePanelTAC;

	public static final int CLASSICAL_SIDE_PANEL = 0, TABTAC_SIDE_PANEL = 1, DECONVOLVE_SIDE_PANEL = 2;

	private int currentSidePanel;

	public TabCurves(FenResults parent, String title) {
		super(parent, title, true);
		// TODO Auto-generated constructor stub
		this.setSidePanelTitle("Curves");
		((ModelHepaticDynamic) this.parent.getModel()).setResultTab(this);

		this.tabPane = new JTabbedPane();
		// this.tabsResult = new ArrayList<>();

		this.getResultContent().add(tabPane);

		this.currentSidePanel = 1;

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		if (this.examDone) {
			switch (currentSidePanel) {
			case TABTAC_SIDE_PANEL:
				return this.sidePanelTabTAC();
			case DECONVOLVE_SIDE_PANEL:
				return this.sidePanelTabDeconvolve();
			default:
				return this.sidePanelClassical();
			}
		} else
			return null;
	}

	@Override
	public Container getResultContent() {
		if (!this.examDone) {
			JPanel pan = new JPanel();
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());

			btn_addImp = new JButton("Start the exam");
			btn_addImp.addActionListener(this);
			box.add(btn_addImp);
			box.add(Box.createHorizontalGlue());

			pan.add(box);
			// pan.setPreferredSize(new Dimension(1050, 700));
			return pan;

		} else {
			JPanel panelDeFin = new JPanel();
			this.tabPane = new JTabbedPane();
			this.tabTAC = new TabTAC(this.getParent(), this);
			TabVasculaire tabVasculaire = new TabVasculaire(this.getParent(), this);

			this.tabPane.addTab(this.tabTAC.getTitle(), this.tabTAC.getPanel());
			this.tabPane.addTab(tabVasculaire.getTitle(), tabVasculaire.getPanel());
			if (Prefs.get("petctviewer.scin.experimental", false)) {
				this.deconvolvGraph = new TabDeconvolv(this.getParent(), this);
				this.tabPane.addTab(deconvolvGraph.getTitle(), deconvolvGraph.getPanel());
			}

			this.tabPane.addChangeListener(this);

			panelDeFin.add(this.tabPane);
			// panelDeFin.setPreferredSize(new Dimension(1050, 700));
			return tabPane;
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JButton button = (JButton) arg0.getSource();
		if (button == btn_addImp) {

			ModelScin model = TabCurves.this.parent.getModel();
			ImageSelection[] ims = model.getImageSelection();
			ImageSelection[] selectedImages = new ImageSelection[] { ims[1], ims[2], ims[3], ims[4] };
			Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 12);
			Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.YELLOW);

			this.vueBasic = new FenApplicationSecondHepaticDyn(selectedImages[0], model.getStudyName());
			selectedImages[0].getImagePlus().setOverlay(overlay);

			this.vueBasic.setControleur(
					new ControllerWorkflowHepaticDyn(this.vueBasic, new ModelSecondMethodHepaticDynamic(selectedImages,
							model.getStudyName(), ((ModelScinDyn) model).getFrameduration()), this));

		} else if (button == buttonSwitchGraph) {
			this.tabTAC.switchGraph(this.buttonSwitchGraph);
		}

	}

	public void setExamDone(boolean boobool) {
		this.examDone = boobool;
		((ModelHepaticDynamic) this.parent.getModel()).setExamDone(boobool);
	}

	public JTabbedPane getTabPane() {
		return this.tabPane;
	}

	public FenApplication getFenApplication() {
		return this.vueBasic;
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if (arg0.getSource() instanceof JSpinner) {

			JSpinner spin = (JSpinner) arg0.getSource();
			if (spin == this.spinnerDeconvolve) {
				this.deconvolvGraph.setDeconvolvFactor((int) spin.getValue());
			} else if (spin == this.spinnerConvolve) {
				this.deconvolvGraph.setConvolvFactor((int) spin.getValue());
			}
			this.deconvolvGraph.reloadDisplay();

		} else {
			int index = -1;
			switch (this.tabPane.getSelectedIndex()) {
			case 0:
				this.currentSidePanel = TabCurves.TABTAC_SIDE_PANEL;
				break;
			case 2:
				this.currentSidePanel = TabCurves.DECONVOLVE_SIDE_PANEL;
				break;
			default:
				this.currentSidePanel = TabCurves.CLASSICAL_SIDE_PANEL;
				break;
			}
			index = this.tabPane.getSelectedIndex();
			this.reloadSidePanelContent();
			this.parent.repaint();
			this.parent.revalidate();
			this.parent.pack();
			this.tabPane.setSelectedIndex(index);
		}
	}

	public JPanel sidePanelTabDeconvolve() {

		if (this.sidePanelDeconvolve == null) {
			JPanel resultPane = new JPanel(new GridLayout(0, 2));

			this.spinnerDeconvolve = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
			spinnerDeconvolve.addChangeListener(this);

			this.spinnerConvolve = new JSpinner(new SpinnerNumberModel(6, 1, 100, 1));
			spinnerConvolve.addChangeListener(this);

			resultPane.add(new JLabel("Number of convolution : "));
			resultPane.add(spinnerConvolve);
			resultPane.add(new JLabel("Initial value of the deconvolution : "));
			resultPane.add(spinnerDeconvolve);
			this.sidePanelDeconvolve = resultPane;
		}

		return this.sidePanelDeconvolve;
	}

	public JPanel sidePanelTabTAC() {
		if (this.sidePanelTAC == null) {

			JPanel resultPane = this.sidePanelClassical();

			resultPane.add(new JLabel(""));
			this.buttonSwitchGraph = new JButton("Single Graph");
			this.buttonSwitchGraph.addActionListener(this);

			resultPane.add(buttonSwitchGraph);
			this.sidePanelTAC = resultPane;
		}

		return this.sidePanelTAC;
	}

	public JPanel sidePanelClassical() {
		JPanel resultPane = new JPanel(new GridLayout(0, 2));
		HashMap<String, String> results = ((ModelSecondMethodHepaticDynamic) this.vueBasic.getControleur().getModel())
				.getResultsHashMap();
		String[] keys = { "T1/2 Righ Liver", "", "Maximum Right Liver", "end/max Ratio Right", "T1/2 Left Liver", "",
				"Maximum Left Liver", "end/max Ratio Left", "T1/2 Blood pool", "", "Blood pool ratio 20mn/5mn" };
		for (String s : keys) {
			if (s.equals(""))
				resultPane.add(new JLabel(""));
			else
				resultPane.add(new JLabel(s + " : " + results.get(s)));
		}

		return resultPane;
	}

	public void setSidePanel(int sidePanelNumber) {
		this.currentSidePanel = sidePanelNumber;
	}

}
