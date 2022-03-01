import {angular, Behaviours, FolderTreeProps, workspace} from "entcore";
import {NEXTCLOUD_APP} from "../nextcloud.behaviours";
import models = workspace.v2.models;
import {Subscription} from "rxjs";
import {Tree} from "entcore/types/src/ts/workspace/model";
import {nextcloudMockup} from "../models/__mocks__/nextcloud.model.test";
import {WorkspaceEntcoreUtils} from "../utils/workspace-entcore.utils";
import {Draggable} from "../models/nextcloud-draggable.model";

declare let window: any;

interface IViewModel {
    initDraggable(): void;
    onSelectContent(content: any): void;
    onOpenContent(content: any): void;
    draggable: Draggable;
    folderContents: Array<any>;
}

class ViewModel implements IViewModel {
    folderContents: Array<any>;
    subscriptions: Subscription = new Subscription();
    draggable: Draggable;

    constructor(scope) {
        this.subscriptions.add(Behaviours.applicationsBehaviours[NEXTCLOUD_APP].nextcloudService
            .getFolderState()
            .subscribe((state: string) => {
                console.log("nextcloud-content received: ", state);
            }));

        this.folderContents = [
            {
                eParent: "3dcbeec3-4b3a-4114-8c4f-96a41e7a09fe",
                _id: "3b97fa07-021d-4090-bbcc-4c97d8c080e8",
                eType: "folder",
                name: "cc"
            },
            {
                eParent: "3dcbeec3-4b3a-4114-8c4f-96a41e7a09fe",
                _id: "3b97fa07-021d-4090-bbcc-4c97d8c080e8",
                eType: "file",
                name: "my_file"
            }
        ]

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
}

export const workspaceNextcloudContent = {
    title: 'nextcloud.content',
    public: false,
    that: null,
    controller: {
        init: async function (): Promise<void> {
            this.vm = new ViewModel(this);
        },
    }

};