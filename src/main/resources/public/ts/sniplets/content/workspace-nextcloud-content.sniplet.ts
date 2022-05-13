import {Behaviours, model, idiom as lang, angular, Document, workspace} from "entcore";
import {NEXTCLOUD_APP} from "../../nextcloud.behaviours";
import {Subscription} from "rxjs";
import {Draggable, SyncDocument} from "../../models";
import {safeApply} from "../../utils/safe-apply.utils";
import {INextcloudService, nextcloudService} from "../../services";
import {ToolbarSnipletViewModel} from "./workspace-nextcloud-toolbar.sniplet";
import {AxiosError, AxiosResponse} from "axios";
import {UploadFileSnipletViewModel} from "./workspace-nextcloud-upload-file.sniplet";
import models = workspace.v2.models;

declare let window: any;

const nextcloudTree: string = 'nextcloud-folder-tree';

interface IViewModel {
    // util
    safeApply(): void;

    initDraggable(): void;
    onSelectContent(document: SyncDocument): void;
    onOpenContent(document: SyncDocument): void;
    getFile(document: SyncDocument): string;

    draggable: Draggable;
    lockDropzone: boolean;
    parentDocument: SyncDocument;
    documents: Array<SyncDocument>;
    selectedDocuments: Array<SyncDocument>;

    // drag & drop action
    moveDocument(element: any, document: SyncDocument): Promise<void>;

    // dropzone
    isDropzoneEnabled(): boolean;
    canDropOnFolder(): boolean;
    onCannotDropFile(): void;
}

class ViewModel implements IViewModel {
    private scope: any;
    private nextcloudService: INextcloudService;

    subscriptions: Subscription = new Subscription();
    draggable: Draggable;
    lockDropzone: boolean;

    parentDocument: SyncDocument;
    documents: Array<SyncDocument>;
    selectedDocuments: Array<SyncDocument>;

    constructor(scope, nextcloudService: INextcloudService) {
        this.scope = scope;
        this.nextcloudService = nextcloudService;
        this.documents = [new SyncDocument()];

        // on init we first sync its main folder content
        this.initDocumentsContent(nextcloudService, scope);

        // on receive documents from folder-tree sniplet
        this.subscriptions.add(Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService
            .getDocumentsState()
            .subscribe((res: {parentDocument: SyncDocument, documents: Array<SyncDocument>}) => {
                if (res.documents && res.documents.length > 0) {
                    this.parentDocument = res.parentDocument;
                    this.documents = res.documents.filter((syncDocument: SyncDocument) => syncDocument.name != model.me.userId);
                } else {
                    this.parentDocument = res.parentDocument;
                    this.documents = [];
                }
                safeApply(scope);
            }));

        this.initDraggable();

        scope.$parent.$on("$destroy", () => {
            this.subscriptions.unsubscribe();
        });
    }

    private initDocumentsContent(nextcloudService: INextcloudService, scope) {
        let selectedFolderFromNextcloudTree: SyncDocument = this.getNextcloudTreeController()['selectedFolder'];
        nextcloudService.listDocument(model.me.userId, selectedFolderFromNextcloudTree.path)
            .then((documents: Array<SyncDocument>) => {
                this.documents = documents
                    .filter((syncDocument: SyncDocument) => syncDocument.path != selectedFolderFromNextcloudTree.path)
                    .filter((syncDocument: SyncDocument) => syncDocument.name != model.me.userId);
                this.parentDocument = new SyncDocument().initParent();
                safeApply(scope);
            })
            .catch((err: AxiosError) => {
                const message: string = "Error while attempting to fetch documents children from content";
                console.error(message + err.message);
                return [];
            });
    }

    getNextcloudTreeController(): ng.IScope {
        return angular.element(document.getElementById(nextcloudTree)).scope()['vm'];
    }

    safeApply(): void {
        safeApply(this.scope);
    }

    initDraggable(): void {
        // use this const to make it accessible to its folderTree inner context
        const viewModel: IViewModel = this;
        this.draggable = {
           dragConditionHandler(event: DragEvent, content?: any): boolean {
               return false;
           },
           dragDropHandler(event: DragEvent, content?: any): void {
           },
           async dragEndHandler(event: DragEvent, content?: any): Promise<void> {
               await viewModel.moveDocument(document.elementFromPoint(event.x, event.y), content);
               viewModel.lockDropzone = false;
               viewModel.safeApply();
           },
           dragStartHandler(event: DragEvent, content?: any): void {
               viewModel.lockDropzone = true;
               try {
                   event.dataTransfer.setData('application/json', JSON.stringify(content));
               } catch (e) {
                   event.dataTransfer.setData('Text', JSON.stringify(content));
               }
               Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService.setContentContext(content);
           },
           dropConditionHandler(event: DragEvent, content?: any): boolean {
               return true;
           }
        }
    }

    async moveDocument(element: any, document: SyncDocument): Promise<void> {
        let folderContent: any = angular.element(element).scope();
        if (folderContent && folderContent.folder) {
            if (folderContent.folder instanceof models.Element) {
                this.moveDocumentToWorkspace(folderContent.folder, document)
                    .then(async (_: AxiosResponse) => {
                        return nextcloudService.listDocument(model.me.userId, this.parentDocument.path ?
                           this.parentDocument.path : null);
                    })
                    .then((syncedDocument: Array<SyncDocument>) => {
                        this.documents = syncedDocument.filter((syncDocument: SyncDocument) => syncDocument.name != model.me.userId);
                        this.safeApply();
                    })
                    .catch((err: AxiosError) => {
                        const message: string = "Error while attempting to move nextcloud document to workspace " +
                            "or update nextcloud list";
                        console.error(message + err.message);
                    });
            }
            if (folderContent.folder instanceof SyncDocument) {
                // if interacted into nextcloud
            }
        }
    }

    async moveDocumentToWorkspace(folder: models.Element, document: SyncDocument): Promise<AxiosResponse> {
        return nextcloudService.moveDocumentNextcloudToWorkspace(model.me.userId, [document.path], folder._id);
    }

    onSelectContent(content: SyncDocument): void {
        this.selectedDocuments = this.documents.filter((document: SyncDocument) => document.selected);
    }

    onOpenContent(document: SyncDocument): void {
        if (document.isFolder) {
            Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService.sendOpenFolderDocument(document);
        } else {
            window.open(this.getFile(document));
        }
    }

    getFile(document: SyncDocument): string {
        return this.nextcloudService.getFile(model.me.userId, document.name, document.path, document.contentType);
    }

    isDropzoneEnabled(): boolean {
        return !this.lockDropzone;
    }

    canDropOnFolder(): boolean {
        return true;
    }

    onCannotDropFile(): void {

    }
}

export const workspaceNextcloudContent = {
    title: 'nextcloud.content',
    public: false,
    that: null,
    controller: {
        init: async function (): Promise<void> {
            lang.addBundle('/nextcloud/i18n', () => {
                this.vm = new ViewModel(this, nextcloudService);
                this.vm.toolbar = new ToolbarSnipletViewModel(this);
                this.vm.upload = new UploadFileSnipletViewModel(this);
            });
        },
    }

};