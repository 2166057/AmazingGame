package net.wattpadpremium;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EndGamePacket implements Packet {

    public static final int ID = 6;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void readData(DataInputStream input) throws IOException {

    }

    @Override
    public void writeData(DataOutputStream output) throws IOException {

    }

}
