import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;


public class RssGenerator {
    // 配置常量
    private static final String POSTS_DIR = "D:\\project\\jametry.github.io\\html\\post";
    private static final String STATUSES_DIR = "D:\\project\\jametry.github.io\\easy-talk\\talks";
    private static final String SITE_URL = "https://jame.work/";
    private static final String RSS_FILE = "feed.xml";

    // 日期格式
    private static final SimpleDateFormat INPUT_DATE = new SimpleDateFormat("yyyy年MM月dd日");
    private static final SimpleDateFormat OUTPUT_DATE = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    // 定义类型常量
    private static final String TYPE_ARTICLE = "随笔";
    private static final String TYPE_STATUS = "说说";

    public static void main(String[] args) throws Exception {
        List<RssItem> items = new ArrayList<>();

        // 处理 HTML 文章（类型：文章）
        File htmlDir = new File(POSTS_DIR);
        for (File file : htmlDir.listFiles((d, name) -> name.endsWith(".html"))) {
            Document doc = Jsoup.parse(file, "UTF-8");
            String title = escapeXml(doc.select("p:first-of-type").text().trim());
            title = title.length() > 20 ? title.substring(0, 20) + "..." : title;
            Elements paragraphs = doc.select("p");
            StringBuilder content = new StringBuilder();
            for (Element p : paragraphs) {
                content.append("<p>").append(p.text()).append("</p>");
            }

            String timeStr = doc.select("div.time").text().split(" ")[0];
            Date pubDate = INPUT_DATE.parse(timeStr);
            String link = SITE_URL + "html/post/" + file.getName().replace(".html", "");

            items.add(new RssItem(title, content.toString(), pubDate, link, TYPE_ARTICLE));
        }

        // 处理 JSON 说说（类型：说说）
        File jsonDir = new File(STATUSES_DIR);
        for (File file : jsonDir.listFiles((d, name) -> name.endsWith(".json"))) {
            String jsonStr = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);

            // 提取原始内容并去除 HTML 标签
            String rawContent = jsonObject.getString("content");
            String plainText = Jsoup.parse(rawContent).text(); // 去除所有 HTML 标签

            // 生成标题（纯文本截取）
            String title = plainText.length() > 20 ? plainText.substring(0, 20) + "..." : plainText;
            title = escapeXml(title); // 转义 XML 特殊字符

            // 处理其他字段...
            Date pubDate = INPUT_DATE.parse(jsonObject.getString("publishedTime"));
            String link = SITE_URL + "easy-talk/talks/" + file.getName();

            // 构建 RSS 条目
            items.add(new RssItem(title, "<p>" + rawContent + "</p>", pubDate, link, TYPE_STATUS));
        }

        // 按时间排序
        items.sort((a, b) -> b.pubDate.compareTo(a.pubDate));

        // 构建 RSS
        StringBuilder rss = new StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rss version=\"2.0\">\n<channel>\n")
                .append("<title>Jame</title>\n<link>").append(SITE_URL).append("</link>\n")
                .append("<description>随笔与说说的合集</description>\n")

                .append("<lastBuildDate>")
                .append(OUTPUT_DATE.format(new Date()))  // 使用当前时间作为最后更新时间
                .append("</lastBuildDate>\n");
        for (RssItem item : items) {
            rss.append("<item>\n")
                    .append("<title>").append(item.title).append("</title>\n")
                    .append("<link>").append(item.link).append("</link>\n")
                    .append("<description><![CDATA[").append(item.content).append("]]></description>\n")
                    .append("<pubDate>").append(OUTPUT_DATE.format(item.pubDate)).append("</pubDate>\n")
                    .append("<guid>").append(item.link).append("</guid>\n")
                    .append("<category>").append(item.type).append("</category>\n") // 添加类型标签
                    .append("</item>\n");
        }

        rss.append("</channel>\n</rss>");

        try (FileWriter writer = new FileWriter(RSS_FILE)) {
            writer.write(rss.toString().replaceAll("><", ">\n<"));
        }
        System.out.println("RSS 生成成功：" + RSS_FILE);
    }

    // XML转义方法（不变）
    private static String escapeXml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // 新增类型字段
    private static class RssItem {
        String title;
        String content;
        Date pubDate;
        String link;
        String type; // 类型标识

        RssItem(String title, String content, Date pubDate, String link, String type) {
            this.title = title;
            this.content = content;
            this.pubDate = pubDate;
            this.link = link;
            this.type = type;
        }
    }
}