package processing.test.skropclient.game;

import java.util.concurrent.atomic.AtomicBoolean;

import processing.core.PApplet;
import processing.core.PConstants;
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

    private boolean client1 = false;
    private int myScore = 0;
    private int enemyScore = 0;

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
                break;
        }
    }

    private void inGameDrawFunction() {
        parent.noStroke();
        for (Rectangle r : rectangles.getRectangles()) {
            int x = (int)(r.x * parent.width);
            int y = (int)(r.y * parent.height);

            int width = (int)(r.width * parent.width);
            int height = (int)(r.height * parent.height);

            parent.fill(r.color());
            parent.rectMode(PConstants.CENTER);
            parent.rect(x,y,width,height);
        }
    }

    public void draw() {
        switch (gameState) {
            case IN_GAME:
                inGameDrawFunction();
                break;
        }
    }

    private void updateInGame() {
        String tcpReceived = client.receiveTCP();
        if (tcpReceived.startsWith("sync s1:")) {
            int s1 = Integer.parseInt(tcpReceived.substring(tcpReceived.indexOf(":")+1, tcpReceived.indexOf("s2:")));
            int s2 = Integer.parseInt(tcpReceived.substring(tcpReceived.indexOf("s2:")+3, tcpReceived.indexOf("w:")));

            if (client1) {
                myScore = s1;
                enemyScore = s2;
            } else {
                myScore = s2;
                enemyScore = s1;
            }
            String worldData = tcpReceived.substring(tcpReceived.indexOf("w:") + 2);

            try {
                rectangles = (RectangleList) Serialize.fromString(worldData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (rectangles != null) {
                rectangles.update(1);
            }
        }
    }

    private void updateWaitingForGameBegin() {
        String tcpReceived = client.receiveTCP();

        if (tcpReceived != null && tcpReceived.startsWith("world ")) {
            String worldData = tcpReceived.substring("world ".length() + 1);
            try {
                rectangles = (RectangleList) Serialize.fromString(worldData);
            } catch (Exception e) {
                e.printStackTrace();
            }

            gameState = GameState.IN_GAME;
        }
    }

    private void updateWaitingForConnection2() {
        String tcpReceived = client.receiveTCP();

        if (tcpReceived != null && tcpReceived.equals("waiting for game")) {
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
                    client1 = true;
                }
            } else {
                client.sendTCP("hello");
                gameState = GameState.WAITING_FOR_CONNECTION_2;
                client1 = false; //It's best to be explicit here, for clarity
            }
        }
    }

    public void mousePressed() {
        if (gameState == GameState.IN_GAME) {
            float x = parent.mouseX/(float)parent.width;
            float y = parent.mouseY/(float)parent.height;

            client.sendTCP("mouse " + x + "," + y);
        }

        if (gameState == GameState.WAITING_FOR_GAME_BEGIN) {
            client.sendTCP("ready");
        }
    }
}
