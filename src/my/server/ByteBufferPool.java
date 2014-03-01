/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.server;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author kdevaras
 */
class ByteBufferPool {
    
    //Does this class need thread-safety?
    //Dispatcher/Selector threads keep invoking this pool for ByteBuffers.
    //

    private final ByteBuffer[] bb;
    private final int bufCapacity = 8192;
    private final int size;
    private final int[] busy;//0 => free, 1 => allocated
    private final Lock guard = new ReentrantLock();

    public ByteBufferPool(int num) {
        bb = new ByteBuffer[num];
        busy = new int[num];
        size = num;

        for (int i = 0; i < num; i++) {
            bb[i] = ByteBuffer.allocateDirect(bufCapacity);
            busy[i] = 0;
        }
    }

    public ByteBuffer tryAllocate() {
        guard.lock();
        try {
            for (int i=0; i< size; i++) {
                if (busy[i] == 0) {
                    busy[i] = 1;
                    return bb[i];
                }
            }
        } finally {
            guard.unlock();
        }
        
        return null;
    }
    
    //How do you return a bb to a pool?
    public void returnToPool(ByteBuffer iAmBack) {
        guard.lock();
        try {
            for (int i=0; i< size; i++) {
                if (busy[i] == 1 && bb[i] == iAmBack) {
                    bb[i].clear();
                    busy[i] = 0;
                    return;
                }
            }
        } finally {
            guard.unlock();
        }
    }

    public void reset() {
        guard.lock();
        try {
            for (int i = 0; i < size; i++) {
                bb[i].clear();
                busy[i] = 0;
            }
        } finally {
            guard.unlock();
        }
    }

}
