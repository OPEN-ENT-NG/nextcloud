import {SyncDocument} from "../models";
import {model, workspace} from "entcore";
import {AxiosError} from "axios";
import {nextcloudService} from "../services";
import models = workspace.v2.models;
import {NextcloudDocumentsUtils} from "../utils/nextcloud-documents.utils";

interface VirtualMediaLibraryScope {
    folders: Array<models.Tree>;
    documents: Array<models.Tree>;

    // tree
    enableInitFolderTree(): boolean;
    initFolderTree(): Promise<void>;

    // content view list/icons
    openFolder(folder: models.Element): Promise<void>;
}

export class MediaLibraryService implements VirtualMediaLibraryScope {

    folders: Array<models.Tree>;
    documents: Array<models.Tree>;

    constructor() {
        this.folders = [];
        this.documents = [];
    }

    enableInitFolderTree(): boolean {
        return true;
    }

    async initFolderTree(): Promise<void> {
        let syncDocuments: Array<SyncDocument> = await nextcloudService.listDocument(model.me.userId, null)
            .catch((err: AxiosError) => {
                const message: string = "Error while attempting to fetch documents children ";
                console.error(message + err.message);
                return [];
            });

        // populate folder content to media library behaviours
        this.folders = (<any>syncDocuments)
            .filter(NextcloudDocumentsUtils.filterRemoveNameFile())
            .filter(NextcloudDocumentsUtils.filterDocumentOnly());

        // populate file content to media library behaviours
        this.documents = (<any>syncDocuments)
            .filter(NextcloudDocumentsUtils.filterRemoveNameFile())
            .filter(NextcloudDocumentsUtils.filterFilesOnly());
    }

    async openFolder(folder: models.Element): Promise<void> {
        let syncDocuments: Array<SyncDocument> = await nextcloudService.listDocument(model.me.userId, (<any>folder).path ? (<any>folder).path : null)
            .catch((err: AxiosError) => {
                const message: string = "Error while attempting to fetch documents children ";
                console.error(message + err.message);
                return [];
            });

        // first filter applies only when we happen to fetch its own folder and the second applies on document only
        (<any>this.folders) = syncDocuments
            .filter(NextcloudDocumentsUtils.filterRemoveOwnDocument((<any>folder)))
            .filter(NextcloudDocumentsUtils.filterDocumentOnly());

        (<any>this.documents) = syncDocuments
            .filter(NextcloudDocumentsUtils.filterRemoveOwnDocument((<any>folder)))
            .filter(NextcloudDocumentsUtils.filterFilesOnly());
    }
}