package fr.openent.nextcloud.service;

import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.service.impl.DefaultDocumentsService;
import fr.openent.nextcloud.service.impl.DefaultTokenProviderService;
import fr.openent.nextcloud.service.impl.DefaultUserService;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.client.WebClient;
import org.entcore.common.bus.WorkspaceHelper;
import org.entcore.common.folders.FolderManager;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.sql.Sql;
import org.entcore.common.storage.Storage;

public class ServiceFactory {
    private final Vertx vertx;
    private final Storage storage;
    private final Neo4j neo4j;
    private final Sql sql;
    private final MongoDb mongoDb;
    private final NextcloudConfig nextcloudConfig;
    private final WebClient webClient;

    public ServiceFactory(Vertx vertx, Storage storage, Neo4j neo4j, Sql sql, MongoDb mongoDb, WebClient webClient,
                          NextcloudConfig nextcloudConfig) {
        this.vertx = vertx;
        this.storage = storage;
        this.neo4j = neo4j;
        this.sql = sql;
        this.mongoDb = mongoDb;
        this.nextcloudConfig = nextcloudConfig;
        this.webClient = webClient;
    }

    public UserService userService() {
        return new DefaultUserService(this);
    }
    public DocumentsService documentsService() {
        return new DefaultDocumentsService(this);
    }

    public TokenProviderService tokenProviderService() {
        return new DefaultTokenProviderService(webClient, nextcloudConfig);
    }

    // Helpers

    public WebClient webClient() {
        return this.webClient;
    }

    public Storage storage() {
        return this.storage;
    }

    public WorkspaceHelper workspaceHelper() {
        return new WorkspaceHelper(eventBus(), storage);
    }

    public NextcloudConfig nextcloudConfig() {return this.nextcloudConfig;}

    public EventBus eventBus() {
        return this.vertx.eventBus();
    }

    public Vertx vertx() {
        return this.vertx;
    }
}
