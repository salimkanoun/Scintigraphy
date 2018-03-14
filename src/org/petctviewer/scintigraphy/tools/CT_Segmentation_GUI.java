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
import java.awt.FlowLayout;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class CT_Segmentation_GUI extends JFrame {

	private JPanel contentPane;
	private CT_Segmentation segmentation;

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
	
	protected void makeGui() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel Main_Panel = new JPanel();
		contentPane.add(Main_Panel, BorderLayout.CENTER);
		Main_Panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel Grid_Panel = new JPanel();
		Main_Panel.add(Grid_Panel);
		Grid_Panel.setLayout(new GridLayout(2, 2, 0, 0));
		
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
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		panel_1.setLayout(new GridLayout(0, 3, 0, 0));
		
		JRadioButton rdbtnBone = new JRadioButton("Bone");
		panel_1.add(rdbtnBone);
		
		JRadioButton rdbtnSoftTissue = new JRadioButton("Soft Tissue");
		panel_1.add(rdbtnSoftTissue);
		
		JRadioButton rdbtnGrease = new JRadioButton("Fat Tissue");
		panel_1.add(rdbtnGrease);
		
		JRadioButton rdbtnLung = new JRadioButton("Lung");
		panel_1.add(rdbtnLung);
		
		JRadioButton rdbtnOutside = new JRadioButton("Outside");
		panel_1.add(rdbtnOutside);
		
		JButton btnGenerateMaskedImage = new JButton("Generate Masked Image");
		btnGenerateMaskedImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				segmentation.makeMaskedImage(0);
			}
		});
		panel.add(btnGenerateMaskedImage);
	}

}
