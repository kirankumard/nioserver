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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kdevaras
 */
public class ThreadPoolBasedServer {

    private Socket connection = null;
    
    private final ExecutorService executors = Executors.newFixedThreadPool(10);
    
    public static void main(String[] args) throws IOException {
        new ThreadPoolBasedServer().startServer();
    }
    
    public void startServer() throws IOException {
        ServerSocket server = new ServerSocket(90);
        
        while (true) {
            System.out.println("Listening for new connection....");
            connection = server.accept();
            System.out.println("Accepted new connection.");
            executors.execute(new Task(connection));
        }
    }

    private class Task implements Runnable {
        
        Socket connection;
        
        Task(Socket connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                handleRequest();
            } catch (IOException ex) {
                Logger.getLogger(ThreadPoolBasedServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        private void handleRequest() throws IOException {

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

}
