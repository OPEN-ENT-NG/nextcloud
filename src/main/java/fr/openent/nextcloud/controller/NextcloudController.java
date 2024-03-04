package fr.openent.nextcloud.controller;

import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.core.constants.WorkflowRight;
import fr.openent.nextcloud.security.Access;
import fr.openent.nextcloud.service.ServiceFactory;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.Renders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.http.filter.ResourceFilter;

import java.util.Map;

public class NextcloudController extends ControllerHelper {

    private final EventStore eventStore;
    private final Map<String, NextcloudConfig> nextcloudConfigMapByHost;

    public NextcloudController(ServiceFactory serviceFactory) {
        this.eventStore = EventStoreFactory.getFactory().getEventStore(fr.openent.nextcloud.Nextcloud.class.getSimpleName());
        this.nextcloudConfigMapByHost = serviceFactory.nextcloudConfigMapByHost();
    }

    @Get("")
    @ApiDoc("Render view")
    @SecuredAction(WorkflowRight.VIEW)
    public void view(HttpServerRequest request) {
        Renders.notFound(request);
    }

    @Get("/config/url")
    @ApiDoc("Fetch Nextcloud url")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(Access.class)
    public void getNextcloudUrl(HttpServerRequest request) {
        Renders.renderJson(request, new JsonObject().put(Field.URL, nextcloudConfigMapByHost.get(Renders.getHost(request)).host()));
    }
}
