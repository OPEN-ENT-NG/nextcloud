import {SyncDocument} from "../models";
import {model} from "entcore";

export class NextcloudDocumentsUtils {

    static filterRemoveNameFile(): (syncDocument: SyncDocument) => boolean {
        return (syncDocument: SyncDocument) => syncDocument.name !== model.me.userId;
    }

    static filterDocumentOnly(): (syncDocument: SyncDocument) => boolean {
        return (syncDocument: SyncDocument) => syncDocument.isFolder && syncDocument.name != model.me.userId;
    }

    static filterFilesOnly(): (syncDocument: SyncDocument) => boolean {
        return (syncDocument: SyncDocument) => !syncDocument.isFolder && syncDocument.name != model.me.userId;
    }

    static filterRemoveOwnDocument(document: SyncDocument): (syncDocument: SyncDocument) => boolean {
        return (syncDocument: SyncDocument) => syncDocument.path !== document.path;
    }

    static getExtension(filename: string): string {
        let words: Array<string> = filename.split(".");
        return words[words.length - 1];
    }
}