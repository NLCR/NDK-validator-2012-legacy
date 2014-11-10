package com.logica.ndk.tm.utilities.test.waiters;

import com.logica.ndk.tm.utilities.AbstractUtility;

/**
 *
 * @author brizat
 */
public class WaitForTimeImpl extends AbstractUtility{
    
    public void execute(Long time){
        log.info("Utility WaitForTimeImpl started, will be waiting: " + time);
        try {
            Thread.sleep(time);
            log.info("Waiting is ended");
        } catch (InterruptedException ex) {
            log.error("Waiting was interupted!" ,ex);
        }
        
    }
    
}
