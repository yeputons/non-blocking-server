package net.yeputons.example.nonblockingserver;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.function.Function;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        new NonBlockingServer(new InetSocketAddress(12345),
                getClientHandlerFactory(args[0])
        ).run();
    }

    private static Function<SocketChannel, NonBlockingClientHandler> getClientHandlerFactory(String type) {
        switch (type) {
            case "echoer": return Echoer::new;
            case "palindrome": return PalindromeChecker::new;
            case "printer": return (channel) -> new StringPrinter(channel, "Hello, World!\n");
            case "reader": return StringReader::new;
            default:
                throw new AssertionError("Invalid client handler type: " + type);
        }
    }
}
