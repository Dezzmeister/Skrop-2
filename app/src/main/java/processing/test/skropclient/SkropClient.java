package processing.test.skropclient;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*;
import processing.test.skropclient.network.Communicator;
import processing.test.skropclient.network.DualClient;
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
    private DualClient client = new DualClient("10.0.0.9", 10222, 10222);

    public void settings() {
        fullScreen();
    }

    public void setup() {
        System.out.println("yee");
        client.start();
    }

    public void draw() {
        background(150);
    }

    public void mousePressed() {
        client.sendUDP("udp from phone");
        client.sendTCP("tcp from phone");
    }

    public void mouseReleased() {

    }
}
