package fr.openent.nextcloud.helper;


import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class StringHelperTest {
    @Test
    public void testDecode(TestContext ctx) {
        String encodedURL = "test%20%C3%A0%20accent";
        String decodedURL = StringHelper.decodeUrlForNc(encodedURL);
        ctx.assertEquals(decodedURL, "test à accent");
    }

    @Test
    public void testEncode(TestContext ctx) {
        String encodedURL = "test à accent";
        String decodedURL = StringHelper.encodeUrlForNc(encodedURL);
        ctx.assertEquals(decodedURL, "test%20%C3%A0%20accent");
    }

    @Test
    public void testIsInsideUserRoot(TestContext ctx) {
        ctx.assertTrue(StringHelper.isInsideUserRoot(null));
        ctx.assertTrue(StringHelper.isInsideUserRoot("/Documents/Cours 2026/"));
        ctx.assertTrue(StringHelper.isInsideUserRoot("Documents/..notes/v1.2"));
        ctx.assertFalse(StringHelper.isInsideUserRoot("../alice"));
        ctx.assertFalse(StringHelper.isInsideUserRoot("Documents/../../alice"));
        ctx.assertFalse(StringHelper.isInsideUserRoot("Documents\\..\\alice"));
        ctx.assertFalse(StringHelper.isInsideUserRoot("./Documents"));
    }

    @Test
    public void testIsValidFileName(TestContext ctx) {
        ctx.assertTrue(StringHelper.isValidFileName("compte-rendu v1.2.pdf"));
        ctx.assertTrue(StringHelper.isValidFileName("..notes"));
        ctx.assertFalse(StringHelper.isValidFileName(null));
        ctx.assertFalse(StringHelper.isValidFileName(" "));
        ctx.assertFalse(StringHelper.isValidFileName(".."));
        ctx.assertFalse(StringHelper.isValidFileName("."));
        ctx.assertFalse(StringHelper.isValidFileName("../secret.pdf"));
        ctx.assertFalse(StringHelper.isValidFileName("dossier\\secret.pdf"));
    }
}