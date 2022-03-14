import {ng} from 'entcore'
import {Observable, Subject} from "rxjs";
import {SyncDocument} from "../models";

export class NextcloudEventService {
    private subject = new Subject<Array<SyncDocument>>();
    private contentContext: any;

    constructor() {
        this.contentContext = {};
    }

    sendDocuments(documents: Array<SyncDocument>): void {
        this.subject.next(documents);
    }

    getDocumentsState(): Observable<Array<SyncDocument>> {
        return this.subject.asObservable();
    }

    unsubscribe(): void {
        this.subject.unsubscribe();
    }

    getContentContext(): void {
        return this.contentContext;
    }

    setContentContext(content: any): void {
        this.contentContext = content;
    }
}

export const nextcloudEventService = ng.service('NextcloudEventService', NextcloudEventService);