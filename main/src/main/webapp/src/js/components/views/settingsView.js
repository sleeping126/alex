/*
 * Copyright 2016 TU Dortmund
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * The component for the about page.
 */
class SettingsView {

    /**
     * Constructor.
     *
     * @param {SettingsResource} SettingsResource
     * @param {ToastService} ToastService
     */
    // @ngInject
    constructor(SettingsResource, ToastService) {
        this.SettingsResource = SettingsResource;
        this.ToastService = ToastService;

        this.settings = null;

        this.SettingsResource.get()
            .then(settings => this.settings = settings)
            .catch(err => console.log(err));
    }

    /**
     * Updates the settings.
     */
    updateSettings() {
        this.SettingsResource.update(this.settings)
            .then(() => {
                this.ToastService.success("The settings have been updated.");
            })
            .catch(res => {
                this.ToastService.danger("<strong>Update failed!</strong> " + res.data.message);
            });
    }
}

export const settingsView = {
    controller: SettingsView,
    controllerAs: 'vm',
    templateUrl: 'html/components/views/settings-view.html'
};