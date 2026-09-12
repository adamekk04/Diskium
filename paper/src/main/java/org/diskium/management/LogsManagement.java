package org.diskium.management;

import org.diskium.utils.DateUtils;
import org.diskium.Diskium;
import org.diskium.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class LogsManagement {

    private static final File logsDir = new File(Diskium.getInstance().getDataFolder().getParentFile().getParentFile(), "logs");

    public static File[] getLogs(LocalDate startDate, LocalDate endDate) {
        File[] dir = logsDir.listFiles((file, s) -> s.endsWith(".log.gz"));
        if (startDate == null && endDate == null) return dir;
        return filter(dir, startDate, endDate);
    }

    public static boolean delete(String start, String end) {
        File[] logs;

        if (!DateUtils.isValidDate(start, end)) return false;
        if (start == null && end == null) {
            logs = getLogs(null, null);
        } else if (start == null) {
            logs = getLogs(getOldest(), LocalDate.parse(end));
        } else {
            logs = getLogs(LocalDate.parse(start), getNewest());
        }
        if (logs == null) return false;

        for (File name : logs) {
            FileUtils.del(name);
        }

        return true;
    }

    public static Map<File, Integer> search(String keyword) {
        List<File> logs = new ArrayList<>(Arrays.stream(getLogs(null, null)).toList());
        logs.add(getLatestLog());
        Map<File, Integer> map = new HashMap<>();

        for (File log : logs) {
            try {
                FileInputStream fis = new FileInputStream(log);
                GZIPInputStream gzip = new GZIPInputStream(fis);
                String unzipped = new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
                gzip.close();
                map.put(log, searchLog(unzipped, keyword));
            } catch (IOException e) {
                return null;
            }
        }
        return map;
    }


    private static File getLatestLog() {
        return new File(Diskium.getInstance().getServer().getWorldContainer(), "logs/latest.log");
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

    private static File[] filter(File[] original, LocalDate start, LocalDate end) {
        List<File> list = new ArrayList<>();
        if (start == null) {
            for (File i : original) {
                if (supposedToAdd(i, null, end)) {
                    list.add(i);
                }
            }
        }
        return list.toArray(File[]::new);
    }

    private static boolean supposedToAdd(File file, LocalDate start, LocalDate end) {
        if (start == null) {
            return end.isAfter(LocalDate.parse(file.getName().substring(0, 10)));
        } else if (end == null) {
            return end.isBefore(LocalDate.parse(file.getName().substring(0, 10)));
        }
        return start.isBefore(LocalDate.parse(file.getName().substring(0, 10))) && end.isAfter(LocalDate.parse(file.getName().substring(0, 10)));
    }

    private static LocalDate getOldest() {
        File[] logs = getLogs(null, null);
        String date = logs[0].getName().substring(0, 10);

        return LocalDate.parse(date);
    }

    private static LocalDate getNewest() {
        File[] logs = getLogs(null, null);
        String date = logs[logs.length - 1].getName().substring(0, 10);

        return LocalDate.parse(date);
    }
}
