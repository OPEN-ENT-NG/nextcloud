package fr.openent.nextcloud.service.impl;

import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.helper.StringHelper;
import fr.openent.nextcloud.model.UserNextcloud;
import fr.openent.nextcloud.service.DocumentsService;
import fr.openent.nextcloud.service.ServiceFactory;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.entcore.common.sql.Sql;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;

@RunWith(VertxUnitRunner.class)
public class DocumentServiceTest {

    private Vertx vertx;
    private WebClient webClient;

    private DocumentsService documentService;
    private ServiceFactory serviceFactory;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        webClient = Mockito.spy(WebClient.create(vertx));
        JsonObject config = new JsonObject();
        config
                .put(Field.NEXTCLOUDHOST, "nextcloud:8080/")
                .put(Field.ADMINCREDENTIAL, new JsonObject().put(Field.USERNAME, "test").put(Field.PASSWORD, "test"))
                .put(Field.ENDPOINT, new JsonObject().put(Field.OCS_ENDPOINT_API, "ocs").put(Field.WEBDAV_ENDPOINT_API, "webdav"));
        NextcloudConfig nextcloudConfig = new NextcloudConfig(config);
        serviceFactory = new ServiceFactory(vertx, null, null, null, null, webClient, nextcloudConfig);
        this.documentService = Mockito.spy(new DefaultDocumentsService(serviceFactory));
        Sql.getInstance().init(vertx.eventBus(), "fr.openent.next");
    }

    @Test
    public void testListFiles(TestContext ctx) {
        Async async = ctx.async();
        String filePath = "Documents/test % test";
        String encodedFilePath = "Documents/test%20%25%20test";
        String userId = "1";
        String userName = "test";
        String userToken = "token";
        String expectedURI = serviceFactory.nextcloudConfig().host() + serviceFactory.nextcloudConfig().webdavEndpoint() + "/" + userId + "/" + encodedFilePath;

        UserNextcloud.TokenProvider userSession = new UserNextcloud.TokenProvider();
        userSession.setUserId(userId);
        userSession.setUserName(userName);
        userSession.setToken(userToken);

        HttpRequest<Buffer> httpRequest = Mockito.spy(HttpRequest.class);

        Mockito.doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            String absoluteURI = invocation.getArgument(1);
            ctx.assertEquals(method, "PROPFIND");
            ctx.assertEquals(absoluteURI, expectedURI);
            async.complete();
            return httpRequest;
        }).when(this.webClient).rawAbs(Mockito.anyString(), Mockito.anyString());

        Mockito.doReturn(httpRequest).when(httpRequest).basicAuthentication(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(httpRequest).when(httpRequest).as(Mockito.any());
        Mockito.doNothing().when(httpRequest).sendBuffer(Mockito.any(), Mockito.any());
        this.documentService.parameterizedListFiles(userSession, filePath, null);

        async.awaitSuccess(10000);
    }

    @Test
    public void testMoveFile(TestContext ctx) {
        Async async = ctx.async(3);
        String filePath = "Documents/test % test";
        String encodedFilePath = "Documents/test%20%25%20test";
        String userId = "1";
        String userName = "test";
        String userToken = "token";
        String expectedURI = serviceFactory.nextcloudConfig().host() + serviceFactory.nextcloudConfig().webdavEndpoint() + "/" + userId + "/" + encodedFilePath;
        String moveURI = "/test/" + filePath;
        String expectedMoveURI = serviceFactory.nextcloudConfig().host() + serviceFactory.nextcloudConfig().webdavEndpoint() + "/" + userId + "/test/" + encodedFilePath;

        UserNextcloud.TokenProvider userSession = new UserNextcloud.TokenProvider();
        userSession.setUserId(userId);
        userSession.setUserName(userName);
        userSession.setToken(userToken);

        Mockito.doReturn(Future.succeededFuture(new JsonArray().add("not empty"))).when(this.documentService).listFiles(userSession, moveURI);
        this.documentService.moveDocument(userSession, filePath, moveURI).onFailure(error -> {
            ctx.assertEquals(error.getMessage(), "nextcloud.file.already.exist");
            async.countDown();
        });

        Mockito.doReturn(Future.succeededFuture(new JsonArray())).when(this.documentService).listFiles(userSession, moveURI);

        HttpRequest<Buffer> httpRequest = Mockito.spy(HttpRequest.class);

        Mockito.doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            String absoluteURI = invocation.getArgument(1);
            ctx.assertEquals(method, "MOVE");
            ctx.assertEquals(absoluteURI, expectedURI);
            async.countDown();
            return httpRequest;
        }).when(this.webClient).rawAbs(Mockito.anyString(), Mockito.anyString());

        Mockito.doAnswer(invocation -> {
            String headerName = invocation.getArgument(0);
            String absoluteURI = invocation.getArgument(1);
            ctx.assertEquals(headerName, "destination");
            ctx.assertEquals(absoluteURI, expectedMoveURI);
            async.countDown();
            return httpRequest;
        }).when(httpRequest).putHeader(Mockito.anyString(), Mockito.anyString());

        Mockito.doReturn(httpRequest).when(httpRequest).basicAuthentication(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(httpRequest).when(httpRequest).as(Mockito.any());
        Mockito.doNothing().when(httpRequest).sendBuffer(Mockito.any(), Mockito.any());
        this.documentService.moveDocument(userSession, filePath, moveURI);

        async.awaitSuccess(5000);
    }

    @Test
    public void testEncode(TestContext ctx) {
        String encodedURL = "test à accent";
        String decodedURL = StringHelper.encodeUrlForNc(encodedURL);
        ctx.assertEquals(decodedURL, "test%20%C3%A0%20accent");
    }

}
