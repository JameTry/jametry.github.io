import com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacePrevAndNextHref {
    public static void run(String path) throws Exception {
        run(path + "\\html\\post", "$1/html/post/");
        run(path + "\\html\\other", "$1/html/other/");
    }

    public static void run(String path, String pat) throws IOException {
        for (File htmlFile : getAllFiles(path)) {
            // 从文件名提取数字
            String fileName = htmlFile.getName();
            if (fileName.equals("0.html")) {
                continue;
            }
            int currentNumber = Integer.parseInt(fileName.split("\\.")[0]);

            String content = new String(Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8);

            String prevPattern = "(<a\\s+href=\")[^\"]*(\"\\s+id=\"prev\")";
            String prevReplacement = pat + (currentNumber - 1) + "$2";

            String nextPattern = "(<a\\s+href=\")[^\"]*(\"\\s+id=\"next\")";
            String nextReplacement = pat + (currentNumber + 1) + "$2";

            if (currentNumber != 1) {
                content = content.replaceAll(prevPattern, prevReplacement);
            }
            content = content.replaceAll(nextPattern, nextReplacement);

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
