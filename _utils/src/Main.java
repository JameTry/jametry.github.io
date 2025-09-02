
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
