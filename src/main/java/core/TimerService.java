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

    // 测试模式 - 更人性化的参数设置
    private boolean testMode = false;
    private final int TEST_CYCLE_SECONDS = 60; // 测试模式下60秒一个小周期（相当于正常模式的5分钟）
    private final int TEST_SHORT_BREAK = 10; // 测试模式下短休息10秒
    private final int TEST_LONG_BREAK = 30; // 测试模式下长休息30秒
    private final int TEST_TOTAL_CYCLES = 3; // 测试模式下3个小周期后进入长休息（相当于正常模式的18个周期）

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
        if (testMode) {
            // 测试模式：3个小周期后进入长休息
            if (elapsedSeconds >= TEST_CYCLE_SECONDS * TEST_TOTAL_CYCLES) {
                invokeLongBreak();
                nextReminderTime = -1; // 重置提示音时间
                return;
            }
        } else {
            // 正常模式：90分钟后进入长休息
            if (elapsedSeconds >= CYCLE_SECONDS) {
                invokeLongBreak();
                nextReminderTime = -1; // 重置提示音时间
                return;
            }
        }
        
        handleReminders();
    }
    
    private void handleReminders() {
        if (testMode) {
            // 测试模式下的逻辑：每60秒周期中，在30-45秒之间随机触发提示音
            int currentCycle = elapsedSeconds / TEST_CYCLE_SECONDS;
            int secondsInCurrentCycle = elapsedSeconds % TEST_CYCLE_SECONDS;

            // 只在新周期开始时或未设置提示时间时才设置新的提示音时间
            if (secondsInCurrentCycle == 0 || nextReminderTime == -1) {
                // 在每个60秒周期的30-45秒之间随机触发（相当于正常模式的3-5分钟比例）
                nextReminderTime = currentCycle * TEST_CYCLE_SECONDS + 30 + random.nextInt(16);
                System.out.println("测试模式：下一次提示音将在 " + nextReminderTime + " 秒触发（测试周期" + (currentCycle + 1) + "）");
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

            // 只在新周期开始时或未设置提示时间时才设置新的提示音时间
            if (secondsInCurrentCycle == 0 || nextReminderTime == -1) {
                // 第三分钟到第五分钟之间随机（180-300秒）
                nextReminderTime = currentCycle * REMINDER_CYCLE + 180 + random.nextInt(121);
                System.out.println("下一次提示音将在 " + nextReminderTime + " 秒触发（周期" + (currentCycle + 1) + "）");
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

        int breakDuration = testMode ? TEST_LONG_BREAK : longBreakSeconds;
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
                int totalSeconds = testMode ? (TEST_CYCLE_SECONDS * TEST_TOTAL_CYCLES) : CYCLE_SECONDS;
                bar.setMaximum(totalSeconds);
                bar.setValue(elapsedSeconds);

                int remaining = totalSeconds - elapsedSeconds;
                String timeText = testMode ?
                    String.format("测试剩余：%02d:%02d", remaining / 60, remaining % 60) :
                    String.format("学习剩余：%02d:%02d", remaining / 60, remaining % 60);
                bar.setString(timeText);

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

        // 如果当前正在学习或暂停状态，需要停止计时器并重置状态
        if (stateManager.isStudying() || stateManager.isPaused()) {
            // 停止当前的计时任务
            if (scheduledTask != null) {
                scheduledTask.cancel(false);
                scheduledTask = null;
            }

            // 重置状态为空闲
            stateManager.setState(LearningState.IDLE);
            System.out.println("模式切换：已停止当前学习任务，请重新点击开始学习");
        }

        // 重置计时相关变量
        elapsedSeconds = 0;
        nextReminderTime = -1;

        // 更新进度条显示
        updateProgressBar();

        if (enabled) {
            System.out.println("测试模式已启用：60秒周期，30-45秒随机提示音，10秒短休息，3周期后30秒长休息");
        } else {
            System.out.println("测试模式已禁用，恢复正常模式：5分钟周期，3-5分钟随机提示音");
        }
    }

    public boolean isTestMode() {
        return testMode;
    }
} 