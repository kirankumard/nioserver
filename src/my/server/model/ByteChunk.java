/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.server.model;

import java.nio.ByteBuffer;

/**
 *
 * @author kdevaras
 */
public class ByteChunk {

    ByteBuffer bb;

    public ByteChunk() {
        bb = ByteBuffer.allocate(8192);
    }

    public boolean isFull() {
        if (bb.position() == bb.capacity()) {
            return true;
        } else {
            return false;
        }
    }

    public void append(ByteBuffer srcBuffer) {
        int srcRem = srcBuffer.remaining();
        int thisRem = bb.remaining();
        int length = 0;

        if (srcRem > thisRem) {
            length = thisRem;
        } else {
            length = srcRem;
        }

        while (length-- > 0) {
            bb.put(srcBuffer.get());
        }
    }

    public ByteBuffer getBuffer() {
        return bb;
    }

    void clear() {
        bb.clear();
    }
}
