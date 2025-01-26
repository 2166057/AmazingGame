package net.wattpadpremium.amazinggame.client;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GameInstance {

    private final Profile profile;

    @Setter
    private GameScene mazeGame = null;

    public GameInstance(Profile profile) {
        this.profile = profile;
        new GameMenu(this).setVisible(true);
    }

    public static void main(String[] args) {
        new GameInstance(new Profile());
    }

}
