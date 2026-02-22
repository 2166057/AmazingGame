package net.wattpadpremium.amazinggame.client.tcp;

import net.wattpadpremium.Packet;
import net.wattpadpremium.listeners.PacketListener;

import java.util.HashMap;

public abstract class AbstractTCPClient {

    public abstract HashMap<Integer, PacketListener> getPacketHandler();

    public abstract void sendPacketToServer(Packet packet);

    public abstract void stopClient();

}
