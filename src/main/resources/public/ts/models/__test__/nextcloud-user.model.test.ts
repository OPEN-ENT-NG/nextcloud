import {Quota} from "../nextcloud-user.model";

describe('NextcloudModel', () => {

    it('test Quota.build with big used', () => {
        const quota = new Quota().build({
            free: 805306368,
            used: 1342177280,
            total: 2147483648,
            relative: 12.42,
            quota: 2147483648,
        })

        expect(quota.free).toEqual(805306368);
        expect(quota.quota).toEqual(2147483648);
        expect(quota.relative).toEqual(12.42);
        expect(quota.total).toEqual(2);
        expect(quota.unit).toEqual('Go');
        expect(quota.used).toEqual(1.25);
    });

    it('test Quota.build with small used', () => {
        const quota = new Quota().build({
            free: 2061584303,
            used: 85899345,
            total: 2147483648,
            relative: 12.42,
            quota: 2147483648,
        })

        expect(quota.free).toEqual(2061584303);
        expect(quota.quota).toEqual(2147483648);
        expect(quota.relative).toEqual(12.42);
        expect(quota.total).toEqual(2);
        expect(quota.unit).toEqual('Go');
        expect(quota.used).toEqual(0.08);
    });

    it('test Quota.build with less than 2000 Mo', () => {
        const quota = new Quota().build({
            free: 410300000,
            used: 1200312736,
            total: 1610612736,
            relative: 12.42,
            quota: 1610612736,
        })

        expect(quota.free).toEqual(410300000);
        expect(quota.quota).toEqual(1610612736);
        expect(quota.relative).toEqual(12.42);
        expect(quota.total).toEqual(1536);
        expect(quota.unit).toEqual('Mo');
        expect(quota.used).toEqual(1145);
    });
})