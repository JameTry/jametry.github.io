import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlDateSort {

    public static void run(String path) {
        File folder = new File(path + "\\html\\post");

        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("错误：文件夹不存在或不是目录");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".html"));

        if (files == null || files.length == 0) {
            System.out.println("未找到HTML文件");
            return;
        }

        List<FileDatePair> fileDateList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        Pattern datePattern = Pattern.compile("<div\\s+class=\"time\"[^>]*>(.*?)</div>");

        for (File file : files) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                Matcher matcher = datePattern.matcher(content);

                if (matcher.find()) {
                    String dateStr = matcher.group(1);
                    dateStr = normalizeDateString(dateStr);
                    Date date = dateFormat.parse(dateStr);
                    fileDateList.add(new FileDatePair(file, date));
                } else {
                    System.err.println("警告: " + file.getName() + " 中未找到日期信息，将排在最后");
                    fileDateList.add(new FileDatePair(file, new Date(Long.MAX_VALUE)));
                }
            } catch (IOException | ParseException e) {
                System.err.println("处理文件出错: " + file.getName() + " - " + e.getMessage());
                fileDateList.add(new FileDatePair(file, new Date(Long.MAX_VALUE)));
            }
        }

        Collections.sort(fileDateList);

        File tempDir = new File(folder, "temp_rename_" + System.currentTimeMillis());
        if (!tempDir.exists() && !tempDir.mkdir()) {
            System.err.println("无法创建临时目录");
            return;
        }

        for (FileDatePair pair : fileDateList) {
            File tempFile = new File(tempDir, pair.file.getName());
            if (!pair.file.renameTo(tempFile)) {
                System.err.println("移动失败: " + pair.file.getName());
            }
        }

        int newIndex = 1;
        for (FileDatePair pair : fileDateList) {
            File tempFile = new File(tempDir, pair.file.getName());
            File newFile = new File(folder, newIndex + ".html");

            if (tempFile.renameTo(newFile)) {
                System.out.printf("重命名: %s -> %s (日期: %s)%n",
                        tempFile.getName(),
                        newFile.getName(),
                        dateFormat.format(pair.date));
            } else {
                System.err.println("重命名失败: " + tempFile.getName());
            }
            newIndex++;
        }

        if (!tempDir.delete()) {
            System.err.println("警告: 无法删除临时文件夹");
        }
    }


    private static String normalizeDateString(String dateStr) {
        String[] parts = dateStr.split("年|月|日");
        if (parts.length < 3) return dateStr;

        String year = parts[0].trim();

        String month = parts[1].trim();
        if (month.length() == 1) month = "0" + month;

        String day = parts[2].trim();
        if (day.length() == 1) day = "0" + day;

        return year + "年" + month + "月" + day + "日";
    }

    static class FileDatePair implements Comparable<FileDatePair> {
        File file;
        Date date;

        public FileDatePair(File file, Date date) {
            this.file = file;
            this.date = date;
        }

        @Override
        public int compareTo(FileDatePair other) {
            return this.date.compareTo(other.date);
        }
    }
}