package fr.openent.nextcloud.service.impl;

import fr.openent.nextcloud.model.UserNextcloud;
import fr.openent.nextcloud.service.TokenProviderService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.sql.Sql;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;

@RunWith(VertxUnitRunner.class)
public class TokenProviderServiceTest {

    private Vertx vertx;
    private TokenProviderService tokenProviderService;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        this.tokenProviderService = new DefaultTokenProviderService(null, null);
        Sql.getInstance().init(vertx.eventBus(), "fr.openent.next");
    }

    @Test
    public void testPersistToken(TestContext ctx) throws Exception {
        Async async = ctx.async();
        vertx.eventBus().consumer("fr.openent.next", message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(body.getString("statement"), "INSERT INTO nextcloud.user (user_id, username, password, last_modified)  VALUES" +
                    " (?, ?, ?, now())  ON CONFLICT (user_id)  DO UPDATE SET username = ?, password = ?, last_modified = now()");
            ctx.assertEquals(body.getJsonArray("values"), new JsonArray()
                    .add("userId")
                    .add("loginName")
                    .add("token")
                    .add("loginName")
                    .add("token"));
            async.complete();
        });

        UserNextcloud.TokenProvider tokenProvider = new UserNextcloud.TokenProvider();
        tokenProvider.setUserId("userId");
        tokenProvider.setToken("token");
        tokenProvider.setUserName("loginName");
        Whitebox.invokeMethod(this.tokenProviderService, "persistToken", tokenProvider);
    }
}
