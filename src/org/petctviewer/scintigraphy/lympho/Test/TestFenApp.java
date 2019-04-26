package org.petctviewer.scintigraphy.lympho.Test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.petctviewer.scintigraphy.lympho.FenApplicationLympho;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

import ij.ImagePlus;

public class TestFenApp extends JFrame implements ActionListener, ChangeListener{

	private static final long serialVersionUID = -7721675828264590938L;

	private FenApplication fenApplication;
	ControleurScin controller;
	
	private JSlider slider;
	private JLabel sliderLabel;
	protected Box boxSlider;
	protected JButton reverseButton;
	
	private ImagePlus imp;

	public TestFenApp(ImagePlus imagePlus, String studyName) {
		this.fenApplication = new FenApplicationLympho(imagePlus, studyName);
		this.fenApplication.setVisible(false);
		// TODO Auto-generated constructor stub
		Component[] compo = this.fenApplication.getComponents();
		
		this.imp = imagePlus;

		Panel borderLayout = new Panel(new BorderLayout());
		this.add(borderLayout);

		JPanel pan_center = new JPanel();
		// for (Component component : compo)
		// pan_center.add(component);
		for (int i = 0; i < 1; i++) {
			pan_center.add(compo[i]);
		}
		SidePanel sidePanel;
		
		
		sidePanel = new SidePanel(null, "Bone scintigraphy", imagePlus);
		
		this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.slider = new JSlider(SwingConstants.HORIZONTAL, 0, (int) imagePlus.getStatistics().max, 4);
		slider.addChangeListener(this);

		this.boxSlider = Box.createVerticalBox();
		boxSlider.add(this.sliderLabel);
		boxSlider.add(this.slider);
		
		
		this.reverseButton = new JButton("Inverser");														// Boutton inversant le contraste.
		this.reverseButton.setPreferredSize(new Dimension(200, 40));
		this.reverseButton.addActionListener(this);

		JPanel gbl = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 2;       //third row
		c.insets = new Insets(10,0,0,0);  //top padding
		gbl.add(boxSlider);
		gbl.add(this.reverseButton,c);
		
		sidePanel.addContent(gbl);
		
		borderLayout.add(pan_center, BorderLayout.CENTER);
		borderLayout.add(sidePanel, BorderLayout.WEST);

		this.setVisible(true);
		this.pack();
	}

	public void setControleur(ControleurScin controllerWorkflowLympho) {
		this.controller = controllerWorkflowLympho;

	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.imp.setLut(this.imp.getLuts()[0].createInvertedLut());

	}
	
	void setContrast(JSlider slider) {
		this.imp.getProcessor().setMinAndMax(0, (slider.getModel().getMaximum() - slider.getValue())+1);	// On change son contraste.
		this.imp.updateAndRepaintWindow();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider) e.getSource();
		this.setContrast(slider);
	}
}
