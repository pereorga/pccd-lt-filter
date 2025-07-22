package pccd.ltfilter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

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
    private static boolean outputRuleNames = false;
    private static int port = 0;

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

            if (port > 0) {
                startServer(port);
                return;
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
                case "--port":
                    if (i + 1 >= args.length) {
                        System.err.println("Error: --port requires a port number.");
                        errorInArgs = true;
                        return null;
                    }
                    try {
                        port = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.err.println("Error: Invalid port number.");
                        errorInArgs = true;
                        return null;
                    }
                    break;
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
                case "-r":
                case "--rule-names":
                    outputRuleNames = true;
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
        System.err.println("    lt-filter --port <port>");
        System.err.println();
        System.err.println("ARGUMENTS:");
        System.err.println("    <input-file>   Input text file (one sentence per line).");
        System.err.println(
            "                   If no file is specified, reads from standard input."
        );
        System.err.println("    -              Explicitly read from standard input (stdin).");
        System.err.println();
        System.err.println("OPTIONS:");
        System.err.println(
            "    --port <port>              Run as a REST server on the specified port"
        );
        System.err.println("    -c, --correct              Output correct sentences to stdout");
        System.err.println("    -f, --flagged              Output flagged sentences to stdout");
        System.err.println(
            "    -d, --disable-rules RULES  Comma-separated list of additional rules to disable"
        );
        System.err.println(
            "    --disable-rules-replace RULES  Comma-separated list of rules to disable"
        );
        System.err.println("                               (replaces default disabled rules)");
        System.err.println(
            "    -r, --rule-names           Include rule names after flagged sentences"
        );
        System.err.println("    -h, --help                 Show this help message");
        System.err.println("    -v, --version              Show version information");
        System.err.println();
        System.err.println("DEFAULT BEHAVIOR (CLI):");
        System.err.println("    When no options are specified:");
        System.err.println("    - Correct sentences are sent to stdout");
        System.err.println("    - Flagged sentences are sent to stderr");
        System.err.println();
        System.err.println("SERVER MODE:");
        System.err.println("    When --port is used, the application starts a REST server.");
        System.err.println("    Make POST requests to '/' with the text in the body.");
        System.err.println("    Options can be passed as query parameters, e.g.:");
        System.err.println(
            "    curl -d \"Text a verificar\" \"http://localhost:8080/?rule-names=true&disable-rules=RULE1\""
        );
        System.err.println(
            "    The response is a JSON object: { \"correct\": [\"...\"], \"flagged\": [ { \"sentence\": \"...\", \"rules\": [\"...\"] } ] }"
        );
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
                    line = reader.readLine();
                    continue;
                }
                List<RuleMatch> matches = langTool.check(line);
                boolean isCorrect = matches.isEmpty();
                printResult(line, isCorrect, matches);
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
                    line = reader.readLine();
                    continue;
                }
                List<RuleMatch> matches = langTool.check(line);
                boolean isCorrect = matches.isEmpty();
                printResult(line, isCorrect, matches);
                line = reader.readLine();
            }
        }
    }

    private static void printResult(String line, boolean isCorrect, List<RuleMatch> matches) {
        if (optionsProvided) {
            if (isCorrect && outputCorrectSentences) {
                System.out.println(line);
            } else if (!isCorrect && outputFlaggedSentences) {
                printLineWithRules(line, matches, System.out);
            }
        } else {
            if (isCorrect) {
                System.out.println(line);
            } else {
                printLineWithRules(line, matches, System.err);
            }
        }
    }

    private static void printLineWithRules(
        String line,
        List<RuleMatch> matches,
        java.io.PrintStream out
    ) {
        if (outputRuleNames && !matches.isEmpty()) {
            StringBuilder ruleNames = new StringBuilder();
            for (int i = 0; i < matches.size(); i++) {
                if (i > 0) {
                    ruleNames.append(", ");
                }
                ruleNames.append(matches.get(i).getRule().getId());
            }
            out.println(line + " [" + ruleNames.toString() + "]");
        } else {
            out.println(line);
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

    private static void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RestHandler());
        server.setExecutor(null);
        server.start();
        System.err.println("Server started on port " + port);
    }

    static class RestHandler implements HttpHandler {

        private static class FlaggedSentence {

            String sentence;
            List<String> rules;

            FlaggedSentence(String sentence, List<String> rules) {
                this.sentence = sentence;
                this.rules = rules;
            }
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            boolean reqOutputRuleNames = Boolean.parseBoolean(params.get("rule-names"));
            List<String> reqDisabledRules = new ArrayList<>();
            boolean reqAppendToDefaultRules = true;

            if (params.containsKey("disable-rules-replace")) {
                reqAppendToDefaultRules = false;
                reqDisabledRules.addAll(
                    Arrays.asList(params.get("disable-rules-replace").split(","))
                );
            } else if (params.containsKey("disable-rules")) {
                reqDisabledRules.addAll(Arrays.asList(params.get("disable-rules").split(",")));
            }

            List<String> effectiveRules = new ArrayList<>();
            if (reqAppendToDefaultRules) {
                effectiveRules.addAll(DEFAULT_DISABLED_RULES);
            }
            effectiveRules.addAll(reqDisabledRules);

            JLanguageTool langTool = new JLanguageTool(
                Languages.getLanguageForShortCode(DEFAULT_LANGUAGE)
            );
            langTool.disableRules(effectiveRules);

            String body;
            try (
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)
                );
            ) {
                body = reader.lines().collect(Collectors.joining("\n"));
            }

            List<String> correctSentences = new ArrayList<>();
            List<FlaggedSentence> flaggedSentences = new ArrayList<>();

            for (String line : body.split("\\r?\\n")) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<RuleMatch> matches = langTool.check(line);
                if (matches.isEmpty()) {
                    correctSentences.add(line);
                } else {
                    List<String> ruleNames = new ArrayList<>();
                    if (reqOutputRuleNames) {
                        for (RuleMatch match : matches) {
                            ruleNames.add(match.getRule().getId());
                        }
                    }
                    flaggedSentences.add(new FlaggedSentence(line, ruleNames));
                }
            }

            String jsonResponse = buildJsonResponse(
                correctSentences,
                flaggedSentences,
                reqOutputRuleNames
            );
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            sendResponse(exchange, 200, jsonResponse);
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> result = new HashMap<>();
            if (query == null) {
                return result;
            }
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                try {
                    if (entry.length > 1) {
                        result.put(
                            URLDecoder.decode(entry[0], "UTF-8"),
                            URLDecoder.decode(entry[1], "UTF-8")
                        );
                    } else {
                        result.put(URLDecoder.decode(entry[0], "UTF-8"), "");
                    }
                } catch (java.io.UnsupportedEncodingException e) {
                    // Should not happen with UTF-8
                }
            }
            return result;
        }

        private String buildJsonResponse(
            List<String> correct,
            List<FlaggedSentence> flagged,
            boolean outputRuleNames
        ) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"correct\": [\n");
            for (int i = 0; i < correct.size(); i++) {
                sb.append("    \"").append(escapeJson(correct.get(i))).append("\"");
                if (i < correct.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("  ],\n");
            sb.append("  \"flagged\": [\n");
            for (int i = 0; i < flagged.size(); i++) {
                FlaggedSentence flaggedSentence = flagged.get(i);
                sb.append("    {\n");
                sb
                    .append("      \"sentence\": \"")
                    .append(escapeJson(flaggedSentence.sentence))
                    .append("\"");
                if (outputRuleNames) {
                    sb.append(",\n");
                    sb.append("      \"rules\": [");
                    for (int j = 0; j < flaggedSentence.rules.size(); j++) {
                        sb
                            .append("\"")
                            .append(escapeJson(flaggedSentence.rules.get(j)))
                            .append("\"");
                        if (j < flaggedSentence.rules.size() - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append("]\n");
                } else {
                    sb.append("\n");
                }
                sb.append("    }");
                if (i < flagged.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("  ]\n");
            sb.append("}\n");
            return sb.toString();
        }

        private String escapeJson(String text) {
            return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response)
            throws IOException {
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}
