/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package my.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kdevaras
 */
public class TemporarySelectorPool {

    
    private static final Queue<Selector> tempSelectors = new ArrayBlockingQueue<>(5, true);
    
    static {
        for (int i=0; i<5; i++) {
            try {
                tempSelectors.offer(Selector.open());
            } catch (IOException ex) {
                Logger.getLogger(TemporarySelectorPool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static Selector allocateSelectorFromPool() {
        return tempSelectors.poll();
    }
    
    public static void returnSelectorToPool(Selector selector) {
        //Wake the selector. Otherwise, it could wait until the timeout period.
        //TODO: Can also choose to cancel all keys registered in this selector.
        selector.wakeup();
        tempSelectors.offer(selector);
    }
}
