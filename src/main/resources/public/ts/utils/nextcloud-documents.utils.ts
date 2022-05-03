import {SyncDocument} from "../models";
import {model} from "entcore";

export class NextcloudDocumentsUtils {

    static filterRemoveNameFile() {
        return (syncDocument: SyncDocument) => syncDocument.name !== model.me.userId;
    }

    static filterDocumentOnly() {
        return (syncDocument: SyncDocument) => syncDocument.isFolder && syncDocument.name != model.me.userId;
    }

    static filterFilesOnly() {
        return (syncDocument: SyncDocument) => !syncDocument.isFolder && syncDocument.name != model.me.userId;
    }

    static filterRemoveOwnDocument(document: SyncDocument) {
        return (syncDocument: SyncDocument) => syncDocument.path !== document.path;
    }
}