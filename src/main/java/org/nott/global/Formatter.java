package org.nott.global;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;


/**
 * @author Nott
 * @date 2024-9-12
 */
public class Formatter {

    public interface DATE {

        public static final SimpleDateFormat HH_MM = new SimpleDateFormat("HH:mm");

        public static final SimpleDateFormat HH_MM_SS = new SimpleDateFormat("HH:mm:ss");

        public static final DateTimeFormatter DATE_TIME_HOUR = DateTimeFormatter.ofPattern("HH:mm");
    }
}
