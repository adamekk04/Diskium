package org.diskium.utils;

public class ASCII {
    public static String printASCII(String platform, String version, String mcVersion) {
        return  "   ####  \n" +
                " ##    ##    DISKIUM\n" +
                "#        #   v" + version + "\n" +
                " ##    ##    Platform: " + platform + " " + mcVersion + "\n" +
                "   ####   ";
    }
}
