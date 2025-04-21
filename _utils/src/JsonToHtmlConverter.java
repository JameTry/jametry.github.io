import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonToHtmlConverter {
    public static void run(String projectPath) {
        String jsonDir = projectPath + "\\easy-talk\\talks";
        String templatePath = projectPath + "\\_utils\\template\\template.html";
        String outputDir = projectPath + "\\html\\talks";
        try {
            // 读取模板 HTML
            String template = readFile(templatePath);

            int index = 1;
            int lastIndex = getLastIndex(jsonDir);

            while (true) {
                String jsonFilePath = jsonDir + File.separator + index + ".json";
                File jsonFile = new File(jsonFilePath);
                if (!jsonFile.exists()) {
                    break;
                }

                // 读取 JSON 文件内容
                String jsonContent = readFile(jsonFilePath);
                JSONObject jsonObject = JSONObject.parseObject(jsonContent);
                String content = jsonObject.getString("content");
                String publishedTime = jsonObject.getString("publishedTime");

                // 填充模板
                String html = fillTemplate(template, content, publishedTime, index, lastIndex);

                // 生成 HTML 文件
                String outputFilePath = outputDir + File.separator + index + ".html";
                writeFile(outputFilePath, html);

                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static void writeFile(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    private static String fillTemplate(String template, String content, String publishedTime, int index, int lastIndex) {
        String filledContent = template.replace("<div class=\"time\"></div>", "<div class=\"time\">" + publishedTime + "</div>");
        int contentIndex = filledContent.indexOf("<div class=\"content-text\">") + "<div class=\"content-text\">".length();
        filledContent = new StringBuilder(filledContent)
                .insert(contentIndex, content)
                .toString();

        String prevLink = "/html/talks/" + (index - 1);
        String nextLink = "/html/talks/" + (index + 1);

        if (index == 1) {
            // 第一个文件，隐藏上一篇
            filledContent = filledContent.replace("<a href=\"/html/talks/1\" id=\"prev\">", "<a href=\"/html/talks/1\" id=\"prev\" style=\"display: none;\">");
        } else {
            filledContent = filledContent.replace("/html/talks/1", prevLink);
        }

        if (index == lastIndex) {
            // 最后一个文件，隐藏下一篇
            filledContent = filledContent.replace("<a href=\"/html/talks/3\" id=\"next\">", "<a href=\"/html/talks/3\" id=\"next\" style=\"display: none;\">");
        } else {
            filledContent = filledContent.replace("/html/talks/3", nextLink);
        }

        return filledContent;
    }

    private static int getLastIndex(String jsonDir) {
        int index = 1;
        while (true) {
            String jsonFilePath = jsonDir + File.separator + index + ".json";
            File jsonFile = new File(jsonFilePath);
            if (!jsonFile.exists()) {
                break;
            }
            index++;
        }
        return index - 1;
    }
}    