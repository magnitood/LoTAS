package de.pfannekuchen.lotas.gui;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import org.lwjgl.opengl.GL11;

import de.pfannekuchen.lotas.renderer.PotionRenderer;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import rlog.RLogAPI;

public class GuiVideoUpspeeder extends GuiScreen {

	static GuiTextField text;
	static float progress = 0f;
	static float lerp = 0f;
	static GuiTextField tickrate;
	static File f;
	static String videoFormat;
	static String codec;
	static String resolution;
	static String length;
	static String filesize;
	static long lengthInMilliseconds;
	
	static FFmpeg ffmpeg;
	static FFprobe ffprobe;
	
	static String codecFFMPEG;
	static FFmpegProbeResult result;
	
	public GuiVideoUpspeeder() {
		try {
			ffmpeg = new FFmpeg(System.getenv("localappdata") + "\\ffmpeg\\bin\\ffmpeg.exe");
			ffprobe = new FFprobe(System.getenv("localappdata") + "\\ffmpeg\\bin\\ffprobe.exe");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		codecFFMPEG = GL11.glGetString(GL11.GL_VENDOR).toUpperCase().contains("NVIDIA") ? "nvenc_h264" : "x264";
	}
	
	@Override
	public void initGui() {
		text = new GuiTextField(0, mc.fontRenderer, (width / 12) * 1 - (width / 24), (height / 8), (width / 12) * 9, 20);
		addButton(new GuiButton(1, (width / 12) * 10 + 5 - (width / 24), (height / 8), (width / 12) * 2, 20, "Select File"));
		addButton(new GuiButton(2, (width / 2) - 70, (height / 8) * 2 + 20, 20, 20, "-"));
		addButton(new GuiButton(3, (width / 2) + 50, (height / 8) * 2 + 20, 20, 20, "+"));
		addButton(new GuiButton(4, (width / 2) - 98, this.height - (this.height / 10) - 15 - 20 - 5, 204, 20, "Speed up video"));
		addButton(new GuiCheckBox(5, 2, this.height  - (this.height / 10), "High Quality", false));
		buttonList.get(3).enabled = f == null ? false : f.exists();
		tickrate = new GuiTextField(4, mc.fontRenderer, (width / 2) - 45, (height / 8) * 2 + 23, 90, 14);
		tickrate.setText("20");
		super.initGui();
	}
	
	public void loadFile() throws Exception {
		if (!f.exists() || f.isDirectory()) throw new Exception();
		
		result = ffprobe.probe(f.getAbsolutePath());
		
		videoFormat = result.format.format_name.split(",")[0];
		codec = result.getStreams().get(0).codec_name; 
		length = getTime(Duration.ofMillis((long) result.format.duration * 1000));
		resolution = result.streams.get(0).width + "x" + result.streams.get(0).height;
		filesize = (Files.size(f.toPath()) / 1024 / 1024) + " MB";
		lengthInMilliseconds = (long) (result.format.duration * 1000);
		
		buttonList.get(3).enabled = true;
	}
	
	public static String getTime(Duration d) {
		return d.toHours() + ":" + d.toMinutes() % 60 + ":" + d.getSeconds() % 60 + "." + (int) ((d.toMillis() % 1000) / 100);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		tickrate.textboxKeyTyped(typedChar, keyCode);
		try {
			int ticks = Integer.parseInt(tickrate.getText());
			
			if (ticks <= 0) {
				throw new Exception();
			}
			
			buttonList.get(1).enabled = true;
			buttonList.get(2).enabled = true;
		} catch (Exception e) {
			RLogAPI.logError(e, "Parse Invalid Data #2");
			buttonList.get(1).enabled = false;
			buttonList.get(2).enabled = false;
		}
		super.keyTyped(typedChar, keyCode);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		tickrate.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 1) {
			if (mc.isFullScreen()) mc.toggleFullscreen();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
				    FileDialog dialog = new FileDialog((Frame)null, "Select File to Open");
				    dialog.setMode(FileDialog.LOAD);
				    dialog.setMultipleMode(false);
				    dialog.setVisible(true);
				    try {
				    	f = dialog.getFiles()[0];
				    	loadFile();
				    } catch (Exception e) {
						
					}
				}
			}).start();
		} else if (button.id == 2) {
			if (Integer.parseInt(tickrate.getText()) <= 1) return;
			tickrate.setText((Integer.parseInt(tickrate.getText()) - 1) + "");
		} else if (button.id == 3) {
			tickrate.setText((Integer.parseInt(tickrate.getText()) + 1) + "");
		} else if (button.id == 4) {
			button.enabled = false;
			float fullFrames = result.getStreams().get(0).nb_frames * (Integer.parseInt(tickrate.getText()) / 20F);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						new Timer().scheduleAtFixedRate(new TimerTask() {
							
							@Override
							public void run() {
								lerp = PotionRenderer.lerp(lerp, progress, .025f);
							}
						}, 16L, 16L);
						ffmpeg.run(ffmpeg.builder().addInput(f.getAbsolutePath())
								.overrideOutputFiles(true)
								.addExtraArgs("-hwaccel", "auto")
								.addOutput(new File(f.getParentFile(), f.getName().split("\\.")[0] + "-upspeeder.mp4").getAbsolutePath())
									.setAudioFilter("asetrate=48000*" + (1F / (Integer.parseInt(tickrate.getText()) / 20F) + ",aresample=48000"))
									.setVideoFilter("setpts=" + (Integer.parseInt(tickrate.getText()) / 20F) + "*PTS")
									.disableSubtitle()
									.setFormat("mp4")
									.setVideoBitRate(((GuiCheckBox) buttonList.get(4)).isChecked() ? 20000000 : 8000000)
									.setVideoCodec(codecFFMPEG)
									.setVideoFrameRate(60)
									.done(), new ProgressListener() {
										@Override
										public void progress(Progress p) {
											progress = p.frame / fullFrames;
										}
									});
						
						if (mc.isFullScreen()) mc.toggleFullscreen();
						Runtime.getRuntime().exec(("explorer.exe \"" + f.getParentFile().getAbsolutePath() + "\"").split(" "));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			
		}
		super.actionPerformed(button);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		text.drawTextBox();
		tickrate.drawTextBox();
		drawCenteredString(mc.fontRenderer, "Tickrate", (width / 2), (height / 8) * 2, 0xFFFFFF);
		
		if (f != null) drawString(mc.fontRenderer, f.getAbsolutePath(), (width / 12) * 1 - (width / 24) + 4, (height / 8) + 6, 0xFFFFFF);
		
		drawCenteredString(mc.fontRenderer, "Input File", (width / 4), (height / 8) * 3 + 10, 0x808080);
		drawCenteredString(mc.fontRenderer, "Output File", (width / 4) * 3, (height / 8) * 3 + 10, 0x808080);
	    
		drawString(mc.fontRenderer, "Format: " + videoFormat, (width / 4) - (width / 12), (height / 8) * 4, 0xFFFFFF);
		drawString(mc.fontRenderer, "Codec: " + codec, (width / 4) - (width / 12), (height / 8) * 4 + 10, 0xFFFFFF);
		drawString(mc.fontRenderer, "Resolution: " + resolution, (width / 4) - (width / 12), (height / 8) * 4 + 20, 0xFFFFFF);
		drawString(mc.fontRenderer, "Length: " + length, (width / 4) - (width / 12), (height / 8) * 4 + 30, 0xFFFFFF);
		drawString(mc.fontRenderer, "Size: " + filesize, (width / 4) - (width / 12), (height / 8) * 4 + 40, 0xFFFFFF);
		
		drawString(mc.fontRenderer, "Format: mp4", (width / 4) * 3 - (width / 12), (height / 8) * 4, 0xFFFFFF);
		drawString(mc.fontRenderer, "Encoder: " + codecFFMPEG, (width / 4) * 3 - (width / 12), (height / 8) * 4 + 10, 0xFFFFFF);
		drawString(mc.fontRenderer, "Resolution: " + resolution, (width / 4) * 3 - (width / 12), (height / 8) * 4 + 20, 0xFFFFFF);
		String dur = null;
		try {
			dur = getTime(Duration.ofMillis((long) (lengthInMilliseconds * (Integer.parseInt(tickrate.getText()) / 20F))));
		} catch (Exception e) {
			
		}
		drawString(mc.fontRenderer, "Length: " + dur, (width / 4) * 3 - (width / 12), (height / 8) * 4 + 30, 0xFFFFFF);
		drawString(mc.fontRenderer, "Size: " + calcSize(), (width / 4) * 3 - (width / 12), (height / 8) * 4 + 40, 0xFFFFFF);
		
		
		drawRect(0, this.height - 10, progressToPixels(lerp), this.height, 0x66CDAAFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private String calcSize() {
		try {
			return ((int) ((Duration.ofMillis((long) (lengthInMilliseconds * (Integer.parseInt(tickrate.getText()) / 20F))).getSeconds() * (((GuiCheckBox) buttonList.get(4)).isChecked() ? 20000 : 8000)) / 1024 / 8)) + " MB";
		} catch (Exception e) {
			return null;
		}
	}

	public int progressToPixels(float progress) {
	    return (int) (progress * this.width);
	}
	
}

