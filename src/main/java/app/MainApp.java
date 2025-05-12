package app;

import com.formdev.flatlaf.FlatLightLaf;
import ui.MainWindow;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // 设置现代化主题
        FlatLightLaf.setup();
        
        SwingUtilities.invokeLater(() -> {
            new MainWindow().createAndShowGUI();
        });
    }
} 