import {ng} from 'entcore'
import {Observable, ReplaySubject, Subject} from "rxjs";

export class NextcloudEventService {
    private subject = new Subject<string>();
    private contentContext: any;

    constructor() {
        this.contentContext = {};
    }

    sendFolder(message: string): void {
        this.subject.next(message);
    }

    getFolderState(): Observable<string> {
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