package net.wattpadpremium.amazinggame;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GameInstance {

    private final Profile profile;

    @Setter
    private MazeGame mazeGame = null;

    public GameInstance(Profile profile) {
        this.profile = profile;
        new GameMenu(this).setVisible(true);
    }

}
