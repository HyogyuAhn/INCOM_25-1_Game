package incom.hyogyu.util;

import java.awt.Font;

public class FontManager {

    public enum GameFont {
        JALNAN("Jalnan2.ttf"),
        DUNG_GEUN_MO("DungGeunMo.ttf"),
        LINE_SEED_BOLD("LINESeedKR-Bd.ttf"),
        LINE_SEED_REGULAR("LINESeedKR-Rg.ttf"),
        CHILD_FUND_KOREA("YoonChildfundkoreaDaeHan.ttf");

        private final String fileName;

        GameFont(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public Font loadCustomFont(GameFont fontType, float size) {
        try {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/" + fontType.getFileName()));
            return customFont.deriveFont(size);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.BOLD, (int) size);
        }
    }

}
