/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kdevaras
 */
public class Dispatcher implements Runnable {

    private String dispatcherId;

    public String getDispatcherId() {
        return dispatcherId;
    }

    private Selector selector;
    private boolean isRunning;
    private volatile boolean isLocked = false;

    private DispatcherEventHandler eventHandler = DispatcherEventHandler.instance;

    //selector needs to be guarded as the event handler
    //calls back for triggering new events.
    private final Lock lock = new ReentrantLock();

    String name;

    private final List<SocketChannel> newChannelsToRegisterForRead = new ArrayList<>();
    private final List<SocketChannel> newChannelsToRegisterForWrite = new ArrayList<>();

    public Dispatcher(String dispatcherId) throws IOException {
        this.dispatcherId = dispatcherId;
        selector = Selector.open();
    }

    public void addChannelForRead(SocketChannel channel) {
        lock();
        try {
            newChannelsToRegisterForRead.add(channel);
//            System.out.println("added channel to read channel list.");
            selector.wakeup();
        } finally {
            unlock();
        }
    }

    public void addChannelForWrite(SocketChannel channel) {
        lock();
        try {
            newChannelsToRegisterForWrite.add(channel);
//            System.out.println("added channel to write channel list.");
            selector.wakeup();
        } finally {
            unlock();
        }
    }

    private void registerNewChannels() throws IOException {
        lock();
        try {
            registerNewChannelsForRead();
            registerNewChannelsForWrite();
        } finally {
            unlock();
        }
    }

    private void registerNewChannelsForRead() throws IOException {
        if (newChannelsToRegisterForRead.isEmpty()) {
            return;
        }

        selector.wakeup();
        for (SocketChannel channel : newChannelsToRegisterForRead) {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        }
        newChannelsToRegisterForRead.clear();
    }

    private void registerNewChannelsForWrite() throws IOException {
        if (newChannelsToRegisterForWrite.isEmpty()) {
            return;
        }

        selector.wakeup();
        for (SocketChannel channel : newChannelsToRegisterForWrite) {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_WRITE);
        }
        newChannelsToRegisterForWrite.clear();
    }

    void makeWriteReady(SocketChannel channel) {
        addChannelForWrite(channel);
    }

    void makeReadReady(SocketChannel channel) {
        addChannelForRead(channel);
    }

    private void lock() {
        lock.lock();
        isLocked = true;
    }

    private void unlock() {
        lock.unlock();
        isLocked = false;
    }

    @Override
    public void run() {
        isRunning = true;
        System.out.println("Dispatcher is running now.");

        while (isRunning) {
            try {
                registerNewChannels();
                System.out.println(dispatcherId + ": no. of channels registered = " + selector.keys().size());
                int eventCount = selector.select();//blocking call
                System.out.println(dispatcherId + ": eventCount = " + eventCount);
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();//removing from the set of selected keys

                    if (key.isValid() && key.isReadable()) {
                        //avoid repeated event reporting.
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                        System.out.println(dispatcherId + ": deregistering key from OP_READ. " + key.isReadable());
                        eventHandler.handleReadEvent((SocketChannel) key.channel());
                    }

                    if (key.isValid() && key.isWritable()) {
                        System.out.println("channel ready for write op.");
                        //avoid repeated event reporting.
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                        System.out.println(dispatcherId + ": deregistering key from OP_WRITE. " + key.isWritable());
                        eventHandler.handleWriteEvent((SocketChannel) key.channel());
                    }
                }
                selector.selectedKeys().clear();
            } catch (IOException ex) {
                Logger.getLogger(Dispatcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    Selector getSelector() {
        return selector;
    }

}
