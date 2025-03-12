package incom.hyogyu.gui.games;

import incom.hyogyu.gui.GUIManager;
import incom.hyogyu.util.FontManager;
import incom.hyogyu.util.FontManager.GameFont;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class DodgeMasterPanel extends JPanel implements ActionListener {

    // --- [ 상수 ] ---
    private static final int GAME_WIDTH = 1344;
    private static final int GAME_HEIGHT = 710;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int INITIAL_PLAYER_SPEED = 6;
    private static final int INITIAL_DELAY = 20;

    // --- [ 게임 상태 ] ---
    private int playerX, playerY;
    private int playerSpeed = INITIAL_PLAYER_SPEED;
    private int score = 0;
    private int scoreMultiplier = 1;
    private boolean shieldActive = false;
    private boolean speedUpActive = false;
    private boolean scoreMultiplierActive = false;
    private boolean running = false;
    private Timer gameTimer;
    private Timer spawnTimer;
    private final List<FallingObject> objects = new ArrayList<>();
    private final Random random = new Random();
    private int updateCounter = 0;
    private boolean isGameOver = false;
    private final HashMap<String, Image> images = new HashMap<>();
    private Image gameOverBackground;
    private FontManager fontManager = new FontManager();
    private int playerAnimFrame = 0;
    private Timer playerAnimTimer;
    private String lastPlayerImageKey = "playerLeft";

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
        gameOverBackground = new ImageIcon(getClass().getResource("/images/Gameover.jpeg")).getImage();
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
        
        loadAssets();
        removeAll();
        add(restartButton);
        add(mainMenuButton);
        
        gameTimer = new Timer(INITIAL_DELAY, this);
        gameTimer.start();
    
        spawnTimer = new Timer(800, e -> spawnFallingObject());
        spawnTimer.start();
        
        playerAnimTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (leftPressed || rightPressed) {
                    playerAnimFrame = (playerAnimFrame + 1) % 2;
                    repaint();
                } else {
                    playerAnimFrame = 0;
                }
            }
        });
        playerAnimTimer.start();
        
        revalidate();
        repaint();
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
        if (playerAnimTimer != null) {
            playerAnimTimer.stop();
            playerAnimTimer = null;
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
        Font buttonFont = fontManager.loadCustomFont(GameFont.JALNAN, 24f);
        restartButton.setFont(buttonFont);
        mainMenuButton.setFont(buttonFont);

        Color buttonColor = new Color(165, 145, 109);
        restartButton.setBackground(buttonColor);
        restartButton.setForeground(Color.WHITE);
        restartButton.setOpaque(true);
        restartButton.setContentAreaFilled(false);
        restartButton.setBorderPainted(false);
        restartButton.setFocusPainted(false);

        mainMenuButton.setBackground(buttonColor);
        mainMenuButton.setForeground(Color.WHITE);
        mainMenuButton.setOpaque(true);
        mainMenuButton.setContentAreaFilled(false);
        mainMenuButton.setBorderPainted(false);
        mainMenuButton.setFocusPainted(false);

        restartButton.addActionListener(e -> startGame());
        mainMenuButton.addActionListener(e -> manager.switchTo("MainMenu"));

        restartButton.setVisible(false);
        mainMenuButton.setVisible(false);
        setLayout(null);
        restartButton.setBounds(GAME_WIDTH / 2 - 275, GAME_HEIGHT / 2 - 10, 200, 60);
        mainMenuButton.setBounds(GAME_WIDTH / 2 + 140, GAME_HEIGHT / 2 - 10, 200, 60);
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

    // --- [ 이미지 로드 ] ---
    private void loadAssets() {
        String[] imageKeys = {
            "trash1", "trash2", "trash3", "trash4",
            "multiScore", "speedUp", "shield",
            "dodgeBackground",
            "playerLeft", "playerRight", "playerSLeft", "playerSRight"
        };
        for (String key : imageKeys) {
            images.put(key, loadImage(key + ".png"));
        }
    }

    private Image loadImage(String fileName) {
        return new ImageIcon(getClass().getResource("/images/dodgeAssets/" + fileName)).getImage();
    }

    // --- [ FallingObject 클래스 및 관련 메서드 ] ---
    private enum FallingType { HARM, SCORE_MULTIPLIER, SPEED_UP, SHIELD }

    private static class FallingObject {
        int x, size;
        double y, speed;
        FallingType type;
        String imageKey;

        FallingObject(int x, double y, int size, double speed, FallingType type, String imageKey) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
            this.type = type;
            this.imageKey = imageKey;
        }

        Rectangle getBounds() {
            return new Rectangle(x, (int) y, size, size);
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
        double y = -size;
        double baseSpeed = 6.5 + score / 500;
        FallingType type = FallingType.HARM;

        double specialProbability = (score < 120) ? (0.01 + Math.min(0.09, score / 10000.0))
                : (0.01 + Math.min(0.05, score / 20000.0));
        boolean isSpecialItem = random.nextDouble() < specialProbability;
        
        if (isSpecialItem) {
            List<FallingType> specials = new ArrayList<>();
            if (!shieldActive && getCountOfType(FallingType.SHIELD) < 1) specials.add(FallingType.SHIELD);
            if (!scoreMultiplierActive && getCountOfType(FallingType.SCORE_MULTIPLIER) < 1) specials.add(FallingType.SCORE_MULTIPLIER);
            if (!speedUpActive && getCountOfType(FallingType.SPEED_UP) < 1) specials.add(FallingType.SPEED_UP);

            if (!specials.isEmpty()) {
                type = specials.get(random.nextInt(specials.size()));
            }
        }

        double speed = (type == FallingType.HARM) ? baseSpeed : Math.max(6.0, baseSpeed - 1.5);

        String imageKey;
        if (type == FallingType.HARM) {
            String[] trashImages = {"trash1", "trash2", "trash3", "trash4"};
            imageKey = trashImages[random.nextInt(trashImages.length)];
        } else if (type == FallingType.SCORE_MULTIPLIER) {
            imageKey = "multiScore";
        } else if (type == FallingType.SPEED_UP) {
            imageKey = "speedUp";
        } else {
            imageKey = "shield";
        }

        objects.add(new FallingObject(x, y, size, speed, type, imageKey));

        if (isSpecialItem) {
            for (int i = 0; i < 2; i++) {
                int harmX = random.nextInt(GAME_WIDTH - size);
                String harmImage = new String[]{"trash1", "trash2", "trash3", "trash4"}[random.nextInt(4)];
                objects.add(new FallingObject(harmX, y, size, baseSpeed, FallingType.HARM, harmImage));
            }
        }
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
        if (updateCounter % 20 == 0) {
            score += scoreMultiplier;
        }
        updatePlayer();

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

        int newDelay = (score < 140) ? Math.max(40, 500 - (score * 3))
                : (score < 280) ? Math.max(50, 600 - (score * 2))
                : Math.max(60, 700 - (score * 1));
        if (spawnTimer != null && spawnTimer.getDelay() != newDelay) {
            spawnTimer.setDelay(newDelay);
        }
    }

    private void handleCollision(FallingObject obj) {
        switch (obj.type) {
            case HARM -> {
                if (shieldActive) {
                    shieldActive = false;
                    showEventMessage("< 방어막 소멸! >");
                } else {
                    endGame();
                }
            }
            case SCORE_MULTIPLIER -> {
                if (!scoreMultiplierActive) {
                    scoreMultiplierActive = true;
                    scoreMultiplier = 2;
                    showEventMessage("< 점수 2배 획득! >");
                    new Timer(6000, e -> {
                        scoreMultiplierActive = false;
                        scoreMultiplier = 1;
                        repaint();
                    }).start();
                }
            }
            case SPEED_UP -> {
                if (!speedUpActive) {
                    speedUpActive = true;
                    playerSpeed += 2;
                    showEventMessage("< 플레이어 속도 증가! >");
                    new Timer(10000, e -> {
                        speedUpActive = false;
                        playerSpeed = INITIAL_PLAYER_SPEED;
                        repaint();
                    }).start();
                }
            }
            case SHIELD -> {
                shieldActive = true;
                showEventMessage("< 1회 방어 효과 획득! >");
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

        JLabel gameOverLabel = new JLabel("피하기 게임 종료!", SwingConstants.CENTER);
        gameOverLabel.setFont(fontManager.loadCustomFont(GameFont.JALNAN, 40f));
        gameOverLabel.setForeground(Color.WHITE);
        gameOverLabel.setBounds(GAME_WIDTH / 2 - 200, 150, 400, 50);
        add(gameOverLabel);

        JLabel scoreLabel = new JLabel("점수: " + score, SwingConstants.CENTER);
        scoreLabel.setFont(fontManager.loadCustomFont(GameFont.JALNAN, 30f));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setBounds(GAME_WIDTH / 2 - 200, 220, 400, 50);
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
        if (gameOverBackground != null) {
                g.drawImage(gameOverBackground, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            return;
        }
 
        if (images.get("dodgeBackground") != null) {
            g.drawImage(images.get("dodgeBackground"), 0, 0, getWidth(), getHeight(), this);
        }

        String imageKey;
        if (leftPressed || rightPressed) {
            if (playerAnimFrame == 0) {
                imageKey = shieldActive ? "playerSLeft" : "playerLeft";
            } else {
                imageKey = shieldActive ? "playerSRight" : "playerRight";
            }
            lastPlayerImageKey = imageKey;
        } else {
            imageKey = lastPlayerImageKey;
        }
        Image playerImage = images.get(imageKey);
        if (playerImage != null) {
            g.drawImage(playerImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, this);
        }

        for (FallingObject obj : objects) {
            Image objectImage = images.get(obj.imageKey);
            if (objectImage != null) {
                g.drawImage(objectImage, obj.x, (int) obj.y, obj.size, obj.size, this);
            }
        }

        g.setColor(Color.WHITE);
        g.setFont(fontManager.loadCustomFont(GameFont.LINE_SEED_BOLD, 20f));
        g.drawString("점수: " + score, 20, 30);
        if (scoreMultiplier > 1) {
            g.drawString("점수배율: " + scoreMultiplier + "x", 20, 60);
        }
        if (playerSpeed > INITIAL_PLAYER_SPEED) {
            g.drawString("속도 증가!", 20, 90);
        }
        if (!activeEventMessage.isEmpty()) {
            g.setFont(fontManager.loadCustomFont(GameFont.DUNG_GEUN_MO, 28f));
            g.setColor(Color.BLACK);
            
            FontMetrics fm = g.getFontMetrics();
            g.drawString(activeEventMessage, (getWidth() - fm.stringWidth(activeEventMessage)) / 2, 155);
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