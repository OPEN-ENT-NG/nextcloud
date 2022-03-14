import {Me, model} from "entcore";
import {INextcloudService, nextcloudService} from "../services";
import {Meta} from "./nextcloud.model";
import {AxiosError} from "axios";

export interface INextcloudUserPreference {
    active: boolean;
}

export class NextcloudPreference {
    private nextcloudService: INextcloudService;

    public readonly USER_NEXTCLOUD = "userNextcloud";
    public body: INextcloudUserPreference;

    constructor(nextcloudService: INextcloudService) {
        this.nextcloudService = nextcloudService;
        this.body = {active: false};
    }

    init(): NextcloudPreference {
        Me.preference(this.USER_NEXTCLOUD).then((res: INextcloudUserPreference) => {
            if (!this.isUserNextcloudCreated(res)) {
                nextcloudService.createUser(decodeURI(model.me.login))
                    .then((res: Meta) => {
                        this.body.active = (res.statusCode === 100 || res.statusCode === 102);
                        this.setUserNextcloudPreference(this.body);
                    })
                    .catch((err: AxiosError) => {
                        const message: string = "Error while attempting to create user: ";
                        console.error(message + err.message);
                        this.setUserNextcloudPreference(this.body);
                    });
            } else {
                this.body.active = res.active;
            }
        });

        return this;
    }

    isUserNextcloudCreated(userPreference: INextcloudUserPreference): boolean {
        return (userPreference && Object.keys(userPreference).length > 0 && userPreference.active);
    }

    setUserNextcloudPreference(pref: INextcloudUserPreference): void {
        Me.preferences[this.USER_NEXTCLOUD] = pref;
        Me.savePreference(this.USER_NEXTCLOUD);
    }


}