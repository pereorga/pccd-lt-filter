package pccd.ltfilter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.languagetool.JLanguageTool;
import org.languagetool.ResultCache;
import org.languagetool.UserConfig;
import org.languagetool.language.Catalan;

public class ltfilter {
    public ltfilter() {
    }

    static ResultCache cache = null;
    static UserConfig userConfig = new UserConfig(new ArrayList<String>(), new HashMap<String, Integer>());
    private static JLanguageTool langTool = new JLanguageTool(new Catalan(), cache, userConfig);

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.err.println("Usage: java -jar target/lt-filter-0.0.1-jar-with-dependencies.jar FILE.txt > OUTPUT.txt 2> EXCLUDED.txt");
            System.exit(1);
        }

        String inputFilename = args[0];

        langTool.disableRules(Arrays.asList(
            "EXIGEIX_VERBS_CENTRAL",
            "EXIGEIX_ACCENTUACIO_GENERAL",
            "EXIGEIX_POSSESSIUS_V",
            "EVITA_PRONOMS_VALENCIANS",
            "EVITA_DEMOSTRATIUS_EIXE",
            "VOCABULARI_VALENCIA",
            "EXIGEIX_US",
            "SER_ESSER",
            "WHITESPACE_RULE",
            "CA_UNPAIRED_BRACKETS",
            "ESPAIS_SOBRANTS",
            "MAJ_DESPRES_INTERROGANT",
            "UPPERCASE_SENTENCE_START"
        ));

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilename))) {
            String line;
            line = br.readLine();
            while (line != null) {
                String str = line.trim();
                if (langTool.check(str).isEmpty()) {
                    System.out.println(str);
                } else {
                    System.err.println(str);
                }
                line = br.readLine();
            }
        }
    }
}
