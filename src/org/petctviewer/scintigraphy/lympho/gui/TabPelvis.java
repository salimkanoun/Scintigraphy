package org.petctviewer.scintigraphy.lympho.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.lympho.ModelLympho;
import org.petctviewer.scintigraphy.lympho.pelvis.PelvisScintigraphy;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabPelvis extends TabResult implements ActionListener {

	private boolean examDone;

	private PelvisScintigraphy vueBasic;

	public TabPelvis(FenResults parent, String title, boolean captureBtn) {
		super(parent, title, captureBtn);
		// TODO Auto-generated constructor stub
		this.setSidePanelTitle("Pelvis Scintigraphy");
		((ModelLympho) this.parent.getModel()).setResultTab(this);
		
		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		if (!this.examDone) {
			return null;
		} else {
			String[] result = ((ModelLympho) this.getParent().getModel()).getResultPelvis();
			JPanel res = new JPanel(new GridLayout(result.length, 1));
			for (String s : result)
				res.add(new JLabel(s));
			return res;
		}
	}

	@Override
	public JPanel getResultContent() {
		if (!this.examDone) {
			JPanel pan = new JPanel();
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());

			JButton btn_addImp = new JButton("Choose Pelvis dicom");
			btn_addImp.addActionListener(this);
			box.add(btn_addImp);
			box.add(Box.createHorizontalGlue());

			pan.add(box);
			return pan;

		} else {
			return new DynamicImage(
					((ModelLympho) this.getParent().getModel())
							.getPelvisMontage().getImage());
		}

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.vueBasic = new PelvisScintigraphy("Post Scinty", this);
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

}