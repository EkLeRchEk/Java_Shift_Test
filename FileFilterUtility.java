import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileFilterUtility {

    private static class NumberStats {
        long count = 0;
        double sum = 0;
        Double min = null;
        Double max = null;

        void add(double value) {
            count++;
            sum += value;
            if (min == null || value < min) min = value;
            if (max == null || value > max) max = value;
        }

        double avg() {
            return count == 0 ? 0 : sum / count;
        }
    }

    private static class StringStats {
        long count = 0;
        Integer minLen = null;
        Integer maxLen = null;

        void add(String s) {
            int len = s.length();
            count++;
            if (minLen == null || len < minLen) minLen = len;
            if (maxLen == null || len > maxLen) maxLen = len;
        }
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Нет входных файлов");
            return;
        }

        List<String> inputFiles = new ArrayList<>();
        String outputDir = ".";
        String prefix = "";
        boolean append = false;
        boolean shortStats = false;
        boolean fullStats = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                    if (i + 1 < args.length) {
                        outputDir = args[++i];
                    } else {
                        System.out.println("Ошибка: после -o нужен путь");
                        return;
                    }
                    break;

                case "-p":
                    if (i + 1 < args.length) {
                        prefix = args[++i];
                    } else {
                        System.out.println("Ошибка: после -p нужен префикс");
                        return;
                    }
                    break;

                case "-a":
                    append = true;
                    break;

                case "-s":
                    shortStats = true;
                    break;

                case "-f":
                    fullStats = true;
                    break;

                default:
                    inputFiles.add(args[i]);
            }
        }

        if (inputFiles.isEmpty()) {
            System.out.println("Не указаны входные файлы");
            return;
        }

        NumberStats intStats = new NumberStats();
        NumberStats floatStats = new NumberStats();
        StringStats stringStats = new StringStats();

        List<String> integers = new ArrayList<>();
        List<String> floats = new ArrayList<>();
        List<String> strings = new ArrayList<>();

        for (String fileName : inputFiles) {
            Path path = Paths.get(fileName);

            if (!Files.exists(path)) {
                System.out.println("Файл не найден: " + fileName);
                continue;
            }

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) continue;

                    if (isLong(line)) {
                        integers.add(line);
                        intStats.add(Long.parseLong(line));
                    } else if (isDouble(line)) {
                        floats.add(line);
                        floatStats.add(Double.parseDouble(line));
                    } else {
                        strings.add(line);
                        stringStats.add(line);
                    }
                }

            } catch (IOException e) {
                System.out.println("Ошибка чтения файла: " + fileName);
            }
        }

        try {
            Files.createDirectories(Paths.get(outputDir));

            writeIfNotEmpty(outputDir, prefix + "integers.txt", integers, append);
            writeIfNotEmpty(outputDir, prefix + "floats.txt", floats, append);
            writeIfNotEmpty(outputDir, prefix + "strings.txt", strings, append);

        } catch (IOException e) {
            System.out.println("Ошибка при создании выходной папки: " + outputDir);
            return;
        }

        if (shortStats || fullStats) {
            printNumberStats("Integers", intStats, fullStats);
            printNumberStats("Floats", floatStats, fullStats);
            printStringStats("Strings", stringStats, fullStats);
        }
    }

    private static void writeIfNotEmpty(String dir, String fileName, List<String> data, boolean append) {
        if (data.isEmpty()) return;

        Path filePath = Paths.get(dir, fileName);

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath.toFile(), append))) {

            for (String s : data) {
                writer.write(s);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Ошибка записи файла: " + filePath);
        }
    }

    private static boolean isLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return s.contains(".") || s.toLowerCase().contains("e");
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void printNumberStats(String name, NumberStats stats, boolean full) {
        System.out.println(name + ": count = " + stats.count);
        if (full && stats.count > 0) {
            System.out.println("  min = " + stats.min);
            System.out.println("  max = " + stats.max);
            System.out.println("  sum = " + stats.sum);
            System.out.println("  avg = " + stats.avg());
        }
    }

    private static void printStringStats(String name, StringStats stats, boolean full) {
        System.out.println(name + ": count = " + stats.count);
        if (full && stats.count > 0) {
            System.out.println("  min length = " + stats.minLen);
            System.out.println("  max length = " + stats.maxLen);
        }
    }
}