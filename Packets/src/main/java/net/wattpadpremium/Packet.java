package net.wattpadpremium;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Packet {

    int getId();
    void readData(DataInputStream input) throws IOException;
    void writeData(DataOutputStream output) throws IOException;

}

