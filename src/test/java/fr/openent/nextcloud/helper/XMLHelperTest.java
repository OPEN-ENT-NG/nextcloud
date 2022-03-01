package fr.openent.nextcloud.helper;

import fr.openent.nextcloud.core.enums.XmlnsAttr;
import fr.openent.nextcloud.model.XmlnsOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class XMLHelperTest {

    private XmlnsOptions xmlnsOptions;

    @Before
    public void setUp() {
        xmlnsOptions = new XmlnsOptions()
                .setWebDavTag(XmlnsAttr.D)
                .setNextcloudTag(XmlnsAttr.NC)
                .setOwnCloudTag(XmlnsAttr.OC);
        
        // needed, some "cache" can remain during each test
        System.clearProperty("javax.xml.transform.TransformerFactory");
    }

    @Test
    public void testCreateXMLShould_return_XML_correctly_With_Value_Standalone(TestContext ctx) {
        String json = "{\"d:propfind\":{\"d:prop\": 5}}";

        // todo https://stackoverflow.com/questions/45152707/transformerfactory-and-xalan-dependency-conflict
        //  set manually TO MAKE IT NOTICE IN PULL REQUEST
        System.setProperty("javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

        String myXMLRes = XMLHelper.createXML(new JsonObject(json), "d:propfind", xmlnsOptions);

        // expected
        String expectedQuery = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><d:propfind xmlns:d=\"DAV:\" " +
                "xmlns:nc=\"http://nextcloud.org/ns\" xmlns:oc=\"http://owncloud.org/ns\"><d:prop>5</d:prop></d:propfind>";

        ctx.assertEquals(expectedQuery, myXMLRes);

    }

    @Test
    public void testCreateXMLShould_return_XML_correctly_With_Json_Standalone(TestContext ctx) {
        String json = "{\"d:propfind\":{\"d:prop\":{\"d:getlastmodified\":{},\"d:getetag\": {}}}}";

        System.setProperty("javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

        String myXMLRes = XMLHelper.createXML(new JsonObject(json), "d:propfind", xmlnsOptions);

        // expected
        String expectedQuery = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><d:propfind xmlns:d=\"DAV:\" " +
                "xmlns:nc=\"http://nextcloud.org/ns\" xmlns:oc=\"http://owncloud.org/ns\"><d:prop><d:getlastmodified/>" +
                "<d:getetag/></d:prop></d:propfind>";

        ctx.assertEquals(expectedQuery, myXMLRes);
    }

    @Test
    public void testCreateXMLShould_return_XML_correctly_With_Json_Default(TestContext ctx) {
        String json = "{\"d:propfind\":{\"d:prop\":{\"d:getlastmodified\":{},\"d:getetag\": {}}}}";

        String myXMLRes = XMLHelper.createXML(new JsonObject(json), "d:propfind", xmlnsOptions);

        // expected
        String expectedQuery = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><d:propfind xmlns:d=\"DAV:\" " +
                "xmlns:nc=\"http://nextcloud.org/ns\" xmlns:oc=\"http://owncloud.org/ns\"><d:prop><d:getlastmodified/>" +
                "<d:getetag/></d:prop></d:propfind>";

        ctx.assertEquals(expectedQuery, myXMLRes);
    }

    @Test
    public void testCreateXMLShould_return_XML_correctly_With_Value_Default(TestContext ctx) {
        String json = "{\"d:propfind\":{\"d:prop\": 6}}";

        String myXMLRes = XMLHelper.createXML(new JsonObject(json), "d:propfind", xmlnsOptions);

        // expected
        String expectedQuery = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><d:propfind xmlns:d=\"DAV:\" " +
                "xmlns:nc=\"http://nextcloud.org/ns\" xmlns:oc=\"http://owncloud.org/ns\"><d:prop>6</d:prop></d:propfind>";

        ctx.assertEquals(expectedQuery, myXMLRes);

    }
}