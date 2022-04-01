import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import {nextcloudService} from '../nextcloud.service';
import {IDocumentResponse} from "../../models";

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
});
