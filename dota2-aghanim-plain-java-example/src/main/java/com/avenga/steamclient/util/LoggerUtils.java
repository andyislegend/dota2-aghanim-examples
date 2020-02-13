package com.avenga.steamclient.util;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LoggerUtils {

    public static void initDebugLogger() {
        Logger.getRootLogger().setLevel(Level.DEBUG);
        BasicConfigurator.configure();
    }
}
