package fr.openent.nextcloud.controller;

import fr.openent.nextcloud.service.ServiceFactory;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.Renders;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;

public class NextcloudController extends ControllerHelper {

    private final EventStore eventStore;

    public NextcloudController(ServiceFactory serviceFactory) {
        this.eventStore = EventStoreFactory.getFactory().getEventStore(fr.openent.nextcloud.Nextcloud.class.getSimpleName());
    }

    @Get("")
    @ApiDoc("Render view")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void view(HttpServerRequest request) {
        Renders.notFound(request);
    }
}
