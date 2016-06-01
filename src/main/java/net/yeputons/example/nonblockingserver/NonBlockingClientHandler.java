package net.yeputons.example.nonblockingserver;

import java.io.IOException;

public interface NonBlockingClientHandler {
    int onAccepted();
    int onReadable() throws IOException;
    int onWritable() throws IOException;
}
