package core;

import ui.MainWindow;

import javax.swing.*;
import java.util.Random;
import java.util.concurrent.*;

public class TimerService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final StateManager stateManager;
    private final MainWindow mainWindow;
    private ScheduledFuture<?> scheduledTask;
    private int elapsedSeconds = 0;
    private int breakPoint = -1;
    private final SoundPlayer soundPlayer = new SoundPlayer();

    public TimerService(StateManager stateManager, MainWindow mainWindow) {
        this.stateManager = stateManager;
        this.mainWindow = mainWindow;
    }

    public void start() {
        stateManager.setState(LearningState.STUDYING);
        scheduledTask = scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
        System.out.println("学习计时器启动");
    }

    public void pause() {
        if (scheduledTask != null) scheduledTask.cancel(false);
        stateManager.setState(LearningState.PAUSED);
        System.out.println("学习已暂停");
    }

    public void stop() {
        if (scheduledTask != null) scheduledTask.cancel(true);
        scheduler.shutdownNow();
        System.out.println("学习任务已停止");
    }

    private void tick() {
        if (stateManager.getState() != LearningState.STUDYING) return;

        elapsedSeconds++;
        updateProgressBar();

        int cycleSeconds = 90 * 60;
        if (elapsedSeconds % 300 == 1) { // 每5分钟起始设定一个休息点（3-5分钟之间）
            Random random = new Random();
            breakPoint = elapsedSeconds + 120 + random.nextInt(121); // 当前+2~4分钟
            System.out.println("下一个短休息时间点预计在第 " + breakPoint + " 秒");
        }

        if (elapsedSeconds == breakPoint) {
            invokeShortBreak();
        }

        if (elapsedSeconds >= cycleSeconds) {
            invokeLongBreak();
        }
    }

    private void invokeShortBreak() {
        SwingUtilities.invokeLater(() -> {
            stateManager.setState(LearningState.BREAK);
            soundPlayer.playBeep();
            JOptionPane.showMessageDialog(null, "短休息 20 秒！", "提示", JOptionPane.INFORMATION_MESSAGE);
        });

        pauseTimerThenResume(20);
    }

    private void invokeLongBreak() {
        SwingUtilities.invokeLater(() -> {
            stateManager.setState(LearningState.BREAK);
            soundPlayer.playBeep();
            JOptionPane.showMessageDialog(null, "学习周期结束！开始 20 分钟长休息", "提示", JOptionPane.INFORMATION_MESSAGE);
        });

        pauseTimerThenResume(20 * 60);
        elapsedSeconds = 0;
    }

    private void pauseTimerThenResume(int seconds) {
        if (scheduledTask != null) scheduledTask.cancel(false);

        scheduler.schedule(() -> {
            if (stateManager.getState() == LearningState.BREAK) {
                stateManager.setState(LearningState.STUDYING);
                scheduledTask = scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
            }
        }, seconds, TimeUnit.SECONDS);
    }

    private void updateProgressBar() {
        SwingUtilities.invokeLater(() -> {
            JProgressBar bar = mainWindow.getProgressBar();
            bar.setValue(elapsedSeconds);
            int remaining = (90 * 60) - elapsedSeconds;
            bar.setString(String.format("剩余：%02d:%02d", remaining / 60, remaining % 60));
        });
    }
}
