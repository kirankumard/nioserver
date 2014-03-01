/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.server.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.server.model.ByteChunk;

/**
 *
 * @author kdevaras
 */
public class ResponseOutputStream extends OutputStream {

    SocketChannel sink;

    ResponseOutputStream(SocketChannel channel) {
        this.sink = channel;
    }

    @Override
    public void write(int b) throws IOException {
        //change this. Add 1 byte to the current or new ByteChunk.
        byte[] bytes = new byte[1];
        bytes[0] = (byte) b;

        sink.write(ByteBuffer.wrap(bytes));
    }

    public void flushBuffer() {

    }

    public void writeToSink(ByteChunk chunk) {
        writeToSink(chunk.getBuffer());
    }

    public void writeToSink(ByteBuffer bb) {
        try {
            if (bb.position() != 0) {
                bb.flip();
            }
//            System.out.println("bb.limit() = " + bb.limit());
            while (bb.hasRemaining()) {
                int len = sink.write(bb);
                if (len < 0) {
                    //eof case
                }
                
                if (len == 0) {
                    //handle case with a temp selector
                }
            }
            bb.clear();
        } catch (IOException ex) {
            Logger.getLogger(ResponseOutputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public SocketChannel getChannel() {
        return sink;
    }
}
