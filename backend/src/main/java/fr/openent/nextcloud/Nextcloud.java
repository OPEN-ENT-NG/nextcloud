package fr.openent.nextcloud;

import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.controller.DocumentsController;
import fr.openent.nextcloud.controller.NextcloudController;
import fr.openent.nextcloud.controller.UserController;
import fr.openent.nextcloud.service.ServiceFactory;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.entcore.common.http.BaseServer;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.sql.Sql;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

import java.util.HashMap;
import java.util.Map;

public class Nextcloud extends BaseServer {

	public static final int TIMEOUT_VALUE = 30000;
	public static final String DB_SCHEMA = "nextcloud";

	@Override
	public void start() throws Exception {
		super.start();
		final Map<String, NextcloudConfig> nextcloudConfigMapByHost = new HashMap<>();
		if (config.containsKey("nextcloud-providers")) {
			final JsonObject nexcloudProviders = config.getJsonObject("nextcloud-providers", new JsonObject());
			for (String host : nexcloudProviders.getMap().keySet()) {
				nextcloudConfigMapByHost.put(host, new NextcloudConfig(nexcloudProviders.getJsonObject(host, new JsonObject())));
			}
		} else {
			final String host = config.getString("host").split("//")[1];
			NextcloudConfig nextcloudConfig = new NextcloudConfig(config);
			nextcloudConfigMapByHost.put(host, nextcloudConfig);
		}



		Storage storage = new StorageFactory(vertx, config).getStorage();

		ServiceFactory serviceFactory = new ServiceFactory(vertx, storage, Neo4j.getInstance(), Sql.getInstance(),
				MongoDb.getInstance(), initWebClient(), nextcloudConfigMapByHost);

		addController(new NextcloudController(serviceFactory));
		addController(new UserController(serviceFactory));
		addController(new DocumentsController(serviceFactory));
	}

	private WebClient initWebClient() {
		WebClientOptions options = new WebClientOptions()
				.setConnectTimeout(Nextcloud.TIMEOUT_VALUE);
		return WebClient.create(vertx, options);
	}

}
