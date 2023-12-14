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
import sancus.service.SmartContractABIService;
import sancus.service.TranslationRestService;

@Controller
@Api(tags = "Smart Contract ABI")
public class SmartContractABIRestController {
	
	@Autowired
	public SmartContractABIService scABIProvider;
	
	
	/**
	 * Translate contracts in JSON-LD
	 * @param request
	 * @param response
	 * @param options
	 */
	@ApiOperation(value = "Translate an ABI contract to JSON-LD 1.1", notes = "Add some information")
	@RequestMapping(value="/solidityABI/ABItoRDF", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public String translateContract(HttpServletRequest request, HttpServletResponse response, @RequestBody(required=true) @Valid BlockchainSmartContract contract) {
		String block = null;
		block = scABIProvider.getABIContractJSONLD(contract);
		response.setStatus(HttpServletResponse.SC_CREATED);
		return block;
	}

}
