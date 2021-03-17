package de.pfannekuchen.lotas.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import de.pfannekuchen.lotas.ConfigManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;

public class GuiConfiguration extends Screen {
	
	public GuiConfiguration() {
		super(new LiteralText("Configuration"));
	}


	public static String[] optionsBoolean = new String[] {
		"B:tools:saveTickrate:INSERT",
		"B:ui:hideTickrateMessages:INSERT",
		"B:tools:showTickIndicator:INSERT"
	};
	
	public static String[] optionsInteger = new String[] {
			"I:hidden:explosionoptimization:INSERT"
	};
	
	public static String[] optionsString = new String[] {
			"S:ui:runner:INSERT"
	};
	
	public ArrayList<TextFieldWidget> strings = new ArrayList<TextFieldWidget>();
	public ArrayList<TextFieldWidget> ints = new ArrayList<TextFieldWidget>();
	public HashMap<Integer, String> messages = new HashMap<Integer, String>();
	
	@Override
	public void init() {
		strings.clear();
		messages.clear();
		ints.clear();
		buttons.clear();
		children.clear();
		int y = 25;
		int i = 0;
		for (String option : optionsBoolean) {
			String title = option.split(":")[2];
			if (option.split(":")[0].equalsIgnoreCase("B")) {
				boolean v = Boolean.parseBoolean(option.split(":")[3]);
				addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, title + ": " + (v ? "\u00A7atrue" : "\u00A7cfalse"), actionPerformed(i++)));
			}
			y += 25;
		}
		i = 20;
		for (String option : optionsString) {
			String title = option.split(":")[2];
			if (option.split(":")[0].equalsIgnoreCase("S")) {
				String v = option.split(":")[3];
				strings.add(new TextFieldWidget(minecraft.textRenderer, width / 2 - 100, y, 200, 20, v));
				strings.get(strings.size() - 1).setText(v);
				strings.get(strings.size() - 1).setUneditableColor(i++);
				messages.put(y, title);
			}
			y += 25;
		}
		i = 40;
		for (String option : optionsInteger) {
			String title = option.split(":")[2];
			if (option.split(":")[0].equalsIgnoreCase("I")) {
				String v = option.split(":")[3];
				ints.add(new TextFieldWidget(minecraft.textRenderer, width / 2 - 100, y, 200, 20, v));
				ints.get(ints.size() - 1).setText(v);
				ints.get(ints.size() - 1).setUneditableColor(i++);
				messages.put(y, title);
			}
			y += 25;
		}
	}
	
	protected PressAction actionPerformed(int id) {
		return b -> {
			if (optionsBoolean[id].startsWith("B:")) {
				optionsBoolean[id] = setBoolean(optionsBoolean[id], !Boolean.parseBoolean(optionsBoolean[id].split(":")[3]));
				ConfigManager.setBoolean(optionsBoolean[id].split(":")[1], optionsBoolean[id].split(":")[2], Boolean.parseBoolean(optionsBoolean[id].split(":")[3]));
				ConfigManager.save();
			}
			init();
		};
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		for (TextFieldWidget field : strings) {
			field.mouseClicked(mouseX, mouseY, mouseButton);
		}
		for (TextFieldWidget field : ints) {
			field.mouseClicked(mouseX, mouseY, mouseButton);
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		for (TextFieldWidget field : strings) {
			if (field.keyReleased(keyCode, scanCode, modifiers)) {
				String line = optionsString[field.uneditableColor - 20];
				ConfigManager.setString(line.split(":")[1], line.split(":")[2], field.getText());
				ConfigManager.save();
				optionsString[field.uneditableColor - 20] = setString(line.split(":")[1], line.split(":")[2], field.getText());
			}
		}
		for (TextFieldWidget field : ints) {
			String textBefore = field.getText();
			if (field.keyReleased(keyCode, scanCode, modifiers)) {
				String line = optionsInteger[field.uneditableColor - 40];
				try {
					if (field.getText().isEmpty()) field.setText("0");
					if (field.getText().startsWith("0") && field.getText().length() != 1) field.setText(field.getText().substring(1));
					ConfigManager.setInt(line.split(":")[1], line.split(":")[2], Integer.parseInt(field.getText()));
					ConfigManager.save();
					optionsInteger[field.uneditableColor - 40] = setInt(line.split(":")[1], line.split(":")[2], Integer.parseInt(field.getText()));
				} catch (Exception e) {
					field.setText(textBefore);
				}
			}
		}
		return super.keyReleased(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		for (TextFieldWidget field : strings) {
			if (field.keyPressed(keyCode, scanCode, modifiers)) {
				String line = optionsString[field.uneditableColor - 20];
				ConfigManager.setString(line.split(":")[1], line.split(":")[2], field.getText());
				ConfigManager.save();
				optionsString[field.uneditableColor - 20] = setString(line.split(":")[1], line.split(":")[2], field.getText());
			}
		}
		for (TextFieldWidget field : ints) {
			String textBefore = field.getText();
			if (field.keyPressed(keyCode, scanCode, modifiers)) {
				String line = optionsInteger[field.uneditableColor - 40];
				try {
					if (field.getText().isEmpty()) field.setText("0");
					if (field.getText().startsWith("0") && field.getText().length() != 1) field.setText(field.getText().substring(1));
					ConfigManager.setInt(line.split(":")[1], line.split(":")[2], Integer.parseInt(field.getText()));
					ConfigManager.save();
					optionsInteger[field.uneditableColor - 40] = setInt(line.split(":")[1], line.split(":")[2], Integer.parseInt(field.getText()));
				} catch (Exception e) {
					field.setText(textBefore);
				}
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		for (TextFieldWidget field : strings) {
			if (field.charTyped(typedChar, keyCode)) {
				String line = optionsString[field.uneditableColor - 20];
				ConfigManager.setString(line.split(":")[1], line.split(":")[2], field.getText());
				ConfigManager.save();
				optionsString[field.uneditableColor - 20] = setString(line.split(":")[1], line.split(":")[2], field.getText());
			}
		}
		for (TextFieldWidget field : ints) {
			String textBefore = field.getText();
			if (field.charTyped(typedChar, keyCode)) {
				String line = optionsInteger[field.uneditableColor - 40];
				try {
					if (field.getText().isEmpty()) field.setText("0");
					if (field.getText().startsWith("0") && field.getText().length() != 1) field.setText(field.getText().substring(1));
					ConfigManager.setInt(line.split(":")[1], line.split(":")[2], Integer.parseInt(field.getText()));
					ConfigManager.save();
					optionsInteger[field.uneditableColor - 40] = setInt(line.split(":")[1], line.split(":")[2], Integer.parseInt(field.getText()));
				} catch (Exception e) {
					field.setText(textBefore);
				}
			}
		}
		return super.charTyped(typedChar, keyCode);
	}
	

	
	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground(0);
		drawCenteredString(minecraft.textRenderer, "Configuration Menu", width / 2, 5, 0xFFFFFFFF);
		for (Entry<Integer, String> entry : messages.entrySet()) {
			drawString(minecraft.textRenderer, entry.getValue(), 35, entry.getKey() + 5, 0xFFFFFFFF);
		}
		for (TextFieldWidget field : strings) {
			field.render(mouseX, mouseY, delta);
		}
		for (TextFieldWidget field : ints) {
			field.render(mouseX, mouseY, delta);
		}
		super.render(mouseX, mouseY, delta);
	}
	
	public static String getTitle(String line) {
		return line.split(":")[2];
	}
	
	public static String getCategory(String line) {
		return line.split(":")[1];
	}
	
	public static String setString(String cat, String title, String val) {
		return "S:" + cat + ":" + title + ":" + val;
	}
	
	public static String setInt(String cat, String title, int val) {
		return "I:" + cat + ":" + title + ":" + val;
	}
	
	
	public static String setBoolean(String line, boolean value) {
		return line.endsWith("true") ? line.replaceFirst("true", value + "") : line.replaceFirst("false", value + "").replaceFirst("INSERT", value + "");
	}
	
}
