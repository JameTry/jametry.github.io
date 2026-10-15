
public class Main {

    public static void main(String[] args) throws Exception {
        String projectPath = System.getProperty("user.dir");

        // 扫描 html/post，按日期倒序重建首页的文章索引
        HtmlGenIndex.run(projectPath);

        // 按同一份日期重建 feed.xml
        RssGen.run(projectPath);

        // 压缩 style 和 js
        AssetMinifier.run(projectPath);

    }

}
