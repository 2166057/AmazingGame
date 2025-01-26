package net.wattpadpremium.amazinggame.client;

import lombok.Getter;

@Getter
public class GameVersionCandidate {

    private final String name;
    private final String downloadURL;

    public GameVersionCandidate(String name, String downloadURL) {
        this.name = name;
        this.downloadURL = downloadURL;
    }
}
