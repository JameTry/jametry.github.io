import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 扫描 html/post 下的所有文章，按日期倒序重建首页的 ul.index。
 *
 * 日期和标题都取自文章页面自身，不再依赖文件名顺序：
 *
 *   <header class="post-head">
 *       <time datetime="2024-04-02">2024.04.02</time>
 *       <h1>如果可以的话</h1>
 *   </header>
 *
 * 只替换 index.html 中 ul.index 的内部，该文件的其余部分（包括 intro、
 * 侧栏、脚本）原样保留，不会被 jsoup 重新格式化。
 *
 * 用法：在项目根目录下执行，项目路径取 user.dir。
 */
public class HtmlGenIndex {

    private static final String INDEX_FILE = "\\index.html";
    private static final String POST_DIR = "\\html\\post";
    private static final String LIST_OPEN = "<ul class=\"index\">";
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static void main(String[] args) throws Exception {
        run(System.getProperty("user.dir"));
    }

    public static void run(String projectPath) throws Exception {
        List<Post> posts = collect(projectPath + POST_DIR);
        if (posts.isEmpty()) {
            System.out.println("html/post 下没有可用的文章");
            return;
        }

        // 新的在前；同一天的保持文件读取顺序（List.sort 是稳定排序）
        posts.sort(Comparator.comparing((Post p) -> p.date).reversed());

        String indexPath = projectPath + INDEX_FILE;
        String html = new String(Files.readAllBytes(Paths.get(indexPath)), StandardCharsets.UTF_8);

        int open = html.indexOf(LIST_OPEN);
        if (open < 0) {
            throw new IllegalStateException("index.html 里找不到 " + LIST_OPEN);
        }
        int bodyStart = open + LIST_OPEN.length();
        int close = html.indexOf("</ul>", bodyStart);
        if (close < 0) {
            throw new IllegalStateException("ul.index 没有闭合标签");
        }

        String result = html.substring(0, bodyStart) + "\n" + render(posts) + html.substring(close);
        Files.write(Paths.get(indexPath), result.getBytes(StandardCharsets.UTF_8));

        System.out.println("已写入 " + posts.size() + " 条");
        System.out.println("  最新: " + posts.get(0).date + "  " + posts.get(0).title);
        System.out.println("  最早: " + posts.get(posts.size() - 1).date + "  " + posts.get(posts.size() - 1).title);
    }

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

                posts.add(new Post(
                        file.getName().replace(".html", ""),
                        LocalDate.parse(iso),
                        h1.text().trim()));
            } catch (Exception e) {
                System.out.println("跳过 " + file.getName() + "：" + e.getMessage());
            }
        }
        return posts;
    }

    private static String render(List<Post> posts) {
        StringBuilder sb = new StringBuilder();
        for (Post p : posts) {
            sb.append("                <li>\n")
                    .append("                    <a href=\"/html/post/").append(p.id).append("\">\n")
                    .append("                        <time datetime=\"").append(p.date).append("\">")
                    .append(p.date.format(DISPLAY)).append("</time>\n")
                    .append("                        <span>").append(escape(p.title)).append("</span>\n")
                    .append("                    </a>\n")
                    .append("                </li>\n");
        }
        return sb.toString();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
