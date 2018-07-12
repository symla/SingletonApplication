package de.symla.java.general.singletonapplication.sockets;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.net.Socket;

public class SocketInterface {

    /**
     * Socket that is handled.
     */
    private final Socket socket;

    /**
     * Receiver for socket.
     */
    private final SocketReceiver socketReceiver;

    /**
     * Local string builder.
     */
    private final StringBuilder sb;

    /**
     * Thread for listening.
     */
    private final Thread listenThread;

    public SocketInterface(@NotNull final Socket socket) {
        if ( socket == null ) throw new NullPointerException("Socket must not be null.");

        this.socket = socket;
        this.socketReceiver = new SocketReceiver(this.socket);
        this.sb = new StringBuilder();
        this.listenThread = new Thread(this.socketReceiver);
    }

    public void listen() {
        this.listenThread.start();
    }

    public void close() {
        try {
            this.socket.close();
        } catch ( IOException e ) {
            throw new RuntimeException("Could not close socket.", e);
        }
    }

    public void send(@NotNull final String key, @NotNull final String value, final boolean throwExceptionOnClosed) {
        if ( key == null ) throw new NullPointerException("Key must not be null.");
        if ( value == null ) throw new NullPointerException("Value must not be null.");

        if ( this.socket.isOutputShutdown() ) {
            if ( throwExceptionOnClosed ) {
                throw new IllegalStateException("OutputStream is closed.");
            }
        } else {
            sb.append("#");
            sb.append(key);
            sb.append("=");
            sb.append(value);
            sb.append("#");
            try {
                this.socket.getOutputStream().write(sb.toString().getBytes());
            } catch ( IOException e ) {
                throw new RuntimeException("Unexpected exception.", e);
            }
            sb.setLength(0);
        }
    }

    public void addListener(@NotNull final SocketReceiver.SocketResponderListener listener) {
        this.socketReceiver.addListener(listener);
    }

    public void removeListener(@NotNull final SocketReceiver.SocketResponderListener listener) {
        this.socketReceiver.removeListener(listener);
    }

}
