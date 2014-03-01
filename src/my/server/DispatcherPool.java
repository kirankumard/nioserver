/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Change the name later to an appropriate one.
 *
 * @author kdevaras
 */
public class DispatcherPool {

    public static final int STATE_SHUTDOWN = 0;
    public static final int STATE_INITIALIZED = 1;
    public static final int STATE_STARTED = 2;

    private static Dispatcher[] dispatchers;
    private static int size;
    private static volatile int next;
    private static int state = STATE_SHUTDOWN;

    private static final ExecutorService dispatchExecutor = Executors.newFixedThreadPool(10);

    static Dispatcher notMe(String dispatcherId) {
        Dispatcher dispatcher = nextDispatcher();
        System.out.println("don't want " + dispatcherId);
        while (dispatcher.getDispatcherId().equals(dispatcherId)) {
            dispatcher = nextDispatcher();
        }
        System.out.println("returning with " + dispatcher.getDispatcherId());
        return dispatcher;
    }

    private DispatcherPool() {

    }

    public static void initializePool(int numDispatchers) throws IOException {
        init(numDispatchers);
        state = STATE_INITIALIZED;
    }

    public static synchronized Dispatcher nextDispatcher() {
        if (next == size) {
            next = 0;
        }
        return dispatchers[next++];
    }

    private static void init(int numDispatchers) throws IOException {
        size = numDispatchers;
        dispatchers = new Dispatcher[size];
        for (int i = 0; i < size; i++) {
            dispatchers[i] = new Dispatcher("Dispatcher" + (i + 1));
            dispatchers[i].name = "Disp " + (i + 1);
        }
    }

    static int getState() {
        return state;
    }

    static void start() {
        for (int i = 0; i < size; i++) {
            dispatchExecutor.execute(dispatchers[i]);
        }

        state = STATE_STARTED;
    }

    ExecutorService getDispatchExecutor() {
        return dispatchExecutor;
    }

    public static void chooseDispatcher(SocketChannel channel, int op) {

        if (channel.isRegistered()) {
            for (Dispatcher dispatcher : dispatchers) {
                SelectionKey key = channel.keyFor(dispatcher.getSelector());
                if (key != null && key.isValid() && (key.interestOps() == op)) {
                    return;
                }
            }

            for (Dispatcher dispatcher : dispatchers) {
                SelectionKey key = channel.keyFor(dispatcher.getSelector());
                if (key != null && key.isValid() && (key.interestOps() == 0)) {
                    if (op == SelectionKey.OP_READ) {
                        dispatcher.addChannelForRead(channel);
                    } else if (op == SelectionKey.OP_WRITE) {
                        dispatcher.addChannelForWrite(channel);
                    }
                    return;
                }
            }
        } else {
            if (op == SelectionKey.OP_READ) {
                nextDispatcher().addChannelForRead(channel);
            } else if (op == SelectionKey.OP_WRITE) {
                nextDispatcher().addChannelForWrite(channel);
            }
        }

    }

}
