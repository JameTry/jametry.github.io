import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import com.alibaba.fastjson.JSONObject;

import javax.swing.text.html.HTML;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;


public class RssGenerator {
    private static String POSTS_DIR = "\\html\\post";
    private static String OTHER_DIR = "\\html\\other";

    private static final String SITE_URL = "https://jame.work/";
    private static final String RSS_FILE = "feed.xml";
    static int count = 0;
    private static final SimpleDateFormat INPUT_DATE = new SimpleDateFormat("yyyy年MM月dd日");

    static {
        INPUT_DATE.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    private static final SimpleDateFormat OUTPUT_DATE = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss +0800", Locale.US);


    public static List<RssItem> readContent(String path, boolean isPost) throws Exception {

        File htmlDir = new File(path);
        List<RssItem> items = new ArrayList<>();
        for (File file : htmlDir.listFiles((d, name) -> name.endsWith(".html"))) {
            if (file.getName().equals("0.html")) {
                continue;
            }
            if (!isPost && !file.getName().contains("r")) {
                continue;
            }
            Document doc = Jsoup.parse(file, "UTF-8");
            String title = escapeXml(doc.select("p").get(1).text().trim());
            title = HtmlGenIndex.getTitleExcerpt(title);
            Elements paragraphs0 = doc.select("h1");
            StringBuilder content = new StringBuilder();
            for (Element element : paragraphs0) {
                content.append("<h1>").append(element.text()).append("</h1>");
                title = element.text();
            }

            Element article = doc.select("article").get(0);
            for (Element child : article.children()) {
                String name = child.tag().getName();
                if (name.equals("p")) {
                    content.append("<p>").append(child.text()).append("</p>");
                    count += child.text().length();
                } else if (name.equals("br")) {
                    content.append("<br/>");
                } else {
                    content.append("<").append(name).append(">").append(child.text()).append("</").append(name).append(">");
                    count += child.text().length();
                }


            }

            String timeStr = null;
            try {
                timeStr = doc.select("p.top-op span").get(1).text();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Date pubDate = INPUT_DATE.parse(timeStr);
            String link = SITE_URL + (isPost ? "html/post/" : "html/other/") + file.getName().replace(".html", "");

            items.add(new RssItem(title, content.toString(), pubDate, link));
        }
        return items;
    }


    public static void run(String projectPath) throws Exception {
        POSTS_DIR = projectPath + POSTS_DIR;
        OTHER_DIR = projectPath + OTHER_DIR;
        List<RssItem> items = new ArrayList<>();

        items.addAll(readContent(POSTS_DIR, true));
        items.addAll(readContent(OTHER_DIR, false));


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
                    .append("<author>Jame!</author>\n")
                    .append("</item>\n");
        }

        rss.append("</channel>\n</rss>");

        try (FileWriter writer = new FileWriter(RSS_FILE)) {
            writer.write(rss.toString().replaceAll("><", ">\n<"));
        }
        System.out.println("RSS 生成成功：" + RSS_FILE);
        System.out.println("字数：" + count);
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

        RssItem(String title, String content, Date pubDate, String link) {
            this.title = title;
            this.content = content;
            this.pubDate = pubDate;
            this.link = link;
        }
    }
}