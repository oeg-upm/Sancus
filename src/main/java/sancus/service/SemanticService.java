package sancus.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apicatalog.rdf.RdfNQuad;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.RdfLiteral;
import com.apicatalog.rdf.RdfValue;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.JsonLdVersion;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import helio.blueprints.components.DataProvider;

public class SemanticService{

	static Logger logger = LoggerFactory.getLogger(SemanticService.class);

	public static Model toModel(JsonObject td, String base) {
		Model model = ModelFactory.createDefaultModel();
		toRDF(td,base).toList()
			.stream()
				.forEach(elem -> model.add(toTriple(elem)));
		return model;
	}
	
	private static RdfDataset toRDF(JsonObject jsonld11,String base) {
		try {
			Document jsonDocument = JsonDocument.of(new StringReader(jsonld11.toString()));
			JsonLdOptions options = new JsonLdOptions();
			options.setBase(new URI("https://dlt.linkeddata.es/SolidityContracts/" + base));
			options.setProcessingMode(JsonLdVersion.V1_1);
			return JsonLd.toRdf(jsonDocument).options(options).get();
		} catch (JsonLdError | URISyntaxException e) {
			e.getStackTrace();
		}
		return null;
	}
	
	private static Model toTriple(RdfNQuad quadTriple) {
		Model model = ModelFactory.createDefaultModel();
		
		try {
		Resource subject = ResourceFactory.createResource(quadTriple.getSubject().toString());
		Property predicate =  ResourceFactory.createProperty(quadTriple.getPredicate().toString());
		RdfValue objectRaw = quadTriple.getObject();
		
		if(objectRaw.isIRI() || objectRaw.isBlankNode()) {
			Resource object =  ResourceFactory.createResource(quadTriple.getObject().getValue());
			model.add(subject, predicate, (RDFNode) object);
		}else {
			RdfLiteral literal = objectRaw.asLiteral();
			Literal jenaLiteral = ResourceFactory.createPlainLiteral(literal.getValue());
			if(literal.getLanguage().isPresent()) {
				jenaLiteral = ResourceFactory.createLangLiteral(literal.getValue(), literal.getLanguage().get());
			}else if(literal.getDatatype()!=null && !literal.getDatatype().isEmpty()) {
				jenaLiteral = ResourceFactory.createTypedLiteral(literal.getValue(), new BaseDatatype(literal.getDatatype()));
			}
			model.add(subject, predicate, jenaLiteral);
		}
		}catch(Exception e) {
			e.getStackTrace();
		}
		
		return model;
	}
}
