import {ng} from 'entcore'
import { http, HttpResponse } from 'entcore-toolkit';
import {UserNextcloud} from "../models/nextcloud-user.model";

export interface INextcloudUserService {
    resolveUser(userid: string): Promise<HttpResponse>;
    getUserInfo(userid: string): Promise<UserNextcloud>;
}

export const nextcloudUserService: INextcloudUserService = {

    resolveUser: async (userid: string): Promise<HttpResponse> => {
        return http.get(decodeURI(`/nextcloud/user/${userid}/provide/token`));
    },

    getUserInfo: async (userid: string): Promise<UserNextcloud> => {
        return http.get(`/nextcloud/user/${userid}`).then((response: HttpResponse) => new UserNextcloud().build(response.data));
    }
};

export const NextcloudUserService = ng.service('NextcloudUserService', (): INextcloudUserService => nextcloudUserService);