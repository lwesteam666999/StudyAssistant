package ui;

import javax.swing.*;
import java.awt.*;

public class MainWindow {
    private JFrame frame;
    private JButton startButton;
    private JButton pauseButton;
    private JButton exitButton;
    private JProgressBar progressBar;

    public void createAndShowGUI() {
        frame = new JFrame("自动学习计划程序");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 200);
        frame.setLayout(new BorderLayout());

        // 按钮区
        JPanel buttonPanel = new JPanel();
        startButton = new JButton("开始学习");
        pauseButton = new JButton("暂停");
        exitButton = new JButton("优雅退出");
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(exitButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // 进度条
        progressBar = new JProgressBar(0, 90 * 60); // 单位为秒
        progressBar.setStringPainted(true);
        frame.add(progressBar, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null); // 居中
        frame.setVisible(true);

        // 添加控制器绑定（状态和计时服务初始化）
        core.StateManager stateManager = new core.StateManager();
        core.TimerService timerService = new core.TimerService(stateManager, this);
        new actions.UserActions(this, stateManager, timerService);

    }

    // 控件暴露接口（后续用于控制器绑定事件）
    public JButton getStartButton() { return startButton; }
    public JButton getPauseButton() { return pauseButton; }
    public JButton getExitButton() { return exitButton; }
    public JProgressBar getProgressBar() { return progressBar; }
}