/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package my.server;

import java.nio.ByteBuffer;
import my.server.io.Request;

/**
 *
 * @author kdevaras
 */
public interface ReadCallbackHandler {

    public void postReadEvent(ByteBuffer readBuffer, Request request);
    
//    public void postWriteEvent(Connection con, ByteBuffer readBuffer);
}
