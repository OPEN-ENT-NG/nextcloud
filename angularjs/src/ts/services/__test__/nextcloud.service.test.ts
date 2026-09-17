jest.mock('entcore-toolkit', () => Object.assign({}, (jest as any).requireActual('entcore-toolkit'), {
    http: {get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(), postFile: jest.fn(), putFile: jest.fn()},
}));

import {http} from 'entcore-toolkit';
import {nextcloudService} from '../nextcloud.service';
import {IDocumentResponse} from "../../models";
import {workspace} from "../../models/__mocks__/entcore";
import {mockHttpResponse} from "../../../../test-utils/httpMock";

describe('NextcloudService', () => {

    beforeEach(() => {
        (http.get as jest.Mock).mockReset();
        (http.post as jest.Mock).mockReset();
        (http.put as jest.Mock).mockReset();
    });

    it('test fetching nextcloud config url via axios', done => {
        const data = {url: "your_url"};

        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));

        nextcloudService.getNextcloudUrl().then((e) => {
            expect(http.get).toHaveBeenCalledWith(`/nextcloud/config/url`);
            expect(data.url).toEqual(e);
            done();
        });
    });

    it('test creating folder function with empty name', done => {
        const userId1 = "userId1";

        (http.post as jest.Mock).mockResolvedValueOnce(mockHttpResponse(undefined));

        nextcloudService.createFolder(userId1, "").then((e) => {
            expect(http.post).toHaveBeenCalledWith(`/nextcloud/files/user/userId1/create/folder`, {});
            done();
        });
    });

    it('test creating folder function', done => {
        const userId1 = "userId1";
        const test = "test";

        (http.post as jest.Mock).mockResolvedValueOnce(mockHttpResponse(undefined));

        nextcloudService.createFolder(userId1, "test").then((e) => {
            expect(http.post).toHaveBeenCalledWith(`/nextcloud/files/user/userId1/create/folder?path=test`, {});
            done();
        });
    });

    it('Test listDocument method', done => {
        const date1 = "2022-11-24T14:23:22.213Z"
        const date2 = "2022-11-22T14:23:22.213Z"
        const iDocumentResponse1: IDocumentResponse = {
            contentType: "doc",
            displayname: "displayname1",
            etag: "etag1",
            favorite: 1,
            fileId: 1,
            isFolder: false,
            ownerDisplayName: "ownerDisplayName1",
            path: "path1",
            size: 0,
            lastModified: date1
        }
        const iDocumentResponse2: IDocumentResponse = {
            contentType: "contentType2",
            displayname: "displayname2",
            etag: "etag2",
            favorite: 2,
            fileId: 2,
            isFolder: true,
            ownerDisplayName: "ownerDisplayName2",
            path: "path2",
            size: 0,
            lastModified: date2
        }
        const data = {data: [iDocumentResponse1, iDocumentResponse2]};

        const syncDocument1: any = {
            children: [],
            contentType: "doc",
            etag: "etag1",
            favorite: 1,
            fileId: 1,
            isFolder: false,
            name: "displayname1",
            ownerDisplayName: "ownerDisplayName1",
            path: "path1",
            size: 0,
            type: "file",
            role: "doc",
            editable: true,
            lastModified: date1
        }
        const syncDocument2: any = {
            children: [],
            contentType: "contentType2",
            etag: "etag2",
            favorite: 2,
            fileId: 2,
            isFolder: true,
            name: "displayname2",
            ownerDisplayName: "ownerDisplayName2",
            path: "path2",
            size: 0,
            type: "folder",
            role: "folder",
            editable: false,
            lastModified: date2
        }

        const userId1 = "userId1";
        const userId2 = "userId2";
        const path = "path";

        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));

        nextcloudService.listDocument(userId1, path).then(response => {
            response.forEach(res => {
               if (res.cacheChildren) delete res.cacheChildren;
               if (res.cacheDocument) delete res.cacheDocument;
            });
            expect(response).toEqual([syncDocument1, syncDocument2]);
            (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));

            nextcloudService.listDocument(userId2).then(response => {
                response.forEach(res => {
                    if (res.cacheChildren) delete res.cacheChildren;
                    if (res.cacheDocument) delete res.cacheDocument;
                });
                expect(response).toEqual([syncDocument1, syncDocument2]);
                done();
            });
        });
    });

    it('Test openNextcloudLink with root file', done => {
        const syncDocument1: any = {
            children: [],
            contentType: "doc",
            etag: "etag1",
            favorite: 1,
            fileId: 1,
            isFolder: false,
            name: "displayname1",
            ownerDisplayName: "ownerDisplayName1",
            path: "/",
            size: 0,
            type: "file",
            role: "doc",
            editable: true
        }

        const url = "url";
        const fileId = 1;

        window.open = jest.fn();
        nextcloudService.openNextcloudLink(syncDocument1, url);
        expect(window.open).toHaveBeenCalledTimes(1);
        expect(window.open).toHaveBeenCalledWith(url + "/index.php/apps/files?dir=/&openfile=" + fileId);
        done();
    });

    it('Test getFile method with file', done => {
        const userId = "userId";
        const path = "path";
        const fileName = "fileName";
        const contentType = "contentType";
        const isFolder = false;

        expect(nextcloudService.getFile(userId, fileName, path, contentType))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download?path=path&contentType=contentType&isFolder=false")
        expect(nextcloudService.getFile(userId, fileName, null, contentType))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download?isFolder=false")
        expect(nextcloudService.getFile(userId, fileName, path, null))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download?path=path&isFolder=false")
        expect(nextcloudService.getFile(userId, fileName, null, null))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download?isFolder=false")
        done();
    });

    it('Test getFile method with folder', done => {
        const userId = "userId";
        const path = "path";
        const fileName = "fileName";
        const contentType = "contentType";
        const isFolder = true;

        expect(nextcloudService.getFile(userId, fileName, path, contentType, isFolder))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download?path=path&contentType=contentType&isFolder=true")
        expect(nextcloudService.getFile(userId, fileName, null, contentType, isFolder))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download?isFolder=true")
        expect(nextcloudService.getFile(userId, fileName, path, null, isFolder))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download?path=path&isFolder=true")
        expect(nextcloudService.getFile(userId, fileName, null, null, isFolder))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download?isFolder=true")
        done();
    });

    it('Test getFiles method', done => {
        const userId = "userId";
        const path = "path";
        const files = [];

        expect(nextcloudService.getFiles(userId, path, files))
            .toEqual("/nextcloud/files/user/userId/multiple/download?path=path")
        files.push("file1")
        expect(nextcloudService.getFiles(userId, path, files))
            .toEqual("/nextcloud/files/user/userId/multiple/download?path=path&file=file1")
        files.push("file2")
        expect(nextcloudService.getFiles(userId, path, files))
            .toEqual("/nextcloud/files/user/userId/multiple/download?path=path&file=file1&file=file2")

        done();
    });

    it('Test Put moving document to nextcloud to workspace should have paths appeared in URL request', done => {
        const data = {data: []};

        const userId = "userId";
        const paths = ["path", "path1", "path2"];
        const parentId = "myParentId";

        (http.put as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));

        nextcloudService.moveDocumentNextcloudToWorkspace(userId, paths, parentId).then(() => {
            expect(http.put).toHaveBeenCalledWith('/nextcloud/files/user/userId/move/workspace?path=path&path=path1&path=path2&parentId=myParentId');
            done();
        });
    });

    it('Test Put moving document to nextcloud to workspace should have paths appeared in URL request without parentId', done => {
        const data = {data: []};

        const userId = "userId";
        const paths = ["path", "path1", "path2"];

        (http.put as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));

        nextcloudService.moveDocumentNextcloudToWorkspace(userId, paths).then(() => {
            expect(http.put).toHaveBeenCalledWith('/nextcloud/files/user/userId/move/workspace?path=path&path=path1&path=path2');
            done();
        });
    });


    it('Test Put moving document workspace to nextcloud should have paths appeared in URL request with cloud name document', done => {
        const userId = "userId";
        const ids = ["899de998-af86-4feb-99dc-af86dc8fa57e", "fb3109af-d315-4614-a2e8-239b233cbd4c", "3475ed1a-2345-4558-a6dc-515c331eb11d"];
        const cloudDocumentName = "Documents/test";

        const expectedEndpoint: string = '/nextcloud/files/user/userId/workspace/move/cloud' +
            '?id=899de998-af86-4feb-99dc-af86dc8fa57e&id=fb3109af-d315-4614-a2e8-239b233cbd4c&id=3475ed1a-2345-4558-a6dc-515c331eb11d' +
            '&parentName=Documents/test';

        (http.put as jest.Mock).mockResolvedValueOnce(mockHttpResponse(undefined));

        nextcloudService.moveDocumentWorkspaceToCloud(userId, ids, cloudDocumentName).then(() => {
            expect(http.put).toHaveBeenCalledWith(expectedEndpoint);
            done();
        });
    });

    it('Test Put moving document workspace to nextcloud should have paths appeared in URL request without cloud name document', done => {
        const userId = "userId";
        const ids = ["899de998-af86-4feb-99dc-af86dc8fa57e", "fb3109af-d315-4614-a2e8-239b233cbd4c", "3475ed1a-2345-4558-a6dc-515c331eb11d"];

        const expectedEndpoint: string = '/nextcloud/files/user/userId/workspace/move/cloud' +
            '?id=899de998-af86-4feb-99dc-af86dc8fa57e&id=fb3109af-d315-4614-a2e8-239b233cbd4c&id=3475ed1a-2345-4558-a6dc-515c331eb11d';

        (http.put as jest.Mock).mockResolvedValueOnce(mockHttpResponse(undefined));

        nextcloudService.moveDocumentWorkspaceToCloud(userId, ids).then(() => {
            expect(http.put).toHaveBeenCalledWith(expectedEndpoint);
            done();
        });
    });

    it('Test move document should require action param such as RENAME document/folder', done => {
        const userId = "userId";
        const expectedEndpoint: string = `/nextcloud/files/user/userId/move?path=path1&destPath=path2`;

        (http.put as jest.Mock).mockResolvedValueOnce(mockHttpResponse(undefined));

        nextcloudService.moveDocument(userId, "path1", "path2").then(() => {
            expect(http.put).toHaveBeenCalledWith(expectedEndpoint);
            done();
        });
    });

    it('Test move document should require action param such as MOVE document/folder', done => {
        const userId = "userId";
        const expectedEndpoint: string = `/nextcloud/files/user/userId/move?path=path1&destPath=path2`;

        (http.put as jest.Mock).mockResolvedValueOnce(mockHttpResponse(undefined));

        nextcloudService.moveDocument(userId, "path1", "path2").then(() => {
            expect(http.put).toHaveBeenCalledWith(expectedEndpoint);
            done();
        });
    });

});
