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
    // 配置常量D:\project\mytext\jametry.github.io\easy-talk
    private static String POSTS_DIR = "\\html\\post";
    private static String STATUSES_DIR = "\\easy-talk\\talks";
    private static final String SITE_URL = "https://jame.work/";
    private static final String RSS_FILE = "feed.xml";

    private static final SimpleDateFormat INPUT_DATE = new SimpleDateFormat("yyyy年MM月dd日");
    static {
        INPUT_DATE.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }
    private static final SimpleDateFormat OUTPUT_DATE = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss +0800", Locale.US);
    private static final String TYPE_ARTICLE = "随笔";

    public static void run(String projectPath) throws Exception {
        POSTS_DIR = projectPath + POSTS_DIR;
        STATUSES_DIR = projectPath + STATUSES_DIR;
        List<RssItem> items = new ArrayList<>();

        File htmlDir = new File(POSTS_DIR);
        for (File file : htmlDir.listFiles((d, name) -> name.endsWith(".html"))) {
            if (file.getName().equals("0.html")) {
                continue;
            }
            Document doc = Jsoup.parse(file, "UTF-8");
            String title = escapeXml(doc.select("p:first-of-type").text().trim());
            title = title.length() > 20 ? title.substring(0, 20) + "..." : title;
            Elements paragraphs0 = doc.select("h2");
            StringBuilder content = new StringBuilder();
            for (Element element : paragraphs0) {
                content.append("<h2>").append(element.text()).append("</h2>");
                title = element.text();
            }
            Elements paragraphs = doc.select("p");
            for (Element p : paragraphs) {
                content.append("<p>").append(p.text()).append("</p>");
            }

            String timeStr = doc.select("div.time").text().split(" ")[0];
            Date pubDate = INPUT_DATE.parse(timeStr);
            String link = SITE_URL + "html/post/" + file.getName().replace(".html", "");

            items.add(new RssItem(title, content.toString(), pubDate, link, TYPE_ARTICLE));
        }

        File jsonDir = new File(STATUSES_DIR);
//        for (File file : jsonDir.listFiles((d, name) -> name.endsWith(".json"))) {
//            String jsonStr = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
//            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
//
//            String rawContent = jsonObject.getString("content");
//            String plainText = Jsoup.parse(rawContent).text();
//
//            String title = plainText.length() > 20 ? plainText.substring(0, 20) + "..." : plainText;
//            title = escapeXml(title);
//
//            Date pubDate = INPUT_DATE.parse(jsonObject.getString("publishedTime"));
//            String link = SITE_URL + "html/talks/" +  file.getName().replace(".json", "");
//
//            items.add(new RssItem(title, "<p>" + rawContent + "</p>", pubDate, link, TYPE_STATUS));
//        }

        items.sort((a, b) -> b.pubDate.compareTo(a.pubDate));

        StringBuilder rss = new StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rss version=\"2.0\">\n<channel>\n")
                .append("<title>Jame</title>\n<link>").append(SITE_URL).append("</link>\n")
                .append("<description>随笔</description>\n")

                .append("<lastBuildDate>")
                .append(OUTPUT_DATE.format(new Date()))
                .append("</lastBuildDate>\n");
        for (RssItem item : items) {
            rss.append("<item>\n")
                    .append("<title>").append(item.title).append("</title>\n")
                    .append("<link>").append(item.link).append("</link>\n")
                    .append("<description><![CDATA[").append(item.content).append("]]></description>\n")
                    .append("<pubDate>").append(OUTPUT_DATE.format(item.pubDate)).append("</pubDate>\n")
                    .append("<guid>").append(item.link).append("</guid>\n")
                    .append("<category>").append(item.type).append("</category>\n")
                    .append("</item>\n");
        }

        rss.append("</channel>\n</rss>");

        try (FileWriter writer = new FileWriter(RSS_FILE)) {
            writer.write(rss.toString().replaceAll("><", ">\n<"));
        }
        System.out.println("RSS 生成成功：" + RSS_FILE);
    }

    private static String escapeXml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

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