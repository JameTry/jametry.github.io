/**
 * @author : Jame
 * @date : 2025/4/21 下午 1:26
 */
public class Main {


    public static void main(String[] args) throws Exception {
        String projectPath = System.getProperty("user.dir");
//        HtmlDateSort.run(projectPath);
        HtmlGenIndex.run(projectPath);
//        JsonToHtmlConverter.run(projectPath);
        ReplacePrevAndNextHref.run(projectPath);
        RssGenerator.run(projectPath);
    }



}
