package net.yeputons.example.nonblockingserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Processes arbitrary amount of 'rounds'.
 * Each round should start with client sending three-digit number - length of following string.
 * Then the string itself should follow.
 * Server answers with either "YES" or "NO" (quotes for clarity) depending on whether the string is a palindrome
 * Example:
 *   Input: 003aba
 *   Output: YES
 *   Input: 003abc
 *   Output: NO
 */
public class PalindromeChecker implements NonBlockingClientHandler {
    private final SocketChannel client;
    private ByteBuffer buffer;
    private int stringLength;

    public PalindromeChecker(SocketChannel client) {
        this.client = client;
    }

    private int startRound() {
        buffer = ByteBuffer.allocate(3);
        stringLength = -1;
        return SelectionKey.OP_READ;
    }

    @Override
    public int onAccepted() {
        return startRound();
    }

    @Override
    public int onReadable() throws IOException {
        client.read(buffer);
        if (buffer.hasRemaining()) {
            return SelectionKey.OP_READ;
        }
        String data = new String(buffer.array(), buffer.arrayOffset(), buffer.arrayOffset() + buffer.position());
        if (stringLength < 0) {
            stringLength = Integer.parseInt(data);
            buffer = ByteBuffer.allocate(stringLength);
            return SelectionKey.OP_READ;
        } else {
            String reversedData = new StringBuilder(data).reverse().toString();
            String answer = data.equals(reversedData) ? "YES\n" : "NO\n";
            buffer = ByteBuffer.allocate(answer.length());
            buffer.put(answer.getBytes());
            buffer.flip();
            return SelectionKey.OP_WRITE;
        }
    }

    @Override
    public int onWritable() throws IOException {
        client.write(buffer);
        if (buffer.hasRemaining()) {
            return SelectionKey.OP_WRITE;
        } else {
            return startRound();
        }
    }
}
