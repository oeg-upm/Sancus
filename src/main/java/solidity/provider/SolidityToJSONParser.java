package solidity.provider;

import java.util.*;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

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

	public static JsonObject contractJsonObject(String source) {
		SolidityLexer lexer = new SolidityLexer(CharStreams.fromString(source));
		SolidityParser parser = new SolidityParser(new CommonTokenStream(lexer));
		String json = null;
		if(parser.getNumberOfSyntaxErrors() == 0) {
			json = toJson(parser.sourceUnit());
		}
		return JsonParser.parseString(json).getAsJsonObject();
	}

}
