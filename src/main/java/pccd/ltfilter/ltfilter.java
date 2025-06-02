package pccd.ltfilter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.languagetool.JLanguageTool;
import org.languagetool.ResultCache;
import org.languagetool.UserConfig;
import org.languagetool.Languages;

public class ltfilter {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java -jar target/lt-filter-jar-with-dependencies.jar FILE.txt > OUTPUT.txt 2> EXCLUDED.txt");
            System.exit(1);
        }

        List<String> userList = new ArrayList<>();
        Map<String, Object[]> userMap = new HashMap<>();
        UserConfig userConfig = new UserConfig(userList, userMap);

        JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortCode("ca"));

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

        String inputFilename = args[0];
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
