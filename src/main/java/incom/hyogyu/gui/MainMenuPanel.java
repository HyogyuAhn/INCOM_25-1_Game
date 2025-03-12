package incom.hyogyu.gui;

import javax.swing.*;

import incom.hyogyu.util.FontManager;
import incom.hyogyu.util.FontManager.GameFont;

import java.awt.*;

public class MainMenuPanel extends JPanel {

    private FontManager font = new FontManager();
    private Image backgroundImage;

    public MainMenuPanel(GUIManager manager) {
        setLayout(null);
        backgroundImage = new ImageIcon(getClass().getResource("/images/Main.png")).getImage();

        Font buttonFont = font.loadCustomFont(GameFont.JALNAN, 26f);

        JButton snakeButton = new JButton("스네이크 게임");
        snakeButton.setFont(buttonFont.deriveFont(26f));
        snakeButton.setBackground(new Color(165, 145, 109));
        snakeButton.setForeground(Color.WHITE);
        snakeButton.setOpaque(true);
        snakeButton.setContentAreaFilled(false);
        snakeButton.setBorderPainted(false);
        snakeButton.setFocusPainted(false);
        snakeButton.setBounds(230, 500, 220, 60);
        snakeButton.addActionListener(e -> manager.switchTo("SnakeGame"));
        add(snakeButton);

        JButton dodgeMasterButton = new JButton("피하기 게임");
        dodgeMasterButton.setFont(buttonFont.deriveFont(26f));
        dodgeMasterButton.setBackground(new Color(165, 145, 109));
        dodgeMasterButton.setForeground(Color.WHITE);
        dodgeMasterButton.setOpaque(true);
        dodgeMasterButton.setContentAreaFilled(false);
        dodgeMasterButton.setBorderPainted(false);
        dodgeMasterButton.setFocusPainted(false);
        dodgeMasterButton.setBounds(590, 500, 220, 60);
        dodgeMasterButton.addActionListener(e -> manager.switchTo("DodgeMaster"));
        add(dodgeMasterButton);

        JButton memoryGameButton = new JButton("기억력 게임");
        memoryGameButton.setFont(buttonFont.deriveFont(26f));
        memoryGameButton.setBackground(new Color(165, 145, 109));
        memoryGameButton.setForeground(Color.WHITE);
        memoryGameButton.setOpaque(true);
        memoryGameButton.setContentAreaFilled(false);
        memoryGameButton.setBorderPainted(false);
        memoryGameButton.setFocusPainted(false);
        memoryGameButton.setBounds(950, 500, 220, 60);
        memoryGameButton.addActionListener(e -> manager.switchTo("MemoryGame"));
        add(memoryGameButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
