package net.wattpadpremium;

import lombok.Getter;
import net.wattpadpremium.client.AuthSessionPacket;
import net.wattpadpremium.client.JoinRequestPacket;
import net.wattpadpremium.client.MovePacket;
import net.wattpadpremium.server.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet<D> {

    protected Packet(DataInputStream inputStream) throws IOException {
        this.data = readData(inputStream);
    }

    protected Packet(D data) {
        this.data = data;
    }

    @Getter
    private final D data;

    protected abstract D readData(DataInputStream input) throws IOException;
    public abstract void writeData(DataOutputStream output) throws IOException;

    public abstract PacketType getPacketType();

    public static Packet<?> createPacket(DataInputStream payload) throws IOException {
        int packetId = payload.readInt();
        PacketType packetType = PacketType.findTypeFromId(packetId);
        Packet<?> packet;
        return switch (packetType) {
            case GlobalKeepAlivePacket -> {
                packet = new KeepAlivePacket(payload);
                yield packet;
            }
            case ClientJoinRequestPacket -> {
                packet = new JoinRequestPacket(payload);
                yield packet;
            }
            case ServerMazePacket -> {
                packet = new MazePacket(payload);
                yield packet;
            }
            case ServerPositionChangePacket -> {
                packet = new PositionChangePacket(payload);
                yield packet;
            }
            case ServerPlayerScorePacket -> {
                packet = new PlayerScorePacket(payload);
                yield packet;
            }
            case ServerEndGamePacket -> {
                packet = new EndGamePacket(payload);
                yield packet;
            }
            case ServerPlayerCountPacket -> {
                packet = new PlayerCountPacket(payload);
                yield packet;
            }
            case ServerRemovePlayerPacket -> {
                packet = new RemovePlayerPacket(payload);
                yield packet;
            }
            case ServerTrapPacket -> {
                packet = new TrapPacket(payload);
                yield packet;
            }
            case ServerPlayerStatusPacket -> {
                packet = new PlayerStatusPacket(payload);
                yield packet;
            }
            case ClientAuthSessionPacket -> {
                packet = new AuthSessionPacket(payload);
                yield packet;
            }
            case ServerAcceptConnectionPacket -> {
                packet = new AcceptConnectionPacket(payload);
                yield packet;
            }
            case ClientMovePacket -> {
                packet = new MovePacket(payload);
                yield packet;
            }
            case ServerTextOverlayPacket -> {
                packet = new TextOverlayPacket(payload);
                yield packet;
            }
            case ServerProgressBarPacket -> {
                packet = new ProgressBarPacket(payload);
                yield packet;
            }
            case ServerMazeStylePacket -> {
                packet = new MazeStylePacket(payload);
                yield packet;
            }
            default -> null;
        };
    }

}

