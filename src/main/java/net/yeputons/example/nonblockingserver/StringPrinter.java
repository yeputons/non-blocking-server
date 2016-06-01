package net.yeputons.example.nonblockingserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Prints a fixed string to client and closes the connection
 */
public class StringPrinter implements NonBlockingClientHandler {
    private final SocketChannel client;
    private final ByteBuffer buffer;

    public StringPrinter(SocketChannel client, String toPrint) {
        this.client = client;
        buffer = ByteBuffer.wrap(toPrint.getBytes());
    }

    @Override
    public int onAccepted() {
        return SelectionKey.OP_WRITE;
    }

    @Override
    public int onReadable() throws IOException  {
        return 0;
    }

    @Override
    public int onWritable() throws IOException {
        client.write(buffer);
        if (buffer.hasRemaining()) {
            return SelectionKey.OP_WRITE;
        } else {
            client.close();
            return 0;
        }
    }
}
