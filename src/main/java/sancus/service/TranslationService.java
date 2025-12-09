package sancus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.model.Model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.StringReader;
import java.io.StringWriter;

@Service
public class TranslationService {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final String DEFAULT_BASE_URI = "https://w3id.org/def/Solidity#";
    
    public String convertJsonLd(String jsonld, String format) {
        if (jsonld == null) {
            throw new IllegalArgumentException("jsonld no puede ser null");
        }

        RDFFormat targetFormat = mapFormat(format);

        Model model = new LinkedHashModel();

        try (InputStream in = new ByteArrayInputStream(jsonld.getBytes(StandardCharsets.UTF_8))) {

            RDFParser parser = Rio.createParser(RDFFormat.JSONLD);

            ParserConfig config = parser.getParserConfig();
            config.set(JSONLDSettings.SECURE_MODE, false);

            parser.setRDFHandler(new StatementCollector(model));
            parser.parse(in, DEFAULT_BASE_URI);

        } catch (Exception e) {
            throw new RuntimeException("Error parseando JSON-LD", e);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Rio.write(model, out, targetFormat);
            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando RDF al formato " + targetFormat, e);
        }
    }
    
    private RDFFormat mapFormat(String format) {
        if (format == null) {
            return RDFFormat.JSONLD;
        }

        switch (format.toLowerCase()) {
            case "jsonld":
            case "json-ld":
                return RDFFormat.JSONLD;
            case "n3":
                return RDFFormat.N3;
            case "turtle":
            case "ttl":
                return RDFFormat.TURTLE;
            case "rdfxml":
            case "rdf/xml":
                return RDFFormat.RDFXML;
            case "ntriples":
            case "nt":
                return RDFFormat.NTRIPLES;
            case "trig":
                return RDFFormat.TRIG;
            default:
                throw new IllegalArgumentException("Formato RDF no soportado: " + format);
        }
    }

//
//    public String fromJsonLd(String jsonLd, String format) {
//        System.out.println("jsonLd: " + jsonLd);
//        if (format == null || format.equalsIgnoreCase("jsonld")) {
//            return jsonLd;
//        }
//
//        try {
//            JsonNode root = mapper.readTree(jsonLd);
//
//            JsonNode ctxNode = root.get("@context");
//            if (ctxNode != null && ctxNode.isTextual()) {
//                JsonNode contextJson = mapper.readTree(
//                        TranslationService.class.getResourceAsStream("/Solidity.json")
//                );
//                JsonNode localCtx = contextJson.get("@context") != null
//                        ? contextJson.get("@context")
//                        : contextJson;
//
//                ((ObjectNode) root).set("@context", localCtx);
//
//                jsonLd = mapper.writeValueAsString(root);
//            }
//
//            Lang targetLang = mapFormatToLang(format);
//            if (targetLang == null) {
//                return jsonLd;
//            }
//
//            Model model = ModelFactory.createDefaultModel();
//            RDFParser.create()
//                    .source(new StringReader(jsonLd))
//                    .lang(Lang.JSONLD)
//                    .parse(model);
//
//            StringWriter out = new StringWriter();
//            RDFDataMgr.write(out, model, targetLang);
//            System.out.println("Final: " + out.toString());
//            return out.toString();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return jsonLd;
//        }
//    }
//
//    private Lang mapFormatToLang(String format) {
//        String f = format.toLowerCase();
//        switch (f) {
//            case "n3":
//                return Lang.N3;
//            case "turtle":
//            case "ttl":
//                return Lang.TURTLE;
//            case "nt":
//            case "ntriples":
//                return Lang.NTRIPLES;
//            case "rdfxml":
//            case "rdf+xml":
//                return Lang.RDFXML;
//            case "jsonld":
//                return Lang.JSONLD;
//            default:
//                return null;
//        }
//    }
}

