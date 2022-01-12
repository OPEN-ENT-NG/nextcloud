package fr.openent.nextcloud;

import fr.openent.nextcloud.controller.NextcloudController;
import fr.openent.nextcloud.service.ServiceFactory;
import fr.wseduc.mongodb.MongoDb;
import org.entcore.common.http.BaseServer;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.sql.Sql;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

public class Nextcloud extends BaseServer {

	@Override
	public void start() throws Exception {
		super.start();

		Storage storage = new StorageFactory(vertx, config).getStorage();


		ServiceFactory serviceFactory = new ServiceFactory(vertx, storage, Neo4j.getInstance(), Sql.getInstance(),
				MongoDb.getInstance());

		addController(new NextcloudController(serviceFactory));
	}

}
