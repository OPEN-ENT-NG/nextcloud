package fr.openent.nextcloud.helper;

import fr.openent.nextcloud.model.XmlnsOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.json.JSONException;
import org.json.XML;
import org.w3c.dom.Attr;

import java.io.StringWriter;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLHelper {

    private static final Logger log = LoggerFactory.getLogger(XMLHelper.class);

    private XMLHelper() { throw new IllegalStateException("Helper XMLHelper class"); }

    /**
     * Returns {@link JsonObject} by passing xml value
     *
     * @param   xml   {@link String}
     * @return  A formatted JsonObject
     */
    public static JsonObject toJsonObject(String xml) {
        try {
            return new JsonObject(XML.toJSONObject(xml).toString());
        } catch (JSONException e) {
            String message = String.format("[Nextcloud@%s::parseXMLToJSON] An error has occurred during converting xml to json : %s, " +
                    "returning New JsonObject()", XMLHelper.class.getSimpleName(), e.getMessage());
            log.info(message);
            return new JsonObject();
        }
    }

    /**
     * Returns a formatted data XML as string by sending a JSON data
     *
     * @param   preparedXml   Json with content to send for converting to XML {@link JsonObject}
     * @param   rootElement   name of the first element xml {@link String}
     * @param   optionXmlns   options xmlns namespace attributes {@link XmlnsOptions}
     * @return  A formatted XML as String {@link String}
     */
    public static String createXML(JsonObject preparedXml, String rootElement, XmlnsOptions optionXmlns) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            // root element
            Element root = document.createElement(rootElement);
            document.appendChild(root);

            // set attribute(s) to root element (if assigned)
            if (!optionXmlns.toJSON().isEmpty()) {
                optionXmlns.toJSON().forEach(entry -> {
                    Attr attr = document.createAttribute(entry.getKey());
                    attr.setValue(entry.getValue().toString());
                    root.setAttributeNode(attr);
                });
            }
            // appending elements
            preparedXml.getJsonObject(rootElement, new JsonObject()).forEach(entry -> {
                Element parentElement = document.createElement(entry.getKey());
                root.appendChild(parentElement);
                appendElement(document, entry, parentElement);
            });
            return transformDocumentToXML(document);
        } catch (ParserConfigurationException | TransformerException e) {
            String message = String.format("[Nextcloud@%s::createXML] An error has occurred during converting JSON to xml : %s, " +
                    "returning empty string", XMLHelper.class.getSimpleName(), e.getMessage());
            log.error(message);
            return "";
        }
    }

    /**
     * Method that can be used recursively for parsing json and appending element to Documents
     *
     * @param   document        Document building in progress
     * @param   entry           JSON as a map concerned
     * @param   parentElement   Element we are using to append
     */
    private static void appendElement(Document document, Map.Entry<String, Object> entry, Element parentElement) {
        if (entry.getValue() != null) {
            if (entry.getValue() instanceof JsonObject) {
                ((JsonObject) entry.getValue()).forEach(childEntry -> {
                    Element childElement = document.createElement(childEntry.getKey());
                    if (childEntry.getValue() != null) {
                        if (childEntry.getValue() instanceof JsonObject) {
                            appendElement(document, childEntry, childElement);
                        } else {
                            childElement.appendChild(document.createTextNode(childEntry.getValue().toString()));
                        }
                    }
                    parentElement.appendChild(childElement);
                });
            } else {
                parentElement.appendChild(document.createTextNode(entry.getValue().toString()));
            }
        }
    }

    /**
     * Returns a String transformed once Document is built
     *
     * @param   document   Document built
     * @return  A transformed XML as String {@link String}
     */
    private static String transformDocumentToXML(Document document) throws TransformerException {
        StringWriter stringWriter = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        } catch (IllegalArgumentException e) {
            // We still transform without setting attribute to our transformer
            return transformDOMDocument(document, stringWriter, transformerFactory);
        }
        return transformDOMDocument(document, stringWriter, transformerFactory);
    }

    private static String transformDOMDocument(Document document, StringWriter stringWriter, TransformerFactory transformerFactory)
            throws TransformerException {
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        return stringWriter.toString();
    }
}