import {model} from "entcore";
import {DocumentRole} from "../core/enums/document-role";

export interface IDocumentResponse {
    path: string;
    name: string;
    ownerDisplayName: string;
    contentType: string;
    size: number;
    favorite: number;
    etag: string;
    fileId: number;
    isFolder: boolean;
}

export class SyncDocument {
    path: string;
    name: string;
    ownerDisplayName: string;
    contentType: string;
    size: number;
    favorite: number;
    etag: string;
    fileId: number;
    isFolder: boolean;
    role: string | typeof DocumentRole;
    children: Array<SyncDocument>;

    build(data: IDocumentResponse): SyncDocument {
        this.name = decodeURI(data.name);
        this.ownerDisplayName = data.ownerDisplayName;
        this.path = data.path.split(this.ownerDisplayName).pop();
        this.contentType = data.contentType;
        this.size = data.size;
        this.favorite = data.favorite;
        this.etag = data.etag;
        this.fileId = data.fileId;
        this.isFolder = data.isFolder;
        this.role = this.determineRole();
        this.children = [];
        if (this.size > 0) {
            this.children.push(new SyncDocument());
        }
        return this;
    }

    determineRole(): string {
        if (this.isFolder) {
            return DocumentRole.FOLDER;
        }
        if (this.contentType.includes('image')) {
            return DocumentRole.IMG;
        }
        return DocumentRole.UNKNOWN;
    }

    // create a folder with only one content (synchronized document) and its children all sync documents
    initParent(): SyncDocument {
        const parentNextcloudFolder: SyncDocument = new SyncDocument();
        parentNextcloudFolder.path = null;
        parentNextcloudFolder.name = 'Document synchronize';
        parentNextcloudFolder.ownerDisplayName = model.me.login;
        parentNextcloudFolder.contentType = null;
        parentNextcloudFolder.size = null;
        parentNextcloudFolder.favorite = null;
        parentNextcloudFolder.etag = null;
        parentNextcloudFolder.fileId = null;
        parentNextcloudFolder.isFolder = true;
        parentNextcloudFolder.children = [];
        return parentNextcloudFolder;
    }
}