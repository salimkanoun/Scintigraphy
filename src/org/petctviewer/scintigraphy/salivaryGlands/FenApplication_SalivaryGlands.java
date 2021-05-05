package org.petctviewer.scintigraphy.salivaryGlands;

import ij.ImagePlus;
import ij.gui.Overlay;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

public class FenApplication_SalivaryGlands extends FenApplicationWorkflow {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final SalivaryGlandsScintigraphy main;
	private boolean dyn;
	private final ImagePlus impProj;
	private final ImageSelection imsProj;
	private final Button btn_start;

	public FenApplication_SalivaryGlands(ImageSelection ims, String nom, SalivaryGlandsScintigraphy main) {
		super(ims, nom);
		// Keep default visualisation
		this.setVisualizationEnable(false);
		
		this.main = main;
		this.imsProj = ims;
		

		Overlay overlay = Library_Gui.initOverlay(this.getImagePlus(), 12);
		this.getImagePlus().setOverlay(overlay);
		Library_Gui.setOverlayDG(this.getImagePlus(), Color.YELLOW);

		btn_start = new Button("Start");
		btn_start.addActionListener(this);

		this.getPanel_bttns_droit().removeAll();
		this.getPanel_bttns_droit().add(btn_start);

		this.getBtn_drawROI().setEnabled(false);

		this.getBtn_drawROI().setEnabled(false);

		this.setDefaultSize();

		
		this.impProj = imp;
		
		this.pack();

	}

	@Override
	public void setController(ControllerScin ctrl) {
		super.setController(ctrl);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.btn_start) {
			FenSelectLemonInjection dialogFen = new FenSelectLemonInjection();
			dialogFen.setLocationRelativeTo(this);
			dialogFen.setModal(true);
			((ControllerWorkflowSalivaryGlands) this.getController()).setLemonJuiceInjection(dialogFen.getLemonJuiceInjection());

			this.getBtn_reverse().setEnabled(true);
			this.getPanel_bttns_droit().removeAll();
			this.getPanel_bttns_droit().add(this.createPanelInstructionsBtns());
			this.getBtn_drawROI().setEnabled(true);
			this.setImage(impProj);
			this.updateSliceSelector();
			resizeCanvas();
		}

		super.actionPerformed(e);
	}

	private class FenSelectLemonInjection extends JDialog {
		private JSpinner timeLemonInjection;
		private JButton saveBtn;

		public FenSelectLemonInjection() {
			this.setTitle("Lemon Juice Injection");
			this.setAlwaysOnTop(true);

			JPanel container = new JPanel(new GridLayout(3, 1, 0, 10));
			container.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

			container.add(new JLabel("Indicate when lemon juice was injected"));

			JPanel input = new JPanel(new GridLayout(1, 2, 15, 0));
			SpinnerModel model = new SpinnerNumberModel(10, 1, 30, 0.1);
			this.timeLemonInjection = new JSpinner(model);
			input.add(this.timeLemonInjection);
			input.add(new JLabel("min"));
			container.add(input);

			this.saveBtn = new JButton("Save");
			this.saveBtn.addActionListener(e -> this.dispose());
			container.add(this.saveBtn);

			this.add(container);

			this.pack();
			this.setVisible(true);
		}

		public double getLemonJuiceInjection() {
			return (Double) this.timeLemonInjection.getValue();
		}
	}
}