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
        // 设置HTML文件夹路径
        File folder = new File(path + "\\html\\post");

        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("错误：文件夹不存在或不是目录");
            return;
        }

        // 获取所有HTML文件
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".html"));

        if (files == null || files.length == 0) {
            System.out.println("未找到HTML文件");
            return;
        }

        // 存储文件和对应的日期
        List<FileDatePair> fileDateList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        Pattern datePattern = Pattern.compile("<div\\s+class=\"time\"[^>]*>(.*?)</div>");

        // 从每个文件中提取日期
        for (File file : files) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                Matcher matcher = datePattern.matcher(content);

                if (matcher.find()) {
                    String dateStr = matcher.group(1);
                    // 处理日期中的单位数月份和日期（如"3月"改为"03月"）
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

        // 按日期排序（从早到晚）
        Collections.sort(fileDateList);

        // 创建临时文件夹避免重命名冲突
        File tempDir = new File(folder, "temp_rename_" + System.currentTimeMillis());
        if (!tempDir.exists() && !tempDir.mkdir()) {
            System.err.println("无法创建临时目录");
            return;
        }

        // 第一步：移动所有文件到临时目录
        for (FileDatePair pair : fileDateList) {
            File tempFile = new File(tempDir, pair.file.getName());
            if (!pair.file.renameTo(tempFile)) {
                System.err.println("移动失败: " + pair.file.getName());
            }
        }

        // 第二步：重命名并移回文件，按日期顺序编号
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

        // 删除临时文件夹
        if (!tempDir.delete()) {
            System.err.println("警告: 无法删除临时文件夹");
        }
    }

    // 规范化日期字符串（确保月份和日期是两位数）
    private static String normalizeDateString(String dateStr) {
        // 将"年"、"月"、"日"分割开
        String[] parts = dateStr.split("年|月|日");
        if (parts.length < 3) return dateStr;

        // 格式化年份
        String year = parts[0].trim();

        // 格式化月份（确保两位数）
        String month = parts[1].trim();
        if (month.length() == 1) month = "0" + month;

        // 格式化日期（确保两位数）
        String day = parts[2].trim();
        if (day.length() == 1) day = "0" + day;

        return year + "年" + month + "月" + day + "日";
    }

    // 辅助类：文件与日期的配对
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