package com.evolveum.midpoint.midcredible.framework.comparator.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Viliam Repan (lazyman).
 */
public class StatusLogger {

    private static final Logger LOG = LoggerFactory.getLogger(StatusLogger.class);

    private static final long PRINTOUT_TIME_FREQUENCY = 5000L;

    private ThreadLocal<Long> lastPrintoutTime = new ThreadLocal<>();


    public void printStatus(String message, Object... args) {
        printStatus(LOG, false, message, args);
    }

    public void printStatus(Logger log, String message, Object... args) {
        printStatus(log, false, message, args);
    }

    public void printStatus(Logger log, boolean force, String message, Object... args) {
        if (lastPrintoutTime.get() == null) {
            lastPrintoutTime.set(0L);
        }

        if (!force && (lastPrintoutTime.get() + PRINTOUT_TIME_FREQUENCY > System.currentTimeMillis())) {
            return;
        }

        log.info(message, args);
        lastPrintoutTime.set(System.currentTimeMillis());
    }
}
