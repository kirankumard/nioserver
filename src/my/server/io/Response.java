/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.server.io;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import my.server.Connection;
import my.server.DispatcherEventHandler;
import my.server.DispatcherPool;
import my.server.io.ResponseOutputStream;
import my.server.model.ByteChunk;
import my.server.model.DataBuffer;

/**
 *
 * @author kdevaras
 */
public class Response {

    private final DataBuffer writeData;
    private final ResponseOutputStream outputStream;
//    private PrintWriter writer;

    public Response(SocketChannel channel) {
        writeData = new DataBuffer();
        outputStream = new ResponseOutputStream(channel);
    }

    public void commit() {
        //Can this be done in another thread?
        DispatcherEventHandler.instance.submitTask(new Runnable() {

            @Override
            public void run() {
                //write each chunk to the sink
                ArrayList<ByteChunk> chunks = writeData.getDataQueue();
                System.out.println("chunks.size() = " + chunks.size());
                for (ByteChunk chunk : chunks) {
                    outputStream.writeToSink(chunk);
                }
                
                DispatcherEventHandler.instance.postWriteEvent(Response.this);
            }

        });

        DispatcherPool.chooseDispatcher(outputStream.sink, SelectionKey.OP_WRITE);
    }

    Object getWriter() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void write(byte[] bytes) {
        addToDataBuffer(bytes);
    }

    private void addToDataBuffer(byte[] bytes) {
        System.out.println("bytes.length = " + bytes.length);
        writeData.addData(ByteBuffer.wrap(bytes));
    }

    public SocketChannel getChannel() {
        return outputStream.getChannel();
    }

    public void clearData() {
        writeData.clear();
    }
}
