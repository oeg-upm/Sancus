package sancus.controller.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ethereum.model.ChainExtractorModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import sancus.Keys;
import sancus.service.SemanticEthereumService;
import solidity.provider.JsonParserToJSONOnto;
import solidity.provider.SmartContractABIAnalyser;

@Controller
@Api(tags = "Demo")
public class DemoRestController {

    @Autowired
    public SemanticEthereumService scProvider;

    private static final String STORES_DIR = "stores";

    // ─── Block endpoint ────────────────────────────────────────────────

    @ApiOperation(value = "Retrieve block in the specified RDF format")
    @RequestMapping(value = "/demo/block", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getBlockJsonLd(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "blockNumber", required = true) int blockNumber,
            @RequestParam(value = "format", required = false, defaultValue = "jsonld") String format) {
        try {
            String jsonld = getCachedOrFetch(blockNumber);

            String result;
            switch (format.toLowerCase()) {
                case "turtle":
                case "ttl":
                    result = convertJsonLd(jsonld, Lang.TURTLE);
                    response.setContentType("text/turtle; charset=UTF-8");
                    break;
                case "nt":
                case "ntriples":
                    result = convertJsonLd(jsonld, Lang.NTRIPLES);
                    response.setContentType("application/n-triples; charset=UTF-8");
                    break;
                default:
                    result = jsonld;
                    response.setContentType("application/ld+json; charset=UTF-8");
                    break;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json; charset=UTF-8");
            return "{\"error\": \"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    // ─── Contracts endpoint ────────────────────────────────────────────

    /**
     * Returns the list of smart-contract addresses for a block.
     * Calls Etherscan txlistinternal, intersects tx hashes with the block's
     * hasTransactions hashes, and collects the unique "from" addresses from
     * the Etherscan response as the contract addresses.
     * Results are cached as folders under stores/{blockNumber}/contracts/{from}/
     */
    @ApiOperation(value = "Retrieve smart-contract addresses for a block")
    @RequestMapping(value = "/demo/contracts", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getContracts(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "blockNumber", required = true) int blockNumber) {
        try {
            Path contractsDir = Paths.get(STORES_DIR, String.valueOf(blockNumber), "contracts");

            // 1. If contract folders already exist, return them without calling Etherscan
            if (Files.exists(contractsDir) && Files.isDirectory(contractsDir)) {
                try (Stream<Path> dirs = Files.list(contractsDir)) {
                    List<String> cachedAddresses = dirs
                            .filter(Files::isDirectory)
                            .map(p -> p.getFileName().toString())
                            .collect(Collectors.toList());
                    if (!cachedAddresses.isEmpty()) {
                        System.out.println("[Contracts] Block " + blockNumber + " — loaded " + cachedAddresses.size() + " contracts from cache.");
                        response.setContentType("application/json; charset=UTF-8");
                        return buildContractsJson(blockNumber, cachedAddresses);
                    }
                }
            }

            // 2. Read the cached JSON-LD to get block transaction hashes
            Path cacheFile = Paths.get(STORES_DIR, String.valueOf(blockNumber), blockNumber + ".jsonld");
            if (!Files.exists(cacheFile)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return "{\"error\": \"Block not yet fetched. Fetch the block first.\"}";
            }
            String jsonld = new String(Files.readAllBytes(cacheFile), StandardCharsets.UTF_8);

            // Collect all tx hashes from the block
            Set<String> blockTxHashes = new HashSet<>();
            JsonObject block = JsonParser.parseString(jsonld).getAsJsonObject();
            if (block.has("hasTransactions") && block.get("hasTransactions").isJsonArray()) {
                for (JsonElement el : block.getAsJsonArray("hasTransactions")) {
                    JsonObject tx = el.getAsJsonObject();
                    if (tx.has("hash")) {
                        blockTxHashes.add(tx.get("hash").getAsString().toLowerCase());
                    }
                }
            }

            // 3. Call Etherscan txlistinternal — returns hash -> from (from Etherscan)
            Map<String, String> etherscanHashToFrom = callEtherscanInternalTx(blockNumber);

            // 4. Intersection: only hashes present in both
            Set<String> matchingHashes = new HashSet<>(blockTxHashes);
            matchingHashes.retainAll(etherscanHashToFrom.keySet());

            // 5. Collect unique "from" addresses from the Etherscan response
            Set<String> uniqueAddresses = new LinkedHashSet<>();
            for (String hash : matchingHashes) {
                String fromAddr = etherscanHashToFrom.get(hash);
                if (fromAddr != null && !fromAddr.isEmpty()) {
                    uniqueAddresses.add(fromAddr.toLowerCase());
                }
            }

            List<String> contractAddresses = new ArrayList<>(uniqueAddresses);
            System.out.println("[Contracts] Block " + blockNumber + " — found " + contractAddresses.size()
                    + " unique contract addresses (from " + matchingHashes.size() + " matching txs)");

            // 6. Create the folder structure stores/{blockNumber}/contracts/{from}/
            Files.createDirectories(contractsDir);
            for (String addr : contractAddresses) {
                Files.createDirectories(contractsDir.resolve(addr));
            }

            response.setContentType("application/json; charset=UTF-8");
            return buildContractsJson(blockNumber, contractAddresses);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{\"error\": \"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    // ─── Contract source code endpoint ─────────────────────────────────

    /**
     * Fetches source code for a contract address from Etherscan getsourcecode,
     * saves it in stores/{blockNumber}/contracts/{address}/{address}.json,
     * and returns the result.
     */
    @ApiOperation(value = "Fetch and cache contract source code from Etherscan")
    @RequestMapping(value = "/demo/contract/source", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getContractSource(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "blockNumber", required = true) int blockNumber,
            @RequestParam(value = "address", required = true) String address) {
        try {
            String body = ensureSourceCached(blockNumber, address);
            response.setContentType("application/json; charset=UTF-8");
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{\"error\": \"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    // ─── Cache helpers ─────────────────────────────────────────────────

    /**
     * Ensures the Etherscan getsourcecode response is cached for a contract,
     * downloading it if necessary. Returns the cached JSON string.
     */
    private String ensureSourceCached(int blockNumber, String address) throws Exception {
        String lowerAddr = address.toLowerCase();
        Path contractDir = Paths.get(STORES_DIR, String.valueOf(blockNumber), "contracts", lowerAddr);
        Path sourceFile = contractDir.resolve(lowerAddr + ".json");

        if (Files.exists(sourceFile)) {
            System.out.println("[Source] Contract " + lowerAddr + " loaded from cache.");
            return new String(Files.readAllBytes(sourceFile), StandardCharsets.UTF_8);
        }

        // Call Etherscan getsourcecode
        String apiKey = Keys.getEtherscanKey();
        String urlStr = "https://api.etherscan.io/v2/api"
                + "?module=contract"
                + "&action=getsourcecode"
                + "&address=" + lowerAddr
                + "&chainid=1"
                + "&apikey=" + apiKey;

        System.out.println("[Etherscan] Calling getsourcecode for " + lowerAddr);
        String body = httpGet(urlStr);

        Files.createDirectories(contractDir);
        Files.write(sourceFile, body.getBytes(StandardCharsets.UTF_8));
        System.out.println("[Source] Contract " + lowerAddr + " saved to " + sourceFile);

        return body;
    }

    // ─── Semantic ABI endpoint ─────────────────────────────────────────

    /**
     * Converts the ABI from a contract's Etherscan getsourcecode response
     * into JSON-LD using SmartContractABIAnalyser.abiConverter().
     */
    @ApiOperation(value = "Get semantic ABI (JSON-LD) for a contract")
    @RequestMapping(value = "/demo/contract/semantic-abi", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getSemanticABI(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "blockNumber", required = true) int blockNumber,
            @RequestParam(value = "address", required = true) String address) {
        try {
            String body = ensureSourceCached(blockNumber, address);

            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            if (!json.has("result") || !json.get("result").isJsonArray()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return "{\"error\": \"Invalid Etherscan response\"}";
            }

            JsonArray results = json.getAsJsonArray("result");
            if (results.size() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return "{\"error\": \"No contract data in Etherscan response\"}";
            }

            JsonObject contractData = results.get(0).getAsJsonObject();
            String abi = contractData.has("ABI") ? contractData.get("ABI").getAsString() : "";
            if (abi.isEmpty() || abi.equals("Contract source code not verified")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return "{\"error\": \"ABI not available — contract source code not verified on Etherscan\"}";
            }

            JsonObject abiJsonLd = SmartContractABIAnalyser.abiConverter(contractData, address.toLowerCase());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            response.setContentType("application/ld+json; charset=UTF-8");
            return gson.toJson(abiJsonLd);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{\"error\": \"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    // ─── Semantic Solidity endpoint ────────────────────────────────────

    /**
     * Converts the Solidity source code from a contract's Etherscan response
     * into JSON-LD using JsonParserToJSONOnto.
     * If the source contains multiple files (Etherscan {{...}} format),
     * each file is converted separately and returned as a JSON array.
     */
    @ApiOperation(value = "Get semantic Solidity (JSON-LD) for a contract")
    @RequestMapping(value = "/demo/contract/semantic-solidity", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getSemanticSolidity(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "blockNumber", required = true) int blockNumber,
            @RequestParam(value = "address", required = true) String address) {
        try {
            String body = ensureSourceCached(blockNumber, address);

            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            if (!json.has("result") || !json.get("result").isJsonArray()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return "{\"error\": \"Invalid Etherscan response\"}";
            }

            JsonArray results = json.getAsJsonArray("result");
            if (results.size() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return "{\"error\": \"No contract data in Etherscan response\"}";
            }

            JsonObject contractData = results.get(0).getAsJsonObject();
            String sourceCode = contractData.has("SourceCode") ? contractData.get("SourceCode").getAsString() : "";
            if (sourceCode.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return "{\"error\": \"Source code not available — contract not verified on Etherscan\"}";
            }

            String lowerAddr = address.toLowerCase();
            JsonParserToJSONOnto parser = new JsonParserToJSONOnto();
            JsonArray allContracts = new JsonArray();

            // Etherscan multi-file sources come in 3 formats:
            // 1) Double braces {{...}} → JSON with "sources" wrapper
            // 2) Single braces {...}  → JSON map of filename → {content: "..."}
            // 3) Plain Solidity source code
            if (sourceCode.startsWith("{{") && sourceCode.endsWith("}}")) {
                // Format 1: Remove outer braces to get valid JSON with "sources" key
                String innerJson = sourceCode.substring(1, sourceCode.length() - 1);
                JsonObject multiFile = JsonParser.parseString(innerJson).getAsJsonObject();

                if (multiFile.has("sources") && multiFile.get("sources").isJsonObject()) {
                    JsonObject sources = multiFile.getAsJsonObject("sources");
                    for (String fileName : sources.keySet()) {
                        parseSourceFile(parser, fileName, sources.getAsJsonObject(fileName), lowerAddr, allContracts);
                    }
                }
            } else if (sourceCode.startsWith("{")) {
                // Format 2: Direct JSON map of filename → {content: "..."}
                try {
                    JsonObject fileMap = JsonParser.parseString(sourceCode).getAsJsonObject();
                    for (String fileName : fileMap.keySet()) {
                        parseSourceFile(parser, fileName, fileMap.getAsJsonObject(fileName), lowerAddr, allContracts);
                    }
                } catch (Exception ex) {
                    // Not valid JSON after all — fall through to plain source
                    System.err.println("[Semantic Solidity] SourceCode starts with '{' but is not JSON, trying as plain Solidity");
                    parsePlainSource(parser, sourceCode, lowerAddr, allContracts);
                }
            } else {
                // Format 3: Plain single-file Solidity source
                parsePlainSource(parser, sourceCode, lowerAddr, allContracts);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            response.setContentType("application/ld+json; charset=UTF-8");
            if (allContracts.size() == 0) {
                return "{\"error\": \"No Solidity contracts could be parsed from source\"}";
            } else if (allContracts.size() == 1) {
                return gson.toJson(allContracts.get(0));
            }
            return gson.toJson(allContracts);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{\"error\": \"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    // ─── Solidity source parsing helpers ──────────────────────────────

    /**
     * Parses a single source file entry ({content: "..."}) and adds valid JSON-LD results
     * to the allContracts array.
     */
    private void parseSourceFile(JsonParserToJSONOnto parser, String fileName,
                                  JsonObject fileObj, String address, JsonArray allContracts) {
        try {
            String content = fileObj.get("content").getAsString();
            String jsonLd = parser.JsonContractToJavaObject(content, address);
            if (jsonLd != null && !jsonLd.equals("{}")) {
                allContracts.add(JsonParser.parseString(jsonLd));
            }
        } catch (Exception ex) {
            System.err.println("[Semantic Solidity] Failed to parse file " + fileName + ": " + ex.getMessage());
        }
    }

    /**
     * Parses plain (non-JSON) Solidity source code and adds the result to allContracts.
     */
    private void parsePlainSource(JsonParserToJSONOnto parser, String sourceCode,
                                   String address, JsonArray allContracts) {
        try {
            String jsonLd = parser.JsonContractToJavaObject(sourceCode, address);
            if (jsonLd != null && !jsonLd.equals("{}")) {
                allContracts.add(JsonParser.parseString(jsonLd));
            }
        } catch (Exception ex) {
            System.err.println("[Semantic Solidity] Failed to parse plain source: " + ex.getMessage());
        }
    }

    // ─── Original cache helpers ────────────────────────────────────────
    private String getCachedOrFetch(int blockNumber) throws Exception {
        Path cacheFile = Paths.get(STORES_DIR, String.valueOf(blockNumber), blockNumber + ".jsonld");

        if (Files.exists(cacheFile)) {
            System.out.println("[Cache] Block " + blockNumber + " loaded from cache.");
            return new String(Files.readAllBytes(cacheFile), StandardCharsets.UTF_8);
        }

        // Fetch from Ethereum
        ChainExtractorModel blockConf = new ChainExtractorModel();
        blockConf.setBlock(blockNumber);
        blockConf.setUrl(Keys.getEthKey());
        String jsonld = scProvider.getBlockJSONLD(blockConf);

        if (jsonld != null && jsonld.contains("\"error\"")) {
            throw new Exception("Ethereum API error: " + jsonld);
        }

        // Save to cache
        try {
            Files.createDirectories(cacheFile.getParent());
            Files.write(cacheFile, jsonld.getBytes(StandardCharsets.UTF_8));
            System.out.println("[Cache] Block " + blockNumber + " saved to cache.");
        } catch (Exception e) {
            System.err.println("[Cache] Failed to save block " + blockNumber + ": " + e.getMessage());
        }

        return jsonld;
    }

    // ─── Etherscan helpers ─────────────────────────────────────────────

    /**
     * Calls Etherscan txlistinternal for the given block and returns a map
     * of tx hash (lowercase) -> from address from the Etherscan response.
     */
    private Map<String, String> callEtherscanInternalTx(int blockNumber) throws Exception {
        String apiKey = Keys.getEtherscanKey();
        String urlStr = "https://api.etherscan.io/v2/api"
                + "?module=account"
                + "&action=txlistinternal"
                + "&startblock=" + blockNumber
                + "&endblock=" + blockNumber
                + "&chainid=1"
                + "&apikey=" + apiKey
                + "&offset=10000";

        System.out.println("[Etherscan] Calling txlistinternal for block " + blockNumber);

        String body = httpGet(urlStr);
        Map<String, String> hashToFrom = new HashMap<>();

        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        if (json.has("result") && json.get("result").isJsonArray()) {
            JsonArray results = json.getAsJsonArray("result");
            for (JsonElement el : results) {
                JsonObject tx = el.getAsJsonObject();
                if (tx.has("hash") && tx.has("from")) {
                    String hash = tx.get("hash").getAsString().toLowerCase();
                    String from = tx.get("from").getAsString().toLowerCase();
                    // Keep the first "from" per hash (they may repeat)
                    if (!hashToFrom.containsKey(hash)) {
                        hashToFrom.put(hash, from);
                    }
                }
            }
        } else {
            String msg = json.has("message") ? json.get("message").getAsString() : body;
            System.err.println("[Etherscan] Unexpected response: " + msg);
        }

        System.out.println("[Etherscan] Found " + hashToFrom.size() + " internal txs for block " + blockNumber);
        return hashToFrom;
    }

    /**
     * Generic HTTP GET that returns the response body as a String.
     */
    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        int status = conn.getResponseCode();
        BufferedReader reader;
        if (status >= 200 && status < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();
        return sb.toString();
    }

    /**
     * Builds the JSON response: just the list of contract addresses.
     */
    private String buildContractsJson(int blockNumber, List<String> addresses) {
        JsonArray contractsArr = new JsonArray();
        for (String addr : addresses) {
            JsonObject obj = new JsonObject();
            obj.addProperty("address", addr);
            contractsArr.add(obj);
        }

        JsonObject result = new JsonObject();
        result.addProperty("blockNumber", blockNumber);
        result.addProperty("count", contractsArr.size());
        result.add("contracts", contractsArr);
        return new Gson().toJson(result);
    }

    // ─── RDF conversion ────────────────────────────────────────────────

    private String convertJsonLd(String jsonld, Lang targetLang) {
        try {
            Model model = ModelFactory.createDefaultModel();
            RDFParser.create()
                    .source(new StringReader(jsonld))
                    .lang(Lang.JSONLD)
                    .parse(model);

            if (model.isEmpty()) {
                System.err.println("[Convert] Jena produced empty model. Returning raw JSON-LD.");
                return jsonld;
            }

            StringWriter out = new StringWriter();
            RDFDataMgr.write(out, model, targetLang);
            return out.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[Convert] Conversion failed, returning raw JSON-LD.");
            return jsonld;
        }
    }

    // ─── Demo status ───────────────────────────────────────────────────

    @RequestMapping(value = "/demo/status", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getDemoStatus(HttpServletRequest request, HttpServletResponse response) {
        return "{\"demoEnabled\": " + Keys.isDemoMode() + "}";
    }

    private String escapeJson(String text) {
        if (text == null) return "Unknown error";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
