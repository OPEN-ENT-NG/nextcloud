jest.mock('entcore-toolkit', () => Object.assign({}, (jest as any).requireActual('entcore-toolkit'), {
    http: {get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(), postFile: jest.fn(), putFile: jest.fn()},
}));

import {http} from 'entcore-toolkit';
import {nextcloudUserService} from "../nextcloud-user.service";
import {IUserResponse} from "../../models/nextcloud-user.model";
import {mockHttpResponse} from "../../../../test-utils/httpMock";

describe('NextcloudUserService', () => {

    beforeEach(() => {
        (http.get as jest.Mock).mockReset();
    });

    it('Test resolveUser method', done => {
        const data = {response: true};
        const userId = "userId";

        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data, {url: `/nextcloud/user/userId/provide/token`}));

        nextcloudUserService.resolveUser(userId).then(response => {
            expect(response.data).toEqual(data);
            expect(response.status).toEqual(200);
            expect((response.config as any).url).toEqual(`/nextcloud/user/userId/provide/token`);
            done();
        });
    });

    it('Test getUserInfo method', done => {
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
            total: 0, unit: "Mo", used: 0
        }
        const result: any = {
            displayName: 0,
            email: "email",
            id: "id",
            itemsPerPage: "itemsperpage",
            phone: "phone",
            quota: quota,
        };

        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));

        nextcloudUserService.getUserInfo(userId).then(response => {
            expect(response).toEqual(result);
            done();
        });
    });
});
