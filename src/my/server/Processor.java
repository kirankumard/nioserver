/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package my.server;

import my.server.io.Response;
import my.server.io.Request;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.ByteChannel;
import my.server.model.ByteChunk;
import my.server.model.DataBuffer;

/**
 *
 * @author kdevaras
 */
public class Processor {
    
    private byte[] eofByte = { (byte)-1};

    public void service(Request request, Response response) {
        printRequestData(request);
        writeToResponse(response);
        response.commit();
    }

    private void writeToResponse(Response response) {
        String data = "Processor's response: Hello!\n";//use \n as of now, as client does a readLine().
        System.out.println("writing data to response...");
        response.write(data.getBytes());
        response.write(eofByte);
    }

    private void printRequestData(Request request) {
        DataBuffer requestData = request.getData();
        for (ByteChunk chunk : requestData.getDataQueue()) {
            chunk.getBuffer().flip();
            byte[] bytes = new byte[chunk.getBuffer().limit()];
            chunk.getBuffer().get(bytes);
            System.out.println("user input: " + bytes);
        }
        
        request.clearData();
    }
}
