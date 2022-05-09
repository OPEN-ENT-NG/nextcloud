import {NextcloudEventService} from "./services";
import {workspaceNextcloudFolder} from "./sniplets/workspace-nextcloud-folder.sniplet";
import {workspaceNextcloudContent} from "./sniplets/content/workspace-nextcloud-content.sniplet";
import rights from "./rights";

export const NEXTCLOUD_APP = "nextcloud";

export const nextcloudBehaviours = {
    rights: rights,
    nextcloudService: new NextcloudEventService,
    sniplets: {
        'nextcloud-folder/workspace-nextcloud-folder': workspaceNextcloudFolder,
        'nextcloud-content/content/workspace-nextcloud-content': workspaceNextcloudContent,
    }
};