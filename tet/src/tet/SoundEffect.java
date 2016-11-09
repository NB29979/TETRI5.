package tet;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundEffect {
	private Clip clip;
	
	public SoundEffect(String soundFilePath){
		try{
			//wavファイルの読み込み
			File soundFile = new File(soundFilePath);
			AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
			
			//音声再生クリップの取得
			clip = AudioSystem.getClip();
			
			//音声を再生可能に
			clip.open(audioInput);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public Clip getClip(){
		return clip;
	}
	
	public void play(){
		//再生中なら停止
		if(clip.isRunning()){
			clip.stop();
		}
		//最初に巻き戻す
		clip.setFramePosition(0);
		clip.start();
	}
	
	public void stop(){
		clip.stop();
		clip.setFramePosition(0);
	}
	
	public void loop(){
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
}
