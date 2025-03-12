from PyQt6.QtWidgets import QWidget, QPushButton
from PyQt6.QtCore import QTimer, Qt, QPoint
from PyQt6.QtGui import QPainter, QFont, QColor
import random

class SnakeGamePanel(QWidget):
    TILE_SIZE = 32
    GAME_WIDTH = 1344
    GAME_HEIGHT = 700
    INITIAL_DELAY = 120
    SCORE_AREA_X = 1344 - 130
    SCORE_AREA_Y = 10
    SCORE_AREA_WIDTH = 120
    SCORE_AREA_HEIGHT = 30
    
    # 방향 상수
    UP, DOWN, LEFT, RIGHT = range(4)
    
    def __init__(self, manager, parent=None):
        super().__init__(parent)
        self.manager = manager
        self.setFixedSize(self.GAME_WIDTH, self.GAME_HEIGHT)
        self.setFocusPolicy(Qt.FocusPolicy.StrongFocus)
        self.setStyleSheet("background-color: black;")
        
        self.running = False
        self.score = 0
        self.applesEaten = 0
        self.scoreMultiplier = 1
        self.sizeReduction = 0
        self.snakeBody = []
        self.currentDirection = self.RIGHT
        self.food = None
        self.isKeyInputLocked = False
        
        self.gameTimer = QTimer()
        self.gameTimer.timeout.connect(self.gameLoop)
        
        self.restartButton = QPushButton("다시 시작", self)
        self.mainMenuButton = QPushButton("메인 메뉴", self)
        font = QFont("Arial", 18, QFont.Weight.Bold)
        self.restartButton.setFont(font)
        self.mainMenuButton.setFont(font)
        self.restartButton.setVisible(False)
        self.mainMenuButton.setVisible(False)
        self.restartButton.setGeometry(self.GAME_WIDTH//2 - 100, self.GAME_HEIGHT//2 + 50, 200, 50)
        self.mainMenuButton.setGeometry(self.GAME_WIDTH//2 - 100, self.GAME_HEIGHT//2 + 110, 200, 50)
        self.restartButton.clicked.connect(self.startGame)
        self.mainMenuButton.clicked.connect(lambda: self.manager.switchTo("MainMenu"))
        
    def startGame(self):
        if self.running:
            return
        self.running = True
        self.score = 0
        self.applesEaten = 0
        self.scoreMultiplier = 1
        self.sizeReduction = 0
        self.snakeBody = [QPoint(100 - i * self.TILE_SIZE, 100) for i in range(3)]
        self.currentDirection = self.RIGHT
        self.spawnFood()
        self.restartButton.setVisible(False)
        self.mainMenuButton.setVisible(False)
        self.gameTimer.start(self.INITIAL_DELAY)
        self.setFocus()
    
    def stopGame(self):
        if self.gameTimer.isActive():
            self.running = False
            self.gameTimer.stop()
    
    def spawnFood(self):
        while True:
            x = random.randint(0, self.GAME_WIDTH // self.TILE_SIZE - 1) * self.TILE_SIZE + 4
            y = random.randint(0, self.GAME_HEIGHT // self.TILE_SIZE - 1) * self.TILE_SIZE + 4
            if not self.isFoodInScoreArea(x, y) and not self.isFoodOnSnake(QPoint(x, y)):
                self.food = QPoint(x, y)
                break
    
    def isFoodInScoreArea(self, x, y):
        return (x >= self.SCORE_AREA_X and x <= self.SCORE_AREA_X + self.SCORE_AREA_WIDTH and
                y >= self.SCORE_AREA_Y and y <= self.SCORE_AREA_Y + self.SCORE_AREA_HEIGHT)
    
    def isFoodOnSnake(self, pt):
        return pt in self.snakeBody
    
    def keyPressEvent(self, event):
        self.changeDirection(event.key())
    
    def changeDirection(self, key):
        if self.isKeyInputLocked:
            return
        newDirection = self.currentDirection
        if key == Qt.Key_Up and self.currentDirection != self.DOWN:
            newDirection = self.UP
        elif key == Qt.Key_Down and self.currentDirection != self.UP:
            newDirection = self.DOWN
        elif key == Qt.Key_Left and self.currentDirection != self.RIGHT:
            newDirection = self.LEFT
        elif key == Qt.Key_Right and self.currentDirection != self.LEFT:
            newDirection = self.RIGHT
        if newDirection != self.currentDirection:
            self.currentDirection = newDirection
            self.isKeyInputLocked = True
    
    def gameLoop(self):
        if self.running:
            self.moveSnake()
            self.checkCollision()
            self.update()
    
    def moveSnake(self):
        self.isKeyInputLocked = False
        head = QPoint(self.snakeBody[0])
        if self.currentDirection == self.UP:
            head.setY(head.y() - self.TILE_SIZE)
        elif self.currentDirection == self.DOWN:
            head.setY(head.y() + self.TILE_SIZE)
        elif self.currentDirection == self.LEFT:
            head.setX(head.x() - self.TILE_SIZE)
        elif self.currentDirection == self.RIGHT:
            head.setX(head.x() + self.TILE_SIZE)
        self.snakeBody.insert(0, head)
        
        if abs(head.x() - self.food.x()) < self.TILE_SIZE//2 and abs(head.y() - self.food.y()) < self.TILE_SIZE//2:
            self.spawnFood()
            self.applesEaten += 1
            self.score += self.scoreMultiplier
            if self.sizeReduction != 0 and len(self.snakeBody) >= (4 + self.sizeReduction):
                for _ in range(self.sizeReduction+1):
                    self.snakeBody.pop()
            # (여기서 10개 사과마다 이벤트 트리거 로직을 추가할 수 있음)
        else:
            self.snakeBody.pop()
    
    def checkCollision(self):
        head = self.snakeBody[0]
        if head.x() < 0 or head.x() >= self.GAME_WIDTH or head.y() < 0 or head.y() >= self.GAME_HEIGHT:
            self.endGame()
        for segment in self.snakeBody[1:]:
            if head == segment:
                self.endGame()
                break
    
    def endGame(self):
        self.running = False
        self.stopGame()
        self.restartButton.setVisible(True)
        self.mainMenuButton.setVisible(True)
        self.update()
    
    def paintEvent(self, event):
        painter = QPainter(self)
        if self.running:
            painter.setBrush(QColor(255, 0, 0))
            if self.food:
                painter.drawRect(self.food.x(), self.food.y(), self.TILE_SIZE, self.TILE_SIZE)
            painter.setBrush(QColor(0, 255, 0))
            for pt in self.snakeBody:
                painter.drawRect(pt.x(), pt.y(), self.TILE_SIZE, self.TILE_SIZE)
            painter.setPen(QColor(255, 255, 255))
            painter.setFont(QFont("Arial", 20, QFont.Weight.Bold))
            painter.drawText(self.SCORE_AREA_X + 50, self.SCORE_AREA_Y + 20, f"점수: {self.score}")
        else:
            painter.setPen(QColor(255, 255, 255))
            painter.setFont(QFont("Arial", 36, QFont.Weight.Bold))
            gameOverText = "게임이 종료되었습니다."
            metrics = painter.fontMetrics()
            painter.drawText((self.GAME_WIDTH - metrics.horizontalAdvance(gameOverText)) // 2, self.GAME_HEIGHT // 2 - 40, gameOverText)
            painter.setFont(QFont("Arial", 28, QFont.Weight.Bold))
            scoreText = f"점수: {self.score}"
            metrics = painter.fontMetrics()
            painter.drawText((self.GAME_WIDTH - metrics.horizontalAdvance(scoreText)) // 2, self.GAME_HEIGHT // 2 + 5, scoreText)