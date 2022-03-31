export class WorkspaceEntcoreUtils {

    static $ENTCORE_WORKSPACE: string = `div[data-ng-include="'folder-content'"]`;

    /**
     * Will fetch <progress-bar> Element type component and its div to toggle hide or show depending on state
     * Format date based on given format using moment
     * @param state boolean determine display default or none
     */
    static toggleProgressBarDisplay(state: boolean): void {
        const htmlQuery: string = '.mobile-navigation > div.row';
        (<HTMLElement>document.querySelector(htmlQuery)).style.display = state ? "block" : "none";
    }

    /**
     * Will fetch all buttons in workspace folder its div to toggle hide or show depending on state
     * @param state boolean determine display default or none
     */
    static toggleWorkspaceButtonsDisplay(state: boolean): void {
        const htmlQuery: string = `.mobile-navigation > a, sniplet[application="lool"`;
        Array.from(document.querySelectorAll(htmlQuery))
            .forEach((elem: Element) => (<HTMLElement>elem).style.display =  state ? "block" : "none");
    }

    /**
     * Will fetch all buttons in workspace folder its div to toggle hide or show depending on state
     * @param state boolean determine display default or none
     */
    static toggleWorkspaceContentDisplay(state: boolean): void {
        const searchImportViewQuery: string = 'section .margin-four > h3, section .margin-four > nav';
        Array.from(document.querySelectorAll(searchImportViewQuery))
            .forEach((elem: Element) => (<HTMLElement>elem).style.display =  state ? "block" : "none");

        const contentEmptyScreenQuery: string = 'div .toggle-buttons-spacer .emptyscreen';
        Array.from(document.querySelectorAll(contentEmptyScreenQuery))
            .forEach((elem: Element) => (<HTMLElement>elem).style.display =  state ? "flex" : "none");
    }
}