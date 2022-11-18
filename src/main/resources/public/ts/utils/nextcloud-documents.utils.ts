import {SyncDocument} from "../models";
import {model} from "entcore";
import {DocumentRole} from "../core/enums/document-role";

export class NextcloudDocumentsUtils {
    static typeMap = new Map<string, DocumentRole>()
        .set("doc", DocumentRole.DOC)
        .set("pdf", DocumentRole.PDF)
        .set("markdown", DocumentRole.MARKDOWN)
        .set("octet-stream", DocumentRole.OCTET_STEAM)
        .set("moodle", DocumentRole.MOODLE)
        .set("image", DocumentRole.IMG)
        .set("video", DocumentRole.VIDEO);

    static determineRole(contentType: string): DocumentRole {
        for (let pattern of Array.from(this.typeMap.keys())) {
            if (contentType.includes(pattern)) {
                return this.typeMap.get(pattern);
            }
        }
        return DocumentRole.UNKNOWN;
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