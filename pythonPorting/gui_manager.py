from PyQt6.QtWidgets import QWidget, QStackedWidget, QVBoxLayout
from main_menu_panel import MainMenuPanel
from snake_game_panel import SnakeGamePanel
from dodge_master_panel import DodgeMasterPanel

class GUIManager(QWidget):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setWindowTitle("[INCOM] 동아리 박람회 게임")
        self.setFixedSize(1366, 768)
        
        self.stackedWidget = QStackedWidget()
        
        self.mainMenu = MainMenuPanel(self)
        self.snakeGame = SnakeGamePanel(self)
        self.dodgeMaster = DodgeMasterPanel(self)
        
        self.stackedWidget.addWidget(self.mainMenu)
        self.stackedWidget.addWidget(self.snakeGame)
        self.stackedWidget.addWidget(self.dodgeMaster)
        
        layout = QVBoxLayout()
        layout.addWidget(self.stackedWidget)
        self.setLayout(layout)
    
    def switchTo(self, panelName: str):
        self.snakeGame.stopGame()
        self.dodgeMaster.stopGame()
        
        if panelName == "MainMenu":
            self.stackedWidget.setCurrentWidget(self.mainMenu)
        elif panelName == "SnakeGame":
            self.stackedWidget.setCurrentWidget(self.snakeGame)
            self.snakeGame.startGame()
        elif panelName == "DodgeMaster":
            self.stackedWidget.setCurrentWidget(self.dodgeMaster)
            self.dodgeMaster.startGame()