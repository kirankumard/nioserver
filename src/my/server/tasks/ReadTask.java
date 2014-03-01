/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package my.server.tasks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.server.ReadCallbackHandler;
import my.server.io.Request;

/**
 * @author kdevaras
 */
public class ReadTask implements Runnable {

    private final Request request;
    private final ByteBuffer buffer;
    private final ReadCallbackHandler handler;
    
    public ReadTask(Request request, ByteBuffer buffer, ReadCallbackHandler handler) {
        this.request = request;
        this.buffer = buffer;
        this.handler = handler;
    }
    
    
    @Override
    public void run() {
        try {
            request.readFromSink(buffer, handler);
        } catch (IOException ex) {
            Logger.getLogger(ReadTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        handler.postReadEvent(buffer, request);
    }
    
}
