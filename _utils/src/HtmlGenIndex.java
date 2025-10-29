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
        Element h2 = doc.select("h2").first();
        if (h2 != null) {
            content = h2.text().trim();
        }
        if (content == null || content.isEmpty()) {
            Element firstP = doc.select("article p").first();
            if (firstP == null) return null;
            content = firstP.text().trim();
        }


        content = getTitleExcerpt(content);

        Element timeDiv = doc.select("p.top-op span").get(1);
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



    private static String getTitleExcerpt(String text) {
        int minLength = 5;
        int maxLength = 12;
        String punctuationStr = "，。：”？,.!?\"";
        int firstIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (punctuationStr.indexOf(text.charAt(i)) >= 0) {
                firstIndex = i;
                break;
            }
        }

        String result;
        if (firstIndex == -1) {
            result = text;
        } else {
            if (firstIndex >= minLength) {
                result = text.substring(0, firstIndex);
            } else {
                int secondIndex = -1;
                for (int i = firstIndex + 1; i < text.length(); i++) {
                    if (punctuationStr.indexOf(text.charAt(i)) >= 0) {
                        secondIndex = i;
                        break;
                    }
                }
                if (secondIndex == -1) {
                    result = text;
                } else {
                    if (secondIndex <= maxLength) {
                        result = text.substring(0, secondIndex);
                    } else {
                        result = text.substring(0, firstIndex);
                    }
                }
            }
        }

        if (!result.isEmpty()) {
            char lastChar = result.charAt(result.length() - 1);
            if ("，。？,.".indexOf(lastChar) >= 0) {
                result = result.substring(0, result.length() - 1);
            }
        }
        if (!result.isEmpty()) {
            if ("“".indexOf(result.charAt(0)) >= 0) {
                result = result.substring(1);
            }
        }
        return result;
    }

    private static Date parseDate(String dateStr) {
        try {
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

    private static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");
        return sdf.format(date);
    }

    private static void updateIndexHtml(List<PostInfo> posts) throws IOException {
        Path indexPath = Paths.get(INDEX_FILE);
        Document doc;

        doc = Jsoup.parse(indexPath.toFile(), "UTF-8");

        String OUTPUT_DIV_CLASS = "Content";
        Element contentDiv = doc.select("main" ).first();
        if (contentDiv == null) {
            throw new RuntimeException("找不到class为" + OUTPUT_DIV_CLASS + "的div");
        }

        contentDiv.html("");
        for (PostInfo post : posts) {
            String html = String.format(
                    "<article class=\"post-item\">\n" +
                            "            <span class=\"post-item-date\">%s</span>\n" +
                            "            <h4 class=\"post-item-title\">\n" +
                            "                <a href=\"/html/post/%s\">%s</a>\n" +
                            "            </h4>\n" +
                            "        </article>",
                    post.formattedDate, post.fileId, post.content
            );
            contentDiv.append(html);
        }

        Files.write(indexPath, doc.html().getBytes(StandardCharsets.UTF_8));
    }

    static class PostInfo {
        String fileId;
        String content;
        Date date;
        String formattedDate;

        PostInfo(String fileId, String content, Date date, String formattedDate) {
            this.fileId = fileId;
            this.content = content;
            this.date = date;
            this.formattedDate = formattedDate;
        }
    }
}