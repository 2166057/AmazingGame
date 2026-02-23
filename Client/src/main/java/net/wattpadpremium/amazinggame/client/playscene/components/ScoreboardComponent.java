package net.wattpadpremium.amazinggame.client.playscene.components;

import net.wattpadpremium.amazinggame.client.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScoreboardComponent {


    // ─────────────────────────────────────────────────────────────────────────
    //  Scoreboard overlay
    // ─────────────────────────────────────────────────────────────────────────

    public static void drawScoreboard(Graphics og, HashMap<Long, Player> otherPlayers, Player localPlayer, int score, int width, int height) {
        List<Player> copy = new ArrayList<>(otherPlayers.values());
        Player localEntry = new Player();
        localEntry.setUsername(localPlayer.getUsername());
        localEntry.setScore(score);
        copy.add(localEntry);
        copy.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        int rows   = Math.min(copy.size(), 5);
        int rectW  = 240;
        int rectH  = 34 + rows * 22 + 28;
        int rectX  = (width  - rectW)  / 2;
        int rectY  = (height - rectH) / 2;

        // Semi-transparent back-drop
        ((Graphics2D) og).setColor(new Color(0, 0, 0, 150));
        og.fillRoundRect(rectX - 6, rectY - 6, rectW + 12, rectH + 12, 14, 14);

        og.setColor(new Color(230, 230, 240));
        og.fillRoundRect(rectX, rectY, rectW, rectH, 12, 12);

        og.setColor(new Color(50, 50, 60));
        og.setFont(og.getFont().deriveFont(Font.BOLD, 14f));
        FontMetrics fm = og.getFontMetrics();
        String hdr = "SCOREBOARD";
        og.drawString(hdr, rectX + (rectW - fm.stringWidth(hdr)) / 2, rectY + 24);

        og.setFont(og.getFont().deriveFont(Font.PLAIN, 12f));
        fm = og.getFontMetrics();
        int textY = rectY + 24 + fm.getHeight() + 2;

        for (int i = 0; i < rows; i++) {
            Player p       = copy.get(i);
            boolean isMe   = localPlayer.getUsername() != null
                    && localPlayer.getUsername().equals(p.getUsername());
            og.setColor(isMe ? new Color(40, 100, 200) : new Color(50, 50, 60));
            og.drawString("#" + (i + 1) + "  " + p.getUsername() + "  —  " + p.getScore(),
                    rectX + 14, textY);
            textY += fm.getHeight() + 2;
        }

        og.setFont(og.getFont().deriveFont(Font.BOLD, 12f));
        og.setColor(new Color(70, 70, 180));
        og.drawString("Your score: " + score, rectX + 14, textY + 6);
        return ;
    }

}
