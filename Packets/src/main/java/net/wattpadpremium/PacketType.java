package net.wattpadpremium;

public enum PacketType {

    ClientAuthSessionPacket, ClientJoinRequestPacket, ClientMovePacket,

    ServerAcceptConnectionPacket, ServerEndGamePacket, ServerMazePacket, ServerMazeStylePacket, ServerPlayerCountPacket,
    ServerPlayerScorePacket, ServerPlayerStatusPacket, ServerPositionChangePacket, ServerProgressBarPacket, ServerRemovePlayerPacket,
    ServerTextOverlayPacket, ServerTrapPacket,

    GlobalKeepAlivePacket;


    public static int findIdFromType(PacketType packetType) {
        return packetType.ordinal();
    }

    public static PacketType findTypeFromId(int packetId) {
        return PacketType.values()[packetId];
    }

}
