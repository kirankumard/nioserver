/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package my.server.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import my.server.Connection;
import my.server.DispatcherPool;
import my.server.ReadCallbackHandler;
import my.server.TemporarySelectorPool;

/**
 *
 * @author kdevaras
 */
public class RequestInputStream extends InputStream {

    private SocketChannel sink;

    public RequestInputStream(SocketChannel channel) {
        this.sink = channel;
    }
    
    @Override
    public int read() throws IOException {
        return 0;
    }

    SocketChannel getChannel() {
        return sink;
    }
    
    public void read(Request request, ByteBuffer readBuffer, ReadCallbackHandler callback) throws IOException {
        
        int totalRead = 0;
        int bytesRead = 1;//to enter the while loop
        int eof = -1;

        Selector tempSelector = null;
        SelectionKey tempKey = null;

        try {

            while (bytesRead > 0) {
                //TO VERIFY: No need to keep appending to the buffer until full capacity.
                //Assumption: If only 10 bytes(out of a bigger capacity) are filled, then
                //bb.clear() iterates through and clears only these 10 bytes.
                readBuffer.clear();
                bytesRead = sink.read(readBuffer);
                if (bytesRead > 0) {
                    totalRead += bytesRead;
                    //Note: It is upto the read handler/read queue implementation
                    //how it adds to the queue. Either simply add each byte buffer
                    //or ensure each buffer is full before adding to the next one.
                    request.addRequestData(bufferCopy(readBuffer));
                }
            }

            //out of the above loop means, bytes are not available for read.
            //case 1: end of stream is reached - use a readcallbackhandler
            if (eof == bytesRead) {
                callback.postReadEvent(readBuffer, request);
                return;
            }

            //case 2: more bytes to read but temporarily unavailable
            //register the channel to a temporary selector and wait for the
            //read op to be complete.
            tempSelector = TemporarySelectorPool.allocateSelectorFromPool();
            tempKey = sink.register(tempSelector, SelectionKey.OP_READ);
            tempKey.interestOps(tempKey.interestOps() & SelectionKey.OP_READ);
            int eventCount = tempSelector.select(1000);
            if (eventCount > 0) {
                if (tempKey.isValid() && tempKey.isReadable()) {
                    tempKey.interestOps(tempKey.interestOps() & (~SelectionKey.OP_READ));
                }
            }

            //TODO: rethink. What about the bytes read so far? What else to do?
            //Perhaps in this case, you could attach the data read/connection object
            //to the key. Handle this later.
            if (eventCount == 0) {
                //send back to dispatcher
                DispatcherPool.nextDispatcher().addChannelForRead(sink);
            }

            bytesRead = 1;
            int secondLoopBytes = 0;
            while (bytesRead > 0) {
                //again read sequence
                readBuffer.clear();
                bytesRead = request.getChannel().read(readBuffer);
                if (bytesRead > 0) {
                    totalRead += bytesRead;
                    secondLoopBytes += bytesRead;
                    request.addRequestData(bufferCopy(readBuffer));
                }
            }
            callback.postReadEvent(readBuffer, request);

        } finally {
            request.setContentLength(totalRead);
            //TODO: Move this logic to TemporarySelectorPool
            if (tempKey != null) {
                //why cancel? De-registering for interestOps is more helpful.
//                tempKey.cancel();
            }
            if (tempSelector != null) {
                TemporarySelectorPool.returnSelectorToPool(tempSelector);
            }
        }
    }

    private ByteBuffer bufferCopy(ByteBuffer buffer) {
        if (buffer.position() > 0) {
            buffer.flip();
        }
        ByteBuffer copy = ByteBuffer.allocate(buffer.limit());
        copy.put(buffer);
        copy.flip();
        return copy;
    }
}
