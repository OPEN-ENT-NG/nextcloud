package fr.openent.nextcloud.service.impl;

import fr.openent.nextcloud.service.ServiceFactory;
import fr.openent.nextcloud.service.UserService;
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

@RunWith(VertxUnitRunner.class)
public class UserServiceTest {

    private Vertx vertx;

    private UserService userService;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        ServiceFactory serviceFactory = new ServiceFactory(vertx, null, null, null, null, null, null);
        this.userService = new DefaultUserService(serviceFactory);
        Sql.getInstance().init(vertx.eventBus(), "fr.openent.next");
    }

    @Test
    public void testGetUserSession(TestContext ctx) throws Exception{
        Async async = ctx.async();
        vertx.eventBus().consumer("fr.openent.next", message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(body.getString("statement"), "SELECT *, (token_expires_at IS NOT NULL AND token_expires_at <= now()) AS token_expired " +
                    "FROM nextcloud.user WHERE user_id = ?");
            ctx.assertEquals(body.getJsonArray("values"), new JsonArray()
                    .add("userId"));
            async.complete();
        });

        this.userService.getUserSession("host", "userId");
    }
}
