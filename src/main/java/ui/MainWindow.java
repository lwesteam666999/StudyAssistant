package ui;

import core.SoundPlayer;

import javax.swing.*;
import java.awt.*;

public class MainWindow {
    private JFrame frame;
    private JButton startButton;
    private JButton pauseButton;
    private JButton exitButton;
    private JButton breakTimeButton;
    private JButton testButton;
    private JProgressBar progressBar;
    private core.TimerService timerService;
    private boolean testModeEnabled = false;

    public void createAndShowGUI() {
        frame = new JFrame("学习辅助工具");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 150);
        frame.setResizable(false); // 禁止调整窗口大小
        frame.setLayout(new BorderLayout());

        // 进度条
        progressBar = new JProgressBar(0, 90 * 60); // 单位为秒
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(450, 30));
        frame.add(progressBar, BorderLayout.NORTH);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        startButton = new JButton("开始学习");
        pauseButton = new JButton("暂停");
        exitButton = new JButton("退出");
        breakTimeButton = new JButton("休息设置");
        testButton = new JButton("测试模式");
        
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(breakTimeButton);
        buttonPanel.add(testButton);
        buttonPanel.add(exitButton);
        
        frame.add(buttonPanel, BorderLayout.CENTER);
        
        // 音量控制滑块
        JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel volumeLabel = new JLabel("音量: ");
        JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
        volumeSlider.setPreferredSize(new Dimension(150, 20));
        volumeSlider.addChangeListener(e -> {
            float volume = volumeSlider.getValue() / 100.0f;
            SoundPlayer.setVolume(volume);
        });
        
        volumePanel.add(volumeLabel);
        volumePanel.add(volumeSlider);
        frame.add(volumePanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null); // 居中
        frame.setVisible(true);

        // 添加控制器绑定（状态和计时服务初始化）
        core.StateManager stateManager = new core.StateManager();
        timerService = new core.TimerService(stateManager, this);
        new actions.UserActions(this, stateManager, timerService);
        
        // 绑定休息时间设置按钮事件
        breakTimeButton.addActionListener(e -> showBreakTimeDialog());
        
        // 绑定测试按钮事件
        testButton.addActionListener(e -> toggleTestMode());
    }
    
    private void showBreakTimeDialog() {
        int[] currentTimes = timerService.getBreakTimes();
        int shortBreakSeconds = currentTimes[0];
        int longBreakMinutes = currentTimes[1] / 60;
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("短休息时间(秒):"));
        JSpinner shortBreakSpinner = new JSpinner(new SpinnerNumberModel(shortBreakSeconds, 10, 60, 1));
        panel.add(shortBreakSpinner);
        
        panel.add(new JLabel("长休息时间(分钟):"));
        JSpinner longBreakSpinner = new JSpinner(new SpinnerNumberModel(longBreakMinutes, 15, 30, 1));
        panel.add(longBreakSpinner);
        
        int result = JOptionPane.showConfirmDialog(frame, panel, 
            "休息时间设置", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION) {
            int newShortBreak = (Integer) shortBreakSpinner.getValue();
            int newLongBreak = (Integer) longBreakSpinner.getValue() * 60;
            timerService.setBreakTimes(newShortBreak, newLongBreak);
        }
    }
    
    private void toggleTestMode() {
        testModeEnabled = !testModeEnabled;
        testButton.setText(testModeEnabled ? "关闭测试" : "测试模式");
        testButton.setBackground(testModeEnabled ? new Color(255, 200, 200) : null);
        timerService.setTestMode(testModeEnabled);
        
        if (testModeEnabled) {
            JOptionPane.showMessageDialog(frame, 
                "测试模式已启用：\n" +
                "- 5秒后将触发提示音\n" +
                "- 短休息时间为5秒\n" +
                "- 学习周期缩短为15秒", 
                "测试模式", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 控件暴露接口（后续用于控制器绑定事件）
    public JButton getStartButton() { return startButton; }
    public JButton getPauseButton() { return pauseButton; }
    public JButton getExitButton() { return exitButton; }
    public JButton getTestButton() { return testButton; }
    public JProgressBar getProgressBar() { return progressBar; }
} 