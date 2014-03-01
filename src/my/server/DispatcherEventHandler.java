/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.server;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.server.io.ReadHandler;
import my.server.io.Request;
import my.server.io.Response;
import my.server.tasks.ReadTask;

/**
 * All dispatchers delegate event handling to a single DispatcherEventHandler.
 * All ops(read, process, write) should be done concurrently. Each of these ops
 * is presented as a task to the executor.
 *
 * Dispatcher(n)...(1)DispatcherEventHandler
 *
 * @author kdevaras
 */
public class DispatcherEventHandler implements ReadCallbackHandler {

    public final static DispatcherEventHandler instance = new DispatcherEventHandler();

    private final ExecutorService eventExecutor = Executors.newFixedThreadPool(10);

    //no. of ByteBuffers = no. of active threads in order to avoid waiting.
    //Guessing that each read is followed by a write. Hence, splitting the
    //buffers equally for read and write. This can also be done using a single
    //pool by maintaining a configurable ratio.
    private final ByteBufferPool readBufferPool = new ByteBufferPool(10);

    //Not using this currently.
//    private final ByteBufferPool writeBufferPool = new ByteBufferPool(5);
    ApplicationHandler appHandler = ApplicationHandler.instance;

    private DispatcherEventHandler() {
    }

//    void handleReadEvent(final Connection con) {
    void handleReadEvent(final SocketChannel channel) {

        final ByteBuffer readBuffer = readBufferPool.tryAllocate();
        if (readBuffer != null) {
            submitTask(new ReadTask(new Request(channel), readBuffer, this));
        } else {
            //re-register connection for read.
            System.out.println("read buffer not allocated. Register for read again.");
            DispatcherPool.chooseDispatcher(channel, SelectionKey.OP_READ);
        }
    }

    void handleWriteEvent(final SocketChannel channel) {

        DispatcherPool.chooseDispatcher(channel, SelectionKey.OP_READ);
    }

    public void postReadEvent(ByteBuffer readBuffer, final Request request) {
        final Response response = new Response(request.getChannel());
        //process the request(in a new thread).
        readBuffer.clear();
        readBufferPool.returnToPool(readBuffer);
        eventExecutor.execute(new Runnable() {

            @Override
            public void run() {
                new Processor().service(request, response);

            }
        });
    }

    public void submitTask(Runnable task) {
        eventExecutor.execute(task);
    }

    public void postWriteEvent(final Response response) {
        SocketChannel channel = response.getChannel();
        response.clearData();
        DispatcherPool.chooseDispatcher(channel, SelectionKey.OP_READ);
    }

}
