import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import {nextcloudUserService} from "../nextcloud-user.service";
import {IUserResponse} from "../../models/nextcloud-user.model";

describe('NextcloudUserService', () => {
    it('Test resolveUser method', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true};
        const userId = "userId"
        const body = {userid: "userId"};

        mock.onGet(`/nextcloud/user/userId/provide/token`, body).reply(200, data);


        nextcloudUserService.resolveUser(userId).then(response => {
            expect(response.data).toEqual(data);
            expect(response.status).toEqual(200);
            expect(response.config.url).toEqual(`/nextcloud/user/provide/token?userid=userId`);
            expect(response.config.data).toEqual(JSON.stringify(body));
            done();
        });
    });

    it('Test getUserInfo method', done => {
        const mock = new MockAdapter(axios);
        const data: IUserResponse = {
            displayname: 0,
            email: "email",
            id: "id",
            itemsperpage: "itemsperpage",
            phone: "phone",
            quota: {free: "free", quota: 0, relative: 0, total: "0", used: "0"}
        };
        const userId = "userId";

        const quota: any = {
            free: "free", quota: 0, relative: 0, total: 0, unit: "Mo", used: 0
        }
        const result: any = {
            displayName: 0,
            email: "email",
            id: "id",
            itemsPerPage: "itemsperpage",
            phone: "phone",
            quota: quota,
        }

        mock.onGet(`/nextcloud/user/userId`).reply(200, data);


        nextcloudUserService.getUserInfo(userId).then(response => {
            expect(response).toEqual(result);
            done();
        });
    });
});
