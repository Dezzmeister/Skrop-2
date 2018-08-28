package processing.test.skropclient.network.udp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

import processing.test.skropclient.network.Communicator;
import processing.test.skropclient.network.RecentPasser;

public class UDPClient implements Runnable {
    public static final String TERMINATOR = Character.toString((char) 0);

    private int incomingPacketMaxLength = 4096;

    public final int port;
    public final String address;
    private final InetSocketAddress clientAddress;

    private final RecentPasser<String> messageReceiver;
    private final RecentPasser<String> messageSender;

    private DatagramChannel channel;

    /**
     * Creates a <code>UDPClient</code> that will send and receive data through the
     * local UDP port, <code>_port</code>. Received data will be available in
     * <code>_messageReceiver</code>; data can be sent through
     * <code>_messageSender</code>.
     *
     * @param _address
     *            IP address of the Server
     * @param _port
     *            UDP port to listen on
     * @param _messageSender
     *            used to send data to the other device
     * @param _messageReceiver
     *            used to receive data from the other device
     */
    public UDPClient(String _address, int _port, RecentPasser<String> _messageSender, RecentPasser<String> _messageReceiver) {
        address = _address;
        port = _port;
        messageSender = _messageSender;
        messageReceiver = _messageReceiver;

        clientAddress = new InetSocketAddress(address, port);

        createSocket();
    }

    /**
     * Creates a <code>UDPClient</code> that will send and receive data through the
     * local UDP port, <code>_port</code>. Received data will be available in
     * <code>communicator</code>, which can also be used to send data.
     *
     * @param _address
     *            IP address of the Server
     * @param _port
     *            UDP port to listen on
     * @param communicator
     *            <code>Communicator</code> to send and receive String data
     */
    public UDPClient(String _address, int _port, Communicator<String> communicator) {
        this(_address, _port, communicator.getSender(), communicator.getReceiver());
    }

    /**
     * Creates a new <code>UDPClient</code> with the specified port and
     * <code>Communicator</code>. Returns a new <code>Thread</code> with this
     * <code>UDPClient</code>, but does not start the <code>Thread</code>.
     *
     * @param _address
     *            IP address of the Server
     * @param _port
     *            port to create <code>UDPClient</code> with
     * @param communicator
     *            <code>Communicator</code> to create <code>UDPClient</code> with
     * @return <code>Thread</code> to run newly created <code>UDPClient</code>
     */
    public static Thread createOnNewThread(String _address, int _port, Communicator<String> communicator) {
        return new Thread(new UDPClient(_address, _port, communicator));
    }

    /**
     * <b>DO NOT EXPLICITLY CALL THIS METHOD. USE THIS UDP SERVER IN A THREAD.</b>
     * <p>
     * <b>THIS METHOD BLOCKS INDEFINITELY.</b>
     */
    @Override
    public void run() {
        ByteBuffer inBuffer = ByteBuffer.allocate(incomingPacketMaxLength);
        inBuffer.clear();

        try {
            System.out.println("UDP Client listening at UDP port " + port + ". Waiting for data...");

            channel.configureBlocking(false);

            while (true) {
                InetSocketAddress receivedAddress = (InetSocketAddress) channel.receive(inBuffer);

                if (receivedAddress != null) {
                    String inMessage = "";
                    byte[] bytes = inBuffer.array();

                    String fullMessage = new String(bytes);
                    inMessage = fullMessage.substring(0, fullMessage.indexOf(TERMINATOR));

                    System.out.println("UDP RECEIVED: " + inMessage);

                    messageReceiver.pass(inMessage);
                    inBuffer.clear();
                    inBuffer.put(new byte[incomingPacketMaxLength]);
                    inBuffer.clear();
                }

                if (messageSender.hasNew()) {
                    String outMessage = messageSender.retrieve();

                    ByteBuffer outBuffer = ByteBuffer.allocate(outMessage.length());
                    outBuffer.clear();
                    outBuffer.put(outMessage.getBytes());
                    outBuffer.flip();

                    channel.send(outBuffer, clientAddress);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSocket() {
        try {
            channel = DatagramChannel.open();

            System.out.println("UDP Datagram Socket created at UDP port " + port + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the maximum length of packets received by this {@link UDPClient}. Any
     * extra characters will be discarded.
     *
     * @param maxLength
     *            maximum packet length
     */
    public void setIncomingPacketMaxLength(int maxLength) {
        incomingPacketMaxLength = maxLength;
    }
}
