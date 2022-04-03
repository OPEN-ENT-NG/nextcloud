import {SyncDocument} from "../../models";
import {Behaviours, model} from "entcore";
import {NEXTCLOUD_APP} from "../../nextcloud.behaviours";

declare let window: any;

interface ILightboxViewModel {
    properties: boolean;
}

interface IViewModel {
    lightbox: ILightboxViewModel;
    currentDocument: SyncDocument;

    hasOneDocumentSelected(selectedDocuments: Array<SyncDocument>): boolean;

    // download action
    downloadFiles(selectedDocuments: Array<SyncDocument>): void

    // properties action
    toggleRenameView(state: boolean, selectedDocuments: Array<SyncDocument>): void;
    renameDocument();
}

export class ToolbarSnipletViewModel implements IViewModel {
    private vm: any;

    lightbox: ILightboxViewModel;
    currentDocument: SyncDocument;

    constructor(vm) {
        this.vm = vm;
        this.lightbox = {
            properties: false
        };
        this.currentDocument = null;
    }

    hasOneDocumentSelected(selectedDocuments: Array<SyncDocument>): boolean {
        const total: number = selectedDocuments ? selectedDocuments.length : 0;
        return total == 1;
    }

    downloadFiles(selectedDocuments: Array<SyncDocument>): void {
        if (selectedDocuments.length === 1) {
            window.open(this.vm.nextcloudService.getFile(model.me.userId, selectedDocuments[0].name,
                selectedDocuments[0].path, selectedDocuments[0].contentType))
        } else {
            const selectedDocumentsName: Array<string> = selectedDocuments.map((selectedDocuments: SyncDocument) => selectedDocuments.name);
            // if path parent is null (meaning is the parent folder sync), we return "/"
            const getPathParent: string = this.vm.parentDocument.path ? this.vm.parentDocument.path : "/";
            window.open(this.vm.nextcloudService.getFiles(model.me.userId, getPathParent, selectedDocumentsName));
        }
    }

    toggleRenameView(state: boolean, selectedDocuments?: Array<SyncDocument>): void {
        this.lightbox.properties = state;
        if (state && selectedDocuments) {
            this.currentDocument = Object.assign({}, selectedDocuments[0]);
        } else {
            this.currentDocument = null;
        }
    }

    renameDocument(): void {
        const oldDocumentToRename: SyncDocument = this.vm.selectedDocuments[0];
        if (oldDocumentToRename) {
            // we take old document path and we replace the matched file name by the new one
            const targetDocument: string = this.currentDocument.path.replace(oldDocumentToRename.name, this.currentDocument.name);
            console.log("old: ", oldDocumentToRename.path);
            console.log("target: ", targetDocument);
            // this.vm.nextcloudService.moveDocument(model.me.login, oldDocumentToRename.path, targetDocument)

            Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService.sendOpenFolderDocument(this.vm.parentDocument);
            this.toggleRenameView(false);
            this.vm.selectedDocuments = [];
        }
    }


}