package processing.test.skropclient;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*;
import processing.test.skropclient.network.Communicator;
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
    private Communicator<String> communicator = new Communicator<String>();
    private UDPClient client = new UDPClient("10.0.0.9", 10222, communicator);
    private Thread clientThread = new Thread(client);

    public void settings() {
        fullScreen();
    }

    public void setup() {
        System.out.println("yee");
        clientThread.start();
    }

    public void draw() {
        background(150);
    }

    public void mousePressed() {
        communicator.send("from da fone");
        System.out.println("sent");
        background(255,0,0);
    }

    public void mouseReleased() {

    }
}
