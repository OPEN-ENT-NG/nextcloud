import {NextcloudEventService} from "./services";
import {workspaceNextcloudFolder} from "./sniplets/workspace-nextcloud-folder.sniplet";
import {workspaceNextcloudContent} from "./sniplets/content/workspace-nextcloud-content.sniplet";

export const NEXTCLOUD_APP = "nextcloud";

export const nextcloudBehaviours = {
    rights: {

    },
    nextcloudService: new NextcloudEventService,
    sniplets: {
        'nextcloud-folder/workspace-nextcloud-folder': workspaceNextcloudFolder,
        'nextcloud-content/workspace-nextcloud-content': workspaceNextcloudContent,
    }
};