package net.yeputons.example.nonblockingserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Reads bytes from the client until the connection is closed and the prints the string to stdout
 */
public class StringReader implements NonBlockingClientHandler {
    private final SocketChannel client;
    private final ByteArrayOutputStream storage = new ByteArrayOutputStream();
    private final ByteBuffer buffer = ByteBuffer.allocate(10);

    public StringReader(SocketChannel client) {
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
        if (read == -1 || !buffer.hasRemaining()) {
            int offset = buffer.arrayOffset(); // should probably be zero at all times
            storage.write(buffer.array(), offset, offset + buffer.position());
            buffer.rewind();
        }
        if (read == -1) {
            System.out.println("Read: " + new String(storage.toByteArray(), Charset.defaultCharset()));
            client.close();
            return 0;
        }
        return SelectionKey.OP_READ;
    }

    @Override
    public int onWritable() throws IOException {
        return 0;
    }
}
