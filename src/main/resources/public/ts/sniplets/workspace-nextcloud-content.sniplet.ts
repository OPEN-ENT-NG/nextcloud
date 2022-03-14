import {Behaviours, model, idiom as lang} from "entcore";
import {NEXTCLOUD_APP} from "../nextcloud.behaviours";
import {Subscription} from "rxjs";
import {Draggable, SyncDocument} from "../models";
import {safeApply} from "../utils/safe-apply.utils";
import {INextcloudService, nextcloudService} from "../services";

declare let window: any;

interface IViewModel {
    initDraggable(): void;
    onSelectContent(content: any): void;
    onOpenContent(content: any): void;
    getFile(path: string): string;
    draggable: Draggable;
    documents: Array<SyncDocument>;
}

class ViewModel implements IViewModel {
    private scope: any;
    private nextService: INextcloudService;

    documents: Array<SyncDocument>;
    subscriptions: Subscription = new Subscription();
    draggable: Draggable;

    constructor(scope, nextService: INextcloudService) {
        this.scope = scope;
        this.nextService = nextService;
        this.subscriptions.add(Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService
            .getDocumentsState()
            .subscribe((documents: Array<SyncDocument>) => {
                this.documents = documents.filter((syncDocument: SyncDocument) => syncDocument.name != model.me.login);
                console.log("nextcloud-content received: ", this.documents);
                safeApply(scope);
            }));

        // this.folderContents = [
        //     {
        //         eParent: "3dcbeec3-4b3a-4114-8c4f-96a41e7a09fe",
        //         _id: "3b97fa07-021d-4090-bbcc-4c97d8c080e8",
        //         eType: "folder",
        //         name: "cc"
        //     },
        //     {
        //         eParent: "3dcbeec3-4b3a-4114-8c4f-96a41e7a09fe",
        //         _id: "3b97fa07-021d-4090-bbcc-4c97d8c080e8",
        //         eType: "file",
        //         name: "my_file"
        //     }
        // ]

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

    onSelectContent(content: any): void {
        console.log("onSelectItem: ", content);
    };

    onOpenContent(content: any): void {
        console.log("openingContent: ", content);
    };

    getFile(path: string): string {
        return this.nextService.getFile(model.me.login, path);
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
            });
        },
    }

};