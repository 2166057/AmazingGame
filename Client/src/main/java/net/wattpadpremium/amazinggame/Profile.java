package net.wattpadpremium.amazinggame;

import lombok.Data;

import java.awt.*;
import java.util.UUID;

@Data
public class Profile {

    private Color color = new Color(231, 38, 116);
    private String username = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

}
