import axios, {AxiosResponse} from 'axios';
import MockAdapter from 'axios-mock-adapter';
import {nextcloudService} from '../nextcloud.service';
import {IDocumentResponse} from "../../models";
import http from "axios";

describe('NextcloudService', () => {
    it('Test listDocument method', done => {
        const mock = new MockAdapter(axios);
        const iDocumentResponse1: IDocumentResponse = {
            contentType: "contentType1",
            displayname: "displayname1",
            etag: "etag1",
            favorite: 1,
            fileId: 1,
            isFolder: false,
            ownerDisplayName: "ownerDisplayName1",
            path: "path1",
            size: 0
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
            size: 0
        }
        const data = {data: [iDocumentResponse1, iDocumentResponse2]};

        const syncDocument1: any = {
            children: [],
            contentType: "contentType1",
            etag: "etag1",
            favorite: 1,
            fileId: 1,
            isFolder: false,
            name: "displayname1",
            ownerDisplayName: "ownerDisplayName1",
            path: "path1",
            size: 0,
            role: "unknown"
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
            role: "folder"
        }

        const userId1 = "userId1";
        const userId2 = "userId2";
        const path = "path";

        mock.onGet(`/nextcloud/files/user/userId1?path=path`).reply(200, data);


        nextcloudService.listDocument(userId1, path).then(response => {
            expect(response).toEqual([syncDocument1, syncDocument2])
            mock.onGet(`/nextcloud/files/user/userId2`).reply(200, data);


            nextcloudService.listDocument(userId2).then(response => {
                expect(response).toEqual([syncDocument1, syncDocument2])
                done();
            });
        });
    });

    it('Test getFile method', done => {
        const mock = new MockAdapter(axios);

        const userId = "userId";
        const path = "path";
        const fileName = "fileName";
        const contentType = "contentType";

        expect(nextcloudService.getFile(userId, fileName, path, contentType))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download?path=path&contentType=contentType")
        expect(nextcloudService.getFile(userId, fileName, null, contentType))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download")
        expect(nextcloudService.getFile(userId, fileName, path, null))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download?path=path")
        expect(nextcloudService.getFile(userId, fileName, null, null))
            .toEqual("/nextcloud/files/user/userId/file/fileName/download")
        done();
    });

    it('Test getFiles method', done => {
        const mock = new MockAdapter(axios);

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
        const mock = new MockAdapter(axios);
        const data = {response: true};

        const userId = "userId";
        const paths = ["path", "path1", "path2"];
        const parentId = "myParentId";

        let spy = jest.spyOn(axios, "put");
        mock.onPut('/nextcloud/files/user/userId/move/workspace?path=path&path=path1&path=path2&parentId=myParentId')
            .reply(200, data);

        nextcloudService.moveDocumentNextcloudToWorkspace(userId, paths, parentId).then(() => {
            expect(spy).toHaveBeenCalledWith( '/nextcloud/files/user/userId/move/workspace?path=path&path=path1&path=path2&parentId=myParentId');
            done();
        });
    });

    it('Test Put moving document to nextcloud to workspace should have paths appeared in URL request without parentId', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true};

        const userId = "userId";
        const paths = ["path", "path1", "path2"];

        let spy = jest.spyOn(axios, "put");
        mock.onPut('/nextcloud/files/user/userId/move/workspace?path=path&path=path1&path=path2').reply(200, data);

        nextcloudService.moveDocumentNextcloudToWorkspace(userId, paths).then(() => {
            expect(spy).toHaveBeenCalledWith( '/nextcloud/files/user/userId/move/workspace?path=path&path=path1&path=path2');
            done();
        });
    });


    it('Test Put moving document workspace to nextcloud should have paths appeared in URL request with cloud name document', done => {
        const mock = new MockAdapter(axios);

        const userId = "userId";
        const ids = ["899de998-af86-4feb-99dc-af86dc8fa57e", "fb3109af-d315-4614-a2e8-239b233cbd4c", "3475ed1a-2345-4558-a6dc-515c331eb11d"];
        const cloudDocumentName = "Documents/test";

        const expectedEndpoint: string = '/nextcloud/files/user/userId/workspace/move/cloud' +
            '?id=899de998-af86-4feb-99dc-af86dc8fa57e&id=fb3109af-d315-4614-a2e8-239b233cbd4c&id=3475ed1a-2345-4558-a6dc-515c331eb11d' +
            '&parentName=Documents/test';

        let spy = jest.spyOn(axios, "put");
        mock.onPut(expectedEndpoint).reply(200);

        nextcloudService.moveDocumentWorkspaceToCloud(userId, ids, cloudDocumentName).then(() => {
            expect(spy).toHaveBeenCalledWith(expectedEndpoint);
            done();
        });
    });

    it('Test Put moving document workspace to nextcloud should have paths appeared in URL request without cloud name document', done => {
        const mock = new MockAdapter(axios);

        const userId = "userId";
        const ids = ["899de998-af86-4feb-99dc-af86dc8fa57e", "fb3109af-d315-4614-a2e8-239b233cbd4c", "3475ed1a-2345-4558-a6dc-515c331eb11d"];

        const expectedEndpoint: string = '/nextcloud/files/user/userId/workspace/move/cloud' +
            '?id=899de998-af86-4feb-99dc-af86dc8fa57e&id=fb3109af-d315-4614-a2e8-239b233cbd4c&id=3475ed1a-2345-4558-a6dc-515c331eb11d';

        let spy = jest.spyOn(axios, "put");
        mock.onPut(expectedEndpoint).reply(200);

        nextcloudService.moveDocumentWorkspaceToCloud(userId, ids).then(() => {
            expect(spy).toHaveBeenCalledWith(expectedEndpoint);
            done();
        });
    });

});
