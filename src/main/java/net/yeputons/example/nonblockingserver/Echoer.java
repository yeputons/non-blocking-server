package net.yeputons.example.nonblockingserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Receives data and prints it back as soon as possible
 */
public class Echoer implements NonBlockingClientHandler {
    private final SocketChannel client;
    private final ByteBuffer buffer = ByteBuffer.allocate(10);

    public Echoer(SocketChannel client) {
        this.client = client;
    }

    @Override
    public int onAccepted() {
        return SelectionKey.OP_READ;
    }

    @Override
    public int onReadable() throws IOException {
        int read;
        try {
            read = client.read(buffer);
        } catch (IOException e) {
            read = -1;
        }
        if (read == -1) {
            client.close();
            return 0;
        }
        buffer.flip();
        return SelectionKey.OP_WRITE;
    }

    @Override
    public int onWritable() throws IOException {
        client.write(buffer);
        if (buffer.hasRemaining()) {
            return SelectionKey.OP_WRITE;
        } else {
            buffer.clear();
            return SelectionKey.OP_READ;
        }
    }
}
