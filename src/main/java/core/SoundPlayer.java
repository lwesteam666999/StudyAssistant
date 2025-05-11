package core;

import javax.sound.sampled.*;
import java.io.IOException;

public class SoundPlayer {
    public void playBeep() {
        try {
            Clip clip = AudioSystem.getClip();
            AudioFormat format = new AudioFormat(44100, 8, 1, true, false);
            byte[] buf = new byte[44100 / 4];
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte)(Math.sin(2 * Math.PI * i / (44100 / 440)) * 127);
            }
            clip.open(format, buf, 0, buf.length);
            clip.start();
        } catch (LineUnavailableException e) {
            System.err.println("提示音播放失败: " + e.getMessage());
        }
    }
}
