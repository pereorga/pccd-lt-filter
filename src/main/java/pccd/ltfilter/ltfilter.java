package pccd.ltfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;

public class ltfilter {

    private static final String DEFAULT_LANGUAGE = "ca";
    private static final List<String> DEFAULT_DISABLED_RULES = Arrays.asList(
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
    );

    private static boolean outputCorrectSentences = false;
    private static boolean outputFlaggedSentences = false;
    private static boolean optionsProvided = false;
    private static boolean exitAfterParsing = false;
    private static boolean errorInArgs = false;
    private static List<String> disabledRules = new ArrayList<>();
    private static boolean appendToDefaultRules = true;

    private static String getVersion() {
        String path = "/META-INF/maven/pccd/lt-filter/pom.properties";
        try (InputStream stream = ltfilter.class.getResourceAsStream(path)) {
            if (stream != null) {
                Properties props = new Properties();
                props.load(stream);
                return props.getProperty("version", "unknown");
            }
        } catch (IOException e) {
            return "unknown";
        }
        return "unknown";
    }

    public static void main(String[] args) {
        try {
            String inputFilename = parseArguments(args);

            if (errorInArgs) {
                System.exit(1);
            }

            // For --version and --help options
            if (exitAfterParsing) {
                System.exit(0);
            }

            if (inputFilename == null || inputFilename.equals("-")) {
                processStdin();
            } else {
                processFile(inputFilename);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(2);
        }
    }

    private static String parseArguments(String[] args) {
        String inputFilename = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-c":
                case "--correct":
                    if (outputFlaggedSentences) {
                        System.err.println(
                            "Error: Cannot use both --correct and --flagged options together."
                        );
                        errorInArgs = true;
                        return null;
                    }
                    outputCorrectSentences = true;
                    optionsProvided = true;
                    break;
                case "-f":
                case "--flagged":
                    if (outputCorrectSentences) {
                        System.err.println(
                            "Error: Cannot use both --correct and --flagged options together."
                        );
                        errorInArgs = true;
                        return null;
                    }
                    outputFlaggedSentences = true;
                    optionsProvided = true;
                    break;
                case "-h":
                case "--help":
                    printUsage();
                    exitAfterParsing = true;
                    return null;
                case "-v":
                case "--version":
                    System.out.println("lt-filter version " + getVersion());
                    exitAfterParsing = true;
                    return null;
                case "-d":
                case "--disable-rules":
                    if (i + 1 >= args.length) {
                        System.err.println(
                            "Error: --disable-rules requires a comma-separated list of rules."
                        );
                        errorInArgs = true;
                        return null;
                    }
                    i++;
                    String[] rules = args[i].split(",");
                    for (String rule : rules) {
                        disabledRules.add(rule.trim());
                    }
                    break;
                case "--disable-rules-replace":
                    if (i + 1 >= args.length) {
                        System.err.println(
                            "Error: --disable-rules-replace requires a comma-separated list of rules."
                        );
                        errorInArgs = true;
                        return null;
                    }
                    i++;
                    appendToDefaultRules = false;
                    String[] replaceRules = args[i].split(",");
                    for (String rule : replaceRules) {
                        disabledRules.add(rule.trim());
                    }
                    break;
                default:
                    if (args[i].startsWith("-") && !args[i].equals("-")) {
                        System.err.println("Unknown option: " + args[i]);
                        printUsage();
                        errorInArgs = true;
                        return null;
                    } else {
                        if (inputFilename != null) {
                            System.err.println(
                                "Error: Multiple input sources specified. Only one is allowed."
                            );
                            errorInArgs = true;
                            return null;
                        }
                        inputFilename = args[i];
                    }
                    break;
            }
        }

        return inputFilename;
    }

    private static void printUsage() {
        System.err.println("lt-filter - LanguageTool based sentence filter for Catalan text");
        System.err.println();
        System.err.println("USAGE:");
        System.err.println("    lt-filter [OPTIONS] [<input-file> | -]");
        System.err.println();
        System.err.println("ARGUMENTS:");
        System.err.println("    <input-file>   Input text file (one sentence per line).");
        System.err.println(
            "                   If no file is specified, reads from standard input."
        );
        System.err.println("    -              Explicitly read from standard input (stdin).");
        System.err.println();
        System.err.println("OPTIONS:");
        System.err.println("    -c, --correct              Output correct sentences to stdout");
        System.err.println("    -f, --flagged              Output flagged sentences to stdout");
        System.err.println(
            "    -d, --disable-rules RULES  Comma-separated list of additional rules to disable"
        );
        System.err.println("                               (appends to default disabled rules)");
        System.err.println(
            "    --disable-rules-replace RULES  Comma-separated list of rules to disable"
        );
        System.err.println("                               (replaces default disabled rules)");
        System.err.println("    -h, --help                 Show this help message");
        System.err.println("    -v, --version              Show version information");
        System.err.println();
        System.err.println("DEFAULT BEHAVIOR:");
        System.err.println("    When no options are specified:");
        System.err.println("    - Correct sentences are sent to stdout");
        System.err.println("    - Flagged sentences are sent to stderr");
    }

    private static void processStdin() throws IOException {
        JLanguageTool langTool = new JLanguageTool(
            Languages.getLanguageForShortCode(DEFAULT_LANGUAGE)
        );
        langTool.disableRules(getEffectiveDisabledRules());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line = reader.readLine();
            while (line != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                boolean isCorrect = langTool.check(line).isEmpty();
                printResult(line, isCorrect);
                line = reader.readLine();
            }
        }
    }

    private static void processFile(String inputFilename) throws IOException {
        File inputFile = new File(inputFilename);
        if (!inputFile.exists() || !inputFile.canRead() || inputFile.isDirectory()) {
            throw new IOException(
                "File not found, is a directory, or cannot be read: " + inputFilename
            );
        }

        JLanguageTool langTool = new JLanguageTool(
            Languages.getLanguageForShortCode(DEFAULT_LANGUAGE)
        );
        langTool.disableRules(getEffectiveDisabledRules());

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line = reader.readLine();
            while (line != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                boolean isCorrect = langTool.check(line).isEmpty();
                printResult(line, isCorrect);
                line = reader.readLine();
            }
        }
    }

    private static void printResult(String line, boolean isCorrect) {
        if (optionsProvided) {
            if (isCorrect && outputCorrectSentences) {
                System.out.println(line);
            } else if (!isCorrect && outputFlaggedSentences) {
                System.out.println(line);
            }
        } else {
            if (isCorrect) {
                System.out.println(line);
            } else {
                System.err.println(line);
            }
        }
    }

    private static List<String> getEffectiveDisabledRules() {
        List<String> effectiveRules = new ArrayList<>();

        if (appendToDefaultRules) {
            effectiveRules.addAll(DEFAULT_DISABLED_RULES);
        }

        effectiveRules.addAll(disabledRules);

        return effectiveRules;
    }
}
