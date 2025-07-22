package ui;

import core.SoundPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class MainWindow {
    private JFrame frame;
    private JButton startButton;
    private JButton pauseButton;
    private JButton exitButton;
    private JButton breakTimeButton;
    private JButton testButton;
    private JProgressBar progressBar;
    private core.TimerService timerService;
    private core.StateManager stateManager;
    private boolean testModeEnabled = false;

    // 系统托盘相关
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private boolean isMinimizedToTray = false;

    public void createAndShowGUI() {
        frame = new JFrame("学习辅助工具");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 改为不直接退出
        frame.setSize(450, 150);
        frame.setResizable(false); // 禁止调整窗口大小
        frame.setLayout(new BorderLayout());

        // 初始化系统托盘
        initSystemTray();

        // 添加窗口关闭监听器
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                minimizeToTray();
            }
        });

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
        stateManager = new core.StateManager();
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

    // 系统托盘相关方法
    private void initSystemTray() {
        // 检查系统是否支持托盘
        if (!SystemTray.isSupported()) {
            System.out.println("系统不支持托盘功能");
            return;
        }

        systemTray = SystemTray.getSystemTray();

        // 创建托盘图标（使用默认图标，后续可以替换为自定义图标）
        Image trayImage = createTrayImage();

        // 创建弹出菜单
        PopupMenu popup = new PopupMenu();

        // 显示窗口菜单项
        MenuItem showItem = new MenuItem("显示窗口");
        showItem.addActionListener(e -> showWindow());
        popup.add(showItem);

        popup.addSeparator();

        // 开始学习菜单项
        MenuItem startItem = new MenuItem("开始学习");
        startItem.addActionListener(e -> {
            if (!stateManager.isStudying()) {
                timerService.start();
            }
        });
        popup.add(startItem);

        // 暂停学习菜单项
        MenuItem pauseItem = new MenuItem("暂停学习");
        pauseItem.addActionListener(e -> {
            if (stateManager.isStudying()) {
                timerService.pause();
            } else if (stateManager.isPaused()) {
                timerService.start();
            }
        });
        popup.add(pauseItem);

        popup.addSeparator();

        // 退出菜单项
        MenuItem exitItem = new MenuItem("退出程序");
        exitItem.addActionListener(e -> exitApplication());
        popup.add(exitItem);

        // 创建托盘图标
        trayIcon = new TrayIcon(trayImage, "学习辅助工具", popup);
        trayIcon.setImageAutoSize(true);

        // 双击托盘图标显示窗口
        trayIcon.addActionListener(e -> showWindow());

        try {
            systemTray.add(trayIcon);
            System.out.println("系统托盘初始化成功");
        } catch (AWTException e) {
            System.err.println("无法添加到系统托盘: " + e.getMessage());
        }
    }

    private Image createTrayImage() {
        // 创建一个简单的16x16像素的图标
        // 这里使用程序化生成的图标，你也可以加载图片文件
        int size = 16;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制一个简单的圆形图标
        g2d.setColor(new Color(46, 204, 113)); // 绿色
        g2d.fillOval(2, 2, size-4, size-4);

        // 绘制边框
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(2, 2, size-4, size-4);

        g2d.dispose();
        return image;
    }

    private void minimizeToTray() {
        if (systemTray != null && trayIcon != null) {
            frame.setVisible(false);
            isMinimizedToTray = true;

            // 显示托盘通知
            trayIcon.displayMessage("学习辅助工具",
                "程序已最小化到系统托盘，双击图标可重新显示窗口",
                TrayIcon.MessageType.INFO);
        } else {
            // 如果托盘不可用，则最小化到任务栏
            frame.setState(JFrame.ICONIFIED);
        }
    }

    private void showWindow() {
        if (isMinimizedToTray) {
            frame.setVisible(true);
            frame.setState(JFrame.NORMAL);
            frame.toFront();
            frame.requestFocus();
            isMinimizedToTray = false;
        }
    }

    private void exitApplication() {
        int result = JOptionPane.showConfirmDialog(null,
                "确定要退出吗？", "退出确认",
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            // 从系统托盘移除图标
            if (systemTray != null && trayIcon != null) {
                systemTray.remove(trayIcon);
            }
            timerService.stop();
            System.exit(0);
        }
    }

    // 获取frame引用，供其他类使用
    public JFrame getFrame() {
        return frame;
    }

    // 供外部调用的退出方法
    public void handleExit() {
        exitApplication();
    }
}