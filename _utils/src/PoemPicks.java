import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 从 18r.html 里收集手工挑好的诗句（class="pick" 的段落），
 * 生成首页随机展示用的 JSON，写入 index.html 的 poem-picks 占位。
 *
 * 锚点用 site.js 里 initToc 运行时生成的 sec-N，序号与这里的顺序一致。
 */
public class PoemPicks {

    private static final String POEM_FILE = "\\html\\other\\18r.html";
    private static final String INDEX_FILE = "\\index.html";
    private static final Pattern PLACEHOLDER = Pattern.compile(
            "(<script id=\"poem-picks\" type=\"application/json\">)(.*?)(</script>)",
            Pattern.DOTALL);

    public static void run(String projectPath) throws Exception {
        String poemPath = projectPath + POEM_FILE;
        File poemFile = new File(poemPath);
        if (!poemFile.exists()) {
            System.out.println("找不到 " + poemPath + "，跳过诗句候选");
            return;
        }

        String html = new String(Files.readAllBytes(Paths.get(poemPath)), StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html, "UTF-8");
        Elements heads = doc.select("article.poems > h2");

        List<Item> items = new ArrayList<>();
        int index = 0;
        for (Element head : heads) {
            List<String> lines = new ArrayList<>();
            Element next = head.nextElementSibling();
            while (next != null && !"h2".equals(next.tagName())) {
                List<String> group = new ArrayList<>();
                if ("pk".equals(next.tagName())) {
                    for (Element p : next.select("p")) {
                        String text = p.text().trim();
                        if (!text.isEmpty()) {
                            group.add(text);
                        }
                    }
                } else if (next.hasAttr("pick")) {
                    String text = next.text().trim();
                    if (!text.isEmpty()) {
                        group.add(text);
                    }
                }
                if (!group.isEmpty()) {
                    items.add(new Item("sec-" + index, head.text().trim(), group));
                }
                next = next.nextElementSibling();
            }
            index++;
        }

        String indexPath = projectPath + INDEX_FILE;
        String indexHtml = new String(Files.readAllBytes(Paths.get(indexPath)), StandardCharsets.UTF_8);
        Matcher m = PLACEHOLDER.matcher(indexHtml);
        if (!m.find()) {
            System.out.println("index.html 里找不到 poem-picks 占位，跳过");
            return;
        }

        String json = toJson(items);
        String replaced = indexHtml.substring(0, m.start(2)) + json + indexHtml.substring(m.end(2));
        Files.write(Paths.get(indexPath), replaced.getBytes(StandardCharsets.UTF_8));

        System.out.println("诗句候选：" + items.size() + " 首");
    }

    private static String toJson(List<Item> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"anchor\":").append(quote(it.anchor))
                    .append(",\"title\":").append(quote(it.title))
                    .append(",\"lines\":[");
            for (int j = 0; j < it.lines.size(); j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append(quote(it.lines.get(j)));
            }
            sb.append("]}");
        }
        sb.append(']');
        return sb.toString();
    }

    private static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }

    private static class Item {
        final String anchor;
        final String title;
        final List<String> lines;

        Item(String anchor, String title, List<String> lines) {
            this.anchor = anchor;
            this.title = title;
            this.lines = lines;
        }
    }
}
