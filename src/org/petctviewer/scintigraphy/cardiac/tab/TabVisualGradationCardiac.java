package org.petctviewer.scintigraphy.cardiac.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.cardiac.Model_Cardiac;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabVisualGradationCardiac extends TabResult implements ActionListener {

//	private JRadioButton grade0 = new JRadioButton("Grade 0"), grade1 = new JRadioButton("Grade 1"),
//			grade2 = new JRadioButton("Grade 2"), grade3 = new JRadioButton("Grade 3");

//	private ButtonGroup radio;

	private final BufferedImage capture;

	private final HashMap<String, String> resultsThorax;

	public TabVisualGradationCardiac(FenResults parent, String title, HashMap<String, String> resultsThorax,
									 BufferedImage capture, int onlyThoraxImage) {
		super(parent, title, true);

//		radio = new ButtonGroup();

//		grade0.addActionListener(this);
//		grade1.addActionListener(this);
//		grade2.addActionListener(this);
//		grade3.addActionListener(this);

//		radio.add(grade0);
//		radio.add(grade1);
//		radio.add(grade2);
//		radio.add(grade3);

		this.capture = capture;

		this.resultsThorax = resultsThorax;

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {

		JPanel globalPane = new JPanel();
		globalPane.setLayout(new BoxLayout(globalPane, BoxLayout.Y_AXIS));

		JPanel container = new JPanel();
		JLabel heartToContralateral = new JLabel("Heart to contralateral: " + resultsThorax.get("Heart to contralateral"));
		heartToContralateral.setFont(new Font("Arial", Font.BOLD, 15));
		heartToContralateral.setHorizontalAlignment(JLabel.CENTER);
		heartToContralateral.setForeground(new Color(128, 51, 0));

		container.add(heartToContralateral);
		globalPane.add(container);
		globalPane.add(new JLabel(" "));
		globalPane.add(new JLabel(" "));

		globalPane.add(this.getCombo());

		return globalPane;
	}

	@Override
	public Container getResultContent() {
		JPanel listPane = new JPanel();

		JPanel globalPane = new JPanel(new BorderLayout());
		DynamicImage dynImg = new DynamicImage(this.capture);
		dynImg.setPreferredSize(new Dimension(30,30));
		globalPane.add(dynImg, BorderLayout.NORTH);

		listPane.setLayout(new GridLayout(2, 2));
		listPane.setBorder(BorderFactory.createTitledBorder("Visual Gradation"));

		String grade0 = "No myocardial uptake";
		String grade1 = "Myocardial uptake &lsaquo; gril costal";
		String grade2 = "Myocardial uptake equal to gril costal";
		String grade3 = "Myocardial uptake &rsaquo; gril costal (with attenuation of gril costal on whole body images";


		listPane.add(new JLabel("<html> &nbsp; Grade 0 : " + grade0 + "</html>"));
		listPane.add(new JLabel("<html>  Grade 1 : " + grade1 + "</html>"));
		listPane.add(new JLabel("<html> &nbsp; Grade 2 : " + grade2 + "</html>"));
		listPane.add(new JLabel("<html>  Grade 3 : " + grade3 + "</html>"));

		listPane.setPreferredSize(new Dimension(200, 150));
		globalPane.add(listPane, BorderLayout.SOUTH);

		return globalPane;

	}

	public JPanel getCombo() {

		JPanel globalPane = new JPanel();
		globalPane.setLayout(new GridLayout(2, 1));

		globalPane.add(new JLabel("Select the Visual Gradation : "));

		Integer[] gradeString = {0, 1, 2, 3};

		JComboBox<Integer> gradeList = new JComboBox<>(gradeString);
		gradeList.setSelectedIndex(0);
		JPanel comboContainer = new JPanel();
		comboContainer.add(gradeList);

		globalPane.add(comboContainer);
		
		gradeList.addActionListener(this);

		return globalPane;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource() instanceof JComboBox)
			((Model_Cardiac)this.parent.getModel()).setOnlyThoraxGradation((Integer)((JComboBox<?>)arg0.getSource()).getSelectedItem());
	}

}