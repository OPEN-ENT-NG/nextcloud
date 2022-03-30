import {ng} from 'entcore'
import {Observable, Subject} from "rxjs";
import {SyncDocument} from "../models";

export class NextcloudEventService {
    // catch openFolder
    private openFolderSubject = new Subject<SyncDocument>();

    // sending document observable
    private documentSubject = new Subject<{path: string, documents: Array<SyncDocument>}>();

    private contentContext: any;

    constructor() {
        this.contentContext = {};
    }

    sendDocuments(documents: {path: string, documents: Array<SyncDocument>}): void {
        this.documentSubject.next(documents);
    }

    getDocumentsState(): Observable<{path: string, documents: Array<SyncDocument>}> {
        return this.documentSubject.asObservable();
    }

    sendOpenFolderDocument(document: SyncDocument): void {
        this.openFolderSubject.next(document);
    }

    getOpenedFolderDocument(): Observable<SyncDocument> {
        return this.openFolderSubject.asObservable();
    }

    getContentContext(): void {
        return this.contentContext;
    }

    setContentContext(content: any): void {
        this.contentContext = content;
    }
}

export const nextcloudEventService = ng.service('NextcloudEventService', NextcloudEventService);