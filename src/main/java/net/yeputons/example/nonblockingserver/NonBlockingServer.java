package net.yeputons.example.nonblockingserver;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.function.Function;

public class NonBlockingServer implements Runnable {
    private final SocketAddress bindingAddress;

    // Creates client handlers for each accepted connection
    private final Function<SocketChannel, NonBlockingClientHandler> clientProducer;

    public NonBlockingServer(
            SocketAddress bindingAddress,
            Function<SocketChannel, NonBlockingClientHandler> clientProducer) {
        this.bindingAddress = bindingAddress;
        this.clientProducer = clientProducer;
    }

    public void run() {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()
        ) {
            serverChannel.bind(bindingAddress);
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT); // watch for new connections

            while (true) {
                selector.select(); // wait until something happens
                if (Thread.interrupted()) { // if we were interrupted, exit
                    break;
                }

                // iterate over non-processed events
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next(); // take event
                    selectionKeyIterator.remove(); // mark event as processed

                    if (selectionKey.isAcceptable()) {
                        // if the event comes from ServerSocketChannel
                        SocketChannel newClientSocket = serverChannel.accept();
                        if (newClientSocket != null) {
                            newClientSocket.configureBlocking(false);
                            NonBlockingClientHandler newClient = clientProducer.apply(newClientSocket);
                            int waitFor = newClient.onAccepted(); // ask client what to wait for
                            if (waitFor != 0) {
                                newClientSocket.register(selector, waitFor, newClient); // add to waiting queue
                            }
                        }
                    } else {
                        // if the event comes from one of clients
                        NonBlockingClientHandler client = (NonBlockingClientHandler) selectionKey.attachment();
                        SelectableChannel clientChannel = selectionKey.channel();
                        int waitFor = 0;
                        if (selectionKey.isReadable()) {
                            waitFor = client.onReadable();
                        } else if (selectionKey.isWritable()) {
                            waitFor = client.onWritable();
                        }
                        if (waitFor != 0) {
                            // if client is interested in further events, change mask of interesting events
                            clientChannel.register(selector, waitFor, client);
                        } else {
                            // if client is not interested, unregister it
                            selectionKey.cancel();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
