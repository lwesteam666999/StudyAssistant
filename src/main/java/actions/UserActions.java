package actions;

import core.StateManager;
import core.TimerService;
import ui.MainWindow;



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
            mainWindow.handleExit();
        });
    }
}