package fr.openent.nextcloud;

import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.controller.DocumentsController;
import fr.openent.nextcloud.controller.NextcloudController;
import fr.openent.nextcloud.controller.UserController;
import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.service.ServiceFactory;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.entcore.common.http.BaseServer;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.sql.Sql;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

public class Nextcloud extends BaseServer {

	public static final int TIMEOUT_VALUE = 30000;

	@Override
	public void start() throws Exception {
		super.start();
		NextcloudConfig nextcloudConfig = new NextcloudConfig(config);
		Storage storage = new StorageFactory(vertx, config).getStorage();

		ServiceFactory serviceFactory = new ServiceFactory(vertx, storage, Neo4j.getInstance(), Sql.getInstance(),
				MongoDb.getInstance(), initWebClient(), nextcloudConfig);

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
