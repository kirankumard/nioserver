/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package my.server.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import my.server.ReadCallbackHandler;
import my.server.io.RequestInputStream;
import my.server.model.DataBuffer;

/**
 * O
 * @author kdevaras
 */
public class Request {

    DataBuffer requestData;
    public RequestInputStream input;
    int contentLength = 0;

    public Request(SocketChannel channel) {
        requestData = new DataBuffer();
        input = new RequestInputStream(channel);
    }
    
    public void addRequestData(ByteBuffer readBuffer) {
        requestData.addData(readBuffer);
    }
    

    public void clearData() {
        requestData.clear();
    }

    //not the right way. Data should in fact be accessed via RequestInputStream.
    public DataBuffer getData() {
        return requestData;
    }

    public SocketChannel getChannel() {
        return input.getChannel();
    }
    
    public void readFromSink(ByteBuffer allocatedBB, ReadCallbackHandler callback) throws IOException {
        input.read(this, allocatedBB, callback);
    }

    void setContentLength(int totalRead) {
        contentLength = totalRead;
    }

   
}
