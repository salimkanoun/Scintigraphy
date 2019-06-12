package org.petctviewer.scintigraphy.cardiac.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabVisualGradationCardiac extends TabResult{

//	private JRadioButton grade0 = new JRadioButton("Grade 0"), grade1 = new JRadioButton("Grade 1"),
//			grade2 = new JRadioButton("Grade 2"), grade3 = new JRadioButton("Grade 3");

//	private ButtonGroup radio;
	
	private BufferedImage capture;

	private HashMap<String, String> resultsThorax;

	public TabVisualGradationCardiac(FenResults parent, String title, HashMap<String, String> resultsThorax,
			BufferedImage capture, int onlyThoraxImage) {
		super(parent, title, true);
		// TODO Auto-generated constructor stub

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
		// TODO Auto-generated method stub

		JPanel globalPane = new JPanel();
		globalPane.setLayout(new BoxLayout(globalPane, BoxLayout.Y_AXIS));
		
		JLabel heartToContralateral = new JLabel("Heart to contralateral : " + resultsThorax.get("Heart to contralateral"));
		heartToContralateral.setFont(new Font("Arial", Font.BOLD, 20));
		heartToContralateral.setHorizontalAlignment(JLabel.CENTER);
		heartToContralateral.setForeground(new Color(128, 51, 0));
		
		globalPane.add(heartToContralateral);
		globalPane.add(new JLabel(" "));
		globalPane.add(new JLabel(" "));
		
		globalPane.add(this.getCombo());

		return globalPane;
	}

	@Override
	public Container getResultContent() {
		// TODO Auto-generated method stub
		JPanel listPane = new JPanel();

		JPanel globalPane = new JPanel(new BorderLayout());
		globalPane.add(new DynamicImage(this.capture), BorderLayout.CENTER);

		listPane.setLayout(new GridLayout(2, 2));
		listPane.setBorder(BorderFactory.createTitledBorder("Visual Gradation"));

		String grade0 = "No myocardial uptake";
		String grade1 = "Myocardial uptake &lsaquo; bone uptake";
		String grade2 = "Myocardial uptake equal to bone uptake";
		String grade3 = "Myocardal uptake &rsaquo; bone uptake (with attenuatuon of bone uptake on whole body images";

		
		listPane.add( new JLabel("<html> &nbsp; Grade 0 : " + grade0+"</html>"));
		listPane.add(new JLabel("<html>  Grade 1 : " + grade1+"</html>"));
		listPane.add(new JLabel("<html> &nbsp; Grade 2 : " + grade2+"</html>"));
		listPane.add(new JLabel("<html>  Grade 3 : " + grade3+"</html>"));

		listPane.setPreferredSize(new Dimension(200, 150));
		globalPane.add(listPane, BorderLayout.SOUTH);

		return globalPane;

	}

	public JPanel getCombo() {

		JPanel globalPane = new JPanel();
		globalPane.setLayout(new GridLayout(2,1));

		globalPane.add(new JLabel("Select the Visual Gradation : "));

		Integer[] gradeString = { 0, 1, 2 , 3};

		JComboBox<Integer> gradeList = new JComboBox<>(gradeString);
		gradeList.setSelectedIndex(0);
		JPanel comboContainer = new JPanel();
		comboContainer.add(gradeList);

		globalPane.add(comboContainer);

		return globalPane;
	}

}
