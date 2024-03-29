package org.petctviewer.scintigraphy.lympho.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.petctviewer.scintigraphy.scin.ImagePreparator;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabVisualGradation extends TabResult implements ActionListener {

	private final JRadioButton l0 = new JRadioButton("L0");
	private final JRadioButton p1 = new JRadioButton("P1");
	private final JRadioButton p2 = new JRadioButton("P2");
	private final JRadioButton p3 =
			new JRadioButton("P3");
	private final JRadioButton t4 = new JRadioButton("T4");
	private final JRadioButton t5 = new JRadioButton("T5");
	private final JRadioButton t6 = new JRadioButton(
			"T6");

	private boolean imgSelected;

	private JButton btn_addImp;

	private JButton btn_switchLimb;

	private boolean upperLimb;

	private ImageSelection img;

	public TabVisualGradation(FenResults parent, String title) {
		super(parent, title, true);

		this.imgSelected = false;

		l0.addActionListener(this);
		p1.addActionListener(this);
		p2.addActionListener(this);
		p3.addActionListener(this);
		t4.addActionListener(this);
		t5.addActionListener(this);
		t6.addActionListener(this);

		ButtonGroup radio = new ButtonGroup();
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

		if (!this.imgSelected) return null;
		else {

			JPanel globalPane = new JPanel();
			globalPane.setLayout(new BoxLayout(globalPane, BoxLayout.Y_AXIS));
			JPanel pan = this.getRadio();

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

		JPanel pan = new JPanel();

		if (!this.imgSelected) {
			this.btn_addImp = new JButton("Start Visual Gradiation");
			this.btn_addImp.addActionListener(this);
			pan.add(btn_addImp);
		} else pan = this.getImages(this.upperLimb);

		return pan;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() instanceof JButton) {
			JButton button = (JButton) arg0.getSource();
			if (button == btn_addImp) {

				FenSelectionDicom fen = new FenSelectionDicom(new ImagePreparator() {
					@Override
					public String getStudyName() {
						return "Visual Gradation";
					}

					@Override
					public Column[] getColumns() {
						return Column.getDefaultColumns();
					}

					@Override
					public List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws
							WrongInputException {
						if (openedImages.size() != 1) throw new WrongNumberImagesException(openedImages.size(), 1);

						List<ImageSelection> selectedImages = new ArrayList<>();
						for (ImageSelection openedImage : openedImages) selectedImages.add(openedImage.clone());

						openedImages.forEach(ImageSelection::close);

						return selectedImages;
					}

					@Override
					public String instructions() {
						return "1 image needed.";
					}

					@Override
					public void start(List<ImageSelection> preparedImages) {
						update(preparedImages.get(0));
					}
				});
				fen.setVisible(true);

			} else if (button == btn_switchLimb) {
				this.switchLimb(this.btn_switchLimb);
			}
		} else if (arg0.getSource() instanceof JRadioButton) {
			JRadioButton radioButton = (JRadioButton) arg0.getSource();
			String gradation = null;
			if (radioButton.getText().contains("0")) gradation = "Normal";
			else if (radioButton.getText().contains("1")) gradation = "Partial obstruction - Score 1";
			else if (radioButton.getText().contains("2")) gradation = "Partial obstruction - Score 2";
			else if (radioButton.getText().contains("3")) gradation = "Partial obstruction - Score 3";
			else if (radioButton.getText().contains("4")) gradation = "Total obstruction - Score 4";
			else if (radioButton.getText().contains("5")) gradation = "Total obstruction - Score 5";
			else if (radioButton.getText().contains("6")) gradation = "Total obstruction - Score 6";
			((FenResultatsLympho) this.parent).updateVisualGradation("Visual Gradation : " + gradation);
		}

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

		JPanel pan = new JPanel(new GridLayout(1, 7));

		JPanel l0 = null, p1 = null, p2 = null, p3 = null, t4 = null, t5 = null, t6 = null;

		l0 = new JPanel(new BorderLayout());
		l0.add(new JLabel("L0", SwingConstants.CENTER), BorderLayout.NORTH);

//		Image image;
		JLabel labelfake;
//		image = Toolkit.getDefaultToolkit().getImage(
//				this.getClass().getClassLoader().getResource("images/lympho/visualgradation/" + limb + "/L0.jpg"));
		labelfake = new JLabel("",SwingConstants.CENTER);
		labelfake.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				this.getClass().getClassLoader().getResource("images/lympho/visualgradation/" + limb + "/L0.jpg"))));
		l0.add(labelfake);

		p1 = new JPanel(new BorderLayout());
		p1.add(new JLabel("P1", SwingConstants.CENTER), BorderLayout.NORTH);
		labelfake = new JLabel("",SwingConstants.CENTER);
		labelfake.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				this.getClass().getClassLoader().getResource("images/lympho/visualgradation/" + limb + "/P1.jpg"))));
		p1.add(labelfake);

		p2 = new JPanel(new BorderLayout());
		p2.add(new JLabel("P2", SwingConstants.CENTER), BorderLayout.NORTH);
		labelfake = new JLabel("",SwingConstants.CENTER);
		labelfake.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				this.getClass().getClassLoader().getResource("images/lympho/visualgradation/" + limb + "/P2.jpg"))));
		p2.add(labelfake);

		p3 = new JPanel(new BorderLayout());
		p3.add(new JLabel("P3", SwingConstants.CENTER), BorderLayout.NORTH);
		labelfake = new JLabel("",SwingConstants.CENTER);
		labelfake.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				this.getClass().getClassLoader().getResource("images/lympho/visualgradation/" + limb + "/P3.jpg"))));
		p3.add(labelfake);

		t4 = new JPanel(new BorderLayout());
		t4.add(new JLabel("T4", SwingConstants.CENTER), BorderLayout.NORTH);
		labelfake = new JLabel("",SwingConstants.CENTER);
		labelfake.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				this.getClass().getClassLoader().getResource("images/lympho/visualgradation/" + limb + "/T4.jpg"))));
		t4.add(labelfake);

		t5 = new JPanel(new BorderLayout());
		t5.add(new JLabel("T5", SwingConstants.CENTER), BorderLayout.NORTH);
		labelfake = new JLabel("",SwingConstants.CENTER);
		labelfake.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				this.getClass().getClassLoader().getResource("images/lympho/visualgradation/" + limb + "/T5.jpg"))));
		t5.add(labelfake);

		t6 = new JPanel(new BorderLayout());
		t6.add(new JLabel("T6", SwingConstants.CENTER), BorderLayout.NORTH);
		labelfake = new JLabel("",SwingConstants.CENTER);
		labelfake.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				this.getClass().getClassLoader().getResource("images/lympho/visualgradation/" + limb + "/T6.jpg"))));
		t6.add(labelfake);

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

	public void update(ImageSelection imageSelection) {
		this.img = imageSelection;

		this.imgSelected = true;

		this.reloadDisplay();

		this.getParent().toFront();

	}
}