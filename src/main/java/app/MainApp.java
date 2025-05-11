package app;

import ui.MainWindow;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainWindow().createAndShowGUI();
        });
    }
}

