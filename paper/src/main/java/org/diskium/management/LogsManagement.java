package org.diskium.management;

import org.diskium.utils.DateUtils;
import org.diskium.Diskium;
import org.diskium.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class LogsManagement {

    private static final File logsDir = new File(Diskium.getInstance().getDataFolder().getParentFile().getParentFile(), "logs");

    public static String[] getLogs(LocalDate startDate, LocalDate endDate) {
        String[] dir = logsDir.list((File directory, String s) -> s.endsWith(".log.gz"));
        if (startDate == null && endDate == null) return dir;
        return filter(dir, startDate, endDate);
    }

    public static boolean delete(String start, String end) {
        String[] logs;

        if (!DateUtils.isValidDate(start, end)) return false;
        if (start == null || end == null) {
            logs = getLogs(null, null);
        } else if (start == null) {
            logs = getLogs(getOldest(), LocalDate.parse(end));
        } else {
            logs = getLogs(LocalDate.parse(start), getNewest());
        }
        if (logs == null) return false;

        for (String name : logs) {
            FileUtils.del(new File(name));
        }

        return true;
    }

    public static Map<String, Integer> search(String keyword) {
        String[] logs = getLogs(null, null);
        Map<String, Integer> map = new HashMap<>();
        for (String log : logs) {
            File zipped = new File(Diskium.getInstance().getServer().getWorldContainer(), "logs/" + log);
            try {
                FileInputStream fis = new FileInputStream(zipped);
                GZIPInputStream gzip = new GZIPInputStream(fis);
                String unzipped = new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
                gzip.close();
                map.put(zipped.getName(), searchLog(unzipped, keyword));
            } catch (IOException e) {
                return null;
            }
        }
        String latest = getLatestLog();
        if (latest == null) return map;
        map.put("latest.log", searchLog(latest, keyword));
        return map;
    }


    private static String getLatestLog() {
        File file = new File(Diskium.getInstance().getServer().getWorldContainer(), "logs/latest.log");
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            return null;
        }
    }

    private static int searchLog(String unzipped, String keyword) {
        int count = 0;
        int index = 0;
        while ((index = unzipped.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }

    private static String[] filter(String[] original, LocalDate start, LocalDate end) {
        List<String> list = new ArrayList<>();
        if (start == null) {
            for (String i : original) {
                if (supposedToAdd(i, null, end)) {
                    list.add(i);
                }
            }
        }
        return list.toArray(String[]::new);
    }

    private static boolean supposedToAdd(String fileName, LocalDate start, LocalDate end) {
        if (start == null) {
            return end.isAfter(LocalDate.parse(fileName.substring(0, 10)));
        } else if (end == null) {
            return end.isBefore(LocalDate.parse(fileName.substring(0, 10)));
        }
        return start.isBefore(LocalDate.parse(fileName.substring(0, 10))) && end.isAfter(LocalDate.parse(fileName.substring(0, 10)));
    }

    private static LocalDate getOldest() {
        String[] logs = getLogs(null, null);
        String date = logs[0].substring(0, 10);

        return LocalDate.parse(date);
    }

    private static LocalDate getNewest() {
        String[] logs = getLogs(null, null);
        String date = logs[logs.length - 1].substring(0, 10);

        return LocalDate.parse(date);
    }
}
