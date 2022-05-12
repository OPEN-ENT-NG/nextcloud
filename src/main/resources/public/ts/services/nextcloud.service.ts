import {ng} from 'entcore'
import http, {AxiosResponse} from 'axios';
import {IDocumentResponse, SyncDocument} from "../models";

export interface INextcloudService {
    listDocument(userid: string, path?: string): Promise<Array<SyncDocument>>;
    uploadDocuments(userid: string, files: Array<File>): Promise<AxiosResponse>;
    moveDocument(userid: string, path: string, destPath: string): Promise<AxiosResponse>;
    deleteDocuments(userid: string, path: Array<string>): Promise<AxiosResponse>;
    getFile(userid: string, fileName: string, path: string, contentType: string): string;
    getFiles(userid: string, path: string, files: Array<string>): string;
}

export const nextcloudService: INextcloudService = {

    listDocument: async (userid: string, path?: string): Promise<Array<SyncDocument>> => {
        const urlParam: string = path ? `?path=${path}` : '';
        return http.get(`/nextcloud/files/user/${userid}${urlParam}`)
            .then((res: AxiosResponse) => res.data.data.map((document: IDocumentResponse) => new SyncDocument().build(document)));
    },

    uploadDocuments(userid: string, files: Array<File>): Promise<AxiosResponse> {
        const formData: FormData = new FormData();
        const headers = {'headers': {'Content-type': 'multipart/form-data', 'File-Count': files.length}};
        files.forEach(file => {
            formData.append('fileToUpload[]', file);
        });
        return http.put(`/nextcloud/files/user/${userid}/upload`, formData, headers);
    },

    moveDocument: (userid: string, path: string, destPath: string): Promise<AxiosResponse> => {
        const urlParam: string = `?path=${path}&destPath=${destPath}`
        return http.put(`/nextcloud/files/user/${userid}/move${urlParam}`);
    },

    deleteDocuments(userid: string, paths: Array<string>): Promise<AxiosResponse> {
        let urlParams: URLSearchParams = new URLSearchParams();
        paths.forEach((path: string) => {
            urlParams.append('path', path);
        });
        return http.delete(`/nextcloud/files/user/${userid}/delete?${urlParams}`);
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
};

export const NextcloudService = ng.service('NextcloudService', (): INextcloudService => nextcloudService);