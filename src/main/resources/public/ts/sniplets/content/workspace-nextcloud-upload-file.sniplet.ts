import {Behaviours} from "entcore";
import {NEXTCLOUD_APP} from "../../nextcloud.behaviours";
import {safeApply} from "../../utils/safe-apply.utils";

declare let window: any;

interface ILightboxViewModel {
    uploadFile: boolean;
}

interface IViewModel {
    lightbox: ILightboxViewModel;

    toggleUploadFilesView(state: boolean): void;
    onImportFiles(files: FileList): void;
    onValidImportFiles(files: FileList): void;

    // document util
    getSize(size: number): string;
    abortFile(doc: File): void

    uploadedDocuments: Array<File>;
}

export class UploadFileSnipletViewModel implements IViewModel {
    private vm: any;
    private scope: any;

    lightbox: ILightboxViewModel;
    uploadedDocuments: Array<File>;

    constructor(scope) {
        this.scope = scope;
        this.vm = scope.vm;
        this.lightbox = {
            uploadFile: false
        };
        this.uploadedDocuments = [];
    }

    toggleUploadFilesView(state: boolean): void {
        this.lightbox.uploadFile = state;
        if (!state) {
            this.uploadedDocuments = [];
        }
    }

    onImportFiles(files: FileList): void {
        this.toggleUploadFilesView(true);

        for (let i = 0; i < files.length; i++) {
            this.uploadedDocuments.push(files[i]);
        }
    }

    onValidImportFiles(): void {
        console.log("confirming uploading file: ", this.uploadedDocuments);
        // insert service tu upload file

        Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService.sendOpenFolderDocument(this.vm.parentDocument);
        this.toggleUploadFilesView(false);
        safeApply(this.scope);
    }

    getSize(size: number): string  {
        const koSize = size / 1024;
        if (koSize > 1024) {
            return (parseInt(String(koSize / 1024 * 10)) / 10) + ' Mo';
        }
        return Math.ceil(koSize) + ' Ko';
    }

    abortFile(doc: File): void {
        const index: number = this.uploadedDocuments.indexOf(doc);
        this.uploadedDocuments.splice(index, 1);

        if (this.uploadedDocuments.length === 0) {
            this.toggleUploadFilesView(false);
        }
    }
}