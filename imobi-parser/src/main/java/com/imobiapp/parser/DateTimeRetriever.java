package com.imobiapp.parser;

import java.util.Date;

/**
 * @author matics.
 */
public class DateTimeRetriever {

    public long getCurrentTimestamp() {
        return new Date().getTime();
    }
}
