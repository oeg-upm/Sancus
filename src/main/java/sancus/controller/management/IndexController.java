package sancus.controller.management;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import sancus.Keys;
import springfox.documentation.annotations.ApiIgnore;

@Controller
public class IndexController {
	
	// Provide GUI
	@ApiIgnore
	@RequestMapping(value= {"/","/index.html","/index"}, method = RequestMethod.GET, produces = {"text/html", "application/xhtml+xml", "application/xml"})
	public String getLoginService(HttpServletRequest request, HttpServletResponse response) {
		return "index.html";
	}

	// Provide Demo page
	@ApiIgnore
	@RequestMapping(value= {"/demo","/demo.html"}, method = RequestMethod.GET, produces = {"text/html", "application/xhtml+xml", "application/xml"})
	public String getDemoService(HttpServletRequest request, HttpServletResponse response) {
		if (!Keys.isDemoMode()) {
			return "redirect:/index";
		}
		return "demo.html";
	}

}
