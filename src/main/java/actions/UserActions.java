package actions;

import core.StateManager;
import core.TimerService;
import ui.MainWindow;

import javax.swing.*;
import java.awt.event.ActionListener;

public class UserActions {
    private final MainWindow mainWindow;
    private final StateManager stateManager;
    private final TimerService timerService;

    public UserActions(MainWindow mainWindow, StateManager stateManager, TimerService timerService) {
        this.mainWindow = mainWindow;
        this.stateManager = stateManager;
        this.timerService = timerService;
        bindActions();
    }

    private void bindActions() {
        JButton startButton = mainWindow.getStartButton();
        JButton pauseButton = mainWindow.getPauseButton();
        JButton exitButton = mainWindow.getExitButton();

        startButton.addActionListener(startAction());
        pauseButton.addActionListener(pauseAction());
        exitButton.addActionListener(exitAction());
    }

    private ActionListener startAction() {
        return e -> {
            if (!stateManager.isStudying()) {
                timerService.start();
            }
        };
    }

    private ActionListener pauseAction() {
        return e -> {
            if (stateManager.isStudying()) {
                timerService.pause();
            } else if (stateManager.isPaused()) {
                timerService.start();
            }
        };
    }

    private ActionListener exitAction() {
        return e -> {
            int result = JOptionPane.showConfirmDialog(null,
                    "确定要优雅退出吗？", "退出确认",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                timerService.stop();
                System.exit(0);
            }
        };
    }
}