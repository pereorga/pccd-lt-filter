package pccd.ltfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;

public class ltfilter {

    private static final String DEFAULT_LANGUAGE = "ca";
    private static final List<String> DISABLED_RULES = Arrays.asList(
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

    public static void main(String[] args) {
        try {
            String inputFilename = parseArguments(args);

            if (inputFilename == null) {
                printUsage();
                System.exit(1);
            }

            processFile(inputFilename);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(2);
        } catch (RuntimeException e) {
            System.err.println("Language processing error: " + e.getMessage());
            System.exit(3);
        }
    }

    private static String parseArguments(String[] args) {
        String inputFilename = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-c":
                case "--correct":
                    if (outputFlaggedSentences) {
                        System.err.println("Error: Cannot use both --correct and --flagged options together");
                        return null;
                    }
                    outputCorrectSentences = true;
                    optionsProvided = true;
                    break;
                case "-f":
                case "--flagged":
                    if (outputCorrectSentences) {
                        System.err.println("Error: Cannot use both --correct and --flagged options together");
                        return null;
                    }
                    outputFlaggedSentences = true;
                    optionsProvided = true;
                    break;
                case "-h":
                case "--help":
                    return null;
                default:
                    if (args[i].startsWith("-")) {
                        System.err.println("Unknown option: " + args[i]);
                        return null;
                    } else {
                        if (inputFilename != null) {
                            System.err.println("Error: Multiple input files specified");
                            return null;
                        }
                        inputFilename = args[i];
                    }
                    break;
            }
        }

        if (inputFilename == null) {
            System.err.println("Error: No input file specified");
            return null;
        }

        return inputFilename;
    }

    private static void printUsage() {
        System.err.println("ltfilter - LanguageTool based sentence filter for Catalan text");
        System.err.println();
        System.err.println("USAGE:");
        System.err.println("    ltfilter [OPTIONS] <input-file>");
        System.err.println();
        System.err.println("ARGUMENTS:");
        System.err.println("    <input-file>    Input text file to process");
        System.err.println();
        System.err.println("OPTIONS:");
        System.err.println("    -c, --correct     Output correct sentences to stdout");
        System.err.println("    -f, --flagged     Output flagged sentences to stdout");
        System.err.println("    -h, --help        Show this help message");
        System.err.println();
        System.err.println("DEFAULT BEHAVIOR:");
        System.err.println("    When no options are specified:");
        System.err.println("    - Correct sentences are sent to stdout");
        System.err.println("    - Flagged sentences are sent to stderr");
        System.err.println();
        System.err.println("    When options are specified:");
        System.err.println("    - Only explicitly requested output is sent to stdout");
        System.err.println();
        System.err.println("EXAMPLES:");
        System.err.println("    ltfilter input.txt > correct.txt 2> flagged.txt");
        System.err.println("    ltfilter --correct input.txt > correct.txt");
        System.err.println("    ltfilter --flagged input.txt > flagged.txt");
    }

    private static void processFile(String inputFilename) throws IOException {
        // Input validation
        File inputFile = new File(inputFilename);
        if (!inputFile.exists()) {
            throw new IOException("File not found: " + inputFilename);
        }
        if (!inputFile.canRead()) {
            throw new IOException("Cannot read file: " + inputFilename);
        }
        if (inputFile.isDirectory()) {
            throw new IOException("Input is a directory, not a file: " + inputFilename);
        }

        // Initialize LanguageTool once
        JLanguageTool languageTool = null;
        languageTool = new JLanguageTool(Languages.getLanguageForShortCode(DEFAULT_LANGUAGE));
        languageTool.disableRules(DISABLED_RULES);

        // Process file
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))) {
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                String trimmedSentence = currentLine.trim();

                if (trimmedSentence.isEmpty()) {
                    continue;
                }

                boolean isCorrectSentence;
                try {
                    isCorrectSentence = languageTool.check(trimmedSentence).isEmpty();
                } catch (Exception e) {
                    // If sentence checking fails, treat as flagged
                    System.err.println("Warning: Failed to check sentence, treating as flagged: " + e.getMessage());
                    isCorrectSentence = false;
                }

                if (optionsProvided) {
                    // When options are provided, only output what was explicitly requested
                    if (isCorrectSentence && outputCorrectSentences) {
                        System.out.println(trimmedSentence);
                    } else if (!isCorrectSentence && outputFlaggedSentences) {
                        System.out.println(trimmedSentence);
                    }
                } else {
                    // Default behavior: correct to stdout, flagged to stderr
                    if (isCorrectSentence) {
                        System.out.println(trimmedSentence);
                    } else {
                        System.err.println(trimmedSentence);
                    }
                }
            }
        }
    }
}
