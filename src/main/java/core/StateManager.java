package core;

public class StateManager {
    private LearningState currentState;

    public StateManager() {
        this.currentState = LearningState.IDLE;
    }

    public LearningState getState() {
        return currentState;
    }

    public void setState(LearningState newState) {
        System.out.println("状态切换: " + currentState + " -> " + newState);
        this.currentState = newState;
    }

    public boolean isStudying() {
        return currentState == LearningState.STUDYING;
    }

    public boolean isPaused() {
        return currentState == LearningState.PAUSED;
    }

    public boolean isBreaking() {
        return currentState == LearningState.BREAK;
    }

    public boolean isIdle() {
        return currentState == LearningState.IDLE;
    }
}
