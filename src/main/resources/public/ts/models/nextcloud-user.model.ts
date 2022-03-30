export interface IUserResponse {
    id: string;
    displayname: number;
    email: string;
    phone: string;
    itemsperpage: string;
    quota : {
        free: string,
        used: string,
        total: string,
        relative: number,
        quota: number
    }
}

export class UserNextcloud {
    id: string;
    displayName: number;
    email: string;
    phone: string;
    itemsPerPage: string;
    quota: Quota;

    build(data: IUserResponse): UserNextcloud {
        this.id = data.id;
        this.displayName = data.displayname;
        this.email = data.email;
        this.phone = data.phone;
        this.itemsPerPage = data.itemsperpage;
        this.quota = new Quota().build(data.quota);
        return this;
    }
}

export class Quota {
    free: string;
    used: number;
    total: number;
    relative: number;
    quota: number;
    unit: string;

    build(data: any): Quota {
        this.free = data.free;
        this.used = data.used / (1024 * 1024);
        this.total = data.total / (1024 * 1024);
        this.relative = data.relative;
        this.quota = data.quota;
        if (this.total > 2000) {
            this.total = Math.round((this.total / 1024) * 10) / 10;
            this.used = Math.round((this.used / 1024) * 10) / 10;
            this.unit = 'Go';
        } else {
            this.total = Math.round(this.total);
            this.used = Math.round(this.used);
            this.unit = 'Mo';
        }
        return this;
    }
}