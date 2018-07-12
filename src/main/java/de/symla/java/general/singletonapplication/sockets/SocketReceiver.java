package de.symla.java.general.singletonapplication.sockets;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketReceiver implements Runnable {

    /**
     * Listener to be notified when socket client calls socket server.
     */
    public interface SocketResponderListener {
        void called(String key, String value);
    }

    /**
     * Socket that will be handled.
     */
    private final Socket socket;

    /**
     * Listeners.
     */
    private final List<SocketResponderListener> listeners;

    /**
     * Buffer for storing messages.
     */
    private final StringBuilder buffer;

    public SocketReceiver(@NotNull final Socket socket) {
        if ( socket == null ) throw new NullPointerException("Socket must not be null.");
        this.socket = socket;
        this.listeners = new ArrayList<>();
        this.buffer = new StringBuilder();
    }

    @Override
    public void run() {
        try {
            buffer.setLength(0);
            final InputStream in = this.socket.getInputStream();
            boolean capture = false;

            while ( true ) {
                final int nextByte = in.read();
                if ( nextByte == -1 ) break;

                if ( nextByte == 35 && !capture ) {
                    capture = true;
                    continue;
                }
                if ( nextByte == 35 && capture ) {
                    capture = false;
                    process();
                }

                if ( capture ) buffer.append((char)nextByte);
            }
        } catch ( IOException e ) {
            throw new RuntimeException("Could not obtain InputStream.", e);
        }
    }

    private void process() {
        final String received = buffer.toString();

        if ( !received.contains("=") ) {
            System.err.println("Received invalid message. Missing '='. Dismissing message '"+received+"'.");
        } else {
            final String[] parts = received.split("=");
            if ( parts.length != 2 ) {
                System.err.println("Received invalid message. More than one '='. Dismissing message '"+received+"'.");
            } else {
                notifyListeners(parts[0], parts[1]);
            }
        }

        buffer.setLength(0);
    }

    private void notifyListeners(final String key, final String value) {
        synchronized ( this.listeners ) {
            for ( SocketResponderListener listener : listeners ) {
                listener.called(key, value);
            }
        }
    }

    public void addListener(@NotNull final SocketResponderListener listener) {
        if ( listener == null ) throw new NullPointerException("Listener must not be null.");
        this.listeners.add(listener);
    }

    public void removeListener(@NotNull final SocketResponderListener listener) {
        if ( listener == null ) throw new NullPointerException("Listener must not be null.");
        this.listeners.remove(listener);
    }
}
