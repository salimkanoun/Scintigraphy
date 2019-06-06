package org.petctviewer.scintigraphy.lympho.gui;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class TabVisualGradation extends TabResult implements ActionListener {

	private JRadioButton l0 = new JRadioButton("L0"), p1 = new JRadioButton("P1"), p2 = new JRadioButton("P2"),
			p3 = new JRadioButton("P3"), t4 = new JRadioButton("T4"), t5 = new JRadioButton("T5"),
			t6 = new JRadioButton("T6");

	private ButtonGroup radio = new ButtonGroup();

	private boolean imgSelected;

	private JButton btn_addImp;

	private JButton btn_switchLimb;

	private boolean upperLimb;

	private ImageSelection img;

	public TabVisualGradation(FenResults parent, String title) {
		super(parent, title, true);
		// TODO Auto-generated constructor stub

		this.imgSelected = false;

		l0.addActionListener(this);
		p1.addActionListener(this);
		p2.addActionListener(this);
		p3.addActionListener(this);
		t4.addActionListener(this);
		t5.addActionListener(this);
		t6.addActionListener(this);

		radio.add(this.l0);
		radio.add(this.p1);
		radio.add(this.p2);
		radio.add(this.p3);
		radio.add(this.t4);
		radio.add(this.t5);
		radio.add(this.t6);

		this.reloadDisplay();
	}

	@Override
	public Component getSidePanelContent() {
		// TODO Auto-generated method stub

		if (!this.imgSelected)
			return null;
		else {

			JPanel globalPane = new JPanel();
			globalPane.setLayout(new BoxLayout(globalPane, BoxLayout.Y_AXIS));
			JPanel pan = new JPanel(new BorderLayout());

			pan = this.getRadio();

			this.btn_switchLimb = new JButton(this.upperLimb ? "Change to lower limbs" : "Change to upper limbs");
			this.btn_switchLimb.addActionListener(this);
			JPanel container = new JPanel();
			container.add(this.btn_switchLimb);

			// pan.add(container, BorderLayout.NORTH);

			globalPane.add(container);
			globalPane.add(pan);

			return globalPane;
		}
	}

	@Override
	public Container getResultContent() {
		// TODO Auto-generated method stub

		JPanel pan = new JPanel();

		if (!this.imgSelected) {
			this.btn_addImp = new JButton("Start Visual Gradiation");
			this.btn_addImp.addActionListener(this);
			pan.add(btn_addImp);
		} else
			pan = this.getImages(this.upperLimb);

		return pan;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() instanceof JButton) {
			JButton button = (JButton) arg0.getSource();
			if (button == btn_addImp) {

				CustomScintigraphy scin = new CustomScintigraphy(this);

				FenSelectionDicom fen = new FenSelectionDicom( scin);
				fen.setVisible(true);

			} else if (button == btn_switchLimb) {
				this.switchLimb(this.btn_switchLimb);
			}
		} else if (arg0.getSource() instanceof JRadioButton) {
			JRadioButton radioButton = (JRadioButton) arg0.getSource();
			String gradation = null;
			if(radioButton.getText().contains("0")) 
				gradation = "Normal";
			else if(radioButton.getText().contains("1"))
				gradation = "Partial obstruction - Score 1";
			else if(radioButton.getText().contains("2"))
				gradation = "Partial obstruction - Score 2";
			else if(radioButton.getText().contains("3"))
				gradation = "Partial obstruction - Score 3";
			else if(radioButton.getText().contains("4"))
				gradation = "Total obstruction - Score 4";
			else if(radioButton.getText().contains("5"))
				gradation = "Total obstruction - Score 5";
			else if(radioButton.getText().contains("6"))
				gradation = "Total obstruction - Score 6";
			
			
			((FenResultatsLympho) this.parent).updateVisualGradation("Visual Gradation : " + gradation);
		}

	}

	public void setimgSelected(boolean boobool) {
		this.imgSelected = boobool;
	}

	public void switchLimb(JButton limbButton) {
		this.upperLimb = !this.upperLimb;

		this.reloadDisplay();
	}

	public JPanel getRadio() {

		JPanel globalPane = new JPanel();
		globalPane.setLayout(new BoxLayout(globalPane, BoxLayout.Y_AXIS));

		JLabel label = new JLabel("Visual Gradation");

		globalPane.add(label);

		JPanel pan = new JPanel(new GridLayout(4, 2));

		pan.add(this.l0);
		pan.add(this.p1);
		pan.add(this.p2);
		pan.add(this.p3);
		pan.add(this.t4);
		pan.add(this.t5);
		pan.add(this.t6);

		globalPane.add(pan);

		return globalPane;
	}

	public JPanel getImages(boolean upperlimb) {

		JPanel globalPane = new JPanel(new BorderLayout());

		String limb = upperLimb ? "upper" : "lower";

		// JPanel pan = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JPanel pan = new JPanel(new GridLayout(1, 7));

		JPanel l0 = null, p1 = null, p2 = null, p3 = null, t4 = null, t5 = null, t6 = null;
		try {
			l0 = new JPanel(new BorderLayout());
			l0.add(new JLabel("L0", SwingConstants.CENTER), BorderLayout.NORTH);
			
//			Image icon = new ImageIcon(ClassLoader.getSystemResource("images/lympho/visualgradation/" + limb + "/L0.jpg")).getImage();
			BufferedImage image;
			image = ImageIO.read(TabVisualGradation.class.getResourceAsStream(
					"/images/lympho/visualgradation/" + limb + "/L0.jpg"));
			l0.add(new DynamicImage(image));

			p1 = new JPanel(new BorderLayout());
			p1.add(new JLabel("P1", SwingConstants.CENTER), BorderLayout.NORTH);
//			icon = new ImageIcon(ClassLoader.getSystemResource("images/lympho/visualgradation/" + limb + "/P1.jpg")).getImage();
			image = (BufferedImage) ImageIO.read(TabVisualGradation.class.getResourceAsStream(
					"/images/lympho/visualgradation/" + limb + "/P1.jpg"));
			DynamicImage dyna1 = new DynamicImage(image);
			p1.add(dyna1);

			p2 = new JPanel(new BorderLayout());
			p2.add(new JLabel("P2", SwingConstants.CENTER), BorderLayout.NORTH);
//			icon = new ImageIcon(ClassLoader.getSystemResource("images/lympho/visualgradation/" + limb + "/P2.jpg")).getImage();
			image = (BufferedImage) ImageIO.read(TabVisualGradation.class.getResourceAsStream(
					"/images/lympho/visualgradation/" + limb + "/P2.jpg"));
			DynamicImage dyna2 = new DynamicImage(image);
			p2.add(dyna2);

			p3 = new JPanel(new BorderLayout());
			p3.add(new JLabel("P3", SwingConstants.CENTER), BorderLayout.NORTH);
//			icon = new ImageIcon(ClassLoader.getSystemResource("images/lympho/visualgradation/" + limb + "/P3.jpg")).getImage();
			image = (BufferedImage) ImageIO.read(TabVisualGradation.class.getResourceAsStream(
					"/images/lympho/visualgradation/" + limb + "/P3.jpg"));
			p3.add(new DynamicImage(image));

			t4 = new JPanel(new BorderLayout());
			t4.add(new JLabel("T4", SwingConstants.CENTER), BorderLayout.NORTH);
//			icon = new ImageIcon(ClassLoader.getSystemResource("images/lympho/visualgradation/" + limb + "/T4.jpg")).getImage();
			image = (BufferedImage) ImageIO.read(TabVisualGradation.class.getResourceAsStream(
					"/images/lympho/visualgradation/" + limb + "/T4.jpg"));
			t4.add(new DynamicImage(image));

			t5 = new JPanel(new BorderLayout());
			t5.add(new JLabel("T5", SwingConstants.CENTER), BorderLayout.NORTH);
//			icon = new ImageIcon(ClassLoader.getSystemResource("images/lympho/visualgradation/" + limb + "/T5.jpg")).getImage();
			image = (BufferedImage) ImageIO.read(TabVisualGradation.class.getResourceAsStream(
					"/images/lympho/visualgradation/" + limb + "/T5.jpg"));
			t5.add(new DynamicImage(image));

			t6 = new JPanel(new BorderLayout());
			t6.add(new JLabel("T6", SwingConstants.CENTER), BorderLayout.NORTH);
//			icon = new ImageIcon(ClassLoader.getSystemResource("images/lympho/visualgradation/" + limb + "/T6.jpg")).getImage();
			image = (BufferedImage) ImageIO.read(TabVisualGradation.class.getResourceAsStream(
					"/images/lympho/visualgradation/" + limb + "/T6.jpg"));
			t6.add(new DynamicImage(image));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		globalPane.add(new DynamicImage(this.img.getImagePlus().getBufferedImage()), BorderLayout.CENTER);

		pan.add(l0);
		pan.add(p1);
		pan.add(p2);
		pan.add(p3);
		pan.add(t4);
		pan.add(t5);
		pan.add(t6);

		globalPane.add(pan, BorderLayout.SOUTH);

		return globalPane;
	}

	/**
	 * Because we want to access to a FenSelectionDicom
	 *
	 */
	private class CustomScintigraphy extends Scintigraphy {

		private ImageSelection[] openedImages;
		private TabVisualGradation tab;

		public CustomScintigraphy(TabVisualGradation tab) {
			super("Visual Gradation");
			// TODO Auto-generated constructor stub

			this.tab = tab;
		}

		@Override
		public ImageSelection[] preparerImp(ImageSelection[] openedImages)
				throws WrongInputException, ReadTagException {
			// TODO Auto-generated method stub
			if (openedImages.length != 1)
				throw new WrongNumberImagesException(openedImages.length, 1);
			
			ImageSelection[] selectedImages = new ImageSelection[openedImages.length];
			for(int i = 0 ; i < openedImages.length ; i++) 
				selectedImages[i] = openedImages[i].clone();
			
			for(ImageSelection selected : openedImages)
				selected.close();
			

			return selectedImages;
		}

		@Override
		public void lancerProgramme(ImageSelection[] selectedImages) {
			// TODO Auto-generated method stub
			this.openedImages = selectedImages;

			tab.update(this.openedImages[0]);
		}

	}

	public void update(ImageSelection imageSelection) {
		// TODO Auto-generated method stub

		this.img = imageSelection;

		this.imgSelected = true;

		this.reloadDisplay();
		
		this.getParent().toFront();

	}
}