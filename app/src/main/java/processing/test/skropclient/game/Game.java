package processing.test.skropclient.game;

import java.util.concurrent.atomic.AtomicBoolean;

import processing.core.PApplet;
import processing.test.skropclient.game.state.GameState;
import processing.test.skropclient.network.DualClient;
import processing.test.skropclient.network.Serialize;
import processing.test.skropclient.network.ServerFullException;
import processing.test.skropclient.network.SkropServerObject;

public class Game {
    private PApplet parent;
    private GameState gameState = GameState.NOT_CONNECTING;
    private DualClient client;

    private SkropServerObject server1;
    private SkropServerObject server2;

    private RectangleList rectangles;

    private Runnable drawFunction = new Runnable() {
        public void run() {

        }
    };

    public Game(PApplet _parent) {
        parent = _parent;
    }

    public void setServer(String address, int port1, int port2) {
        server1 = new SkropServerObject(address, port1, port1);
        server2 = new SkropServerObject(address, port2, port2);
    }

    public void connect() {
        gameState = GameState.CONNECTING;
    }

    public void update() {
        switch (gameState) {
            case CONNECTING:
                try {
                    updateConnecting();
                } catch (ServerFullException e) {
                    gameState = GameState.CONNECTION_FAILED;
                    e.printStackTrace();
                }
                break;
            case WAITING_FOR_CONNECTION_2:
                updateWaitingForConnection2();
                break;
            case WAITING_FOR_GAME_BEGIN:
                updateWaitingForGameBegin();
                break;
            case IN_GAME:
                updateInGame();
                drawFunction = inGameDrawFunction;
                break;
        }
    }

    private Runnable inGameDrawFunction = new Runnable() {
        public void run() {
            rectangles.drawRectangles(parent);
        }
    };

    public void draw() {
        drawFunction.run();
    }

    private void updateInGame() {
        String udpReceived = client.receiveUDP();
        if (udpReceived.startsWith("world ")) {
            String worldData = udpReceived.substring(6);

            try {
                rectangles = (RectangleList) Serialize.fromString(worldData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateWaitingForGameBegin() {
        if (client.receiveTCP() != null && client.receiveTCP().equals("begin")) {
            gameState = GameState.IN_GAME;
        }
    }

    private void updateWaitingForConnection2() {
        if (client.receiveTCP() != null && client.receiveTCP().equals("waiting for game")) {
            gameState = GameState.WAITING_FOR_GAME_BEGIN;
        }
    }

    private void updateConnecting() throws ServerFullException {
        if (client == null) {
            AtomicBoolean bindFailed;
            client = new DualClient(server1);
            client.start();

            while ((bindFailed = client.tcpClient().bindFailed()) == null);

            if (bindFailed.get()) {

                client = new DualClient(server2);
                client.start();

                while ((bindFailed = client.tcpClient().bindFailed()) == null);

                if (bindFailed.get()) {
                    client = null;
                    throw new ServerFullException("Cannot connect to " + server1.address + " on " + " port " + server1.tcpPort + " or " + server2.tcpPort +"; the server is full.");
                } else {
                    client.sendTCP("hello");
                    gameState = GameState.WAITING_FOR_CONNECTION_2;
                }
            } else {
                client.sendTCP("hello");
                gameState = GameState.WAITING_FOR_CONNECTION_2;
            }
        }
    }

    public void mousePressed() {
        if (gameState == GameState.IN_GAME) {
            float x = parent.mouseX/(float)parent.width;
            float y = parent.mouseY/(float)parent.height;

            client.sendUDP("mouse " + x + "," + y);
        }

        if (gameState == GameState.WAITING_FOR_GAME_BEGIN) {
            client.sendTCP("ready");
        }
    }
}
