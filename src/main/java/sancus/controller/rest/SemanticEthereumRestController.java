package sancus.controller.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import ethereum.model.ChainExtractorModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import sancus.service.SemanticEthereumService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@Api(tags = "Semantic Ethereum")
public class SemanticEthereumRestController {
	
	@Autowired
	public SemanticEthereumService scProvider;
	
	@ApiOperation(value = "Retrieve a block and translate into JSONLD")
	@RequestMapping(value="/ethereum/semanticBlock", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public String retrieveBlockJSONLD(HttpServletRequest request, HttpServletResponse response, @RequestBody(required=true) @Valid ChainExtractorModel blockConf) {
		String block = null;
		if(blockConf.isJSONLD()) {
			block = scProvider.getBlockJSONLD(blockConf);
		}else {
			block = scProvider.getBlockRDF(blockConf);
		}
		response.setStatus(HttpServletResponse.SC_CREATED);
		return block;
	}
}
