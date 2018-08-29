package processing.test.skropclient.network;

public class SkropServerObject {
    public final String address;
    public final int tcpPort;
    public final int udpPort;

    public SkropServerObject(String _address, int _tcpPort, int _udpPort) {
        address = _address;
        tcpPort = _tcpPort;
        udpPort = _udpPort;
    }
}
