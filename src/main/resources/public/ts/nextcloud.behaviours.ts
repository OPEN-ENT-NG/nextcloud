import {NextcloudEventService} from "./services";
import {workspaceNextcloudFolder} from "./sniplets/workspace-nextcloud-folder.sniplet";
import {workspaceNextcloudContent} from "./sniplets/content/workspace-nextcloud-content.sniplet";
import {MediaLibraryService} from "./sniplets/virtual-media-library";

export const NEXTCLOUD_APP = "nextcloud";

export const nextcloudBehaviours = {
    rights: {

    },
    mediaLibraryService: new MediaLibraryService(),
    nextcloudService: new NextcloudEventService,
    sniplets: {
        'nextcloud-folder/workspace-nextcloud-folder': workspaceNextcloudFolder,
        'nextcloud-content/content/workspace-nextcloud-content': workspaceNextcloudContent,
    }
};