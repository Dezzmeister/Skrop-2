package processing.test.skropclient;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*;
import processing.test.skropclient.game.Game;
import processing.test.skropclient.network.Communicator;
import processing.test.skropclient.network.DualClient;
import processing.test.skropclient.network.SkropServerObject;
import processing.test.skropclient.network.udp.UDPClient;

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class SkropClient extends PApplet {
    private Game game = new Game(this);

    public void settings() {
        fullScreen();
    }

    @Override
    public void setup() {
        game.setServer("10.0.0.9", 10222, 10223);
    }

    @Override
    public void draw() {
        background(150);
        game.update();
        game.draw();
    }

    @Override
    public void mousePressed() {
        game.connect();
        game.mousePressed();
    }

    public void mouseReleased() {

    }
}
