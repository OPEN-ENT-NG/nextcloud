import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import {nextcloudService} from '../nextcloud.service';

describe('NextcloudService', () => {
    it('returns data when retrieve request is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true};
        mock.onGet(`/nextcloud/test/ok`).reply(200, data);
        nextcloudService.test().then(response => {
            expect(response.data).toEqual(data);
            done();
        });
    });
});
