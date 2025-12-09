package sancus.controller.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import sancus.model.BlockchainSmartContract;
import sancus.service.TranslationRestService;
import sancus.service.TranslationService;


@Controller
@Api(tags = "Semantic Smart Contract")
public class SolidityRestController {

	@Autowired
	public TranslationRestService scProvider;


	/**
	 * Translate contracts in JSON-LD
	 * @param request
	 * @param response
	 * @param options
	 */
	@ApiOperation(value = "Translate own contract to JSON-LD", 
			notes = "Return the contract in JSON-LD, N3, RDF/XML...")
	@RequestMapping(value="/solidity/translateContractByCode", 
		method = RequestMethod.POST
		)
	@ResponseBody
	public ResponseEntity<String> translateContractJSONLD(HttpServletRequest request,
			HttpServletResponse response,
			@RequestBody @Valid String contract,
			@RequestParam(name = "format", required = false, defaultValue = "jsonld") RdfFormatParam format) {
		String jsonld = scProvider.getTranslateContractJSONLD(contract);
		TranslationService converter = new TranslationService();
		String serialized = converter.convertJsonLd(jsonld, format.name());
		MediaType contentType = mapFormatToMediaType(format.name());
		return ResponseEntity
	            .status(HttpServletResponse.SC_CREATED)
	            .contentType(contentType)
	            .body(serialized);
	}

	/**
	 * Receive a Hash. If the hash is valid, convert to RDF or JSON-LD
	 * @param request
	 * @param response
	 * @param options
	 */
	@ApiOperation(value = "Translate contract already deployed in JSON-LD")
	@RequestMapping(value="/solidity/translateContractByHash", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public String translateContractHash(HttpServletRequest request, HttpServletResponse response, @RequestBody(required=true) @Valid BlockchainSmartContract contract) {
		String finalContract = scProvider.getContractFromHash(contract);
		finalContract = scProvider.getTranslateContractJSONLD(finalContract);
		response.setStatus(HttpServletResponse.SC_CREATED);
		return finalContract;
	}


	private MediaType mapFormatToMediaType(String format) {
		if (format == null) {
			return MediaType.valueOf("application/ld+json");
		}

		switch (format.toLowerCase()) {
		case "jsonld":
		case "json-ld":
			return MediaType.valueOf("application/ld+json");
		case "n3":
			return MediaType.valueOf("text/n3");
		case "turtle":
		case "ttl":
			return MediaType.valueOf("text/turtle");
		case "rdfxml":
		case "rdf/xml":
			return MediaType.valueOf("application/rdf+xml");
		case "ntriples":
		case "nt":
			return MediaType.valueOf("application/n-triples");
		default:
			return MediaType.TEXT_PLAIN;
		}
	}
	
	public enum RdfFormatParam {
	    jsonld, n3, turtle
	}

}
