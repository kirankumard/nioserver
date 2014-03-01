/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package my.server.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *
 * @author kdevaras
 */
public class DataBuffer {
    
    private final ArrayList<ByteChunk> dataQueue;
    int position = 0;

    public ArrayList<ByteChunk> getDataQueue() {
        return dataQueue;
    }

    public DataBuffer() {
        this.dataQueue = new ArrayList<>();
    }

    public void addData(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            ByteChunk byteChunk = getCurrentByteChunk(true);
            byteChunk.append(buffer);
        }
    }

    public ByteChunk getCurrentByteChunk(boolean create) {
        int queueSize = dataQueue.size();
        int currentIndex = -1;
        
        if (queueSize > 0)
            currentIndex = queueSize - 1;

        if (queueSize == 0 || dataQueue.get(currentIndex).isFull()) {
            if (create) {
                ByteChunk newOne = new ByteChunk();
                dataQueue.add(newOne);
                currentIndex++;
                return newOne;
            }
        }

        return dataQueue.get(currentIndex);
    }
    
    public boolean hasRemaining() {
        return false;
    }
    
    public long available() {
        return 0L;
    }

    public void clear() {
        for (ByteChunk chunk : dataQueue)
            chunk.clear();
    }
}
