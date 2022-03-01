import {FolderTreeProps, angular, template, Behaviours, workspace} from "entcore";
import {nextcloudMockup} from "../models/__mocks__/nextcloud.model.test";
import {Tree} from "entcore/types/src/ts/workspace/model";
import {safeApply} from "../utils/safe-apply.utils";
import {RootsConst} from "../core/constants/roots.const";
import {NEXTCLOUD_APP} from "../nextcloud.behaviours";
import models = workspace.v2.models;
import {WorkspaceEntcoreUtils} from "../utils/workspace-entcore.utils";
import {Draggable} from "../models/nextcloud-draggable.model";

declare let window: any;

// JQuery that fetches all folder element by its icons
const $folderTreeArrows: string = '#nextcloud-folder-tree i';
const $nextcloudFolder: string = '#nextcloud-folder-tree';

interface IViewModel {
    initTree(): void;
    watchFolderState(): void;
    initDraggable(): void;

    folderTree: FolderTreeProps;
    selectedFolder: models.Element;
    openedFolder: Array<models.Element>;
    droppable: Draggable;
}

class ViewModel implements IViewModel {
    folderTree: FolderTreeProps;
    selectedFolder: models.Element;
    openedFolder: Array<models.Element> = [];
    droppable: Draggable;

    constructor() {
        this.folderTree = {};
        this.selectedFolder = null;
        this.openedFolder = [];
        this.initTree();
        this.initDraggable();
        this.setSwitchDisplayHandler();
    }

    initTree(): void {
        // use this const to make it accessible to its folderTree inner context
        const viewModel: IViewModel = this;
        this.folderTree = {
            cssTree: "folders-tree",
            get trees(): any | Array<Tree> {
                return nextcloudMockup;
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
                // create handler in case icon are only clicked
                viewModel.watchFolderState();
                viewModel.selectedFolder = folder;
                if (!viewModel.openedFolder.some((openFolder: models.Element) => openFolder === folder)) {
                    viewModel.openedFolder.push(folder);
                }
                var workspace = angular.element(document.querySelector(WorkspaceEntcoreUtils.$ENTCORE_WORKSPACE)).scope();
                workspace.openedFolder = new models.FolderContext;

                Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService.sendFolder("from folder");
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

    private setSwitchDisplayHandler(): void {
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
        init: async function (): Promise<void> {
            this.vm = new ViewModel();
        }
    }
};