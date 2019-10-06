package com.qaperf.ai.utils;

public class OSLocator {

        public static boolean isWindows(String OS) {

            return (OS.toLowerCase().indexOf("win") >= 0);

        }

        public static boolean isMac(String OS) {

            return (OS.toLowerCase().indexOf("mac") >= 0);

        }

        public static boolean isUnix(String OS) {

            return (OS.toLowerCase().indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

        }

        public static boolean isSolaris(String OS) {

            return (OS.toLowerCase().indexOf("sunos") >= 0);

        }

}
