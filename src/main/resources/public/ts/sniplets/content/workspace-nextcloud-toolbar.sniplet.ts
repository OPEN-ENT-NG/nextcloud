import {SyncDocument} from "../../models";
import {model} from "entcore";

declare let window: any;

interface IViewModel {
    downloadFiles(selectedDocuments: Array<SyncDocument>): void
}

export class ToolbarSnipletViewModel implements IViewModel {
    private vm: any;

    constructor(vm) {
        this.vm = vm;
    }

    downloadFiles(selectedDocuments: Array<SyncDocument>): void {
        if (selectedDocuments.length === 1) {
            window.open(this.vm.nextcloudService.getFile(model.me.login, selectedDocuments[0].name,
                selectedDocuments[0].path, selectedDocuments[0].contentType))
        } else {
            const selectedDocumentsName = selectedDocuments.map((selectedDocuments: SyncDocument) => selectedDocuments.name);
            // if path parent is the root of the user itself, we return "/"
            const getPathParent = this.vm.pathParent === model.me.login ? "/" : this.vm.pathParent;
            window.open(this.vm.nextcloudService.getFiles(model.me.login, getPathParent, selectedDocumentsName));
        }
    }
}