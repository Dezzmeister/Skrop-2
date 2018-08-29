package processing.test.skropclient.network.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import processing.test.skropclient.network.Communicator;
import processing.test.skropclient.network.RecentPasser;

public class TCPClient implements Runnable {

    public final int port;
    public final String address;
    private Socket socket;
    private final RecentPasser<String> receiver;
    private final RecentPasser<String> sender;
    public AtomicBoolean bindFailed;

    /**
     * Creates a <code>TCPClient</code> that will send and receive data through the
     * local TCP port, <code>_port</code>. Received data will be available in
     * <code>_receiver</code>; data can be sent through <code>_sender</code>.
     *
     * @param _address
     *            IP address of the TCP Server
     * @param _port
     *            TCP port to listen on
     * @param _sender
     *            used to send data to the other device
     * @param _receiver
     *            used to receive data from the other device
     */
    public TCPClient(String _address, int _port, RecentPasser<String> _sender, RecentPasser<String> _receiver) {
        port = _port;
        address = _address;
        receiver = _receiver;
        sender = _sender;
    }

    /**
     * Creates a <code>TCPClient</code> that will send and receive data through the
     * local TCP port, <code>_port</code>. Received data will be available in
     * <code>communicator</code>, which can also be used to send data.
     *
     * @param _address
     *            IP address of the TCP Server
     * @param _port
     *            TCP port to listen on
     * @param communicator
     *            <code>Communicator</code> to send and receive String data
     */
    public TCPClient(String _address, int _port, Communicator<String> communicator) {
        this(_address, _port, communicator.getSender(), communicator.getReceiver());
    }

    /**
     * Creates a new <code>TCPClient</code> with the specified port and
     * <code>Communicator</code>. Returns a new <code>Thread</code> with this
     * <code>TCPClient</code>, but does not start the <code>Thread</code>.
     *
     * @param _address
     *            IP address of the TCP Server
     * @param _port
     *            port to create <code>TCPClient</code> with
     * @param communicator
     *            <code>Communicator</code> to create <code>TCPClient</code> with
     * @return <code>Thread</code> to run newly created <code>TCPClient</code>
     */
    public static Thread createOnNewThread(String _address, int _port, Communicator<String> communicator) {
        return new Thread(new TCPClient(_address, _port, communicator));
    }

    private BufferedReader socketReader;
    private BufferedWriter socketWriter;

    /**
     * <b>DO NOT EXPLICITLY CALL THIS METHOD. USE THIS TCP SERVER IN A THREAD.</b>
     * <p>
     * <b>THIS METHOD BLOCKS INDEFINITELY.</b>
     */
    @Override
    public void run() {
        try {
            InetAddress serverAddress = InetAddress.getByName(address);

            while (true) {
                socket = new Socket(serverAddress, port);
                //socket.connect(new InetSocketAddress(address, port));
                //socket.bind(new InetSocketAddress(address, port));
                bindFailed = new AtomicBoolean(false);

                System.out.println("TCP Client connected to " + socket.getInetAddress().getHostAddress()
                        + " on remote port " + socket.getPort() + ".");

                socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                while (socket.isConnected()) {
                    String messageIn = null;

                    while (socketReader.ready() && (messageIn = socketReader.readLine()) != null) {
                        System.out.println("TCP RECEIVED: " + messageIn);
                        receiver.pass(messageIn);
                    }

                    if (sender.hasNew()) {
                        socketWriter.write(sender.retrieve());
                        socketWriter.write("\n");
                        socketWriter.flush();
                    }
                }

                socket.close();
                receiver.pass(null);
            }
        } catch (BindException e) {
            bindFailed = new AtomicBoolean(true);
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AtomicBoolean bindFailed() {
        return bindFailed;
    }

    protected void finalize() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
