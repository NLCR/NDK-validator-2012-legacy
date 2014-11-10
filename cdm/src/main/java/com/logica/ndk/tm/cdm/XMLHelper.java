package com.logica.ndk.tm.cdm;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.io.output.ProxyOutputStream;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

public class XMLHelper {

    private static final transient Logger log = LoggerFactory.getLogger(XMLHelper.class);
    private static final String SCHEMA_LANG = "http://www.w3.org/2001/XMLSchema";

    public static Document parseXML(InputStream input, boolean validateAndLoadExternalDTD) throws SAXException, IOException, ParserConfigurationException {
        checkNotNull(input);
        final DocumentBuilderFactory factory = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
        if (validateAndLoadExternalDTD == false) {
            factory.setValidating(false);
            factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
        }
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(input);
    }

    public static Document parseXML(InputStream input) throws SAXException, IOException, ParserConfigurationException {
        return parseXML(input, true);
    }

    public static Document parseXML(File file, boolean validateAndLoadExternalDTD) throws SAXException, IOException, ParserConfigurationException {
        checkNotNull(file);
        InputStream input = null;
        try {
            input = new XMLHelper.Input(file);
            return parseXML(input, validateAndLoadExternalDTD);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public static Document parseXML(File file) throws SAXException, IOException, ParserConfigurationException {
        return parseXML(file, true);
    }

    public static void validateXML(InputStream input, InputStream schemaStream) throws SAXException, IOException {
        validateXML(input, schemaStream, new LSResourceResolver() {

            @Override
            public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
                if (systemId.contains("xlink")) {
                    return new XslInputStream(publicId, systemId, new AutoCloseInputStream(XMLHelper.class.getClassLoader().getResourceAsStream("xsd/xlink.xsd")));
                } else if (systemId.equalsIgnoreCase("http://www.loc.gov/mods/xml.xsd")) {
                    return new XslInputStream(publicId, systemId, new AutoCloseInputStream(XMLHelper.class.getClassLoader().getResourceAsStream("xsd/xml.xsd")));
                }
                return null;
            }
        });
    }

    public static void validateXML(InputStream input, InputStream schemaStream, LSResourceResolver resolver) throws SAXException, IOException {
        checkNotNull(input);
        checkNotNull(schemaStream);
        final SchemaFactory factory = SchemaFactory.newInstance(SCHEMA_LANG);
        if (resolver != null) {
            factory.setResourceResolver(resolver);
        }

        final Schema schema = factory.newSchema(new StreamSource(schemaStream));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(input));
    }

    public static void validateXML(Node node, String resourceName, LSResourceResolver resolver) throws IOException, SAXException {
        InputStream schema = null;
        InputStream input = null;
        try {
            schema = new XMLHelper.Input(resourceName);
            input = IOUtils.toInputStream(node.asXML(), "UTF-8");
            validateXML(input, schema, resolver);
        } finally {
            IOUtils.closeQuietly(schema);
            IOUtils.closeQuietly(input);
        }
    }
    
    public static void validateXML(InputStream input, String resourceName) throws IOException, SAXException {
        InputStream schema = null;
        try {
            schema = new XMLHelper.Input(resourceName);
            validateXML(input, schema);
        } finally {
            IOUtils.closeQuietly(schema);
        }
    }

    public static void validateXML(File file, String resourceName) throws IOException, SAXException {
        checkNotNull(file);
        InputStream input = null;
        try {
            input = new XMLHelper.Input(file);
            validateXML(input, resourceName);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
    
   public static void validateXML(File file, String resourceName, LSResourceResolver resolver) throws IOException, SAXException {
        checkNotNull(file);
        InputStream schema = null;
        InputStream input = null;
        try {
            input = new XMLHelper.Input(file);
            schema = new XMLHelper.Input(resourceName);
            validateXML(input, schema, resolver);
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(schema);
        }
    }

    public static void validateXML(Node node, String resourceName) throws IOException, SAXException {
        InputStream input = null;
        try {
            input = IOUtils.toInputStream(node.asXML(), "UTF-8");
            validateXML(input, resourceName);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public static void transformXML(InputStream input, OutputStream output, InputStream style, URIResolver customURIResolver) throws TransformerException, IOException, SAXException, ParserConfigurationException {
        checkNotNull(input);
        checkNotNull(output);
        checkNotNull(style);
        final DocumentBuilderFactory factory = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
        factory.setNamespaceAware(true);
        final Document document = factory.newDocumentBuilder().parse(input);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        if (customURIResolver != null) {
            transformerFactory.setURIResolver(customURIResolver);
        }
        final Transformer transformer = transformerFactory.newTransformer(new StreamSource(style));
        transformer.setOutputProperty("encoding", "UTF-8");
        transformer.setOutputProperty("indent", "yes");
        transformer.transform(new DOMSource(document), new StreamResult(output));
    }

    public static void transformXML(InputStream input, OutputStream output, InputStream style) throws TransformerException, IOException, SAXException, ParserConfigurationException {
        transformXML(input, output, style, null);
    }

    public static void transformXML(File input, File output, InputStream style) throws TransformerException, IOException, SAXException, ParserConfigurationException {
        checkNotNull(input);
        checkNotNull(output);
        InputStream fis = null;
        OutputStream fos = null;
        try {
            fis = new XMLHelper.Input(input);
            fos = new XMLHelper.Output(output);
            transformXML(fis, fos, style);
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);
        }
    }

    public static void writeXML(Document document, OutputStream output) throws TransformerException {
        checkNotNull(document);
        checkNotNull(output);
        final DocumentBuilderFactory factory = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
        factory.setNamespaceAware(true);
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty("encoding", "UTF-8");
        transformer.setOutputProperty("indent", "yes");
        transformer.transform(new DOMSource(document), new StreamResult(output));
    }

    public static void writeXML(Document document, File output) throws TransformerException, FileNotFoundException {
        checkNotNull(document);
        checkNotNull(output);
        OutputStream fos = null;
        try {
            fos = new XMLHelper.Output(output);
            writeXML(document, fos);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    public static class Input extends ProxyInputStream {

        public Input(InputStream is) {
            super(is);
        }

        public Input(File file) throws FileNotFoundException {
            super(new AutoCloseInputStream(new FileInputStream(file)));
        }

        public Input(URL url) throws IOException {
            super(new AutoCloseInputStream(url.openStream()));
        }

        public Input(String resourceName) throws IOException {
            //super(new AutoCloseInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)));
            super(new AutoCloseInputStream(XMLHelper.class.getClassLoader().getResourceAsStream(resourceName)));
        }
    }

    public static class Output extends ProxyOutputStream {

        public Output(OutputStream os) {
            super(os);
        }

        public Output(File file) throws FileNotFoundException {
            super(new FileOutputStream(file));
        }

        public Output(File file, boolean append) throws FileNotFoundException {
            super(new FileOutputStream(file, append));
        }
    }

    public static void pretyPrint(File xmlFile) {
        pretyPrint(xmlFile, false);
    }

    public static void pretyPrint(File xmlFile, boolean externalMets) {
        checkNotNull(xmlFile, "xmlFile must not be null");

        try {
            //org.dom4j.Document document = DocumentHelper.parseText(FileUtils.readFileToString(xmlFile, "UTF-8"));
            org.dom4j.Document document = DocumentHelper.parseText(retriedReadFileToString(xmlFile));

            XPath metsElementsXPath = DocumentHelper.createXPath("//ns:*");
            metsElementsXPath.setNamespaceURIs(ImmutableMap.<String, String>of("ns", "http://www.loc.gov/METS/"));
            Namespace metsNamespace = new Namespace("mets", "http://www.loc.gov/METS/");

            for (Object node : metsElementsXPath.selectNodes(document)) {
                Element element = (Element) node;
                element.setQName(new QName(element.getName(), metsNamespace));
                if (externalMets) {
                    element.remove(element.getNamespaceForPrefix(""));
                }
            }

            XPath metsXPath = DocumentHelper.createXPath("//ns:mets");
            metsXPath.setNamespaceURIs(ImmutableMap.<String, String>of("ns", "http://www.loc.gov/METS/"));

            Element metsElement = (Element) metsXPath.selectSingleNode(document);
            metsElement.setQName(new QName("mets", metsNamespace));
            metsElement.remove(metsElement.getNamespaceForPrefix(""));

            Namespace modsNamespace = new Namespace("mods", "http://www.loc.gov/mods/v3");
            metsElement.add(modsNamespace);

            Namespace oaiDCNamespace = new Namespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
            metsElement.add(oaiDCNamespace);

            Namespace dcNamespace = new Namespace("dc", "http://purl.org/dc/elements/1.1/");
            metsElement.add(dcNamespace);

            Namespace xmlnsi = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            metsElement.add(xmlnsi);

            Namespace xlink = new Namespace("xlink", "http://www.w3.org/1999/xlink");
            metsElement.add(xlink);

            Namespace premis = new Namespace("premis", "info:lc/xmlns/premis-v2");
            metsElement.add(premis);

            //TODO consider if it is necessary due to validity of generated code

            if (!externalMets) {
                metsElement.addAttribute(
                        "xsi:schemaLocation",
                        "http://www.w3.org/2001/XMLSchema-instance http://www.w3.org/2001/XMLSchema.xsd http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd http://www.w3.org/1999/xlink http://www.w3.org/1999/xlink.xsd");
            }

            XMLWriter xmlWriter = new XMLWriter(new FileWriterWithEncoding(xmlFile, "UTF-8"), OutputFormat.createPrettyPrint());
            xmlWriter.write("\ufeff");
            xmlWriter.write(document);
            xmlWriter.close();
            log.info("File {} transformed to pretty-print format.", xmlFile);
        } catch (DocumentException e) {
            log.warn(format("Reformatting file %s to pretty-print form not succesfull. Using old file.", xmlFile));
        } catch (Exception e) {
            log.error(format("Reformatting file %s to pretty-print form failed", xmlFile) , e);
            e.printStackTrace();
            throw new SystemException(format("Reformatting file %s to pretty-print form failed", xmlFile), ErrorCodes.XML_PRETTY_PRINT_FAILED);
        }
    }

    public static org.dom4j.Document qualify(org.dom4j.Document document, Namespace ns) {
        checkNotNull(document, "document must not be null");
        checkNotNull(ns, "ns must not be null");

//    rootElement.setQName(new QName(rootElement.getName(), ns));
        XPath xPath = DocumentHelper.createXPath("//ns:*");
        xPath.setNamespaceURIs(ImmutableMap.<String, String>of("ns", ns.getStringValue()));

        for (Object object : xPath.selectNodes(document)) {
            org.dom4j.Element element = (org.dom4j.Element) object;
            element.setQName(new QName(element.getName(), ns));
        }

        return document;
    }

    public static void removeDuplicateModsNms(File xmlFile) {
        org.dom4j.Document document = null;;
        try {
            //document = DocumentHelper.parseText(FileUtils.readFileToString(xmlFile, "UTF-8"));
            document = DocumentHelper.parseText(retriedReadFileToString(xmlFile));
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        XPath modsXPath = DocumentHelper.createXPath("//ns:mods");
        modsXPath.setNamespaceURIs(ImmutableMap.<String, String>of("ns", "http://www.loc.gov/mods/v3/"));
        List<com.logica.ndk.tm.process.Node> nodes = modsXPath.selectNodes(document);

        System.out.println(nodes.size());

    }
    
    @RetryOnFailure(attempts = 3)
    private static String retriedReadFileToString(File file) throws IOException {
      return FileUtils.readFileToString(file, "UTF-8");
    }
}
