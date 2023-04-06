import {Quota} from "../nextcloud-user.model";

describe('NextcloudModel', () => {

    it('test Quota.build with big used', () => {
        const quota = new Quota().build({
            used: 1342177280,
            quota: 2147483648,
            unit: 'Go'
        })


        expect(quota.total).toEqual(2);
        expect(quota.unit).toEqual('Go');
        expect(quota.used).toEqual(1.25);
    });

    it('test Quota.build with small used', () => {
        const quota = new Quota().build({
            used: 85899345,
            quota: 2147483648,
            unit: 'Go'
        })

        expect(quota.total).toEqual(2);
        expect(quota.unit).toEqual('Go');
        expect(quota.used).toEqual(0.08);
    });

    it('test Quota.build with less than 2000 Mo', () => {
        const quota = new Quota().build({
            used: 1200312736,
            quota: 1610612736,
            unit: 'Mo'
        })


        expect(quota.total).toEqual(1536);
        expect(quota.unit).toEqual('Mo');
        expect(quota.used).toEqual(1145);
    });
})