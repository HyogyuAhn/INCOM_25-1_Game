import sys
from PyQt6.QtWidgets import QApplication
from gui_manager import GUIManager

def main():
    app = QApplication(sys.argv)
    manager = GUIManager()
    manager.show()
    sys.exit(app.exec())

if __name__ == '__main__':
    main()