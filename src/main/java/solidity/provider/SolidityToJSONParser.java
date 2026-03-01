package solidity.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import oeg.upm.solidityantlr.SolidityLexer;
import oeg.upm.solidityantlr.SolidityParser;

public class SolidityToJSONParser {

	private static final Gson PRETTY_PRINT_GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Gson GSON = new Gson();
	public static JsonObject contractJSON;

	public static String toJson(ParseTree tree) {
		return PRETTY_PRINT_GSON.toJson(toMap(tree));
	}

	public static Map<String, Object> toMap(ParseTree tree) {
		Map<String, Object> map = new LinkedHashMap<>();
		traverse(tree, map);
		return map;
	}

	public static void traverse(ParseTree tree, Map<String, Object> map) {
		if (tree instanceof TerminalNodeImpl) {
			Token token = ((TerminalNodeImpl) tree).getSymbol();
//			map.put("type", token.getType());
			map.put("text", token.getText());
		}
		else {
			List<Map<String, Object>> children = new ArrayList<>();
			String name = tree.getClass().getSimpleName().replaceAll("Context$", "");
			map.put(Character.toLowerCase(name.charAt(0)) + name.substring(1), children);

			for (int i = 0; i < tree.getChildCount(); i++) {
				Map<String, Object> nested = new LinkedHashMap<>();
				children.add(nested);
				traverse(tree.getChild(i), nested);
			}
		}
	}

	/**
	 * Preprocesses Solidity source to strip top-level constructs that the
	 * ANTLR grammar does not support (Solidity 0.8+ features: user-defined
	 * types, custom errors, file-level using/function/constant declarations).
	 */
	public static String preprocessSolidity(String source) {
		String[] lines = source.split("\\r?\\n", -1);
		StringBuilder result = new StringBuilder();
		int depth = 0;
		boolean inBlockComment = false;
		// 0 = normal, 1 = skipping until semicolon or open-brace, 2 = skipping block
		int skipMode = 0;

		for (String line : lines) {
			String trimmed = line.trim();

			// Handle multi-line block comments
			if (inBlockComment) {
				if (skipMode == 0) result.append(line).append('\n');
				if (trimmed.contains("*/")) inBlockComment = false;
				continue;
			}
			if (trimmed.startsWith("/*") && !trimmed.contains("*/")) {
				if (skipMode == 0) result.append(line).append('\n');
				inBlockComment = true;
				continue;
			}
			if (trimmed.startsWith("//") || trimmed.startsWith("/*")) {
				if (skipMode == 0) result.append(line).append('\n');
				continue;
			}

			int braceChange = countBraceChange(trimmed);

			if (skipMode == 1) {
				// Looking for end of unsupported multi-line declaration
				depth += braceChange;
				if (braceChange > 0) {
					skipMode = 2; // found opening brace, skip the block
				} else if (trimmed.contains(";")) {
					skipMode = 0; // declaration ended with semicolon
				}
				continue;
			}

			if (skipMode == 2) {
				// Skipping a block until matching close brace
				depth += braceChange;
				if (depth <= 0) {
					skipMode = 0;
					depth = 0;
				}
				continue;
			}

			// At file level (depth 0), check for unsupported constructs
			if (depth == 0 && !trimmed.isEmpty() && isUnsupportedTopLevel(trimmed)) {
				if (braceChange > 0) {
					// Opens a block on this line (e.g., file-level function)
					depth += braceChange;
					skipMode = 2;
				} else if (trimmed.contains(";")) {
					// Single-line declaration (e.g., type X is Y;)
					// just skip
				} else {
					// Multi-line declaration, skip until ; or {
					skipMode = 1;
				}
				continue;
			}

			result.append(line).append('\n');
			depth += braceChange;
		}

		return result.toString();
	}

	/**
	 * Checks whether a trimmed line at file level (depth 0) is an unsupported
	 * top-level construct for our ANTLR grammar.
	 */
	private static boolean isUnsupportedTopLevel(String trimmed) {
		return trimmed.startsWith("type ")
			|| trimmed.startsWith("error ")
			|| trimmed.startsWith("using ")
			|| trimmed.startsWith("function ")
			|| trimmed.startsWith("event ")
			|| trimmed.matches("^(uint\\d*|int\\d*|bytes\\d*|address|bool|string|mapping)\\b.*");
	}

	/**
	 * Counts net brace-depth change ({} braces) in a line, ignoring
	 * braces in strings and comments.
	 */
	private static int countBraceChange(String line) {
		int change = 0;
		boolean inStr = false;
		char strChar = 0;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			char next = (i + 1 < line.length()) ? line.charAt(i + 1) : 0;
			if (c == '/' && next == '/') break;  // line comment
			if (c == '/' && next == '*') break;  // block comment start
			if (inStr) {
				if (c == '\\') { i++; continue; } // escape
				if (c == strChar) inStr = false;
				continue;
			}
			if (c == '"' || c == '\'') { inStr = true; strChar = c; continue; }
			if (c == '{') change++;
			else if (c == '}') change--;
		}
		return change;
	}

	public static JsonObject contractJsonObject(String source) {
		try {
			// Preprocess to strip unsupported file-level constructs
			source = preprocessSolidity(source);

			SolidityLexer lexer = new SolidityLexer(CharStreams.fromString(source));
			lexer.removeErrorListeners(); // suppress stderr
			SolidityParser parser = new SolidityParser(new CommonTokenStream(lexer));
			parser.removeErrorListeners(); // suppress stderr

			String json = toJson(parser.sourceUnit());
			if (json == null) return null;
			return JsonParser.parseString(json).getAsJsonObject();
		} catch (Exception e) {
			System.err.println("[SolidityParser] Failed to parse source: " + e.getMessage());
			return null;
		}
	}

}
