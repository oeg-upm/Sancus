package sancus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.io.StringWriter;

@Service
public class TranslationService {

    private final ObjectMapper mapper = new ObjectMapper();

    public String fromJsonLd(String jsonLd, String format) {
        System.out.println("jsonLd: " + jsonLd);
        if (format == null || format.equalsIgnoreCase("jsonld")) {
            return jsonLd;
        }

        try {
            JsonNode root = mapper.readTree(jsonLd);

            JsonNode ctxNode = root.get("@context");
            if (ctxNode != null && ctxNode.isTextual()) {
                JsonNode contextJson = mapper.readTree(
                        TranslationService.class.getResourceAsStream("/Solidity.json")
                );
                JsonNode localCtx = contextJson.get("@context") != null
                        ? contextJson.get("@context")
                        : contextJson;

                ((ObjectNode) root).set("@context", localCtx);

                jsonLd = mapper.writeValueAsString(root);
            }

            Lang targetLang = mapFormatToLang(format);
            if (targetLang == null) {
                return jsonLd;
            }

            Model model = ModelFactory.createDefaultModel();
            RDFParser.create()
                    .source(new StringReader(jsonLd))
                    .lang(Lang.JSONLD)
                    .parse(model);

            StringWriter out = new StringWriter();
            RDFDataMgr.write(out, model, targetLang);
            System.out.println("Final: " + out.toString());
            return out.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return jsonLd;
        }
    }

    private Lang mapFormatToLang(String format) {
        String f = format.toLowerCase();
        switch (f) {
            case "n3":
                return Lang.N3;
            case "turtle":
            case "ttl":
                return Lang.TURTLE;
            case "nt":
            case "ntriples":
                return Lang.NTRIPLES;
            case "rdfxml":
            case "rdf+xml":
                return Lang.RDFXML;
            case "jsonld":
                return Lang.JSONLD;
            default:
                return null;
        }
    }
}

