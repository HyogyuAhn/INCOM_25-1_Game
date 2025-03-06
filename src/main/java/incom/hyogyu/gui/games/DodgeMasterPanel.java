package incom.hyogyu.gui.games;

import incom.hyogyu.gui.GUIManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DodgeMasterPanel extends JPanel implements ActionListener {

    // --- [ 상수 ] ---
    private static final int GAME_WIDTH = 1344;
    private static final int GAME_HEIGHT = 710;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int INITIAL_PLAYER_SPEED = 5;
    private static final int INITIAL_DELAY = 20;

    // --- [ 게임 상태 ] ---
    private int playerX, playerY;
    private int playerSpeed = INITIAL_PLAYER_SPEED;
    private int score = 0;
    private int scoreMultiplier = 1;
    private boolean shieldActive = false;
    private boolean running = false;
    private Timer gameTimer;
    private Timer spawnTimer;
    private final List<FallingObject> objects = new ArrayList<>();
    private final Random random = new Random();
    private int updateCounter = 0;
    private boolean isGameOver = false;

    // --- [ 플레이어 이동 플래그 ] ---
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    // --- [ UI Components ] ---
    private final JButton restartButton = new JButton("다시 시작");
    private final JButton mainMenuButton = new JButton("메인 메뉴");
    private GUIManager manager;

    public DodgeMasterPanel(GUIManager manager) {
        this.manager = manager;
        setFocusable(true);
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.DARK_GRAY);
        setupKeyBindings();
        initButtons();
        initPlayer();
    }

    private void initPlayer() {
        playerX = (GAME_WIDTH - PLAYER_WIDTH) / 2;
        playerY = GAME_HEIGHT - PLAYER_HEIGHT - 20;
    }

    public void startGame() {
        if (running) return;
        running = true;
        isGameOver = false;
        score = 0;
        scoreMultiplier = 1;
        shieldActive = false;
        playerSpeed = INITIAL_PLAYER_SPEED;
        objects.clear();
        updateCounter = 0;
        initPlayer();
        leftPressed = false;
        rightPressed = false;
        restartButton.setVisible(false);
        mainMenuButton.setVisible(false);

        gameTimer = new Timer(INITIAL_DELAY, this);
        gameTimer.start();

        spawnTimer = new Timer(2000, e -> spawnFallingObject());
        spawnTimer.start();
    }

    public void stopGame() {
        running = false;
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
        if (spawnTimer != null) {
            spawnTimer.stop();
            spawnTimer = null;
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    // --- [ Key Bindings 설정 ] ---
    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("pressed LEFT"), "leftPressed");
        im.put(KeyStroke.getKeyStroke("released LEFT"), "leftReleased");
        im.put(KeyStroke.getKeyStroke("pressed RIGHT"), "rightPressed");
        im.put(KeyStroke.getKeyStroke("released RIGHT"), "rightReleased");

        am.put("leftPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leftPressed = true;
            }
        });
        am.put("leftReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leftPressed = false;
            }
        });
        am.put("rightPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rightPressed = true;
            }
        });
        am.put("rightReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rightPressed = false;
            }
        });
    }

    private void initButtons() {
        Font buttonFont = new Font("Arial", Font.BOLD, 18);
        restartButton.setFont(buttonFont);
        mainMenuButton.setFont(buttonFont);

        restartButton.addActionListener(e -> startGame());
        mainMenuButton.addActionListener(e -> manager.switchTo("MainMenu"));

        restartButton.setVisible(false);
        mainMenuButton.setVisible(false);
        setLayout(null);
        restartButton.setBounds(GAME_WIDTH / 2 - 100, GAME_HEIGHT / 2 + 50, 200, 50);
        mainMenuButton.setBounds(GAME_WIDTH / 2 - 100, GAME_HEIGHT / 2 + 110, 200, 50);
        add(restartButton);
        add(mainMenuButton);
    }

    // --- [ 플레이어 이동 업데이트 ] ---
    private void updatePlayer() {
        if (leftPressed) {
            playerX -= playerSpeed;
        }
        if (rightPressed) {
            playerX += playerSpeed;
        }
        // 화면 경계 체크
        if (playerX < 0) playerX = 0;
        if (playerX > GAME_WIDTH - PLAYER_WIDTH) playerX = GAME_WIDTH - PLAYER_WIDTH;
    }

    // --- [ FallingObject 클래스 및 관련 메서드 ] ---
    private enum FallingType { HARM, SCORE_MULTIPLIER, SPEED_UP, SHIELD }

    private static class FallingObject {
        int x, y, size, speed;
        FallingType type;

        FallingObject(int x, int y, int size, int speed, FallingType type) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
            this.type = type;
        }

        Rectangle getBounds() {
            return new Rectangle(x, y, size, size);
        }
    }

    // --- [ 아이템 등장 개수 제한 메서드 ] ---
    private int getCountOfType(FallingType type) {
        int count = 0;
        for (FallingObject obj : objects) {
            if (obj.type == type) count++;
        }
        return count;
    }

    private void spawnFallingObject() {
        int size = 40;
        int x = random.nextInt(GAME_WIDTH - size);
        int y = -size;
        // 기본 속도는 점수에 따라 서서히 증가 (최소 1)
        int baseSpeed = 1 + score / 200;
        FallingType type = FallingType.HARM;

        // 특수 효과 아이템 등장 확률 (최대 50%)는 점수가 높아질수록 증가
        double specialProbability = Math.min(0.5, score / 1000.0);
        if (random.nextDouble() < specialProbability) {
            // 특수 아이템 후보 리스트 (각 타입은 최대 2개 제한)
            List<FallingType> specials = new ArrayList<>();
            if (getCountOfType(FallingType.SHIELD) < 2) {
                specials.add(FallingType.SHIELD);
            }
            if (getCountOfType(FallingType.SCORE_MULTIPLIER) < 2) {
                specials.add(FallingType.SCORE_MULTIPLIER);
            }
            if (getCountOfType(FallingType.SPEED_UP) < 2) {
                specials.add(FallingType.SPEED_UP);
            }
            if (!specials.isEmpty()) {
                type = specials.get(random.nextInt(specials.size()));
            }
        }
        // 특수 아이템은 HARM보다 약간 느리게 떨어지도록 설정
        int speed = (type == FallingType.HARM) ? baseSpeed : Math.max(1, baseSpeed - 1);
        objects.add(new FallingObject(x, y, size, speed, type));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            updateGame();
            repaint();
        }
    }

    private void updateGame() {
        updateCounter++;
        // 1초(약 50 업데이트)마다 점수를 증가시킴
        if (updateCounter % 50 == 0) {
            score += scoreMultiplier;
        }
        updatePlayer();

        /*Iterator<FallingObject> it = objects.iterator();
        while (it.hasNext()) {
            FallingObject obj = it.next();
            obj.y += obj.speed;
            if (obj.y > GAME_HEIGHT) {
                it.remove();
                continue;
            }
            if (obj.getBounds().intersects(new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT))) {
                handleCollision(obj);
                it.remove();
            }
        }*/

        objects.removeIf(obj -> {
            obj.y += obj.speed;
            if (obj.y > GAME_HEIGHT) {
                return true;
            }
            if (obj.getBounds().intersects(new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT))) {
                handleCollision(obj);
                return true;
            }
            return false;
        });

        // 일정 점수마다 spawnTimer 딜레이 조정 (최소 500ms)
        int newDelay = Math.max(500, 2000 - score / 10);
        if (spawnTimer != null && spawnTimer.getDelay() != newDelay) {
            spawnTimer.setDelay(newDelay);
        }
    }

    private void handleCollision(FallingObject obj) {
        switch (obj.type) {
            case HARM -> {
                if (shieldActive) {
                    shieldActive = false;
                    showEventMessage("방어막 소멸!");
                } else {
                    endGame();
                }
            }
            case SCORE_MULTIPLIER -> {
                scoreMultiplier = 2;
                showEventMessage("점수 2배 획득!");
                new Timer(10000, e -> {
                    scoreMultiplier = 1;
                    repaint();
                }).start();
            }
            case SPEED_UP -> {
                playerSpeed += 2;
                showEventMessage("플레이어 속도 증가!");
                new Timer(10000, e -> {
                    playerSpeed = INITIAL_PLAYER_SPEED;
                    repaint();
                }).start();
            }
            case SHIELD -> {
                shieldActive = true;
                showEventMessage("1회 방어 효과 획득!");
            }
        }
    }

    private void endGame() {
        running = false;
        isGameOver = true;

        if (gameTimer != null) gameTimer.stop();
        if (spawnTimer != null) spawnTimer.stop();

        removeAll();
        repaint();

        JLabel gameOverLabel = new JLabel("게임 종료!", SwingConstants.CENTER);
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 40));
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setBounds(GAME_WIDTH / 2 - 200, 200, 400, 50);
        add(gameOverLabel);

        JLabel scoreLabel = new JLabel("점수: " + score, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 30));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setBounds(GAME_WIDTH / 2 - 200, 270, 400, 50);
        add(scoreLabel);

        restartButton.setVisible(true);
        mainMenuButton.setVisible(true);
        add(restartButton);
        add(mainMenuButton);

        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isGameOver) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        // 플레이어 그리기 (방어막 활성 시 테두리 표시)
        if (shieldActive) {
            g.setColor(Color.CYAN);
            g.fillOval(playerX - 5, playerY - 5, PLAYER_WIDTH + 10, PLAYER_HEIGHT + 10);
        }
        g.setColor(Color.WHITE);
        g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        // FallingObject 그리기
        for (FallingObject obj : objects) {
            switch (obj.type) {
                case HARM -> g.setColor(Color.RED);
                case SCORE_MULTIPLIER -> g.setColor(Color.MAGENTA);
                case SPEED_UP -> g.setColor(Color.ORANGE);
                case SHIELD -> g.setColor(Color.GREEN);
            }
            g.fillOval(obj.x, obj.y, obj.size, obj.size);
        }

        // 점수 및 상태 표시
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("점수: " + score, 20, 30);
        if (scoreMultiplier > 1) {
            g.drawString("점수배율: " + scoreMultiplier + "x", 20, 60);
        }
        if (playerSpeed > INITIAL_PLAYER_SPEED) {
            g.drawString("속도 증가!", 20, 90);
        }
        if (!activeEventMessage.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString(activeEventMessage, 20, 120);
        }
    }

    private String activeEventMessage = "";

    private void showEventMessage(String message) {
        activeEventMessage = message;
        Timer msgTimer = new Timer(2500, e -> {
            activeEventMessage = "";
            repaint();
        });
        msgTimer.setRepeats(false);
        msgTimer.start();
    }
}