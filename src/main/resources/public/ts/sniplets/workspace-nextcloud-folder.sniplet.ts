import {FolderTreeProps, angular, template, Behaviours, workspace, model, idiom as lang, Document, init} from "entcore";
import {Tree} from "entcore/types/src/ts/workspace/model";
import {safeApply} from "../utils/safe-apply.utils";
import {RootsConst} from "../core/constants/roots.const";
import {NEXTCLOUD_APP} from "../nextcloud.behaviours";
import models = workspace.v2.models;
import {WorkspaceEntcoreUtils} from "../utils/workspace-entcore.utils";
import {INextcloudService, nextcloudService} from "../services";
import {AxiosError, AxiosResponse} from "axios";
import {Draggable, SyncDocument} from "../models";
import {INextcloudUserService, nextcloudUserService} from "../services";
import {UserNextcloud} from "../models/nextcloud-user.model";
import {Subscription} from "rxjs";
import rights from "../rights";
import {NextcloudDocumentsUtils} from "../utils/nextcloud-documents.utils";
import {DocumentsType} from "../core/enums/documents-type";

declare let window: any;

// JQuery that fetches all folder element by its icons
const $folderTreeArrows: string = '#nextcloud-folder-tree i';
const $nextcloudFolder: string = '#nextcloud-folder-tree';

interface IViewModel {
    documents: Array<SyncDocument>;

    initTree(folder: Array<SyncDocument>): void;
    watchFolderState(): void;
    openDocument(folder: any): void;
    setSwitchDisplayHandler(): void;

    userInfo: UserNextcloud;
    folderTree: FolderTreeProps;
    selectedFolder: models.Element;
    openedFolder: Array<models.Element>;

    // drag & drop actions
    initDraggable(): void;
    resolveDragTarget(event: DragEvent): Promise<void>;
    droppable: Draggable;
}

class ViewModel implements IViewModel {
    private nextcloudService: INextcloudService;
    private nextcloudUserService: INextcloudUserService;
    private scope: any;

    userInfo: UserNextcloud;
    folderTree: FolderTreeProps;
    selectedFolder: models.Element;
    openedFolder: Array<models.Element> = [];
    droppable: Draggable;
    documents: Array<SyncDocument>;

    subscriptions: Subscription = new Subscription();

    constructor(scope, nextcloudService: INextcloudService, nextcloudUserService: INextcloudUserService) {
        this.scope = scope;
        this.nextcloudService = nextcloudService;
        this.nextcloudUserService = nextcloudUserService;
        this.userInfo = null;
        this.documents = [];
        this.folderTree = {};
        this.selectedFolder = null;
        this.openedFolder = [];

        // resolve user nextcloud && init tree
        this.nextcloudUserService.getUserInfo(model.me.userId)
            .then((userInfo: UserNextcloud) => {
                this.userInfo = userInfo;
                this.documents = [new SyncDocument().initParent()];
                this.initTree(this.documents);
                this.initDraggable();
                safeApply(this.scope);
            })
            .catch((err: AxiosError) => {
                const message: string = "Error while attempting to fetch user info";
                console.error(message + err.message);
            });

        // on receive openFolder event
        this.subscriptions.add(Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService
            .getOpenedFolderDocument()
            .subscribe((document: SyncDocument) => {
                this.folderTree.openFolder(document);
            }));

    }



    initTree(folder: Array<SyncDocument>): void {
        // use this const to make it accessible to its folderTree inner context
        const viewModel: IViewModel = this;
        this.folderTree = {
            cssTree: "folders-tree",
            get trees(): any | Array<Tree> {
                return folder;
            },
            isDisabled(folder: models.Element): boolean {
                return false;
            },
            isOpenedFolder(folder: models.Element): boolean {
                return viewModel.openedFolder.some((openFolder: models.Element) => openFolder === folder);
            },
            isSelectedFolder(folder: models.Element): boolean {
                return viewModel.selectedFolder === folder;
            },
            async openFolder(folder: models.Element): Promise<void> {
                viewModel.setSwitchDisplayHandler();
                // create handler in case icon are only clicked
                viewModel.selectedFolder = folder;
                viewModel.watchFolderState();
                if (!viewModel.openedFolder.some((openFolder: models.Element) => openFolder === folder)) {
                    viewModel.openedFolder = viewModel.openedFolder.filter((e: models.Element) => (<any> e).path != (<any> folder).path);
                    viewModel.openedFolder.push(folder);
                }
                // synchronize documents and send content to its other sniplet content
                viewModel.openDocument(folder);
            },
        };
    }

    initDraggable(): void {
        const viewModel: IViewModel = this;
        this.droppable = {
            dragConditionHandler(event: DragEvent, content?: any): boolean {
                return false;
            },
            async dragDropHandler(event: DragEvent): Promise<void> {
                await viewModel.resolveDragTarget(event);
            },
            dragEndHandler(event: DragEvent, content?: any): void {},
            dragStartHandler(event: DragEvent, content?: any): void {},
            dropConditionHandler(event: DragEvent, content?: any): boolean {
                return false;
            }

        }
    }

    async resolveDragTarget(event: DragEvent): Promise<void> {
        // case drop concerns nextcloud
        if (Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService.getContentContext()) {
            // nextcloud context
        } else {
            // case drop concerns workspace but we need extra check
            const document: any = JSON.parse(event.dataTransfer.getData("application/json"));
            // check if it s a workspace document with its identifier and format file to proceed move to nextcloud
            if (document && (document._id && document.eType === DocumentsType.FILE || document.eType === DocumentsType.FOLDER)) {
                if (angular.element(event.target).scope().folder instanceof SyncDocument) {
                    const syncedDocument: SyncDocument = angular.element(event.target).scope().folder;
                    let selectedDocuments: Array<Document> = WorkspaceEntcoreUtils.workspaceScope()['documentList']['_documents'];
                    let documentToUpdate: Set<string> = new Set(selectedDocuments.filter((file: Document) => file.selected).map((file: Document) => file._id));
                    documentToUpdate.add(document._id);
                    nextcloudService.moveDocumentWorkspaceToCloud(model.me.userId, Array.from(documentToUpdate), syncedDocument.path)
                        .then((_: AxiosResponse) => WorkspaceEntcoreUtils.updateWorkspaceDocuments(
                            WorkspaceEntcoreUtils.workspaceScope()['openedFolder']['folder'])
                        )
                        .catch((err: AxiosError) => {
                            const message: string = "Error while attempting to fetch documents children ";
                            console.error(message + err.message);
                        });
                }
            }
        }
    }

    watchFolderState(): void {
        let $folders: JQuery = $($folderTreeArrows);
        $folders.off();
        // use this const to make it accessible to its callback $folder on click event inner context
        const viewModel: IViewModel = this;
        $folders.click(this.onClickFolder(viewModel));
    }

    async openDocument(document: any): Promise<void> {
        let syncDocuments: Array<SyncDocument> = await nextcloudService.listDocument(model.me.userId, document.path ? document.path : null)
            .catch((err: AxiosError) => {
                const message: string = "Error while attempting to fetch documents children ";
                console.error(message + err.message);
                return [];
        });
        // first filter applies only when we happen to fetch its own folder and the second applies on document only
        document.children = syncDocuments.filter(NextcloudDocumentsUtils.filterRemoveOwnDocument(document)).filter(NextcloudDocumentsUtils.filterDocumentOnly());
        safeApply(this.scope);
        Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService
            .sendDocuments({
                parentDocument: document.path ? document : new SyncDocument().initParent(),
                documents: syncDocuments.filter(NextcloudDocumentsUtils.filterRemoveOwnDocument(document))
            });
    }

    setSwitchDisplayHandler(): void {
        const viewModel: IViewModel = this;
        const $workspaceFolderTree: JQuery = $(WorkspaceEntcoreUtils.$ENTCORE_WORKSPACE + ' li > a');
        // case nextcloud folder tree is interacted
        // checking if listener does not exist in order to create one
        $($nextcloudFolder)
            .off('click.workspaceNextcloudHandler')
            .on('click.workspaceNextcloudHandler', this.switchWorkspaceTreeHandler());

        // case entcore workspace folder tree is interacted
        // we unbind its handler and rebind it in order to keep our list of workspace updated
        $workspaceFolderTree
            .off('click.nextcloudHandler')
            .on('click.nextcloudHandler', this.switchNextcloudTreeHandler(viewModel));
    }

    /**
     * Remove workspace tree and use nextcloud tree instead.
     */
    private switchWorkspaceTreeHandler() {
        const viewModel: IViewModel = this;
        return function (): void {
            if (!viewModel.selectedFolder) {
                viewModel.folderTree.openFolder(viewModel.documents[0]);
            }
            const $workspaceFolderTree: JQuery = $(WorkspaceEntcoreUtils.$ENTCORE_WORKSPACE + ' li > a');

            // using nextcloud content display
            template.open('documents', `../../../${RootsConst.template}/behaviours/workspace-nextcloud`);

            // clear all potential "selected" class workspace folder tree
            $workspaceFolderTree.each((index: number, element: Element): void => element.classList.remove("selected"));

            // hide workspace progress bar
            WorkspaceEntcoreUtils.toggleProgressBarDisplay(false);
            // hide workspace buttons interactions
            WorkspaceEntcoreUtils.toggleWorkspaceButtonsDisplay(false);
            // hide workspace contents (search bar, menu, list of folder/files...) interactions
            WorkspaceEntcoreUtils.toggleWorkspaceContentDisplay(false);
        };
    }

    /**
     * Remove nextcloud tree and use workspace tree instead.
     */
    private switchNextcloudTreeHandler(viewModel: IViewModel) {
        return function (): void {
            // go back to workspace content display
            // clear nextCloudTree interaction
            viewModel.selectedFolder = null;
            arguments[0].currentTarget.classList.add('selected');
            // update workspace folder content
            WorkspaceEntcoreUtils.updateWorkspaceDocuments(angular.element(arguments[0].target).scope().folder);
            //set the right openedFolder
            WorkspaceEntcoreUtils.workspaceScope()['openedFolder']['folder'] = angular.element(arguments[0].target).scope().folder;
            // display workspace buttons interactions
            WorkspaceEntcoreUtils.toggleProgressBarDisplay(true);
            // display workspace buttons interactions
            WorkspaceEntcoreUtils.toggleWorkspaceButtonsDisplay(true);
            // display workspace contents (search bar, menu, list of folder/files...) interactions
            WorkspaceEntcoreUtils.toggleWorkspaceContentDisplay(true);
            // remove any content context cache
            Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService.setContentContext(null);
            template.open('documents', `icons`);
        };
    }

    private onClickFolder(viewModel: IViewModel) {
        return function () {
            event.stopPropagation();
            const scope: any = angular.element(arguments[0].target).scope();
            const folder: models.Element = scope.folder;
            if (viewModel.openedFolder.some((openFolder: models.Element) => openFolder === folder)) {
                viewModel.openedFolder = viewModel.openedFolder.filter((openedFolder: models.Element) => openedFolder !== folder);
            } else {
                viewModel.openedFolder.push(folder);
            }
            safeApply(scope);
        };
    }
}

export const workspaceNextcloudFolder = {
    title: 'nextcloud.folder',
    public: false,
    that: null,
    controller: {
        init: function (): void {
            if (model.me.hasWorkflow(rights.workflow.access)) {
                lang.addBundle('/nextcloud/i18n', async () => {
                    await nextcloudUserService.resolveUser(model.me.userId);
                    this.vm = new ViewModel(this, nextcloudService, nextcloudUserService);
                });
            }
        }
    }
};