import {Behaviours, model, idiom as lang} from "entcore";
import {NEXTCLOUD_APP} from "../../nextcloud.behaviours";
import {Subscription} from "rxjs";
import {Draggable, SyncDocument} from "../../models";
import {safeApply} from "../../utils/safe-apply.utils";
import {INextcloudService, nextcloudService} from "../../services";
import {ToolbarSnipletViewModel} from "./workspace-nextcloud-toolbar.sniplet";

declare let window: any;

interface IViewModel {
    initDraggable(): void;
    onSelectContent(document: SyncDocument): void;
    onOpenContent(document: SyncDocument): void;
    getFile(document: SyncDocument): string;
    draggable: Draggable;

    pathParent: string;
    documents: Array<SyncDocument>;
    selectedDocuments: Array<SyncDocument>;
}

class ViewModel implements IViewModel {
    private scope: any;
    private nextcloudService: INextcloudService;

    subscriptions: Subscription = new Subscription();
    draggable: Draggable;

    pathParent: string;
    documents: Array<SyncDocument>;
    selectedDocuments: Array<SyncDocument>;

    constructor(scope, nextcloudService: INextcloudService) {
        this.scope = scope;
        this.nextcloudService = nextcloudService;
        this.documents = [new SyncDocument()];

        // on receive documents from folder-tree sniplet
        this.subscriptions.add(Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService
            .getDocumentsState()
            .subscribe((res: {path: string, documents: Array<SyncDocument>}) => {
                if (res.documents && res.documents.length > 0) {
                    this.pathParent = res.path;
                    this.documents = res.documents.filter((syncDocument: SyncDocument) => syncDocument.name != model.me.login);
                } else {
                    this.pathParent = res.path;
                    this.documents = [];
                }
                safeApply(scope);
            }));

        this.initDraggable();

        scope.$parent.$on("$destroy", () => {
            this.subscriptions.unsubscribe();
        });
    }

    initDraggable(): void {
        // use this const to make it accessible to its folderTree inner context
        const viewModel: IViewModel = this;
        this.draggable = {
           dragConditionHandler(event: DragEvent, content?: any): boolean {
               return false;
           },
           dragDropHandler(event: DragEvent, content?: any): void {
               console.log("itemDrop: ", content, " event: ", event);
           },
           dragEndHandler(event: DragEvent, content?: any): void {
               console.log("finishDrag: ", content, " event: ", event);
           },
           dragStartHandler(event: DragEvent, content?: any): void {
               console.log("itemDrag: ", content, " event: ", event);
               try {
                   event.dataTransfer.setData('application/json', JSON.stringify(content));
               } catch (e) {
                   event.dataTransfer.setData('Text', JSON.stringify(content));
               }
               Behaviours.applicationsBehaviours[NEXTCLOUD_APP]
                   .nextcloudService.setContentContext("dragging content: " +  content.name);
           },
           dropConditionHandler(event: DragEvent, content?: any): boolean {
               return true;
           }
        }
    }

    onSelectContent(content: SyncDocument): void {
        this.selectedDocuments = this.documents.filter((document: SyncDocument) => document.selected);
        console.log("selectedDocuments: ", this.selectedDocuments);
    };

    onOpenContent(document: SyncDocument): void {
        if (document.isFolder) {
            Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService.sendOpenFolderDocument(document);
        } else {
            window.open(this.getFile(document));
        }
    };

    getFile(document: SyncDocument): string {
        return this.nextcloudService.getFile(model.me.login, document.name, document.path, document.contentType);
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
                this.vm.toolbar = new ToolbarSnipletViewModel(this.vm);
            });
        },
    }

};