import {SyncDocument} from "../models";
import {model} from "entcore";
import {editableType} from "../core/enums/document-content";

export class NextcloudDocumentsUtils {
    static isDocumentEditable(document: SyncDocument): boolean {
        return !document.isFolder
            && (document.contentType.startsWith(editableType.text)
                || document.contentType.startsWith(editableType.application));
    }
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