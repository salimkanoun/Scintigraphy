package org.petctviewer.scintigraphy.scin.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;

import ij.IJ;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;

import ij.ImagePlus;

/**
 * affichage imaage plus avec reglage contraste SK algo contraste à revoir
 *
 * @author diego
 */
public class TabContrastModifier extends TabResult {
	protected Box boxSlider;
	private DynamicImage dynamicImp;
	private ContrastSlider slider;


	/**
	 * Instantiates an empty tab. This is used if no image is ready to be displayed at the instantiation time. To set
	 * the image later, you can use {@link #setImage}.
	 *
	 * @param parent FenResults where this tab is placed on
	 * @param title  Title of this tab, displayed on the JTabbedPane's title bar
	 */
	public TabContrastModifier(FenResults parent, String title) {
		super(parent, title, true);

		this.setComponentToHide(new ArrayList<>(Arrays.asList(new Component[] {slider})));

		this.boxSlider = null;
		this.dynamicImp = null;
	}

	/**
	 * Instantiates a new tab contrast. The specified image will be updated automatically when the contrast slider is
	 * changed.
	 *
	 * @param parent FenResults where this tab is placed on
	 * @param title  Title of this tab, displayed on the JTabbedPane's title bar
	 * @param image  Image modified by this tab (not null)
	 * @see #setImage(ImagePlus)
	 */
	public TabContrastModifier(FenResults parent, String title, ImagePlus image) {
		super(parent, title, true);

		this.setImage(image);

	}

	/**
	 * Instantiates a new tab contrast. The state must contain a custom image, and the image cannot be null.
	 *
	 * @param parent             FenResults where this tab is placed on
	 * @param title              Title of this tab, displayed on the JTabbedPane's title bar
	 * @param stateImageToModify State containing the image to be modified by this tab (not null)
	 * @see #setImage(ImageState)
	 */
	public TabContrastModifier(FenResults parent, String title, ImageState stateImageToModify) {
		super(parent, title, true);

		this.setImage(stateImageToModify);
	}




	/**
	 * Changes the image to be modified by this contrast tab. This method automatically calls the {@link
	 * #reloadDisplay()} method.
	 *
	 * @param image New image to use
	 */
	public void setImage(ImagePlus image) {

		JPanel sliderPanel = new JPanel();
		JPanel reversePanel = new JPanel();
		JLabel sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.dynamicImp = new DynamicImage(image.getBufferedImage());

		slider = new ContrastSlider(image, this.dynamicImp, this.parent);
		JButton reverse_btn = new JButton("Invert LUT");
		reverse_btn.addActionListener( e-> {
			SwingUtilities.invokeLater(() -> {
				image.getProcessor().invertLut();
				image.updateAndDraw();
				dynamicImp.setImage(image.getBufferedImage());
			});
		});



		this.setComponentToHide(new ArrayList<>(Arrays.asList(new Component[] {slider, sliderLabel})));

		boxSlider = Box.createVerticalBox();
		sliderPanel.add(sliderLabel);
		sliderPanel.add(slider);
		reversePanel.add(reverse_btn);
		boxSlider.add(sliderPanel);
		boxSlider.add(reversePanel);



		this.reloadDisplay();
	}

	/**
	 * Changes the image to be modified by this contrast tab. This method automatically calls the {@link
	 * #reloadDisplay()} method.<br> The state must contain an image (and so, have the {@link
	 * ImageState#ID_CUSTOM_IMAGE} set.
	 *
	 * @param state State of the image to use
	 * @throws IllegalArgumentException if the state doesn't have a custom image
	 */
	public void setImage(ImageState state) throws IllegalArgumentException {
		if (state.getImage() == null || state.getIdImage() != ImageState.ID_CUSTOM_IMAGE)
			throw new IllegalArgumentException("The image of the state must be set");

		JLabel sliderLabel = new JLabel("Contrast", SwingConstants.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		this.dynamicImp = new DynamicImage(state.getImage().getImagePlus().getBufferedImage());

		slider = new ContrastSlider(state, this.dynamicImp, this.parent);
		
		this.setComponentToHide(new ArrayList<>(Arrays.asList(new Component[] {slider, sliderLabel})));

		boxSlider = Box.createVerticalBox();
		boxSlider.add(sliderLabel);
		boxSlider.add(slider);

		this.reloadDisplay();
	}

	/**
	 * Returns the image modified by this tab.
	 *
	 * @return image used by this tab
	 */
	public ImagePlus getAssociatedImage() {
		if (this.slider == null) return null;
		return this.slider.getAssociatedImage();
	}

	@Override
	public Component getSidePanelContent() {
		return this.boxSlider;
	}

	@Override
	public JPanel getResultContent() {
		return this.dynamicImp;
	}

	@SuppressWarnings("deprecation")
	public void setSliderEnable(boolean boo) {
		this.slider.enable(boo);
	}
}
