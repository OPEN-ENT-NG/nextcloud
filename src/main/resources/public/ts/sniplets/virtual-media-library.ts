import {SyncDocument} from "../models";
import {Document, model, workspace, IVirtualMediaLibraryScope} from "entcore";
import {AxiosError} from "axios";
import {nextcloudService, nextcloudUserService} from "../services";
import {NextcloudDocumentsUtils} from "../utils/nextcloud-documents.utils";
import rights from "../rights";
import {WorkspaceEntcoreUtils} from "../utils/workspace-entcore.utils";
import {Element} from "entcore/types/src/ts/workspace/model";
import models = workspace.v2.models;
import service = workspace.v2.service;

export class MediaLibraryService implements IVirtualMediaLibraryScope {
    openedTree: any;
    folders: Array<Document>;
    documents: Array<Document>;

    constructor() {
        this.folders = [];
        this.documents = [];
    }

    enableInitFolderTree(): boolean {
        if (model.me.hasWorkflow(rights.workflow.access)) {
            nextcloudUserService.resolveUser(model.me.userId);
            this.registerNextcloudThumbUrlMapper();
            return true;
        } else {
            return false;
        }
    }

    private registerNextcloudThumbUrlMapper(): void {
        const nextcloudFunction = (element: Element) => {
            if (element.application === "nextcloud") {
                return nextcloudService.getFile(model.me.userId, <any>element.name, (<any>element).path, <any>element.contentType);
            }
        }
        models.Element.registerThumbUrlMapper(nextcloudFunction);
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
        this.documents =  WorkspaceEntcoreUtils.toDocuments(
            (<any>syncDocuments)
                .filter(NextcloudDocumentsUtils.filterRemoveNameFile())
                .filter(NextcloudDocumentsUtils.filterFilesOnly())
        );
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

        (<any>this.documents) = WorkspaceEntcoreUtils.toDocuments(
            syncDocuments
                .filter(NextcloudDocumentsUtils.filterRemoveOwnDocument((<any>folder)))
                .filter(NextcloudDocumentsUtils.filterFilesOnly())
        );
    }

    async onSelectVirtualDocumentsBefore(documents: Array<any>): Promise<Array<Document>> {
        const paths: Array<string> = (documents as Array<SyncDocument>).map((doc: SyncDocument) => doc.path);
        return nextcloudService.copyDocumentToWorkspace(model.me.userId, paths)
            .then((res: Array<models.Element>) => res.map(d => new Document(d)));
    }

    async clearCopiedDocumentsAfterSelect(documents: Array<Document>): Promise<void> {
        if (documents && documents.length)
            service.deleteAll(documents);
    }

}