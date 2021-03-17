package de.pfannekuchen.lotas;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;

/**
 * A LWJGL style keyboard method
 * @author ScribbleLP
 *
 */
public class Keyboard {

	public static boolean isKeyDown(int keyCode) {
		MinecraftClient mc=MinecraftClient.getInstance();
		return GLFW.glfwGetKey(mc.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;
	}
}
