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

/**
 *
 * @author kdevaras
 */
public class SingleThreadServer {
    //Connections are accepted after the previous connection is processed.
    //That means only one connection at a time.
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(80);
        while(true) {
            Socket connection = server.accept();
            handleRequest(connection);
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
            outData = ("Echo: " + line + "\n").getBytes();
            System.out.println("writing to server: " + new String(outData));
            clientOut.write(outData);
            if (line.equals("Q")) {
                System.out.println("Exiting...");
                break;
            }
            
        }
        
        clientOut.close();
        clientReader.close();
        clientIn.close();
    }
}
