package org.petctviewer.scintigraphy.lympho.Test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

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
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;

public class TestFenApp extends JFrame implements ActionListener, ChangeListener {

	private static final long serialVersionUID = -7721675828264590938L;

	private FenApplication fenApplication;
	ControleurScin controller;

	private JSlider slider;
	private JLabel sliderLabel;
	protected Box boxSlider;
	protected JButton reverseButton;

	private ImagePlus imp;

	protected int zoomed;

	public TestFenApp(ImageSelection ims, String studyName) {
		this.fenApplication = new FenApplicationLympho(ims, studyName);
		this.fenApplication.setVisible(true);

		Library_Gui.initOverlay(ims.getImagePlus());
		Library_Gui.setOverlayDG(ims.getImagePlus());

		Component[] compo = this.fenApplication.getComponents();

		this.imp = ims.getImagePlus();

		Panel borderLayout = new Panel(new BorderLayout());
		this.add(borderLayout);

		JPanel pan_center = new JPanel(new BorderLayout());
		// for (Component component : compo)
		// pan_center.add(component);
		pan_center.add(compo[0], BorderLayout.CENTER);
		pan_center.add(compo[1], BorderLayout.SOUTH);

		boolean hyperStack = true;

		ImageCanvas ic = (ImageCanvas) compo[0];

		int originalHeight = ic.getHeight();
		int originalWidth = ic.getWidth();

//		this.fenApplication.setBounds(new Rectangle(0, 0, ic.getWidth(), ic.getHeight()));
//		this.fenApplication.setSize(new Dimension(ic.getWidth(), ic.getHeight()));
//		this.fenApplication.setPreferredSize(new Dimension(ic.getWidth(), ic.getHeight()));
//		// this.fenApplication.setPreferredCanvasSize(ic.getWidth());
		this.fenApplication.setSize(new Dimension(ic.getWidth(), ic.getHeight()));
		int originalZoom = (int) (((ic.getHeight() * 1.0f) / (imp.getHeight() * 1.0f)) * 100);
		System.out.println("Zoom de base : " + originalZoom);
		IJ.run("Set... ", "zoom=" + originalZoom);

		System.out.println("ic.getWidth() : " + ic.getWidth() + "   |||    ic.getHeight() : " + ic.getHeight());

		this.zoomed = 0;

		pan_center.addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				synchronized (this) {
					int rotation = e.getWheelRotation();
					int amount = e.getScrollAmount();
					boolean ctrl = (e.getModifiers() & Event.CTRL_MASK) != 0;
					if (IJ.debugMode) {
						IJ.log("mouseWheelMoved: " + e);
						IJ.log("  type: " + e.getScrollType());
						IJ.log("  ctrl: " + ctrl);
						IJ.log("  rotation: " + rotation);
						IJ.log("  amount: " + amount);
					}
					Rectangle srcRect = ic.getSrcRect();
					int xstart = srcRect.x;
					int ystart = srcRect.y;
					if ((ctrl || IJ.shiftKeyDown()) && ic != null) {
						Point loc = ic.getCursorLoc();
						int x = ic.screenX(loc.x);
						int y = ic.screenX(loc.y);

						int icHeight = ic.getHeight();
						System.out.println("TestFenApp.this.fenApplication.getHeight() : "
								+ TestFenApp.this.fenApplication.getHeight());
						System.out.println("icHeight : " + icHeight);
						System.out.println("originalHeight : " + originalHeight);
						if (rotation < 0) {
							ic.zoomIn(x, y);
							int icHeightPrevious = ic.getHeight();
							System.out.println("\t\t\t icHeightPrevious : " + icHeightPrevious);
							ic.zoomOut(x, y);
							if (icHeightPrevious >= originalHeight) {
								System.out.println("\t\t\t Je suis sensé zoomer en profondeur");
								IJ.run("Set... ", "zoom=600");
								ic.zoomIn(x, y);
								zoomed++;
							} else if (icHeightPrevious < originalHeight) {
								System.out.println("\t Resize : " + zoomed);
								TestFenApp.this.fenApplication.setSize(new Dimension(originalWidth, originalHeight));
								ic.setSize(originalWidth, originalHeight);
								System.out.println("\t\t TestFenApp.this.fenApplication.getHeight() : "
										+ TestFenApp.this.fenApplication.getHeight());
								System.out.println("\t\t ic.getHeight() : " + ic.getHeight());
							}

						} else {
							System.out.println("\t Juste avant condition zoom arrière : " + icHeight);
							System.out.println("\t\t icHeight : " + icHeight);
							System.out.println("\t\t originalHeight : " + originalHeight);
							ic.zoomOut(x, y);
							int icHeightPreviousZoomOut = ic.getHeight();
							System.out.println("\t\t\t icHeightPreviousZoomOut : " + icHeightPreviousZoomOut);

							if (!(icHeightPreviousZoomOut < originalHeight)) {
								zoomed--;
								System.out.println("\tZoom vers l'arrière : " + zoomed);
							} else if (icHeightPreviousZoomOut < originalHeight
									|| icHeightPreviousZoomOut > originalHeight) {
								zoomed = 0;
								ic.zoomIn(x, y);
								System.out.println("\tFin de zoom arrière : " + zoomed);
								ic.setSize(new Dimension(originalWidth, originalHeight));
							}
						}
						System.out.println("icHeight de fin : " + ic.getHeight());
						System.out.println("originalHeight de fin : " + originalHeight + "\n");
						return;
					}
					if (srcRect.x != xstart || srcRect.y != ystart)
						ic.repaint();
					if (hyperStack) {
						if (rotation > 0)
							IJ.run(imp, "Next Slice [>]", "");
						else if (rotation < 0)
							IJ.run(imp, "Previous Slice [<]", "");
					}
				}
			}
		});
		SidePanel sidePanel;

		sidePanel = new SidePanel(null, "Bone scintigraphy", ims.getImagePlus());

		this.sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.slider = new JSlider(SwingConstants.HORIZONTAL, 0, (int) ims.getImagePlus().getStatistics().max, 4);
		slider.addChangeListener(this);

		this.boxSlider = Box.createVerticalBox();
		boxSlider.add(this.sliderLabel);
		boxSlider.add(this.slider);

		this.reverseButton = new JButton("Inverser"); // Boutton inversant le contraste.
		this.reverseButton.setPreferredSize(new Dimension(200, 40));
		this.reverseButton.addActionListener(this);

		JPanel gbl = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 2; // third row
		c.insets = new Insets(10, 0, 0, 0); // top padding
		gbl.add(boxSlider);
		gbl.add(this.reverseButton, c);

		sidePanel.addContent(gbl);

		borderLayout.add(pan_center, BorderLayout.CENTER);
		borderLayout.add(sidePanel, BorderLayout.WEST);
		this.validate();
		this.setVisible(true);
		this.pack();
	}

	protected void zoomInTest(ImageCanvas ic) {
		ic.setBounds(ic.getX(), ic.getY(), ic.getWidth() * 3, ic.getHeight() * 3);

	}

	public void setControleur(ControleurScin controllerWorkflowLympho) {
		this.controller = controllerWorkflowLympho;

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.imp.setLut(this.imp.getLuts()[0].createInvertedLut());

	}

	void setContrast(JSlider slider) {
		this.imp.getProcessor().setMinAndMax(0, (slider.getModel().getMaximum() - slider.getValue()) + 1); // On change
																											// son
																											// contraste.
		this.imp.updateAndRepaintWindow();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider) e.getSource();
		this.setContrast(slider);
	}
}
