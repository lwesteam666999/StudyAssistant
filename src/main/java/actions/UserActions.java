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
        mainWindow.getStartButton().addActionListener(e -> {
            if (!stateManager.isStudying()) {
                timerService.start();
            }
        });
        
        mainWindow.getPauseButton().addActionListener(e -> {
            if (stateManager.isStudying()) {
                timerService.pause();
            } else if (stateManager.isPaused()) {
                timerService.start();
            }
        });
        
        mainWindow.getExitButton().addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(null,
                    "确定要退出吗？", "退出确认",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                timerService.stop();
                System.exit(0);
            }
        });
    }
}