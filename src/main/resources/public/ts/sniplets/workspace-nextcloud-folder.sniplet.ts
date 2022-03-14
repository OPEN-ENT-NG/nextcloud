import {FolderTreeProps, angular, template, Behaviours, workspace, model, idiom as lang} from "entcore";
import {Tree} from "entcore/types/src/ts/workspace/model";
import {safeApply} from "../utils/safe-apply.utils";
import {RootsConst} from "../core/constants/roots.const";
import {NEXTCLOUD_APP} from "../nextcloud.behaviours";
import models = workspace.v2.models;
import {WorkspaceEntcoreUtils} from "../utils/workspace-entcore.utils";
import {INextcloudService, nextcloudService} from "../services";
import {AxiosError} from "axios";
import {Draggable, NextcloudPreference, SyncDocument} from "../models";

declare let window: any;

// JQuery that fetches all folder element by its icons
const $folderTreeArrows: string = '#nextcloud-folder-tree i';
const $nextcloudFolder: string = '#nextcloud-folder-tree';

interface IViewModel {
    initTree(folder: Array<SyncDocument>): void;
    watchFolderState(): void;
    openDocument(folder: any): void;
    initDraggable(): void;
    setSwitchDisplayHandler(): void;

    userPreference: NextcloudPreference;
    folderTree: FolderTreeProps;
    selectedFolder: models.Element;
    openedFolder: Array<models.Element>;
    droppable: Draggable;

    documents: Array<SyncDocument>;
}

class ViewModel implements IViewModel {
    private nextcloudService: INextcloudService;
    private scope: any;

    userPreference: NextcloudPreference;
    folderTree: FolderTreeProps;
    selectedFolder: models.Element;
    openedFolder: Array<models.Element> = [];
    droppable: Draggable;
    documents: Array<SyncDocument>;

    constructor(scope, nextcloudService: INextcloudService) {
        this.scope = scope;
        this.nextcloudService = nextcloudService;
        this.userPreference = this.scope.userPreference;
        this.folderTree = {};
        this.selectedFolder = null;
        this.openedFolder = [];


        this.initTree([new SyncDocument().initParent()]);
        this.initDraggable();
        safeApply(this.scope);
        // init folder and fetch only folder todo
        // Promise.all<Array<SyncDocument>>([
        //     nextcloudService.listDocument(model.me.login)
        //     // todo need to fetch user nextcloud as well
        // ])
        //     .then(([syncDocument]) => {
        //         // filtering by taking folder only and not taking folder with login name included
        //         const folder: Array<SyncDocument> = syncDocument.filter(this.filterDocumentOnly());
        //         this.initTree([new SyncDocument().initParent()]);
        //         this.initDraggable();
        //         safeApply(this.scope);
        //     })
        //     .catch((err: AxiosError) => {
        //         const message: string = "Error while attempting to fetch user and sync documents ";
        //         console.error(message + err.message);
        //     });
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
                viewModel.watchFolderState();
                viewModel.selectedFolder = folder;
                if (!viewModel.openedFolder.some((openFolder: models.Element) => openFolder === folder)) {
                    viewModel.openedFolder.push(folder);
                }

                // synchronize documents and send content to its other sniplet content
                viewModel.openDocument(folder);
            },
        };
    }

    initDraggable(): void {
        this.droppable = {
            dragConditionHandler(event: DragEvent, content?: any): boolean {
                return false;
            },
            dragDropHandler(event: DragEvent): void {
                console.log("FolderTreeitemDrop: ", event.target, "content: ",
                    Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService.getContentContext());
            },
            dragEndHandler(event: DragEvent, content?: any): void {},
            dragStartHandler(event: DragEvent, content?: any): void {},
            dropConditionHandler(event: DragEvent, content?: any): boolean {
                return false;
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
        let syncDocuments: Array<SyncDocument> = await nextcloudService.listDocument(model.me.login, document.path ? document.name : null)
            .catch((err: AxiosError) => {
                const message: string = "Error while attempting to fetch documents children ";
                console.error(message + err.message);
                return [];
        });
        // first filter applies only when we happen to fetch its own folder and the second applies on document only
        document.children = syncDocuments.filter(this.filterRemoveOwnDocument(document)).filter(this.filterDocumentOnly());
        safeApply(this.scope);
        Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService
            .sendDocuments(syncDocuments.filter(this.filterRemoveOwnDocument(document)));
    }

    /* Filter mode */
    private filterDocumentOnly() {
        return (syncDocument: SyncDocument) => syncDocument.isFolder && syncDocument.name != model.me.login;
    }

    private filterRemoveOwnDocument(document: SyncDocument) {
        return (syncDocument: SyncDocument) => syncDocument.path !== document.path;
    }

    setSwitchDisplayHandler(): void {
        const viewModel: IViewModel = this;
        const $workspaceFolderTree: JQuery = $(WorkspaceEntcoreUtils.$ENTCORE_WORKSPACE);
        // case nextcloud folder tree is interacted
        $($nextcloudFolder).click(this.switchWorkspaceTreeHandler());
        // case entcore workspace folder tree is interacted
        $workspaceFolderTree.click(this.switchNextcloudTreeHandler(viewModel));
    }

    private switchWorkspaceTreeHandler() {
        return function (): void {
            const $workspaceFolderTree: JQuery = $(WorkspaceEntcoreUtils.$ENTCORE_WORKSPACE + ' li > a');
            
            // using nextcloud content display
            template.open('documents', `../../../${RootsConst.template}/behaviours/workspace-nextcloud`);

            // clear all potential "selected" class workspace folder tree
            $workspaceFolderTree.each((index: number, element: Element): void => {
                element.classList.remove("selected");
            });

            // hide workspace progress bar
            WorkspaceEntcoreUtils.toggleProgressBarDisplay(false);
            // hide workspace buttons interactions
            WorkspaceEntcoreUtils.toggleWorkspaceButtonsDisplay(false);
            // hide workspace contents (search bar, menu, list of folder/files...) interactions
            WorkspaceEntcoreUtils.toggleWorkspaceContentDisplay(false);
        };
    }

    private switchNextcloudTreeHandler(viewModel: IViewModel) {
        return function (): void {
            // go back to workspace content display
            // clear nextCloudTree interaction
            viewModel.openedFolder = [];
            viewModel.selectedFolder = null;
            arguments[0].currentTarget.classList.add('selected');

            // display workspace buttons interactions
            WorkspaceEntcoreUtils.toggleProgressBarDisplay(true);
            // display workspace buttons interactions
            WorkspaceEntcoreUtils.toggleWorkspaceButtonsDisplay(true);
            // display workspace contents (search bar, menu, list of folder/files...) interactions
            WorkspaceEntcoreUtils.toggleWorkspaceContentDisplay(true);
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
            lang.addBundle('/nextcloud/i18n', () => {
                this.userPreference = new NextcloudPreference(nextcloudService).init();
                this.vm = new ViewModel(this, nextcloudService);
            });
        }
    }
};