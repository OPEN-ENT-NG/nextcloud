import {idiom as lang, model, notify} from "entcore";
import {safeApply} from "../../utils/safe-apply.utils";
import {AxiosError} from "axios";
import {SyncDocument} from "../../models";

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
        let selectedFolderFromNextcloudTree: SyncDocument = this.vm.getNextcloudTreeController()['selectedFolder'];
        this.vm.nextcloudService.uploadDocuments(model.me.userId, this.uploadedDocuments, selectedFolderFromNextcloudTree.path)
            .then(() => {
                return this.vm.nextcloudService.listDocument(model.me.userId, this.vm.parentDocument.path ?
                    this.vm.parentDocument.path : null);
            })
            .then((syncDocuments: Array<SyncDocument>) => {
                this.vm.documents = syncDocuments
                    .filter((syncDocument: SyncDocument) => syncDocument.path != this.vm.parentDocument.path)
                    .filter((syncDocument: SyncDocument) => syncDocument.name != model.me.userId);
                safeApply(this.scope);
            })
            .catch((err: AxiosError) => {
                const message: string = "Error while uploading files to nextcloud: ";
                console.error(`${message}${err.message}: ${this.vm.toolbar.getErrorMessage(err)}`);
                if (err.message.includes("413") || err.message.includes("507")) {
                    notify.error(lang.translate('file.too.large.limit'));
                } else {
                    notify.error(lang.translate('nextcloud.fail.upload'));
                }
            })
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