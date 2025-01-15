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

        JButton startButton = new JButton("Start Snake Game");
        startButton.setBounds(583, 334, 200, 50);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manager.switchTo("SnakeGame");
            }
        });

        add(startButton);
    }

}
