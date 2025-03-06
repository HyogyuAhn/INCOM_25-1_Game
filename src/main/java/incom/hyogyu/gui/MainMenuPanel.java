package incom.hyogyu.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(GUIManager manager) {
        setLayout(null);

        JLabel titleLabel = new JLabel("< 아래에서 게임을 선택하세요 >", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setBounds(440, 10, 500, 100);
        add(titleLabel);

        JButton snakeButton = new JButton("스네이크 게임");
        snakeButton.setFont(new Font("Arial", Font.BOLD, 20));
        snakeButton.setBounds(583, 334, 200, 50);
        snakeButton.addActionListener(e -> manager.switchTo("SnakeGame"));
        add(snakeButton);

        JButton dodgeMasterButton = new JButton("피하기 게임");
        dodgeMasterButton.setFont(new Font("Arial", Font.BOLD, 20));
        dodgeMasterButton.setBounds(583, 400, 200, 50);
        dodgeMasterButton.addActionListener(e -> manager.switchTo("DodgeMaster"));
        add(dodgeMasterButton);
    }

}
