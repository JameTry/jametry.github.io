import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 替换html中a标签中上下章节链接*
 * @author Jame!
 * @date 2025/4/8 下午 8:52
 */
public class ReplacePrevAndNextHref {
    public static void run(String path) throws Exception {
        path += "\\html\\post";
        for (File htmlFile : getAllFiles(path)) {
            // 从文件名提取数字
            String fileName = htmlFile.getName();
            if (fileName.equals("0.html")) {
                continue;
            }
            int currentNumber = Integer.parseInt(fileName.split("\\.")[0]);

            // 读取文件内容
            String content = new String(Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8);

            // 创建替换正则表达式
            String prevPattern = "(<a\\s+href=\")[^\"]*(\"\\s+id=\"prev\")";
            String prevReplacement = "$1/html/post/" + (currentNumber - 1) + "$2";

            String nextPattern = "(<a\\s+href=\")[^\"]*(\"\\s+id=\"next\")";
            String nextReplacement = "$1/html/post/" + (currentNumber + 1) + "$2";

            // 执行替换
            if (currentNumber != 1) {
                content = content.replaceAll(prevPattern, prevReplacement);
            }
            content = content.replaceAll(nextPattern, nextReplacement);

            // 写回文件
            Files.write(htmlFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static List<File> getAllFiles(String folderPath) {
        List<File> fileList = new ArrayList<>();
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                fileList.addAll(Arrays.asList(files));
            }
        }
        return fileList;
    }
}
