from PyQt6.QtWidgets import QWidget, QPushButton, QLabel
from PyQt6.QtGui import QFont
from PyQt6.QtCore import Qt

class MainMenuPanel(QWidget):
    def __init__(self, manager, parent=None):
        super().__init__(parent)
        self.manager = manager
        self.setFixedSize(1366, 768)
        
        self.label = QLabel("< 아래에서 게임을 선택하세요 >", self)
        self.label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.label.setFont(QFont("Arial", 32, QFont.Weight.Bold))
        self.label.setGeometry(440, 10, 500, 100)
        
        self.snakeButton = QPushButton("스네이크 게임", self)
        self.snakeButton.setFont(QFont("Arial", 20, QFont.Weight.Bold))
        self.snakeButton.setGeometry(583, 334, 200, 50)
        self.snakeButton.clicked.connect(lambda: self.manager.switchTo("SnakeGame"))
        
        self.dodgeButton = QPushButton("피하기 게임", self)
        self.dodgeButton.setFont(QFont("Arial", 20, QFont.Weight.Bold))
        self.dodgeButton.setGeometry(583, 400, 200, 50)
        self.dodgeButton.clicked.connect(lambda: self.manager.switchTo("DodgeMaster"))