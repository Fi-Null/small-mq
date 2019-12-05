package com.small.mq.client.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/5/19 3:10 PM
 */
public class ThrowableUtil {

    /**
     * parse error to string
     *
     * @param e
     * @return
     */
    public static String toString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String errorMsg = stringWriter.toString();
        return errorMsg;
    }

}
