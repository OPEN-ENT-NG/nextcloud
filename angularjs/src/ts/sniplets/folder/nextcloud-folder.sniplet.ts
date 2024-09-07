import {SyncDocument} from "../../models";
import {Behaviours, model, workspace} from "entcore";
import {safeApply} from "../../utils/safe-apply.utils";
import {NEXTCLOUD_APP} from "../../nextcloud.behaviours";
import models = workspace.v2.models;
import {AxiosError} from "axios";

interface ILightboxViewModel {
    folder: boolean;
}

interface IViewModel {
    lightbox: ILightboxViewModel;
    currentDocument: SyncDocument;
    toggleCreateFolder(state: boolean, folderCreate: models.Element): void;
    createFolder(folderCreate: models.Element): void;
}

export class FolderCreationModel implements IViewModel {
    private vm: any;
    private scope: any;

    lightbox: ILightboxViewModel;
    currentDocument: SyncDocument;

    constructor(scope) {
        this.scope = scope;
        this.vm = scope.vm;
        this.lightbox = {
            folder: false
        };
        this.currentDocument = null;
    }

    public toggleCreateFolder(state: boolean, folderCreate: models.Element): void {
        if (folderCreate) {
            folderCreate.name = "";
        }
        this.lightbox.folder = state;
    }

    public createFolder(folderCreate: models.Element): void {
        const folder: SyncDocument = this.vm.selectedFolder;
        this.vm.nextcloudService.createFolder(model.me.userId, (folder.path != null ? folder.path + "/" : "") + encodeURI(folderCreate.name))
            .then(() => {
                folderCreate.name = "";
                this.toggleCreateFolder(false, folderCreate);
                Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService.sendOpenFolderDocument(this.vm.selectedFolder);
                safeApply(this.scope);
            })
            .catch((err: AxiosError) => {
                const message: string = "Error while attempting folder creation.";
                console.error(message + err.message);
            });
    }
}
