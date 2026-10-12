import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AssetMinifier {

    private static final String[] ASSET_DIRS = { "\\style", "\\js" };
    private static final Set<String> SKIP_DIRS = new HashSet<>(
            Arrays.asList("_utils", "out", ".git", ".idea"));
    private static final String MINI_SUFFIX = "_mini";
    private static final Pattern ASSET_NAME = Pattern.compile("\\.(css|js)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTR = Pattern.compile("(href|src)=\"([^\"]*)\"");

    public static void run(String projectPath) throws Exception {
        Map<String, String> names = new LinkedHashMap<>();

        for (String dir : ASSET_DIRS) {
            compressDir(projectPath + dir, names);
        }

        if (names.isEmpty()) {
            System.out.println("style 和 js 下没有可压缩的文件");
        }
    }

    private static void compressDir(String dirPath, Map<String, String> names) throws Exception {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String name = file.getName();
            if (!file.isFile() || !ASSET_NAME.matcher(name).find() || isMini(name)) {
                continue;
            }

            String mini = miniName(name);
            File target = new File(dir, mini);
            compress(file, target);
            names.put(name, mini);

            System.out.printf("压缩 %s -> %s（%d -> %d 字节）%n",
                    name, mini, file.length(), target.length());
        }
    }

    /**
     * 压缩单个文件。读写都锁死 UTF-8：YUI Compressor 默认跟随平台编码，
     * 在中文 Windows 上会把注释和字符串里的中文写成乱码。
     */
    private static void compress(File source, File target) throws Exception {
        boolean script = source.getName().toLowerCase().endsWith(".js");

        try (Reader reader = new InputStreamReader(new FileInputStream(source), StandardCharsets.UTF_8);
             Writer writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {

            if (script) {
                JavaScriptCompressor compressor = new JavaScriptCompressor(reader, REPORTER);
                compressor.compress(writer, -1, true, false, false, false);
            } else {
                CssCompressor compressor = new CssCompressor(reader);
                compressor.compress(writer, -1);
            }
        }
    }

    private static String miniName(String name) {
        int dot = name.lastIndexOf('.');
        return name.substring(0, dot) + MINI_SUFFIX + name.substring(dot);
    }

    private static boolean isMini(String name) {
        int dot = name.lastIndexOf('.');
        return name.substring(0, dot).endsWith(MINI_SUFFIX);
    }

    private static final ErrorReporter REPORTER = new ErrorReporter() {

        @Override
        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
            System.out.println("警告: " + message + "（" + sourceName + " 第 " + line + " 行）");
        }

        @Override
        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
            System.err.println("错误: " + message + "（" + sourceName + " 第 " + line + " 行）");
        }

        @Override
        public EvaluatorException runtimeError(String message, String sourceName, int line,
                                               String lineSource, int lineOffset) {
            return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
        }
    };
}
