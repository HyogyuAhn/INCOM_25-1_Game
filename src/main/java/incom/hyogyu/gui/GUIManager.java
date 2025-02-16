package incom.hyogyu.gui;

import incom.hyogyu.gui.games.SnakeGamePanel;

import javax.swing.*;
import java.awt.*;

public class GUIManager {
    private JFrame frame;
    private JPanel mainMenu;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public GUIManager() {
        frame = new JFrame("[INCOM] 동아리 박람회 게임");
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        createMainMenu();

        cardPanel.add(mainMenu, "MainMenu");
        cardPanel.add(new SnakeGamePanel(this), "SnakeGame");

        frame.add(cardPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1366, 768);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private void createMainMenu() {
        mainMenu = new MainMenuPanel(this);
    }

    public void switchTo(String panelName) {
        cardLayout.show(cardPanel, panelName);

        for (Component component : cardPanel.getComponents()) {
            if (component instanceof SnakeGamePanel snakeGamePanel) {
                if (panelName.equals("SnakeGame")) {
                    snakeGamePanel.startGame();
                    snakeGamePanel.enableKeyListener(true);
                } else {
                    snakeGamePanel.stopGame();
                    snakeGamePanel.enableKeyListener(false);
                }
            }
        }
    }

}
