package incom.hyogyu.gui.games;

import incom.hyogyu.gui.GUIManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SnakeGamePanel extends JPanel implements ActionListener {

    // --- [ 상수 ] ---
    private static final int TILE_SIZE = 32;  // 타일 크기
    private static final int GAME_WIDTH = 1344;  // 게임 너비 (연산을 위해 축소)
    private static final int GAME_HEIGHT = 710;  // 게임 높이 (맵 이탈 방지를 위해 축소)
    private static final int INITIAL_DELAY = 120;  // 프레임 속도 (ms)
    private static final int SCORE_AREA_X = GAME_WIDTH - 130;
    private static final int SCORE_AREA_Y = 10;
    private static final int SCORE_AREA_WIDTH = 120;
    private static final int SCORE_AREA_HEIGHT = 30;

    // --- [ 게임 상태 ] ---
    private List<Point> snakeBody;  // 뱀 몸통
    private Point food;  // 사과 위치
    private Direction currentDirection;  // 방향
    private boolean running;  // 게임 실행 여부
    private int score = 0;  // 점수
    private Timer gameTimer;  // 게임 타이머
    private boolean isKeyInputLocked = false;
    private final HashMap<String, Image> images = new HashMap<>();

    // --- [ 이벤트 ] ---
    private String activeEventMessage = "";
    private int applesEaten = 0;
    private int scoreMultiplier = 1;
    private int sizeReduction = 0;

    private Timer eventTimer;  // 이벤트 지속 타이머
    private Timer eventMessageTimer;  // 이벤트 메시지 타이머

    // --- [ UI Components ] ---
    private JButton restartButton = new JButton("다시 시작");
    private JButton mainMenuButton = new JButton("메인 메뉴");
    private GUIManager manager;

    // --- [ 키 입력 처리 ] ---
    private final KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            changeDirection(e.getKeyCode());
        }
    };

    // --- [ SnakeGame ] ---
    public SnakeGamePanel(GUIManager manager) {
        this.manager = manager;
        setFocusable(true);
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.BLACK);
        enableKeyListener(false);
        initButtons();

        food = null;
        running = false;
    }

    // --- [ 게임 시작 ] ---
    public void startGame() {
        if (running) return;
        running = true;
        score = 0;
        applesEaten = 0;
        scoreMultiplier = 1;
        sizeReduction = 0;
        snakeBody = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            snakeBody.add(new Point(100 - i * TILE_SIZE, 100));
        }
        currentDirection = Direction.RIGHT;

        loadAssets();
        spawnFood();
        enableKeyListener(true);
        restartButton.setVisible(false);
        mainMenuButton.setVisible(false);

        gameTimer = new Timer(INITIAL_DELAY, this);
        gameTimer.start();
        System.out.println("[SNAKE] 게임 시작 및 타이머 ON");

    }

    // --- [ 게임 중단 ] ---
    public void stopGame() {
        if (gameTimer != null) {
            running = false;
            enableKeyListener(false);
            resetEventEffects();
            gameTimer.stop();
            gameTimer = null;
            System.out.println("[SNAKE] 게임 중단");
        }
    }

    // --- [ 이벤트 초기화 ] ---
    private void resetEventEffects() {
        if (eventTimer != null) {
            eventTimer.stop();
            eventTimer = null;
        }

        scoreMultiplier = 1;
        sizeReduction = 0;

        if (gameTimer != null) {
            gameTimer.setDelay(INITIAL_DELAY);
        }
        System.out.println("[SNAKE-EVENT] 이벤트 종료");
    }

    // --- [ 키 리스너 설정 ] ---
    public void enableKeyListener(boolean enable) {
        if (enable) {
            addKeyListener(keyAdapter);
            requestFocusInWindow();
        } else {
            removeKeyListener(keyAdapter);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }


    // --- [ 이미지 로드 ] ---
    private void loadAssets() {
        String[] imageKeys = {
                "headUp", "headDown", "headLeft", "headRight",
                "tailUp", "tailDown", "tailLeft", "tailRight",
                "bodyVertical", "bodyHorizontal",
                "bodyTopLeft", "bodyTopRight", "bodyBottomLeft", "bodyBottomRight",
                "apple"
        };
        for (String key : imageKeys) {
            images.put(key, loadImage(key + ".png"));
        }
    }

    private Image loadImage(String fileName) {
        return new ImageIcon(getClass().getResource("/images/snakeAssets/" + fileName)).getImage();
    }

    // --- [ 사과 생성 ] ---
    private void spawnFood() {
        Random random = new Random();
        int x, y;
        do {
            x = (random.nextInt(GAME_WIDTH / TILE_SIZE ) * TILE_SIZE) + 4;
            y = (random.nextInt(GAME_HEIGHT / TILE_SIZE) * TILE_SIZE) + 4;
        } while (isFoodInScoreArea(x, y) || isFoodOnSnake(new Point(x, y)));
        food = new Point(x, y);
    }

    // 점수 영역 예외 처리
    private boolean isFoodInScoreArea(int x, int y) {
        return (x >= SCORE_AREA_X && x <= SCORE_AREA_X + SCORE_AREA_WIDTH &&
                y >= SCORE_AREA_Y && y <= SCORE_AREA_Y + SCORE_AREA_HEIGHT);
    }

    // 사과 예외 처리
    private boolean isFoodOnSnake(Point foodPoint) {
        return snakeBody.stream().anyMatch(segment -> segment.equals(foodPoint));
    }

    // --- [ 키 입력 처리 ] ---
    private void changeDirection(int keyCode) {
        if (isKeyInputLocked) return;
        Direction newDirection = switch (keyCode) {
            case KeyEvent.VK_UP -> (currentDirection != Direction.DOWN) ? Direction.UP : currentDirection;
            case KeyEvent.VK_DOWN -> (currentDirection != Direction.UP) ? Direction.DOWN : currentDirection;
            case KeyEvent.VK_LEFT -> (currentDirection != Direction.RIGHT) ? Direction.LEFT : currentDirection;
            case KeyEvent.VK_RIGHT -> (currentDirection != Direction.LEFT) ? Direction.RIGHT : currentDirection;
            default -> currentDirection;
        };

        if (newDirection != currentDirection) {
            currentDirection = newDirection;
            isKeyInputLocked = true;
        }
    }

    // --- [ 게임 로직 업데이트 ] ---
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            updateGame();
            repaint();
        }
    }

    private void updateGame() {
        moveSnake();
        checkCollision();
    }


    private void moveSnake() {
        isKeyInputLocked = false;
        Point head = new Point(snakeBody.get(0));
        switch (currentDirection) {
            case UP -> head.y -= TILE_SIZE;
            case DOWN -> head.y += TILE_SIZE;
            case LEFT -> head.x -= TILE_SIZE;
            case RIGHT -> head.x += TILE_SIZE;
        }
        snakeBody.add(0, head);

        if (Math.abs(head.x - food.x) < TILE_SIZE / 2 && Math.abs(head.y - food.y) < TILE_SIZE / 2) {
            spawnFood();
            applesEaten++;
            score += scoreMultiplier;
            if (sizeReduction != 0 && snakeBody.size() >= (4 + sizeReduction)) {
                for (int i = 0; i <= sizeReduction; i++) {
                    snakeBody.remove(snakeBody.size() - 1);
                }
            }
            if (applesEaten % 10 == 0) {
                triggerRandomEvent();
            }
        } else {
            snakeBody.remove(snakeBody.size() - 1);
        }
    }

    private void triggerRandomEvent() {
        resetEventEffects();
        Random random = new Random();
        int event = random.nextInt(7);
        switch (event) {
            case 0 -> {
                scoreMultiplier = 2;
                showEventMessage("10초간 점수 2배!");
            }
            case 1 -> {
                scoreMultiplier = 3;
                showEventMessage("10초간 점수 3배!");
            }
            case 2 -> {
                gameTimer.setDelay(100);
                showEventMessage("10초간 속도 소폭 증가!");
            }
            case 3 -> {
                gameTimer.setDelay(85);
                showEventMessage("10초간 속도 증가!");
            }
            case 4 -> {
                gameTimer.setDelay(70);
                showEventMessage("10초간 속도 대폭 증가!");
            }
            case 5 -> {
                sizeReduction = 1;
                showEventMessage("10초간 사과를 먹으면 길이 1 감소!");
            }
            case 6 -> {
                sizeReduction = 2;
                showEventMessage("10초간 사과를 먹으면 길이 2 감소!");
            }
        }
        eventTimer = new Timer(10000, e -> resetEventEffects());
        eventTimer.setRepeats(false);
        eventTimer.start();
    }

    private void checkCollision() {
        Point head = snakeBody.get(0);
        if (head.x < 0 || head.x >= GAME_WIDTH || head.y < 0 || head.y >= GAME_HEIGHT) {
            endGame();
        }
        for (int i = 1; i < snakeBody.size(); i++) {
            if (head.equals(snakeBody.get(i))) {
                endGame();
                break;
            }
        }
    }

    private void endGame() {
        running = false;
        if (gameTimer != null) gameTimer.stop();
    }

    // --- [ 화면 그리기 ] ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (running) {
            g.drawImage(images.get("apple"), food.x, food.y, TILE_SIZE, TILE_SIZE, this);
            for (int i = 0; i < snakeBody.size(); i++) {
                Image segmentImage = getSnakeSegmentImage(i);
                Point segment = snakeBody.get(i);
                g.drawImage(segmentImage, segment.x, segment.y, TILE_SIZE, TILE_SIZE, this);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("점수: " + score, SCORE_AREA_X + 75, SCORE_AREA_Y + 20);
        } else {
            drawGameOverScreen(g);
        }
        if (!activeEventMessage.isEmpty()) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics metrics = g.getFontMetrics();
            int x = (GAME_WIDTH - metrics.stringWidth(activeEventMessage)) / 2;
            int y = GAME_HEIGHT / 5;
            g.drawString(activeEventMessage, x, y);
        }
    }

    private Image getSnakeSegmentImage(int index) {
        if (index == 0) {
            return switch (currentDirection) {
                case UP -> images.get("headUp");
                case DOWN -> images.get("headDown");
                case LEFT -> images.get("headLeft");
                case RIGHT -> images.get("headRight");
            };
        } else if (index == snakeBody.size() - 1) {
            return getTailImage(snakeBody.get(index), snakeBody.get(index - 1));
        } else {
            return getBodyImage(index);
        }
    }

    private Image getTailImage(Point tail, Point beforeTail) {
        if (tail.x == beforeTail.x) {
            return (tail.y < beforeTail.y) ? images.get("tailUp") : images.get("tailDown");
        }
        return (tail.x < beforeTail.x) ? images.get("tailLeft") : images.get("tailRight");
    }

    private Image getBodyImage(int index) {
        Point prev = snakeBody.get(index - 1);
        Point current = snakeBody.get(index);
        Point next = snakeBody.get(index + 1);

        if (prev.x == next.x) return images.get("bodyVertical");
        if (prev.y == next.y) return images.get("bodyHorizontal");

        if ((prev.x < current.x && next.y < current.y) || (next.x < current.x && prev.y < current.y))
            return images.get("bodyTopLeft");
        if ((prev.x > current.x && next.y < current.y) || (next.x > current.x && prev.y < current.y))
            return images.get("bodyTopRight");
        if ((prev.x < current.x && next.y > current.y) || (next.x < current.x && prev.y > current.y))
            return images.get("bodyBottomLeft");
        if ((prev.x > current.x && next.y > current.y) || (next.x > current.x && prev.y > current.y))
            return images.get("bodyBottomRight");

        return images.get("bodyHorizontal");
    }

    private void drawGameOverScreen(Graphics g) {
        String gameOverText = "게임이 종료되었습니다.";
        String scoreText = "점수: " + score;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(gameOverText, (GAME_WIDTH - metrics.stringWidth(gameOverText)) / 2, GAME_HEIGHT / 2 - 40);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        metrics = g.getFontMetrics();
        g.drawString(scoreText, (GAME_WIDTH - metrics.stringWidth(scoreText)) / 2, GAME_HEIGHT / 2 + 5);
        restartButton.setVisible(true);
        mainMenuButton.setVisible(true);
        stopGame();
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

    private void showEventMessage(String message) {
        activeEventMessage = message;
        if (eventMessageTimer != null) {
            eventMessageTimer.stop();
        }
        eventMessageTimer = new Timer(2500, e -> {
            activeEventMessage = "";
            repaint();
        });
        eventMessageTimer.setRepeats(false);
        eventMessageTimer.start();
        repaint();
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}