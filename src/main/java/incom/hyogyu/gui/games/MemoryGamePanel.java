package incom.hyogyu.gui.games;

import incom.hyogyu.gui.GUIManager;
import incom.hyogyu.util.FontManager;
import incom.hyogyu.util.FontManager.GameFont;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MemoryGamePanel extends JPanel {
    private final GUIManager manager;
    private final JButton[] buttons = new JButton[4];
    private final String[] colors = {"RED", "BLUE", "GREEN", "YELLOW"};
    private final List<Integer> sequence = new ArrayList<>();
    private final Random random = new Random();
    private int currentRound = 1;
    private int userIndex = 0;
    private int lives = 3;
    private boolean playerTurn = false;
    private JButton restartButton;
    private JButton mainMenuButton;
    private JLabel scoreLabel;
    private JLabel turnLabel;
    private Timer timer;
    private final HashMap<String, Image> images = new HashMap<>();
    private FontManager fontManager = new FontManager();
    private boolean isGameOver = false;
    private Image gameOverBackground;

    public MemoryGamePanel(GUIManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout());
        loadAssets();
        gameOverBackground = new ImageIcon(getClass().getResource("/images/Gameover.jpeg")).getImage();
        
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.add(Box.createVerticalGlue());

        scoreLabel = new JLabel("라운드: 1");
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreLabel.setFont(fontManager.loadCustomFont(GameFont.CHILD_FUND_KOREA, 30f));
        headerPanel.add(scoreLabel);

        turnLabel = new JLabel("컴퓨터의 차례...");
        turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        turnLabel.setFont(fontManager.loadCustomFont(GameFont.CHILD_FUND_KOREA, 26f));
        headerPanel.add(turnLabel);

        headerPanel.add(Box.createVerticalGlue());
        add(headerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 2, 2));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        for (int i = 0; i < 4; i++) {
            buttons[i] = new JButton();
            buttons[i].setFocusPainted(false);
            ImageIcon icon = new ImageIcon(images.get(colors[i].toLowerCase() + "Button"));
            buttons[i].setIcon(icon);
            buttons[i].setDisabledIcon(icon);
            buttons[i].setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            buttons[i].setOpaque(false);
            buttons[i].setBorderPainted(false);
            buttons[i].setContentAreaFilled(false);
            

        buttonPanel.add(buttons[i]);
        }
        
        // Set up key bindings for q, w, e, r
        setFocusable(true);
        requestFocusInWindow();
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke('q'), "press0");
        actionMap.put("press0", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simulateKeyPress(0);
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke('w'), "press1");
        actionMap.put("press1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simulateKeyPress(1);
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke('e'), "press2");
        actionMap.put("press2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simulateKeyPress(2);
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke('r'), "press3");
        actionMap.put("press3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simulateKeyPress(3);
            }
        });

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setOpaque(false);
        centerWrapper.add(buttonPanel);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(false);
        restartButton = new JButton("다시하기");
        restartButton.setFont(fontManager.loadCustomFont(GameFont.JALNAN, 20f));
        restartButton.addActionListener(e -> startGame());
        restartButton.setVisible(false);
        controlPanel.add(restartButton);

        mainMenuButton = new JButton("메인 메뉴");
        mainMenuButton.setFont(fontManager.loadCustomFont(GameFont.JALNAN, 20f));
        mainMenuButton.addActionListener(e -> {
            manager.switchTo("MainMenu");
            stopGame(); // 메인으로 갈 때 UI 초기화
        });
        mainMenuButton.setVisible(false);
        controlPanel.add(mainMenuButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(centerWrapper, BorderLayout.CENTER);
        southPanel.add(controlPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void simulateKeyPress(int index) {
        if (!playerTurn || sequence.isEmpty()) return;
        buttons[index].setIcon(new ImageIcon(images.get(colors[index].toLowerCase() + "ButtonPush")));
        handleUserInput(index);
        new Timer(200, e -> buttons[index].setIcon(new ImageIcon(images.get(colors[index].toLowerCase() + "Button")))).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isGameOver) {
            if (gameOverBackground != null) {
                g.drawImage(gameOverBackground, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            return;
        }

        Image background = images.get("memoryBackground");
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }

        if (lives > 0) {
            int heartX = 20;
            int heartY = 20;
            for (int i = 0; i < 3; i++) {
                Image heartImage = (i < lives) ? images.get("life") : images.get("brokenLife");
                if (heartImage != null) {
                    g.drawImage(heartImage, heartX + (i * (30 + 10)), heartY, 30, 30, this);
                }
            }
        }
    }

    public void startGame() {
        stopGame();
        removeAll();  // UI 초기화
        setLayout(new BorderLayout());
        
        // UI를 다시 생성
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.add(Box.createVerticalGlue());

        scoreLabel = new JLabel("라운드: 1");
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreLabel.setFont(fontManager.loadCustomFont(GameFont.CHILD_FUND_KOREA, 30f));
        headerPanel.add(scoreLabel);

        turnLabel = new JLabel("컴퓨터의 차례...");
        turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        turnLabel.setFont(fontManager.loadCustomFont(GameFont.CHILD_FUND_KOREA, 26f));
        headerPanel.add(turnLabel);
        
        headerPanel.add(Box.createVerticalGlue());
        add(headerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 2, 2));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        for (int i = 0; i < 4; i++) {
            buttons[i].setEnabled(true);
            buttons[i].setIcon(new ImageIcon(images.get(colors[i].toLowerCase() + "Button")));
            buttonPanel.add(buttons[i]);
        }

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setOpaque(false);
        centerWrapper.add(buttonPanel);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(false);
        restartButton.setVisible(false);
        mainMenuButton.setVisible(false);
        controlPanel.add(restartButton);
        controlPanel.add(mainMenuButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(centerWrapper, BorderLayout.CENTER);
        southPanel.add(controlPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        sequence.clear();
        sequence.add(random.nextInt(4));
        currentRound = 0;
        lives = 3;
        isGameOver = false;

        revalidate(); // UI 갱신
        repaint(); // 다시 그리기

        Timer startDelay = new Timer(1000, e -> {
            nextRound();
            ((Timer)e.getSource()).stop();
        });
        startDelay.setRepeats(false);
        startDelay.start();
    }
    
    private void nextRound() {
        for (JButton button : buttons) {
            button.setEnabled(false);
        }
        playerTurn = false;
        userIndex = 0;
        scoreLabel.setText("라운드: " + (currentRound + 1));
        turnLabel.setText("컴퓨터의 차례...");
        
        Timer showSequenceTimer = new Timer(600, new AbstractAction() {
            private int index = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index < sequence.size()) {
                    int colorIndex = sequence.get(index);
                    highlightButton(colorIndex);
                    index++;
                } else {
                    ((Timer)e.getSource()).stop();
                    for (JButton button : buttons) {
                        button.setEnabled(true);
                    }
                    playerTurn = true;
                currentRound++;
                turnLabel.setText("플레이어의 차례!");
                }
            }
        });
        showSequenceTimer.setInitialDelay(0);
        showSequenceTimer.start();
    }
    
    private void retryRound() {
        for (JButton button : buttons) {
            button.setEnabled(false);
        }
        playerTurn = false;
        userIndex = 0;
        turnLabel.setText("컴퓨터의 차례...");
        
        Timer retryTimer = new Timer(1000, new AbstractAction() {
            private int index = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index < sequence.size()) {
                    int colorIndex = sequence.get(index);
                    highlightButton(colorIndex);
                    index++;
                } else {
                    ((Timer)e.getSource()).stop();
                    for (JButton button : buttons) {
                        button.setEnabled(true);
                    }
                    playerTurn = true;
                    turnLabel.setText("플레이어의 차례!");
                }
            }
        });
        retryTimer.setInitialDelay(0);
        retryTimer.start();
    }
    
    private void highlightButton(int index) {
        ImageIcon pushIcon = new ImageIcon(images.get(colors[index].toLowerCase() + "ButtonPush"));
        buttons[index].setIcon(pushIcon);
        buttons[index].setDisabledIcon(pushIcon);
        buttons[index].repaint();
        
        Timer resetTimer = new Timer(320, e -> {
            ImageIcon normalIcon = new ImageIcon(images.get(colors[index].toLowerCase() + "Button"));
            buttons[index].setIcon(normalIcon);
            buttons[index].setDisabledIcon(normalIcon);
            buttons[index].repaint();
        });
        resetTimer.setRepeats(false);
        resetTimer.start();
    }
    
    private void handleUserInput(int index) {
        if (!playerTurn || sequence.isEmpty()) return;
        
        buttons[index].setFocusPainted(false);
        buttons[index].setBorderPainted(false);
        buttons[index].setBackground(Color.LIGHT_GRAY);
        new Timer(200, e -> buttons[index].setBackground(getColor(colors[index]))).start();
        
        if (sequence.get(userIndex) == index) {
            userIndex++;
            if (userIndex == sequence.size()) {
                JOptionPane.showMessageDialog(this, "성공! 다음 라운드로 이동합니다.", "[INCOM] 기억력 게임", JOptionPane.PLAIN_MESSAGE);
                Timer nextDelay = new Timer(500, e -> {
                    sequence.add(random.nextInt(4));
                    nextRound();
                });
                nextDelay.setRepeats(false);
                nextDelay.start();
            }
        } else {
            lives--;
            repaint();
            if (lives > 0) {
                JOptionPane.showMessageDialog(this, "틀렸습니다!", "[INCOM] 기억력 게임", JOptionPane.PLAIN_MESSAGE);
                Timer retryDelay = new Timer(500, e -> {
                    userIndex = 0;
                    turnLabel.setText("컴퓨터의 차례...");
                    retryRound();
                });
                retryDelay.setRepeats(false);
                retryDelay.start();
            } else {
                endGame();
            }
        }
    }
    
    private void endGame() {
        stopGame();
        lives = 0;
        isGameOver = true;

        removeAll();
        setLayout(null);

        JLabel gameOverLabel = new JLabel("게임 오버!", SwingConstants.CENTER);
        gameOverLabel.setFont(fontManager.loadCustomFont(GameFont.JALNAN, 40f));
        gameOverLabel.setForeground(Color.WHITE);
        gameOverLabel.setBounds(getWidth() / 2 - 200, 150, 400, 50);
        add(gameOverLabel);

        JLabel scoreDisplayLabel = new JLabel("점수 : " + (currentRound - 1), SwingConstants.CENTER);
        scoreDisplayLabel.setFont(fontManager.loadCustomFont(GameFont.JALNAN, 30f));
        scoreDisplayLabel.setForeground(Color.WHITE);
        scoreDisplayLabel.setBounds(getWidth() / 2 - 200, 220, 400, 50);
        add(scoreDisplayLabel);

        Color buttonColor = new Color(165, 145, 109); // 버튼 색상

        JButton restartBtn = new JButton("다시하기");
        restartBtn.setFont(fontManager.loadCustomFont(GameFont.JALNAN, 24f));
        restartBtn.setBackground(buttonColor);
        restartBtn.setForeground(Color.WHITE);
        restartBtn.setOpaque(true);
        restartBtn.setContentAreaFilled(false);
        restartBtn.setBorderPainted(false);
        restartBtn.setFocusPainted(false);
        restartBtn.setBounds(getWidth() / 2 - 275, getHeight() / 2, 200, 60);
        restartBtn.addActionListener(e -> startGame());
        add(restartBtn);

        JButton menuBtn = new JButton("메인 메뉴");
        menuBtn.setFont(fontManager.loadCustomFont(GameFont.JALNAN, 24f));
        menuBtn.setBackground(buttonColor);
        menuBtn.setForeground(Color.WHITE);
        menuBtn.setOpaque(true);
        menuBtn.setContentAreaFilled(false);
        menuBtn.setBorderPainted(false);
        menuBtn.setFocusPainted(false);
        menuBtn.setBounds(getWidth() / 2 + 115, getHeight() / 2, 200, 60);
        menuBtn.addActionListener(e -> manager.switchTo("MainMenu"));
        add(menuBtn);

        revalidate();
        repaint();
    }

    public void stopGame() {
        playerTurn = false;
        userIndex = 0;
        sequence.clear();

        for (JButton button : buttons) {
            button.setEnabled(true);
            button.setIcon(new ImageIcon(images.get(colors[buttons.length - 1].toLowerCase() + "Button")));
        }

        isGameOver = false;
        restartButton.setVisible(false);
        mainMenuButton.setVisible(false);

        if (timer != null) {
            timer.stop();
        }

        removeAll();
        revalidate();
        repaint();
    }

    private Color getColor(String colorName) {
        return switch (colorName) {
            case "RED" -> Color.RED;
            case "BLUE" -> Color.BLUE;
            case "GREEN" -> Color.GREEN;
            case "YELLOW" -> Color.YELLOW;
            default -> Color.BLACK;
        };
    }

    private void loadAssets() {
        String[] imageKeys = {
            "redButton", "redButtonPush", 
            "blueButton", "blueButtonPush",
            "greenButton", "greenButtonPush", 
            "yellowButton", "yellowButtonPush",
            "memoryBackground", "life", "brokenLife"
        };
        for (String key : imageKeys) {
            try {
                images.put(key, loadImage(key + ".png"));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private Image loadImage(String fileName) {
        return new ImageIcon(getClass().getResource("/images/memoryAssets/" + fileName)).getImage();
    }
}
