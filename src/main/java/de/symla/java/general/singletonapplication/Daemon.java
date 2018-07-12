package de.symla.java.general.singletonapplication;

import com.sun.istack.internal.NotNull;
import de.symla.java.general.singletonapplication.components.ApplicationGui;
import de.symla.java.general.singletonapplication.sockets.SocketInterface;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Daemon implements Runnable, Thread.UncaughtExceptionHandler {

    private static final String DAEMON_NAME = "daemon_f313c5784311c7507fedda365f18bf50_0";

    private static Daemon instance;

    private final Thread daemonThread;

    private ServerSocket serverSocket;

    private Socket clientSocket;

    private Thread socketServerThread;

    private final int port;

    private final ApplicationGui applicationGui;

    private final Thread applicationGuiThread;

    private Daemon(final int port,
                   @NotNull final ApplicationGui applicationGui) {
        if ( applicationGui == null ) throw new NullPointerException("de.symla.java.general.singletonapplication.components.ApplicationGui must not be null.");

        this.port = port;
        this.applicationGui = applicationGui;

        this.daemonThread = new Thread(this::run, DAEMON_NAME);
        this.daemonThread.setUncaughtExceptionHandler(this::uncaughtException);
        this.daemonThread.start();

        this.applicationGuiThread = new Thread(this.applicationGui, DAEMON_NAME+"_GUI");
        this.applicationGuiThread.start();
    }

    @Override
    public void run() {
        this.setup();

        while ( true ) {
            try {
                Thread.sleep(100);
            } catch ( InterruptedException e ) {
                System.err.println("Thread "+Thread.currentThread().getName()+" was interrupted.");
            }
        }
    }

    private void setup() {
        final boolean socketStatus = this.openSocket();

        if ( socketStatus ) {
            final SocketInterface socketInterface = new SocketInterface(this.clientSocket);
            socketInterface.addListener((key, value) -> {
                if ( key.equals("RESULT_STATUS") ) {
                    switch ( value ) {
                        case "1":
                            socketInterface.send("REQUEST", "GUI", true);
                            break;
                        default:
                            System.err.println("Unhandled case '"+value+"' for 'RESULT_STATUS'.");
                            break;
                    }
                } else if ( key.equals("RESULT_GUI") ) {
                    switch ( value ) {
                        case "1":
                            System.out.println("Application already running. Opened GUI of old application. Exiting...");
                            socketInterface.close();
                            System.exit(0);
                            break;
                        default:
                            System.err.println("Unhandled case '"+value+"' for 'RESULT_GUI'.");
                            break;
                    }
                } else {
                    System.err.println("Unhandled key '"+key+"'.");
                }
            });
            socketInterface.listen();
            socketInterface.send("REQUEST", "STATUS", true);
        } else {
            this.openSocketServer();
        }
    }

    private boolean openSocket() {
        this.clientSocket = new Socket();

        try {
            this.clientSocket.connect(new InetSocketAddress(this.port));
            final boolean connectStatus = this.clientSocket.isConnected();
            if ( !connectStatus ) this.clientSocket = null;
            return connectStatus;
        } catch ( IOException e ) {
            if ( e instanceof ConnectException ) {
                this.clientSocket = null;
                return false;
            } else {
                throw new RuntimeException("Unhandled exception type.", e);
            }
        }
    }

    private void openSocketServer() {
        this.socketServerThread = new Thread(this::runSocketServer, "SocketServerThread");
        this.socketServerThread.start();

    }

    private void runSocketServer() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            while ( true ) {
                handleSocket(this.serverSocket.accept());
            }
        } catch ( IOException e ) {
            throw new RuntimeException("Could not open server socket.", e);
        }
    }

    private void handleSocket(final Socket socket) {
        final SocketInterface socketInterface = new SocketInterface(socket);
        socketInterface.addListener((key, value) -> {
            if ( key.equals("REQUEST") ) {
                switch (value) {
                    case "STATUS":
                        socketInterface.send("RESULT_STATUS", statusRequested(), true);
                        break;
                    case "GUI":
                        socketInterface.send("RESULT_GUI", guiRequested(), true);
                        break;
                    default:
                        socketInterface.send("RESULT", "ERROR", false);
                        break;
                }
            } else {
                socketInterface.send("RESULT", "UNSUPPORTED", false);
            }
        });
        socketInterface.listen();
    }

    private String statusRequested() {
        return "1";
    }

    private String guiRequested() {
        if ( !this.applicationGui.isGuiVisible() ) {
            this.applicationGui.showGui();
        }
        return  "1";
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        final StringBuilder sb = new StringBuilder();
        Throwable curThrowable = e;

        sb.append("Caught an exception in thread '");
        sb.append(t.getName());
        sb.append("': \n");

        while ( curThrowable != null ) {
            sb.append("\n- ");
            sb.append(curThrowable.getClass().getSimpleName());
            sb.append(":\n");
            sb.append("  -> ");
            sb.append(curThrowable.getMessage());

            curThrowable = curThrowable.getCause();
        }

        System.err.println(sb.toString());
    }

    public static Daemon start(final int port, @NotNull final ApplicationGui applicationGui) {
        if ( instance != null )
            throw new IllegalStateException("de.symla.java.general.singletonapplication.Daemon already created.");

        instance = new Daemon(port, applicationGui);
        return instance;
    }

    public static Daemon get() {
        if ( instance == null )
            throw new IllegalStateException("Must first be instantiated by \"start()\".");
        return instance;
    }

}
