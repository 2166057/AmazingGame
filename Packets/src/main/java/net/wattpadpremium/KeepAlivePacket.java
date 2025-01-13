package net.wattpadpremium;

import lombok.Getter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


@Getter
public class KeepAlivePacket implements Packet {

    public static final int ID = 1;

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
