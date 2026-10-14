import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RssGen {

    private static final String POST_DIR = "\\html\\post";
    private static final String INDEX_FILE = "\\index.html";
    private static final String CNAME_FILE = "\\CNAME";
    private static final String OUTPUT_FILE = "\\feed.xml";
    private static final String POST_PATH_PREFIX = "/html/post/";

    private static final String FALLBACK_SITE = "https://jame.work";
    private static final String FALLBACK_TITLE = "Jame";
    private static final String CHANNEL_DESCRIPTION = "随笔";

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final int PUB_HOUR = 8;
    private static final DateTimeFormatter RFC822 =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    private static final Pattern TITLE_TAG = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL);



    public static void run(String projectPath) throws Exception {
        List<Post> posts = collect(projectPath + POST_DIR);
        if (posts.isEmpty()) {
            System.out.println("html/post 下没有可用的文章，未生成 feed.xml");
            return;
        }

        // 新的在前，和首页索引保持一致
        posts.sort(Comparator.comparing((Post p) -> p.date).reversed());

        String site = siteUrl(projectPath);
        String title = firstGroup(TITLE_TAG, readIfExists(projectPath + INDEX_FILE), FALLBACK_TITLE);

        String xml = render(site, title, posts);
        Files.write(Paths.get(projectPath + OUTPUT_FILE), xml.getBytes(StandardCharsets.UTF_8));

        System.out.println("已写入 feed.xml：" + posts.size() + " 条");
        System.out.println("  最新: " + posts.get(0).date + "  " + posts.get(0).title);
        System.out.println("  最旧: " + posts.get(posts.size() - 1).date + "  " + posts.get(posts.size() - 1).title);
    }

    /**
     * 只读地扫一遍文章页。这里刻意不碰文章文件本身，免得生成 feed 顺带改了别的东西。
     */
    private static List<Post> collect(String dir) {
        File folder = new File(dir);
        File[] files = folder.listFiles((d, name) -> name.endsWith(".html"));
        List<Post> posts = new ArrayList<>();
        if (files == null) {
            return posts;
        }

        for (File file : files) {
            try {
                Document doc = Jsoup.parse(file, "UTF-8");
                Element time = doc.selectFirst("article .post-head time");
                Element h1 = doc.selectFirst("article .post-head h1");
                if (time == null || h1 == null) {
                    System.out.println("跳过 " + file.getName() + "：缺少 time 或 h1");
                    continue;
                }
                String iso = time.attr("datetime").trim();
                if (iso.isEmpty()) {
                    System.out.println("跳过 " + file.getName() + "：time 上没有 datetime");
                    continue;
                }

                posts.add(new Post(file.getName().replace(".html", ""), LocalDate.parse(iso), h1.text().trim()));
            } catch (Exception e) {
                System.out.println("跳过 " + file.getName() + "：" + e.getMessage());
            }
        }
        return posts;
    }

    private static String render(String site, String title, List<Post> posts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        sb.append("  <channel>\n");
        sb.append("    <title>").append(escape(title)).append("</title>\n");
        sb.append("    <link>").append(escape(site)).append("/</link>\n");
        sb.append("    <description>").append(escape(CHANNEL_DESCRIPTION)).append("</description>\n");
        sb.append("    <language>zh-CN</language>\n");
        sb.append("    <lastBuildDate>").append(RFC822.format(ZonedDateTime.now(ZONE))).append("</lastBuildDate>\n");
        sb.append("    <atom:link href=\"").append(escape(site))
                .append("/feed.xml\" rel=\"self\" type=\"application/rss+xml\"/>\n");

        for (Post p : posts) {
            String link = site + POST_PATH_PREFIX + p.id;
            sb.append("    <item>\n");
            sb.append("      <title>").append(escape(p.title)).append("</title>\n");
            sb.append("      <link>").append(escape(link)).append("</link>\n");
            sb.append("      <guid isPermaLink=\"true\">").append(escape(link)).append("</guid>\n");
            sb.append("      <pubDate>").append(RFC822.format(p.date.atTime(PUB_HOUR, 0).atZone(ZONE)))
                    .append("</pubDate>\n");
            sb.append("    </item>\n");
        }

        sb.append("  </channel>\n");
        sb.append("</rss>\n");
        return sb.toString();
    }

    private static String siteUrl(String projectPath) {
        String cname = readIfExists(projectPath + CNAME_FILE).trim();
        if (cname.isEmpty()) {
            return FALLBACK_SITE;
        }
        // CNAME 里可能被写成带协议或带结尾斜杠，统一成 https://host
        String host = cname.replaceFirst("(?i)^https?://", "").replaceAll("/+$", "");
        return "https://" + host;
    }

    private static String readIfExists(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private static String firstGroup(Pattern pattern, String html, String fallback) {
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? matcher.group(1).trim() : fallback;
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static class Post {
        final String id;
        final LocalDate date;
        final String title;

        Post(String id, LocalDate date, String title) {
            this.id = id;
            this.date = date;
            this.title = title;
        }
    }
}
