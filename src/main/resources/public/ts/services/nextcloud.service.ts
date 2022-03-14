import {ng} from 'entcore'
import http, {AxiosResponse} from 'axios';
import {IDocumentResponse, Meta, SyncDocument} from "../models";

export interface INextcloudService {
    createUser(userid: string): Promise<Meta>;
    listDocument(userid: string, path?: string): Promise<Array<SyncDocument>>;
    getFile(userid: string, path: string): string
    test(): Promise<AxiosResponse>;
}

export const nextcloudService: INextcloudService = {

    createUser: async (userid: string): Promise<Meta> => {
        const param: string = `?userid=${userid}`;
        const body: {userid: string} = {userid: userid};
        return http.post(`/nextcloud/user${param}`, body).then((response: AxiosResponse) => new Meta().build(response.data));
    },

    listDocument: async (userid: string, path?: string): Promise<Array<SyncDocument>> => {
        const urlParam: string = path ? `?path=${path}` : '';
        return http.get(`/nextcloud/files/user/${userid}${urlParam}`)
            .then((res: AxiosResponse) => res.data.data.map((document: IDocumentResponse) => new SyncDocument().build(document)));
    },

    getFile: (userid: string, path: string): string => {
        const urlParam: string = `?path=${path}`;
        return `/nextcloud/files/user/${userid}/download${urlParam}`;
    },

    test: async (): Promise<AxiosResponse> => {
        return http.get(`/nextcloud/test/ok`);
    }
};

export const NextcloudService = ng.service('NextcloudService', (): INextcloudService => nextcloudService);