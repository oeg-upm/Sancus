package sancus.controller.management;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import springfox.documentation.annotations.ApiIgnore;

@Controller
public class DocumentationController {
	
	@ApiIgnore
	@RequestMapping(value="/documentation", method = RequestMethod.GET, produces = {"text/html", "application/xhtml+xml", "application/xml"})
	public String getClopudAccessGUI(HttpServletRequest request, HttpServletResponse response) {
		return "documentation.html";
	}

}
