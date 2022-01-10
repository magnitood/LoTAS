package de.pfannkuchen.lotas.gui.widgets;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import de.pfannkuchen.lotas.ClientLoTAS;
import de.pfannkuchen.lotas.loscreen.LoScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

/**
 * Editable Text Field
 * @author Pancake
 */
@Environment(EnvType.CLIENT)
public class TextFieldLoWidget extends LoScreen {

	// Content Text Color
	private static final int TEXT_COLOR = 0xffffffff;
	// Label Color
	private static final int LINE_COLOR = 0xff149b5b;
	// Label Color when mouse is over the box
	private static final int FOCUS_LINE_COLOR = 0xff22e187;
	// Background Color when mouse is over the box
	private static final int FOCUS_BACKGROUND_COLOR = 0xff1b1c21;
	
	// Position of the Text Field
	double x;
	double y;
	// Length of the Text Field
	double length;
	// Content of the Text Field
	String content;
	
	// Whether the Text Field should be drawn or not
	boolean active;
	// Whether the mouse is over the text field or not
	boolean mouseOver;
	// Update Event
	Consumer<String> onChange;
	// Hover animation
	float animationProgress;
	
	/**
	 * Initializes a new Text Field
	 * @param active Whether the Text Field is active by default
	 * @param x Position
	 * @param y Position
	 * @param length Length of the text field (in screen width percentage)
	 * @param Callback after text field content is changed
	 */
	public TextFieldLoWidget(boolean active, double x, double y, double length, Consumer<String> onChange, String content) {
		this.active = active;
		this.x = x;
		this.y = y;
		this.length = length;
		this.content = content;
		this.onChange = onChange;
	}
	
	// Ugly double press fix
	boolean onControl;
	
	@Override
	protected void press(int key) {
		if (!this.active) return;
		if (!this.mouseOver) return;
		// Note: onControl is a fix because control keys are hit for release and press while non control keys are not.
		if (Screen.isPaste(-key)) {
			// paste
			if (this.onControl) {
				this.onControl = false;
			} else {
				this.onControl = true;
				this.content += this.mc.keyboardHandler.getClipboard();
			}
		} else if (-key == 259) {
			// backspace
			if (this.onControl) {
				this.onControl = false;
			} else {
				this.onControl = true;
				if (this.content.length() != 0) this.content = this.content.substring(0, this.content.length()-1);
			}
		} else if (key > 0) {
			// normal character
			this.content += (char) key;
		}
		// update
		this.onChange.accept(this.content);
		super.press(key);
	}
	
	@Override
	protected void render(PoseStack stack, double curX, double curY) {
		if (!active) return;
		if (mouseOver) this.animationProgress = Math.min(6, this.animationProgress + ClientLoTAS.internaltimer.tickDelta);
		else this.animationProgress = Math.max(0, this.animationProgress - ClientLoTAS.internaltimer.tickDelta);
		byte alpha = (byte) (ease(this.animationProgress, 0, 1, 6)*255);
		this.mouseOver = curX > (this.x-0.01) && curX < (this.x+this.length+0.01) && curY > (this.y-0.015) && curY < (this.y+0.044);
		fill(stack, this.x-0.01, this.y-0.015, this.x+this.length+0.01, this.y+0.044, FOCUS_BACKGROUND_COLOR - 0xff000000 + (alpha << 24));
		draw(stack, new TextComponent(this.content + (this.mouseOver ? "_" : "")), this.x, this.y, 20, TEXT_COLOR, false);
		fill(stack, this.x, this.y+0.025, this.x+this.length, this.y+0.028, this.mouseOver ? FOCUS_LINE_COLOR : LINE_COLOR);
		super.render(stack, curX, curY);
	}
	
}