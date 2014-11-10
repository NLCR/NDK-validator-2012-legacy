package com.logica.ndk.tm.cdm;

import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Ignore
public class XMLValidationTest {

	private static final Logger LOG = LoggerFactory.getLogger(XMLValidationTest.class);
	private static final String METS_FILE = "METS_ANL000001.xml";
	private static final String SCHEMA_URL = "http://www.loc.gov/standards/mets/mets.xsd";

	@Ignore
	public void test1() throws Exception {

		// 1. Lookup a factory for the W3C XML Schema language
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

		// 2. Compile the schema.
		// Here the schema is loaded from a java.io.File, but you could use
		// a java.net.URL or a javax.xml.transform.Source instead.
		Schema schema = factory.newSchema(new URL(SCHEMA_URL));

		// 3. Get a validator from the schema.
		Validator validator = schema.newValidator();

		// 4. Parse the document you want to check.
		InputStream in = getClass().getClassLoader().getResourceAsStream(METS_FILE);
		Source source = new StreamSource(in);

		// 5. Check the document
		try {
			validator.validate(source);
			LOG.info(METS_FILE + " is valid.");
		} catch (SAXException ex) {
			LOG.info(METS_FILE + " is not valid because:\n" + ex.getMessage());
		}

	}

	@Ignore
	public void test2() throws Exception {
		// create a SchemaFactory capable of understanding WXS schemas
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		URL metsSchema = new URL(SCHEMA_URL);
		// load a WXS schema, represented by a Schema instance
		Source schemaFile = new StreamSource(metsSchema.openStream());
		Schema schema = factory.newSchema(schemaFile);

		// create a Validator instance, which can be used to validate an instance document
		Validator validator = schema.newValidator();

		InputStream in = getClass().getClassLoader().getResourceAsStream(METS_FILE);
		DocumentBuilderFactory dbf = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
		dbf.setNamespaceAware(true);
		DocumentBuilder builder = dbf.newDocumentBuilder();
		final Document doc = builder.parse(in);
		validator.validate(new DOMSource(doc));
	}

	@Ignore
	public void test3() throws Exception {
		// this is code for validation from METSWrapper
		SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature("http://xml.org/sax/features/namespaces", true);
        spf.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        spf.setValidating(false);
        spf.setNamespaceAware(true);
        SAXParser sp = spf.newSAXParser();
        DefaultMETSHandler ch = new DefaultMETSHandler();
        InputStream is = getClass().getClassLoader().getResourceAsStream(METS_FILE);
        InputSource source = new InputSource(is);
        sp.parse(source, ch);
        Document doc = ch.getDocument();
        
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL metsSchema = new URL(SCHEMA_URL);
        javax.xml.transform.Source schemaFile = new StreamSource(metsSchema.openStream());
        Schema schema = factory.newSchema(schemaFile);
        
        Validator validator = schema.newValidator();
        
        validator.validate(new DOMSource(doc));
	}

}
