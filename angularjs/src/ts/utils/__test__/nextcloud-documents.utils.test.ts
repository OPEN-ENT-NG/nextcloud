import {NextcloudDocumentsUtils} from "../nextcloud-documents.utils";
import {DocumentRole} from "../../core/enums/document-role";

describe('format', () => {

    test("Using document utils with known type", () => {
        expect(NextcloudDocumentsUtils.determineRole("doc")).toEqual(DocumentRole.DOC);
    });

    test("Using document utils with unknown type", () => {
        expect(NextcloudDocumentsUtils.determineRole("xyz")).toEqual(DocumentRole.UNKNOWN);
    });
});
