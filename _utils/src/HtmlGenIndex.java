import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlGenIndex {

    private static String INDEX_FILE = "\\index.html";   // 目标索引文件

    public static void run(String path) {
        INDEX_FILE = path + INDEX_FILE;
        try {
            // 步骤1: 读取并处理所有HTML文件
            List<PostInfo> posts = new ArrayList<>();
            // 定义常量
            // HTML文件所在目录
            String INPUT_DIR = path + "\\html\\post";
            File dir = new File(INPUT_DIR);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".html"));

            if (files == null) {
                System.out.println("目录不存在或没有HTML文件");
                return;
            }

            for (File file : files) {
                PostInfo post = extractPostInfo(file);
                if (post != null) {
                    posts.add(post);
                }
            }

            Collections.sort(posts, (p1, p2) -> p2.date.compareTo(p1.date));
            updateIndexHtml(posts);

            System.out.println("成功更新 " + INDEX_FILE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 提取文章信息
    private static PostInfo extractPostInfo(File file) throws IOException {
        Document doc = Jsoup.parse(file, "UTF-8");

        String content = null;
        Element h3 = doc.select("h3").first();
        if (h3 != null) {
            content = h3.text().trim();
        }
        if (content == null || content.isEmpty()) {
            Element firstP = doc.select("p").first();
            if (firstP == null) return null;
            content = firstP.text().trim();
        }


        // 去除结尾标点符号
        content = removeTrailingPunctuation(content);

        // 提取日期
        Element timeDiv = doc.select("div.time").first();
        if (timeDiv == null) return null;

        String dateStr = timeDiv.text().trim();
        Date date = parseDate(dateStr);
        if (date == null) return null;

        return new PostInfo(
                file.getName().replace(".html", ""),
                content,
                date,
                formatDate(date)
        );
    }

    // 去除结尾标点符号
    private static String removeTrailingPunctuation(String text) {
        int first = -1;
        if (text.length() > 20) {

            first = text.indexOf("，");
            if (first == -1) {
                first = text.indexOf("。");
            }
            if (first == -1) {
                first = text.indexOf("：");
            }
            if (first == -1) {
                first = text.indexOf("”");
            }
            if (first != -1) {
                text = text.substring(0, first);
            }
        }
        char lastChar = text.charAt(text.length() - 1);
        if ("，。！？,.!?".indexOf(lastChar) >= 0) {
            return text.substring(0, text.length() - 1);
        }
        return text;
    }

    // 解析日期字符串
    private static Date parseDate(String dateStr) {
        try {
            // 匹配"YYYY年MM月DD日"格式
            Pattern pattern = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");
            Matcher matcher = pattern.matcher(dateStr);
            if (matcher.find()) {
                String normalized = String.format("%s-%02d-%02d",
                        matcher.group(1),
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3))
                );
                return new SimpleDateFormat("yyyy-MM-dd").parse(normalized);
            }
        } catch (Exception e) {
            System.err.println("日期解析失败: " + dateStr);
        }
        return null;
    }

    // 格式化日期为"YY年M月D日"
    private static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yy年MM月dd日");
        return sdf.format(date);
    }

    // 更新index.html
    private static void updateIndexHtml(List<PostInfo> posts) throws IOException {
        Path indexPath = Paths.get(INDEX_FILE);
        Document doc;

        doc = Jsoup.parse(indexPath.toFile(), "UTF-8");

        String OUTPUT_DIV_CLASS = "content-text";
        Element contentDiv = doc.select("div." + OUTPUT_DIV_CLASS).first();
        if (contentDiv == null) {
            throw new RuntimeException("找不到class为" + OUTPUT_DIV_CLASS + "的div");
        }

        contentDiv.html("");
        for (PostInfo post : posts) {
            String html = String.format(
                    "<p><span class=\"publish-time\">%s</span> <a href=\"/html/post/%s\">%s</a></p>",
                    post.formattedDate, post.fileId, post.content
            );
            contentDiv.append(html);
        }

        // 写回文件
        Files.write(indexPath, doc.html().getBytes(StandardCharsets.UTF_8));
    }

    // 文章信息存储类
    static class PostInfo {
        String fileId;      // 文件名ID（不带.html后缀）
        String content;     // 处理后的p标签内容
        Date date;          // 原始日期对象
        String formattedDate; // 格式化后的日期字符串

        PostInfo(String fileId, String content, Date date, String formattedDate) {
            this.fileId = fileId;
            this.content = content;
            this.date = date;
            this.formattedDate = formattedDate;
        }
    }
}