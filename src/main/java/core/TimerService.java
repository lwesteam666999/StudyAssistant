package core;

import ui.MainWindow;

import javax.swing.*;
import java.util.concurrent.*;
import java.util.Random;
import java.awt.Color;

public class TimerService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final StateManager stateManager;
    private final MainWindow mainWindow;
    private ScheduledFuture<?> scheduledTask;
    private int elapsedSeconds = 0;
    private final Random random = new Random();
    private int nextReminderTime = -1;
    
    // 休息时间配置（秒）
    private int shortBreakSeconds = 20;
    private int longBreakSeconds = 20 * 60;
    // 学习周期（秒）
    private final int CYCLE_SECONDS = 90 * 60;
    // 提示音周期（秒）
    private final int REMINDER_CYCLE = 300; // 5分钟

    // 测试模式
    private boolean testMode = false;
    private final int TEST_REMINDER_SECONDS = 5; // 测试模式下5秒就触发提示音
    private final int TEST_SHORT_BREAK = 5; // 测试模式下短休息5秒
    private final int TEST_CYCLE_SECONDS = 15; // 测试模式下15秒一个周期

    public TimerService(StateManager stateManager, MainWindow mainWindow) {
        this.stateManager = stateManager;
        this.mainWindow = mainWindow;
    }

    public void setBreakTimes(int shortBreakSeconds, int longBreakSeconds) {
        if (shortBreakSeconds >= 10 && shortBreakSeconds <= 60) {
            this.shortBreakSeconds = shortBreakSeconds;
        }
        if (longBreakSeconds >= 15 * 60 && longBreakSeconds <= 30 * 60) {
            this.longBreakSeconds = longBreakSeconds;
        }
    }

    public int[] getBreakTimes() {
        return new int[]{shortBreakSeconds, longBreakSeconds};
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

        // 检查是否完成学习周期
        if ((testMode && elapsedSeconds >= TEST_CYCLE_SECONDS) || 
            (!testMode && elapsedSeconds >= CYCLE_SECONDS)) {
            invokeLongBreak();
            nextReminderTime = -1; // 重置提示音时间
            return;
        } 
        
        handleReminders();
    }
    
    private void handleReminders() {
        if (testMode) {
            // 测试模式下的逻辑
            if (nextReminderTime == -1) {
                nextReminderTime = elapsedSeconds + TEST_REMINDER_SECONDS;
                System.out.println("测试模式：下一次提示音将在 " + nextReminderTime + " 秒触发");
            }
            
            // 到达提示时间，播放提示音并进入短休息状态
            if (elapsedSeconds == nextReminderTime) {
                System.out.println("测试模式：提示音触发于 " + elapsedSeconds + " 秒");
                invokeShortBreak(true); // 使用测试模式短休息
                nextReminderTime = -1; // 重置提示时间
            }
        } else {
            // 正常模式下的逻辑
        // 每五分钟的学习周期
        int currentCycle = elapsedSeconds / REMINDER_CYCLE;
        int secondsInCurrentCycle = elapsedSeconds % REMINDER_CYCLE;
        
        // 如果是新的周期的开始，或未设置下一次提示时间，设置在第三至第四分钟之间的随机时间点
        if (secondsInCurrentCycle == 0 || nextReminderTime == -1) {
            // 第三分钟到第四分钟之间随机（180-300秒）
            nextReminderTime = currentCycle * REMINDER_CYCLE + 180 + random.nextInt(120);
            System.out.println("下一次提示音将在 " + nextReminderTime + " 秒触发");
        }
        
            // 到达提示时间，播放提示音并进入短休息状态
        if (elapsedSeconds == nextReminderTime) {
            System.out.println("提示音触发于 " + elapsedSeconds + " 秒");
                invokeShortBreak(false); // 正常模式短休息
            nextReminderTime = -1; // 重置提示时间，等待下一个周期
        }
        }
    }

    private void invokeShortBreak(boolean testMode) {
        // 更新状态
        stateManager.setState(LearningState.BREAK);
        
        // 播放声音并显示弹窗
        SwingUtilities.invokeLater(() -> {
            // 首先触发声音播放
            SoundPlayer.playSound("short_break.wav");
            JOptionPane.showMessageDialog(null, "该休息了！短休息 " + (testMode ? TEST_SHORT_BREAK : shortBreakSeconds) + " 秒！", "休息提醒", JOptionPane.INFORMATION_MESSAGE);
        });

        pauseTimerThenResume(testMode ? TEST_SHORT_BREAK : shortBreakSeconds);
    }

    private void invokeLongBreak() {
        // 更新状态
        stateManager.setState(LearningState.BREAK);
        
        int breakDuration = testMode ? TEST_SHORT_BREAK : longBreakSeconds;
        String durationText = testMode ? breakDuration + " 秒" : (breakDuration / 60) + " 分钟";
        
        // 播放声音并显示弹窗
        SwingUtilities.invokeLater(() -> {
            // 首先触发声音播放
            SoundPlayer.playSound("short_break.wav");
            JOptionPane.showMessageDialog(null, "学习周期结束！开始 " + durationText + " 长休息", "休息提醒", JOptionPane.INFORMATION_MESSAGE);
        });

        pauseTimerThenResume(breakDuration);
        elapsedSeconds = 0;
    }

    private void pauseTimerThenResume(int seconds) {
        if (scheduledTask != null) scheduledTask.cancel(false);

        // 创建一个单独的计时器任务用于倒计时显示
        final int[] remainingSeconds = {seconds};
        final ScheduledExecutorService countdownService = Executors.newSingleThreadScheduledExecutor();
        
        // 显示倒计时的进度条
        updateBreakProgressBar(remainingSeconds[0], seconds);
        
        // 每秒更新倒计时显示
        ScheduledFuture<?> countdownTask = countdownService.scheduleAtFixedRate(() -> {
            remainingSeconds[0]--;
            if (remainingSeconds[0] >= 0) {
                updateBreakProgressBar(remainingSeconds[0], seconds);
            } else {
                countdownService.shutdown();
            }
        }, 1, 1, TimeUnit.SECONDS);

        // 使用单次执行任务而不是延迟任务，可能会减少延迟
        ScheduledFuture<?> breakTask = scheduler.schedule(() -> {
            if (stateManager.getState() == LearningState.BREAK) {
                // 取消倒计时任务
                countdownTask.cancel(false);
                countdownService.shutdown();
                
                // 播放声音并显示弹窗，然后恢复状态
                SwingUtilities.invokeLater(() -> {
                    // 播放声音
                SoundPlayer.playSound("short_break.wav");
                    JOptionPane.showMessageDialog(null, "休息结束！该继续学习了", "学习提醒", JOptionPane.INFORMATION_MESSAGE);
                
                    // 恢复状态
                stateManager.setState(LearningState.STUDYING);
                    
                    // 启动计时器
                    scheduledTask = scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
                
                // 立即更新一次进度条
                updateProgressBar();
                
                System.out.println("休息结束，恢复学习计时");
                });
            }
        }, seconds, TimeUnit.SECONDS);
    }

    // 更新休息状态下的进度条
    private void updateBreakProgressBar(int remainingSeconds, int totalSeconds) {
        try {
            SwingUtilities.invokeLater(() -> {
                JProgressBar bar = mainWindow.getProgressBar();
                if (bar != null) {
                    // 设置进度条范围为休息总时长
                    bar.setMaximum(totalSeconds);
                    bar.setValue(totalSeconds - remainingSeconds);
                    
                    // 显示倒计时
                    bar.setString(String.format("休息剩余: %02d:%02d", remainingSeconds / 60, remainingSeconds % 60));
                    
                    // 设置为不同颜色以区分学习状态
                    bar.setForeground(new Color(155, 89, 182)); // 紫色表示休息
                }
            });
        } catch (Exception e) {
            System.err.println("更新休息进度条时出错: " + e.getMessage());
        }
    }

    private void updateProgressBar() {
        // 不使用SwingUtilities.invokeLater可能会导致线程问题，但通过直接调用可以减少延迟
        // 这是一个权衡，如果后续发现界面更新不稳定，可能需要改回invokeLater方式
        try {
            JProgressBar bar = mainWindow.getProgressBar();
            if (bar != null) {
                // 设置为学习状态的进度条范围
                int cycleSeconds = testMode ? TEST_CYCLE_SECONDS : CYCLE_SECONDS;
                bar.setMaximum(cycleSeconds);
                bar.setValue(elapsedSeconds);
                
                int remaining = cycleSeconds - elapsedSeconds;
                bar.setString(String.format("学习剩余：%02d:%02d", remaining / 60, remaining % 60));
                
                // 设置为绿色表示学习状态
                bar.setForeground(new Color(46, 204, 113));
            }
        } catch (Exception e) {
            // 捕获可能的线程问题
            System.err.println("更新进度条时出错: " + e.getMessage());
        }
    }

    public void setTestMode(boolean enabled) {
        this.testMode = enabled;
        // 如果启用测试模式，立即重置下一次提示音时间
        if (enabled) {
            nextReminderTime = elapsedSeconds + TEST_REMINDER_SECONDS;
            System.out.println("测试模式已启用，" + TEST_REMINDER_SECONDS + "秒后触发提示音");
        } else {
            // 恢复正常模式，重新计算下一次提示音时间
            nextReminderTime = -1;
            System.out.println("测试模式已禁用，恢复正常模式");
        }
    }

    public boolean isTestMode() {
        return testMode;
    }
} 