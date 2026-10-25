package org.diskium.management;

import org.diskium.MultiplatformLogger;
import org.diskium.Diskium;
import org.diskium.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

public class LogsManagement {

    private static final File logsDir = new File(Diskium.getInstance().getDataFolder().getParentFile().getParentFile(), "logs");

/**
     * Lists archived logs selected by the date filter and appends {@code latest.log}
     * regardless of the supplied bounds.
     *
     * @param startDate the start bound passed to the archive filter, or {@code null}
     * @param endDate the end bound passed to the archive filter, or {@code null}
     * @return selected paths, or an empty array if the logs directory cannot be listed
     */
    public static File[] getLogs(LocalDate startDate, LocalDate endDate) {
        File[] logs = logsDir.listFiles((_, s) -> s.endsWith(".log.gz"));
        if (logs != null) {
            List<File> logsList = new ArrayList<>(Arrays.asList(logs));

            if (startDate != null || endDate != null) {
                logsList = filter(logsList, startDate, endDate);
            }
            logsList.add(getLatestLog());
            return logsList.toArray(File[]::new);
        }

        return new File[0];
    }

/**
     * Attempts to delete or queue deletion of the paths returned by {@link #getLogs}.
     * Individual deletion results are ignored.
     *
     * @param start the start bound passed to log selection, or {@code null}
     * @param end the end bound passed to log selection, or {@code null}
     * @return always {@code true} when the method completes normally
     */
    public static boolean delete(LocalDate start, LocalDate end) {
        File[] logs = getLogs(start, end);

        for (File log : logs) {
            FileUtils.safeDel(log, FileUtils.DelSpecifier.LOGS);
        }

        return true;
    }

/**
     * Searches through all logs (including the latest) for a keyword.
     *
     * @param keyword A keyword being searched for.
     * @return A {@code Map<File, Integer>} containing a log file with a number of occurrences.
     */
    public static Map<File, Integer> search(String keyword) {
        List<File> logs = new ArrayList<>(Arrays.stream(getLogs(null, null)).toList());
        Map<File, Integer> map = new HashMap<>();

        for (File log : logs) {
            try {
                FileInputStream fis = new FileInputStream(log);
                GZIPInputStream gzip = new GZIPInputStream(fis);
                String unzipped = new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
                gzip.close();
                map.put(log, searchLog(unzipped, keyword));
            } catch (ZipException e) {
                MultiplatformLogger.error("Cannot unzip file " + log.getName());
            } catch (IOException e) {
                MultiplatformLogger.error("Something went wrong while searching in logs.", e);
            }
        }

        try (FileInputStream fis = new FileInputStream(getLatestLog())) {
            String content = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
            map.put(getLatestLog(), searchLog(content, keyword));
        } catch (FileNotFoundException e) {
            MultiplatformLogger.error("latest.log not found.");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while reading latest.log.", e);
        }
        return map;
    }

/**
     * Gets the latest log.
     *
     * @return The latest log, or {@code null} if it doesn't exist.
     */
    private static File getLatestLog() {
        File latest = new File(Diskium.getInstance().getServer().getWorldContainer(), "logs/latest.log");
        if (latest.exists()) {
            return latest;
        }

        return null;
    }

/**
     * Search for a keyword in an unzipped text content.
     *
     * @param unzipped The files text content.
     * @param keyword A keyword being searched for.
     * @return Number of occurrences.
     */
    private static int searchLog(String unzipped, String keyword) {
        int count = 0;
        int index = 0;
        while ((index = unzipped.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }

/**
     * Searches for all logs between two dates.
     *
     * @param original All log files.
     * @param start Lower date boundary, or {@code null} for no lower limit.
     * @param end Upper date boundary, or {@code null} for no upper limit.
     * @return List of files of logs between both dates.
     */
    private static List<File> filter(List<File> original, LocalDate start, LocalDate end) {
        List<File> list = new ArrayList<>();
        for (File i : original) {
            if (supposedToAdd(i, start, end)) {
                list.add(i);
            }
        }
        return list;
    }

/**
     * Checks, if a given log is between both dates.
     *
     * @param file A log file.
     * @param start Lower date boundary, or {@code null} for no lower limit.
     * @param end Upper date boundary, or {@code null} for no upper limit.
     * @return Whether the log is between those two days.
     */
    private static boolean supposedToAdd(File file, LocalDate start, LocalDate end) {
        if (start == null) {
            return end.isAfter(LocalDate.parse(file.getName().substring(0, 10)));
        }
        if (end == null) {
            return start.isAfter(LocalDate.parse(file.getName().substring(0, 10)));
        }
        return start.isBefore(LocalDate.parse(file.getName().substring(0, 10))) && end.isAfter(LocalDate.parse(file.getName().substring(0, 10)));
    }
}
