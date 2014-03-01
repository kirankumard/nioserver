/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package my.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kdevaras
 */
class ConnectionAcceptor implements Runnable {

    private DispatcherPool dispatchers;
    
    //This should be based on the number of processors.
    private int numDispatcers = 2;
    
    private final ServerSocketChannel serverChannel;
    private final int PORT = 9006;
    private final String HOST = "127.0.0.1";
    
    private boolean isRunning = false;
    
    public ConnectionAcceptor() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(true);
        serverChannel.bind(new InetSocketAddress(HOST, PORT));
        dispatchers.initializePool(numDispatcers);
        isRunning = true;
    }

    @Override
    public void run() {
        
        dispatchers.start();
        
        while(isRunning) {
            try {
                SocketChannel channel = serverChannel.accept();
                dispatchers.chooseDispatcher(channel, SelectionKey.OP_READ);//decide connection or channel
            } catch (IOException ex) {
                Logger.getLogger(ConnectionAcceptor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
