import {ng} from 'entcore'
import http, {AxiosResponse} from 'axios';

export interface INextcloudService {
    createUser(): Promise<AxiosResponse>;
    listFolder(): Promise<AxiosResponse>;
    test(): Promise<AxiosResponse>;
}

export const nextcloudService: INextcloudService = {

    createUser: async (): Promise<AxiosResponse> => {
        return http.post(`/nextcloud/user`);
    },

    listFolder: async (): Promise<AxiosResponse> => {
        return http.get(`/nextcloud/folder`).then((res: AxiosResponse) => (<any>res.data));
    },

    test: async (): Promise<AxiosResponse> => {
        return http.get(`/nextcloud/test/ok`);
    }
};

export const NextcloudService = ng.service('NextcloudService', (): INextcloudService => nextcloudService);