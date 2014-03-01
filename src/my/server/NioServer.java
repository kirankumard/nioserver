/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.server;

import java.io.IOException;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kdevaras
 */
public class NioServer implements Runnable {

    private boolean running;
    
    private final ServerSocketChannel ssc;
    private final ServerSocket ss;
    private final Selector selector;
    
    private Thread thread;
    
    public NioServer() throws IOException {
        
        ssc = ServerSocketChannel.open();
        ss = ssc.socket();
        ssc.bind(new InetSocketAddress("localost", 6789));
        ssc.configureBlocking(false);
        selector = Selector.open();
        //serversocketchannel only for accept operation.
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        
    }
    
    public void pressStart() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (running) {
            try {
                int selectCount = selector.select();//blocking call
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                for (SelectionKey key : selectedKeys) {
                    if (key.isValid() && key.isAcceptable()) {
                        SocketChannel accept = ssc.accept();
                        accept.register(selector, SelectionKey.OP_READ);
                    }
                    
                    if (key.isValid() && key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                    }
                }
                
                for (int i=0; i<selectCount; i++) {
                    
                }
                
                
                
            } catch (IOException ex) {
                Logger.getLogger(NioServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected void accept() {

    }

}
