export interface IMetaResponse {
    status: string;
    statuscode: number;
    message: string;
    totalitems: string;
    itemsperpage: string;
}

export class Meta {
    status: string;
    statusCode: number;
    message: string;
    totalItems: string;
    itemsPerPage: string;

    build(data: any): Meta {
        const metaResponse: IMetaResponse = data.meta;
        this.status = metaResponse.status;
        this.statusCode = metaResponse.statuscode;
        this.message = metaResponse.message;
        this.totalItems = metaResponse.totalitems;
        this.itemsPerPage = metaResponse.itemsperpage;
        return this;
    }
}