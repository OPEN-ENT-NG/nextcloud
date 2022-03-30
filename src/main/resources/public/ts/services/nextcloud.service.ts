import {ng} from 'entcore'
import http, {AxiosResponse} from 'axios';
import {IDocumentResponse, Meta, SyncDocument} from "../models";

export interface INextcloudService {
    listDocument(userid: string, path?: string): Promise<Array<SyncDocument>>;
    getFile(userid: string, fileName: string, path: string, contentType: string): string;
    getFiles(userid: string, path: string, files: Array<string>): string;
    test(): Promise<AxiosResponse>;
}

export const nextcloudService: INextcloudService = {

    listDocument: async (userid: string, path?: string): Promise<Array<SyncDocument>> => {
        const urlParam: string = path ? `?path=${path}` : '';
        return http.get(`/nextcloud/files/user/${userid}${urlParam}`)
            .then((res: AxiosResponse) => res.data.data.map((document: IDocumentResponse) => new SyncDocument().build(document)));
    },

    getFile: (userid: string, fileName: string, path: string, contentType: string): string => {
        const pathParam: string = path ? `?path=${path}` : '';
        const contentTypeParam: string = path && contentType ? `&contentType=${contentType}` : '';
        const urlParam: string = `${pathParam}${contentTypeParam}`;
        return `/nextcloud/files/user/${userid}/file/${fileName}/download${urlParam}`;
    },

    getFiles: (userid: string, path: string, files: Array<string>): string => {
        const pathParam: string = `?path=${path}`;
        let filesParam: string = '';
        files.forEach((file: string) => {
            filesParam += `&file=${file}`;
        });
        const urlParam: string = `${pathParam}${filesParam}`;
        return `/nextcloud/files/user/${userid}/multiple/download${urlParam}`;
    },

    test: async (): Promise<AxiosResponse> => {
        return http.get(`/nextcloud/test/ok`);
    }
};

export const NextcloudService = ng.service('NextcloudService', (): INextcloudService => nextcloudService);