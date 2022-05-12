import {ng} from 'entcore'
import {Observable, Subject} from "rxjs";
import {SyncDocument} from "../models";

export class NextcloudEventService {
    // catch openFolder
    private openFolderSubject = new Subject<SyncDocument>();

    // sending document observable
    private documentSubject = new Subject<{parentDocument: SyncDocument, documents: Array<SyncDocument>}>();

    private contentContext: SyncDocument;

    constructor() {
        this.contentContext = null;
    }

    sendDocuments(documents: {parentDocument: SyncDocument, documents: Array<SyncDocument>}): void {
        this.documentSubject.next(documents);
    }

    getDocumentsState(): Observable<{parentDocument: SyncDocument, documents: Array<SyncDocument>}> {
        return this.documentSubject.asObservable();
    }

    sendOpenFolderDocument(document: SyncDocument): void {
        this.openFolderSubject.next(document);
    }

    getOpenedFolderDocument(): Observable<SyncDocument> {
        return this.openFolderSubject.asObservable();
    }

    getContentContext(): SyncDocument {
        return this.contentContext;
    }

    setContentContext(content: SyncDocument): void {
        this.contentContext = content;
    }
}

export const nextcloudEventService = ng.service('NextcloudEventService', NextcloudEventService);