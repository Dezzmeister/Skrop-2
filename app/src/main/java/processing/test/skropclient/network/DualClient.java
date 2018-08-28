package processing.test.skropclient.network;

import processing.test.skropclient.network.tcp.TCPClient;
import processing.test.skropclient.network.udp.UDPClient;

public class DualClient {
    private final TCPClient tcpClient;
    private final UDPClient udpClient;

    private final Communicator<String> tcpCommunicator = new Communicator<String>();
    private final Communicator<String> udpCommunicator = new Communicator<String>();

    private final Thread tcpThread;
    private final Thread udpThread;

    public DualClient(String address, int tcpPort, int udpPort) {
        tcpClient = new TCPClient(address, tcpPort, tcpCommunicator);
        udpClient = new UDPClient(address, udpPort, udpCommunicator);

        tcpThread = new Thread(tcpClient, "Skrop TCP Client");
        udpThread = new Thread(udpClient, "Skrop UDP Client");
    }

    public void start() {
        tcpThread.start();
        udpThread.start();
    }

    /**
     * Sends a message to the TCP Client. If the TCP Client has not connected, it
     * will attempt to send the latest message received through this method when it
     * starts.
     * <p>
     * The client automatically appends a newline character to the end of the String
     * before sending it.
     *
     * @param message
     *            String to send through TCP Client
     */
    public void sendTCP(String message) {
        tcpCommunicator.send(message);
    }

    /**
     * Sends a message to the UDP Client. If the UDP Client has not connected, it
     * will attempt to send the latest message received through this method when it
     * starts.
     *
     * @param message
     *            String to send through UDP Client
     */
    public void sendUDP(String message) {
        udpCommunicator.send(message);
    }

    /**
     * Returns the latest, new String received by the TCP Client. If the latest
     * message is not new, returns null.
     *
     * @return latest message received from TCP Client, unless message has been
     *         checked before
     */
    public String receiveTCP() {
        if (tcpCommunicator.hasNew()) {
            return tcpCommunicator.receive();
        }

        return null;
    }

    /**
     * Returns the latest, new String received by the UDP Client. If the latest
     * message is not new, returns null.
     *
     * @return latest message received from UDP Client, unless message has been
     *         checked before
     */
    public String receiveUDP() {
        if (udpCommunicator.hasNew()) {
            return udpCommunicator.receive();
        }

        return null;
    }

    public Communicator<String> getTCPCommunicator() {
        return tcpCommunicator;
    }

    public Communicator<String> getUDPCommunicator() {
        return udpCommunicator;
    }
}
