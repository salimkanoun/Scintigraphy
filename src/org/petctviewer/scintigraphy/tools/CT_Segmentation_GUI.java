package org.petctviewer.scintigraphy.tools;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ij.ImagePlus;
import ij.WindowManager;

import javax.swing.JRadioButton;
import javax.swing.JButton;
import java.awt.GridLayout;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Font;

@SuppressWarnings("serial")
public class CT_Segmentation_GUI extends JFrame {

	private JPanel contentPane;
	private CT_Segmentation segmentation;
	private ButtonGroup group ;
	private JButton btnGenerateMaskedImage;

	/**
	 * Launch the application.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CT_Segmentation_GUI frame = new CT_Segmentation_GUI();
					frame.pack();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CT_Segmentation_GUI() {
		makeGui();
	}
	
	public CT_Segmentation_GUI(CT_Segmentation segmentation) {
		makeGui();
		this.segmentation=segmentation;
	}
	
	public JButton getGenerateMaskButton() {
		return btnGenerateMaskedImage;
	}
	
	protected void makeGui() {
		setTitle("CT Segmentation");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel Main_Panel = new JPanel();
		contentPane.add(Main_Panel, BorderLayout.CENTER);
		Main_Panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(Color.LIGHT_GRAY));
		Main_Panel.add(panel_2, BorderLayout.NORTH);
		
		JPanel Grid_Panel = new JPanel();
		panel_2.add(Grid_Panel);
		Grid_Panel.setLayout(new GridLayout(3, 2, 0, 0));
		
		JLabel lblNa = new JLabel("N/A");
		
		JButton btnSelectImagejStack = new JButton("Select ImageJ Stack");
		btnSelectImagejStack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ImagePlus imageInput=WindowManager.getCurrentImage();
				lblNa.setText(imageInput.getTitle());
				segmentation.setImageInput(imageInput);
			}
		});
		
		Grid_Panel.add(btnSelectImagejStack);	
		Grid_Panel.add(lblNa);
		
		JLabel lblStatusIdle = new JLabel("Status : Idle");
		
		JButton btnCalculateSegmentation = new JButton("Calculate Segmentation");
		btnCalculateSegmentation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				segmentation.runSegmentation(lblStatusIdle);
			}
		});
		Grid_Panel.add(btnCalculateSegmentation);
		
	
		Grid_Panel.add(lblStatusIdle);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		Grid_Panel.add(horizontalStrut);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(Color.LIGHT_GRAY));
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new GridLayout(0, 3, 0, 0));
		
		JRadioButton rdbtnBone = new JRadioButton("Bone");
		rdbtnBone.setActionCommand("Bone");
		rdbtnBone.setSelected(true);
		panel_1.add(rdbtnBone);
		
		JRadioButton rdbtnSoftTissue = new JRadioButton("Soft Tissue");
		rdbtnSoftTissue.setActionCommand("Soft Tissue");
		panel_1.add(rdbtnSoftTissue);
		
		JRadioButton rdbtnGrease = new JRadioButton("Fat Tissue");
		rdbtnGrease.setActionCommand("Fat Tissue");
		panel_1.add(rdbtnGrease);
		
		JRadioButton rdbtnLung = new JRadioButton("Lung");
		rdbtnLung.setActionCommand("Lung");
		panel_1.add(rdbtnLung);
		
		JRadioButton rdbtnOutside = new JRadioButton("Outside");
		rdbtnOutside.setActionCommand("Outside");
		panel_1.add(rdbtnOutside);
		
		btnGenerateMaskedImage = new JButton("Generate Masked Image");
		btnGenerateMaskedImage.setEnabled(false);
		btnGenerateMaskedImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String selectedButton =group.getSelection().getActionCommand();
				
				Integer i=null;
				if (selectedButton.equals("Bone")) i=0;
				else if (selectedButton.equals("Soft Tissue")) i=1;
				else if (selectedButton.equals("Fat Tissue")) i=2;
				else if (selectedButton.equals("Outside")) i=3;
				else if (selectedButton.equals("Lung")) i=4;
				
				segmentation.makeMaskedImage(i);
			}
		});
		panel.add(btnGenerateMaskedImage, BorderLayout.EAST);
		
		JLabel lblPowredByWeka = new JLabel("Powered by Weka Trainable Segmentation");
		lblPowredByWeka.setFont(new Font("Dialog", Font.BOLD, 10));
		panel.add(lblPowredByWeka, BorderLayout.SOUTH);
		lblPowredByWeka.setHorizontalAlignment(SwingConstants.CENTER);
		
		group = new ButtonGroup();
		group.add(rdbtnBone);
		group.add(rdbtnSoftTissue);
		group.add(rdbtnGrease);
		group.add(rdbtnLung);
		group.add(rdbtnOutside);
	}

}
