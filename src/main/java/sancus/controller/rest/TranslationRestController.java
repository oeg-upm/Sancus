package sancus.controller.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import sancus.model.BlockchainSmartContract;
import sancus.service.TranslationRestService;

@Controller
@Api(tags = "Semantic Smart Contract")
public class TranslationRestController {
	
	@Autowired
	public TranslationRestService scProvider;
	
	
	/**
	 * Translate contracts in JSON-LD
	 * @param request
	 * @param response
	 * @param options
	 */
	@ApiOperation(value = "Translate own contract to JSON-LD", notes = "Add some information")
	@RequestMapping(value="/solidity/translateContractJSONLD", method = RequestMethod.POST, consumes="application/text")
	@ResponseBody
	public String translateContractJSONLD(HttpServletRequest request, HttpServletResponse response, @RequestBody(required=true) @Valid String contract) {
		String finalContract = scProvider.getTranslateContractJSONLD(contract);
		response.setStatus(HttpServletResponse.SC_CREATED);
		return finalContract;
	}
	
	/**
	 * Receive a Hash. If the hash is valid, convert to RDF or JSON-LD
	 * @param request
	 * @param response
	 * @param options
	 */
	@ApiOperation(value = "Translate contract already deployed in JSON-LD or RDF")
	@RequestMapping(value="/solidity/translateContractByHash", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public String translateContractHash(HttpServletRequest request, HttpServletResponse response, @RequestBody(required=true) @Valid BlockchainSmartContract contract) {
		String finalContract = scProvider.getContractFromHash(contract);
		response.setStatus(HttpServletResponse.SC_CREATED);
		return finalContract;
	}

}
