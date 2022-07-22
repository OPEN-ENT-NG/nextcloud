import {SyncDocument} from "../../models";
import {angular, Behaviours, model, workspace} from "entcore";
import {AxiosError} from "axios";
import {safeApply} from "../../utils/safe-apply.utils";
import {ToolbarShareSnipletViewModel} from "../content/workspace-nextcloud-toolbar-share.sniplet";
import {NEXTCLOUD_APP} from "../../nextcloud.behaviours";
import models = workspace.v2.models;

declare let window: any;

interface ILightboxViewModel {
    folder: boolean;
}

interface IViewModel {
    lightbox: ILightboxViewModel;
    currentDocument: SyncDocument;
    toggleCreateFolder(state: boolean): void;
    createFolder(name: String);

}

export class FolderCreationModel implements IViewModel {
    private vm: any;
    private scope: any;

    lightbox: ILightboxViewModel;
    currentDocument: SyncDocument;

    // share documents action
    share: any;

    constructor(scope) {
        this.scope = scope;
        this.vm = scope.vm;
        this.lightbox = {
            folder: false
        };
        this.currentDocument = null;
    }

    toggleCreateFolder(state: boolean): void {
        this.lightbox.folder = state;
    }

    createFolder(name: String) {
        const currentFolder: SyncDocument = angular.element(document.getElementById('nextcloud-folder-tree'));
        const folder: SyncDocument = this.vm.selectedFolder;
        this.vm.nextcloudService.createFolder(model.me.userId, (folder.path != null ? folder.path + "/" : "") + name);
        this.toggleCreateFolder(false);
        console.log(this.scope);
        console.log(this.vm.selectedFolder.name);
        console.log(folder);
        // Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService.sendOpenFolderDocument(this.scope.parent.name);
        safeApply(this.scope);
    }



}