package org.diskium.utils;

public class ASCII {

/**
     * Creates ASCII art of Diskium.
     *
     * @param platform Platform the server is running on (e.g. Paper, Fabric)
     * @param version Version of Diskium.
     * @param mcVersion Version of Minecraft.
     * @return Full ASCII art that can be printed into console.
     */
    public static String printASCII(String platform, String version, String mcVersion) {
        return  "\n" +
                "   ####  \n" +
                " ##    ##    DISKIUM\n" +
                "#        #   v" + version + "\n" +
                " ##    ##    Platform: " + platform + " " + mcVersion + "\n" +
                "   ####   ";
    }
}
