package com.qaperf.ai.utils;

public class OSBeans {


        boolean windows, mac, unix, solaris;
        String dirSeparator=null;

        public boolean isWindows() {
            return windows;
        }

        public void setWindows(boolean windows) {
            this.windows = windows;
        }

        public boolean isMac() {
            return mac;
        }

        public void setMac(boolean mac) {
            this.mac = mac;
        }

        public boolean isUnix() {
            return unix;
        }

        public void setUnix(boolean unix) {
            this.unix = unix;
        }

        public boolean isSolaris() {
            return solaris;
        }

        public void setSolaris(boolean solaris) {
            this.solaris = solaris;
        }

        public String getDirSeparator() {
            return dirSeparator;
        }

        public void setDirSeparator(String dirSeparator) {
            this.dirSeparator = dirSeparator;
        }


}
