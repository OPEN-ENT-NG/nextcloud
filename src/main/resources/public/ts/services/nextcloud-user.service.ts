import {ng} from 'entcore'
import http, {AxiosResponse} from 'axios';
import {UserNextcloud} from "../models/nextcloud-user.model";

export interface INextcloudUserService {
    resolveUser(userid: string): Promise<AxiosResponse>;
    getUserInfo(userid: string): Promise<UserNextcloud>;
}

export const nextcloudUserService: INextcloudUserService = {

    resolveUser: async (userid: string): Promise<AxiosResponse> => {
        const param: string = `?userid=${userid}`;
        const body: {userid: string} = {userid: userid};
        return http.post(`/nextcloud/user/provide/token${param}`, body);
    },

    getUserInfo: async (userid: string): Promise<UserNextcloud> => {
        return http.get(`/nextcloud/user/${userid}`).then((response: AxiosResponse) => new UserNextcloud().build(response.data));
    }
};

export const NextcloudUserService = ng.service('NextcloudUserService', (): INextcloudUserService => nextcloudUserService);