from PyQt6.QtWidgets import QWidget, QPushButton
from PyQt6.QtCore import QTimer, Qt, QRect
from PyQt6.QtGui import QPainter, QFont, QColor
import random

class DodgeMasterPanel(QWidget):
    GAME_WIDTH = 1344
    GAME_HEIGHT = 710
    PLAYER_WIDTH = 50
    PLAYER_HEIGHT = 50
    INITIAL_PLAYER_SPEED = 5
    INITIAL_DELAY = 20
    
    # Falling object 타입 상수
    HARM, SCORE_MULTIPLIER, SPEED_UP, SHIELD = range(4)
    
    class FallingObject:
        def __init__(self, x, y, size, speed, type):
            self.x = x
            self.y = y
            self.size = size
            self.speed = speed
            self.type = type
        def getBounds(self):
            return QRect(self.x, self.y, self.size, self.size)
    
    def __init__(self, manager, parent=None):
        super().__init__(parent)
        self.manager = manager
        self.setFixedSize(self.GAME_WIDTH, self.GAME_HEIGHT)
        self.setFocusPolicy(Qt.FocusPolicy.StrongFocus)
        self.setStyleSheet("background-color: darkgray;")
        
        self.playerX = (self.GAME_WIDTH - self.PLAYER_WIDTH) // 2
        self.playerY = self.GAME_HEIGHT - self.PLAYER_HEIGHT - 20
        self.playerSpeed = self.INITIAL_PLAYER_SPEED
        self.score = 0
        self.scoreMultiplier = 1
        self.shieldActive = False
        self.running = False
        self.updateCounter = 0
        self.isGameOver = False
        self.objects = []
        self.leftPressed = False
        self.rightPressed = False
        self.activeEventMessage = ""
        
        self.gameTimer = QTimer()
        self.gameTimer.timeout.connect(self.gameLoop)
        self.spawnTimer = QTimer()
        self.spawnTimer.timeout.connect(self.spawnFallingObject)
        
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
        self.isGameOver = False
        self.score = 0
        self.scoreMultiplier = 1
        self.shieldActive = False
        self.playerSpeed = self.INITIAL_PLAYER_SPEED
        self.objects = []
        self.updateCounter = 0
        self.playerX = (self.GAME_WIDTH - self.PLAYER_WIDTH) // 2
        self.playerY = self.GAME_HEIGHT - self.PLAYER_HEIGHT - 20
        self.leftPressed = False
        self.rightPressed = False
        self.restartButton.setVisible(False)
        self.mainMenuButton.setVisible(False)
        self.gameTimer.start(self.INITIAL_DELAY)
        self.spawnTimer.start(2000)
        self.setFocus()
    
    def stopGame(self):
        self.running = False
        self.gameTimer.stop()
        self.spawnTimer.stop()
    
    def keyPressEvent(self, event):
        if event.key() == Qt.Key_Left:
            self.leftPressed = True
        elif event.key() == Qt.Key_Right:
            self.rightPressed = True
    
    def keyReleaseEvent(self, event):
        if event.key() == Qt.Key_Left:
            self.leftPressed = False
        elif event.key() == Qt.Key_Right:
            self.rightPressed = False
    
    def gameLoop(self):
        if not self.running:
            return
        self.updateCounter += 1
        if self.updateCounter % 50 == 0:
            self.score += self.scoreMultiplier
        if self.leftPressed:
            self.playerX -= self.playerSpeed
        if self.rightPressed:
            self.playerX += self.playerSpeed
        self.playerX = max(0, min(self.playerX, self.GAME_WIDTH - self.PLAYER_WIDTH))
        
        for obj in self.objects[:]:
            obj.y += obj.speed
            if obj.y > self.GAME_HEIGHT:
                self.objects.remove(obj)
                continue
            playerRect = QRect(self.playerX, self.playerY, self.PLAYER_WIDTH, self.PLAYER_HEIGHT)
            if obj.getBounds().intersects(playerRect):
                if obj.type == self.HARM:
                    if self.shieldActive:
                        self.shieldActive = False
                        self.activeEventMessage = "방어막 소멸!"
                    else:
                        self.running = False
                        self.isGameOver = True
                elif obj.type == self.SCORE_MULTIPLIER:
                    self.scoreMultiplier = 2
                    self.activeEventMessage = "점수 2배 획득!"
                    QTimer.singleShot(10000, self.resetScoreMultiplier)
                elif obj.type == self.SPEED_UP:
                    self.playerSpeed += 2
                    self.activeEventMessage = "플레이어 속도 증가!"
                    QTimer.singleShot(10000, self.resetPlayerSpeed)
                elif obj.type == self.SHIELD:
                    self.shieldActive = True
                    self.activeEventMessage = "1회 방어 효과 획득!"
                self.objects.remove(obj)
        newDelay = max(500, 2000 - self.score // 10)
        self.spawnTimer.setInterval(newDelay)
        self.update()
    
    def resetScoreMultiplier(self):
        self.scoreMultiplier = 1
        self.update()
    
    def resetPlayerSpeed(self):
        self.playerSpeed = self.INITIAL_PLAYER_SPEED
        self.update()
    
    def spawnFallingObject(self):
        size = 40
        x = random.randint(0, self.GAME_WIDTH - size)
        y = -size
        baseSpeed = 1 + self.score // 200
        type = self.HARM
        specialProbability = min(0.5, self.score / 1000.0)
        if random.random() < specialProbability:
            specials = []
            shieldCount = sum(1 for o in self.objects if o.type == self.SHIELD)
            multiplierCount = sum(1 for o in self.objects if o.type == self.SCORE_MULTIPLIER)
            speedCount = sum(1 for o in self.objects if o.type == self.SPEED_UP)
            if shieldCount < 2:
                specials.append(self.SHIELD)
            if multiplierCount < 2:
                specials.append(self.SCORE_MULTIPLIER)
            if speedCount < 2:
                specials.append(self.SPEED_UP)
            if specials:
                type = random.choice(specials)
        speed = baseSpeed if type == self.HARM else max(1, baseSpeed - 1)
        obj = self.FallingObject(x, y, size, speed, type)
        self.objects.append(obj)
    
    def paintEvent(self, event):
        painter = QPainter(self)
        if self.isGameOver:
            painter.fillRect(self.rect(), QColor(0, 0, 0))
            return
        if self.shieldActive:
            painter.setBrush(QColor(0, 255, 255))
            painter.drawEllipse(self.playerX - 5, self.playerY - 5, self.PLAYER_WIDTH + 10, self.PLAYER_HEIGHT + 10)
        painter.setBrush(QColor(255, 255, 255))
        painter.drawRect(self.playerX, self.playerY, self.PLAYER_WIDTH, self.PLAYER_HEIGHT)
        for obj in self.objects:
            if obj.type == self.HARM:
                painter.setBrush(QColor(255, 0, 0))
            elif obj.type == self.SCORE_MULTIPLIER:
                painter.setBrush(QColor(255, 0, 255))
            elif obj.type == self.SPEED_UP:
                painter.setBrush(QColor(255, 165, 0))
            elif obj.type == self.SHIELD:
                painter.setBrush(QColor(0, 255, 0))
            painter.drawEllipse(obj.x, obj.y, obj.size, obj.size)
        painter.setPen(QColor(255, 255, 255))
        painter.setFont(QFont("Arial", 20, QFont.Weight.Bold))
        painter.drawText(20, 30, f"점수: {self.score}")
        if self.scoreMultiplier > 1:
            painter.drawText(20, 60, f"점수배율: {self.scoreMultiplier}x")
        if self.playerSpeed > self.INITIAL_PLAYER_SPEED:
            painter.drawText(20, 90, "속도 증가!")
        if self.activeEventMessage:
            painter.setFont(QFont("Arial", 24, QFont.Weight.Bold))
            painter.drawText(20, 120, self.activeEventMessage)