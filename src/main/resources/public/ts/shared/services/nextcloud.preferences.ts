import {ViewMode} from "../../core/enums/view-mode";
import {Me, notify} from "entcore";


export type NextcloudPreference = {
    viewMode: ViewMode;
}

export class Preference {
    private _viewMode: ViewMode;

    get viewMode(): ViewMode {
        return this._viewMode;
    }

    set viewMode(viewMode : ViewMode) {
        this._viewMode = viewMode;
    }

    async init(): Promise<void> {
        try {
            // fetch nextcloud preference from Me
            let preference: NextcloudPreference = Me.preferences['nextcloud'];
            if (this.isEmpty(preference)) {
                preference = await Me.preference('nextcloud');
                // I have no pref, must init and save by default
                if (this.isEmpty(preference)) {
                    // init default
                    preference.viewMode = ViewMode.ICONS;

                    // persist for the first time my nextcloud preference
                    await this.updatePreference(preference);
                }
            }
            this.setProperties(preference);
        } catch (e) {
            notify.error('nextcloud.preferences.init.error');
            throw e;
        }
    }

    async updatePreference(preference: NextcloudPreference): Promise<void> {
        Me.preferences.nextcloud = preference;
        await Me.savePreference('nextcloud');
    }

    private setProperties(preference: NextcloudPreference): void {
        this._viewMode = preference.viewMode;
    }

    private isEmpty(preference: NextcloudPreference): boolean {
        return !preference || !Object.keys(preference).length;
    }

    // async initPreference(): Promise<any> {
    //     let pref;
    //     try {
    //          pref = await Me.preference('nextcloud');
    //          this._viewMode =
    //     } catch (e) {
    //         console.log(e + "nextcloud.preference.viewMode.initPreference")
    //         pref = undefined;
    //     }
    //
    //     if (typeof pref === 'undefined') {
    //         Me.preferences.nextcloud = {"viewMode" : ViewMode.ICONS};
    //         Me.savePreference('nextcloud');
    //     }
    //         return await Me.preference('nextcloud');
    //     // Me.preference('nextcloud')
    //     //     .then(res => res)
    //     //     .then(res => res)
    //     //     .then(res => res)
    //     //     .catch(async() =>{
    //     //         Me.preferences.nextcloud = {"viewMode" : ViewMode.ICONS};
    //     //         await Me.savePreference('nextcloud');
    //     //         return await Me.preference('nextcloud');
    //     //     })
    // }
    //
    //
    // async updatePreference(value: ViewMode): Promise<void> {
    //     Me.preference(Preferences.NEXTCLOUD)
    //         .then(async obj => {
    //             obj = {...obj};
    //             obj.viewMode = value;
    //             Me.preferences[Preferences.NEXTCLOUD] = obj;
    //             await Me.savePreference(Preferences.NEXTCLOUD);
    //         })
    // }

}