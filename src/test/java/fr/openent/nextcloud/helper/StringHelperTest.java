package fr.openent.nextcloud.helper;


import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class StringHelperTest {
    @Test
    public void testDecode(TestContext ctx) {
        String encodedURL = "test %c3%a0 accent";
        String decodedURL = StringHelper.decodeUrlForNc(encodedURL);
        ctx.assertEquals(decodedURL, "test à accent");
    }

    @Test
    public void testEncode(TestContext ctx) {
        String encodedURL = "test à accent";
        String decodedURL = StringHelper.encodeUrlForNc(encodedURL);
        ctx.assertEquals(decodedURL, "test%20%C3%A0%20accent");
    }
}