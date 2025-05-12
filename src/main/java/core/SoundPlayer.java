package core;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundPlayer {
    private static float volume = 1.0f; // 音量范围：0.0f 到 1.0f
    private static Clip currentClip; // 保存当前播放的音频剪辑

    public static void setVolume(float newVolume) {
        if (newVolume < 0.0f) newVolume = 0.0f;
        if (newVolume > 1.0f) newVolume = 1.0f;
        volume = newVolume;
        
        // 如果当前有正在播放的音频，立即应用新的音量设置
        if (currentClip != null && currentClip.isOpen()) {
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }

    public static float getVolume() {
        return volume;
    }

    /**
     * 播放音频文件
     * @param soundFileName 音频文件名
     * @param wait 是否等待播放完成
     */
    public static void playSound(String soundFileName, boolean wait) {
        try {
            if (currentClip != null && currentClip.isRunning()) {
                currentClip.stop();
                currentClip.close();
            }

            URL soundURL = SoundPlayer.class.getClassLoader().getResource("sounds/" + soundFileName);
            if (soundURL == null) {
                System.err.println("音频文件未找到: " + soundFileName);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioIn);
            
            // 设置音量
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
            
            currentClip.start();

            // 如果需要等待，则阻塞当前线程直到播放完毕
            if (wait) {
                Thread.sleep(currentClip.getMicrosecondLength() / 1000);
                currentClip.close();
                currentClip = null;
            }

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 播放音频（不等待完成）
     */
    public static void playSound(String soundFileName) {
        playSound(soundFileName, false);
    }
}
