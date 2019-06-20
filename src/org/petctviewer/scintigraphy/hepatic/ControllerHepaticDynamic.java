package org.petctviewer.scintigraphy.hepatic;

import ij.IJ;
import ij.ImagePlus;
import org.petctviewer.scintigraphy.hepatic.tab.TabCurves;
import org.petctviewer.scintigraphy.hepatic.tab.TabMainHepaticDyn;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.CaptureButton;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.json.SaveAndLoad;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class ControllerHepaticDynamic extends ControllerScin implements MouseListener, ActionListener {

	public static final String COMMAND_END = "command.end";

	public ControllerHepaticDynamic(FenApplication vue, ModelScin model) {
		super(vue, model);
	}

	@Override
	public boolean isOver() {
		return false;
	}

	@Override
	public void end() {
		String value1 = ((FenApplicationHepaticDynamic) this.vue).getTextLabel1();
		String value2 = ((FenApplicationHepaticDynamic) this.vue).getTextLabel2();
		String value3 = ((FenApplicationHepaticDynamic) this.vue).getTextLabel3();
		if (value1.isEmpty() || value2.isEmpty() || value3.isEmpty()) {
			JOptionPane.showConfirmDialog(getVue(), "You have to select a slice for each time", "Missing a time",
					JOptionPane.DEFAULT_OPTION);
			return;
		}
		((ModelHepaticDynamic) this.model).setTimes(Integer.valueOf(value1), Integer.valueOf(value2),
				Integer.valueOf(value3));
		FenResults fenResult = new FenResults(this);
		fenResult.setMainTab(new TabMainHepaticDyn(fenResult, ((ModelHepaticDynamic) this.model)));
		fenResult.addTab(new TabCurves(fenResult, "Curves second method"));

		fenResult.setPreferredSize(new Dimension(1280, 720));
		fenResult.pack();

		fenResult.setVisible(true);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getSource() instanceof Label) {
			Label label = (Label) e.getSource();
			this.vue.getImagePlus().setSlice(Integer.valueOf(!label.getText().isEmpty() ? label.getText() : "1"));
		}

	}

	public void mousePressed(MouseEvent e) {
		if (e.getSource() instanceof Label) {
			Label label = (Label) e.getSource();
			if (label.getText().isEmpty())
				label.setBackground(new Color(229, 77, 77));
			else
				label.setBackground(new Color(124, 118, 218));
		} else {
			Button button = (Button) e.getSource();
			int buttonNumber = ((FenApplicationHepaticDynamic) this.vue).getButtonNumber(button);
			((FenApplicationHepaticDynamic) this.vue).setLabelText("" + this.vue.getImagePlus().getCurrentSlice(),
					buttonNumber);
			((FenApplicationHepaticDynamic) this.vue).setLabelBackground(new Color(147, 142, 222), buttonNumber);
			((ModelHepaticDynamic) this.model).setCapture(this.vue.getImagePlus(), buttonNumber - 1);

		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getSource() instanceof Label) {
			Label label = (Label) e.getSource();
			if (label.getText().isEmpty())
				label.setBackground(new Color(233, 118, 118));
			else
				label.setBackground(new Color(147, 142, 222));
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if ((e.getSource() instanceof Button)) {
			Button source = (Button) e.getSource();
			if (source.getActionCommand().contentEquals(COMMAND_END)) 
				this.end();
			
		} else if (e.getSource() instanceof CaptureButton)
			this.actionCaptureButton((CaptureButton) e.getSource());
	}

	public void actionCaptureButton(CaptureButton captureButton) {

		TabResult tab = captureButton.getTabResult();
		JLabel lbl_credits = captureButton.getLabelCredits();
		Component[] hide = tab.getComponentToHide();
		Component[] show = tab.getComponentToShow();
		String additionalInfo = tab.getAdditionalInfo();

		// generation du tag info
		String info = Library_Capture_CSV.genererDicomTagsPartie1(tab.getParent().getModel().getImagePlus(),
				tab.getParent().getModel().getStudyName(), tab.getParent().getModel().getUID6digits())
				+ Library_Capture_CSV.genererDicomTagsPartie2(tab.getParent().getModel().getImagePlus());

		captureButton.setVisible(false);
		for (Component comp : hide)
			comp.setVisible(false);

		lbl_credits.setVisible(true);
		for (Component comp : show)
			comp.setVisible(true);

		SwingUtilities.invokeLater(() -> {
			// Capture, nouvelle methode a utiliser sur le reste des programmes
			BufferedImage capture = new BufferedImage(tab.getPanel().getWidth(), tab.getPanel().getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			tab.getPanel().paint(capture.getGraphics());
			ImagePlus imp = new ImagePlus("capture", capture);

			captureButton.setVisible(true);
			for (Component comp : hide)
				comp.setVisible(true);

			lbl_credits.setVisible(false);
			for (Component comp : show)
				comp.setVisible(false);

			// on passe a la capture les infos de la dicom
			imp.setProperty("Info", info);
			// on affiche la capture
			imp.show();

			// on change l'outil
			IJ.setTool("hand");

			// generation du csv
			String resultats = tab.getParent().getModel().toString();

			try {
				SaveAndLoad saveAndLoad = new SaveAndLoad();
				saveAndLoad.exportAllWithoutWorkflow(resultats, tab.getParent().getModel().getStudyName(), imp,
						additionalInfo);

				imp.killRoi();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			// Execution du plugin myDicom
			try {
				IJ.run("myDicom...");
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			System.gc();
		});

	}

}
