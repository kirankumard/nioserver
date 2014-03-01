/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kdevaras
 */
public class ThreadPerTaskServer {

    private static Socket connection = null;
    
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(90);
        
        while (true) {
            System.out.println("Listening for new connection....");
            connection = server.accept();
            System.out.println("Accepted new connection.");
            Thread worker = new Thread(new Runnable() {
                public void run() {
                    try {
                        handleRequest(connection);
                    } catch (IOException ex) {
                        Logger.getLogger(ThreadPerTaskServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            worker.start();
        }
    }

    private static void handleRequest(Socket connection) throws IOException {
        
        InputStream clientIn = connection.getInputStream();
        OutputStream clientOut = connection.getOutputStream();
        
        BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientIn), 2048);
        byte[] outData = null;
        while(true) {
            System.out.println("Waiting for data from client");
            String line = clientReader.readLine();
            System.out.println("Received from client: " + line);
            if (line.equals("Q")) {
                System.out.println("Exiting...");
                break;
            }
            
            outData = ("Echo: " + line + "\n").getBytes();
            clientOut.write(outData);
        }
        
        clientOut.close();
        clientReader.close();
        clientIn.close();
    }

}
