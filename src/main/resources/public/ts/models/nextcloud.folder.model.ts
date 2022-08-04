import {model, idiom as lang, workspace} from "entcore";
import {DocumentRole} from "../core/enums/document-role";
import models = workspace.v2.models;

export interface IDocumentResponse {
    path: string;
    displayname: string;
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
    cacheChildren: models.CacheList<any>;
    cacheDocument: models.CacheList<any>;

    // custom field bound by other entity/model
    selected?: boolean;
    isNextcloudParent?: boolean;

    build(data: IDocumentResponse): SyncDocument {
        this.name = decodeURIComponent(data.displayname);
        this.ownerDisplayName = data.ownerDisplayName;
        this.path = data.path.split(model.me.userId).pop();
        this.contentType = data.contentType;
        this.size = data.size;
        this.favorite = data.favorite;
        this.etag = data.etag;
        this.fileId = data.fileId;
        this.isFolder = data.isFolder;
        this.role = this.determineRole();
        this.children = [];
        this.cacheChildren = new models.CacheList<any>(0, () => false, () => false);
        this.cacheChildren.setData([]);
        this.cacheChildren.disableCache();
        this.cacheDocument = new models.CacheList<any>(0, () => false, () => false);
        this.cacheDocument.setData([]);
        this.cacheDocument.disableCache();

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
        parentNextcloudFolder.name = lang.translate('nextcloud.documents');
        parentNextcloudFolder.ownerDisplayName = model.me.login;
        parentNextcloudFolder.contentType = null;
        parentNextcloudFolder.size = null;
        parentNextcloudFolder.favorite = null;
        parentNextcloudFolder.etag = null;
        parentNextcloudFolder.fileId = null;
        parentNextcloudFolder.isFolder = true;
        parentNextcloudFolder.children = [];
        parentNextcloudFolder.cacheChildren = new models.CacheList<any>(0, () => false, () => false);
        parentNextcloudFolder.cacheChildren.setData([]);
        parentNextcloudFolder.cacheChildren.disableCache();
        parentNextcloudFolder.cacheDocument = new models.CacheList<any>(0, () => false, () => false);
        parentNextcloudFolder.cacheDocument.setData([]);
        parentNextcloudFolder.cacheDocument.disableCache();

        parentNextcloudFolder.isNextcloudParent = true;
        return parentNextcloudFolder;
    }
}