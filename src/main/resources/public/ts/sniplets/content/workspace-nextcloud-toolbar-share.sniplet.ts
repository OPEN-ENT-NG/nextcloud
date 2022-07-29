import {model, SharePayload, template, workspace} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {SyncDocument} from "../../models";
import models = workspace.v2.models;
import service = workspace.v2.service;
import {WorkspaceEntcoreUtils} from "../../utils/workspace-entcore.utils";

interface IViewModel {
    copyingForShare: boolean;

    sharedElement: Array<any>;
    toggleShareView(state: boolean, selectedDocuments?: Array<SyncDocument>): void;
    onShareAndNotCopy(): void;
    onShareAndCopy(): void;

    onSubmitSharedElements(share: SharePayload): Promise<void>;
    onCancelShareElements(): Promise<void>;
}

export class ToolbarShareSnipletViewModel implements IViewModel {
    private vm: any;
    private scopeParent: any;

    copyingForShare: boolean;
    sharedElement: Array<any>;

    constructor(scopeParent: any) {
        this.scopeParent = scopeParent;
        this.vm = scopeParent.vm;
        this.sharedElement = [];
    }

    toggleShareView(state: boolean, selectedDocuments?: Array<SyncDocument>): void {
        this.scopeParent.lightbox.share = state;
        if (state && selectedDocuments) {
            this.copyingForShare = false;
            const pathTemplate: string = `../../../${RootsConst.template}/behaviours/sniplet-nextcloud-content/toolbar/share/share-documents-options`;
            template.open('workspace-nextcloud-toolbar-share', pathTemplate);
        } else {
            template.close('workspace-nextcloud-toolbar-share');
            this.copyingForShare = true;
        }
    }

    onShareAndNotCopy(): void {
        const paths: Array<string> = this.vm.selectedDocuments.map((document: SyncDocument) => document.path);
        this.vm.nextcloudService.moveDocumentNextcloudToWorkspace(model.me.userId, paths)
            .then((workspaceDocuments: Array<models.Element>) => {
                this.sharedElement = workspaceDocuments;
                this.vm.updateTree();
                const pathTemplate: string = `../../../${RootsConst.template}/behaviours/sniplet-nextcloud-content/toolbar/share/share`;
                this.vm.selectedDocuments = [];
                template.open('workspace-nextcloud-toolbar-share', pathTemplate);
            });
    }

    onShareAndCopy(): void {
        const paths: Array<string> = this.vm.selectedDocuments.map((document: SyncDocument) => document.path);
        this.vm.nextcloudService.copyDocumentToWorkspace(model.me.userId, paths)
            .then((workspaceDocuments: Array<models.Element>) => {
                this.sharedElement = workspaceDocuments;
                const pathTemplate: string = `../../../${RootsConst.template}/behaviours/sniplet-nextcloud-content/toolbar/share/share`;
                template.open('workspace-nextcloud-toolbar-share', pathTemplate);
            });
    }

    async onSubmitSharedElements(share: SharePayload): Promise<void> {
        this.toggleShareView(false);
        setTimeout(() => {
            WorkspaceEntcoreUtils.toggleWorkspaceContentDisplay(false);
            this.vm.safeApply();
        }, 500);
        this.sharedElement = [];
    }

    async onCancelShareElements(): Promise<void> {
        if (this.sharedElement.length) {
            try {
                await service.deleteAll(this.sharedElement);
                this.sharedElement = [];
            } catch (e) {
                console.error((e));
            }
        }
        this.toggleShareView(false);
    }
}