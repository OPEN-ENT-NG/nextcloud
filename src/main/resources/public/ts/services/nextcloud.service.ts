import {ng} from 'entcore'
import http, {AxiosResponse} from 'axios';

export interface INextcloudService {
    test(): Promise<AxiosResponse>;
}

export const nextcloudService: INextcloudService = {
    test: async (): Promise<AxiosResponse> => {
        return http.get(`/nextcloud/test/ok`);
    }
};

export const NextcloudService = ng.service('NextcloudService', (): INextcloudService => nextcloudService);