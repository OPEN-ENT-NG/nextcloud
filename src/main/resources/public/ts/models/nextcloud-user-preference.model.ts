import {Me} from "entcore";

export class NextcloudPreference {
    public readonly USER_NEXTCLOUD = "user.nextcloud";

    constructor() {
        Me.preference(this.USER_NEXTCLOUD).then((res: boolean) => {
            if (!res) {
                console.log("must create account");
            } else {
                // create user from model.me and then =>

            }
        });
    }

    setUserNextcloudPreference(created: boolean): void {
        Me.preferences[this.USER_NEXTCLOUD] = true;
        Me.savePreference(this.USER_NEXTCLOUD);
    }


}