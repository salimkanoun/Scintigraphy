package org.petctviewer.scintigraphy.hepatic.dynRefactored;

import java.awt.Button;
import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.hepatic.dynRefactored.tab.TabMainHepaticDyn;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.tab.TabOtherMethod;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

public class ControllerHepaticDynamic extends ControleurScin implements MouseListener, ActionListener {

	public static final String COMMAND_END = "command.end";

	public ControllerHepaticDynamic(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, main.getFenApplication(), model);
		System.out.println("MODEL DEPUIS LE CONTROLLER : " + (this.model != null));
		System.out.println("ok");
	}

	@Override
	public boolean isOver() {
		return false;
	}

	public void end() {
		String value1 = ((FenApplicationHepaticDynamic) this.vue).getTextLabel1();
		String value2 = ((FenApplicationHepaticDynamic) this.vue).getTextLabel2();
		String value3 = ((FenApplicationHepaticDynamic) this.vue).getTextLabel3();
		if (value1 == "" || value2 == "" || value3 == "") {
			JOptionPane.showConfirmDialog(getVue(), "You have to select a slice for each time", "Missing a time",
					JOptionPane.DEFAULT_OPTION);
			return;
		}
		((ModelHepaticDynamic) this.model).setTimes(Integer.valueOf(value1), Integer.valueOf(value2),
				Integer.valueOf(value3));
		FenResults fenResult = new FenResults(this);
		fenResult.setMainTab(new TabMainHepaticDyn(fenResult, ((ModelHepaticDynamic) this.model)));
		fenResult.addTab(new TabOtherMethod(fenResult, "Other Method"));
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getSource() instanceof Label) {
			Label label = (Label) e.getSource();
			this.vue.getImagePlus().setSlice(Integer.valueOf(label.getText() != "" ? label.getText() : "1"));
		} else {
			Button button = (Button) e.getSource();
			int buttonNumber = ((FenApplicationHepaticDynamic) this.vue).getButtonNumber(button);
			((FenApplicationHepaticDynamic) this.vue).setLabelText("" + this.vue.getImagePlus().getCurrentSlice(),
					buttonNumber);
			((FenApplicationHepaticDynamic) this.vue).setLabelBackground(new Color(147, 142, 222),buttonNumber);
			((ModelHepaticDynamic) this.model).setCapture(this.vue.getImagePlus(), buttonNumber - 1);
			
		}

	}

	public void mousePressed(MouseEvent e) {
		if (e.getSource() instanceof Label) {
			Label label = (Label) e.getSource();
			if (label.getText() == "")
				label.setBackground(new Color(229, 77, 77));
			else
				label.setBackground(new Color(124, 118, 218));
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getSource() instanceof Label) {
			Label label = (Label) e.getSource();
			if (label.getText() == "")
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

		if (!(e.getSource() instanceof Button))
			return;

		Button source = (Button) e.getSource();
		if (source.getActionCommand().contentEquals(COMMAND_END)) {
			this.end();
		}
	}

}
