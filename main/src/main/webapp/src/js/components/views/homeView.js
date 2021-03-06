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
 * The controller of the index page.
 */
class HomeView {

    /**
     * Constructor.
     *
     * @param $state
     * @param {SessionService} SessionService
     */
    // @ngInject
    constructor($state, SessionService) {

        /**
         * The current user.
         * @type {User}
         */
        this.user = SessionService.getUser();

        /**
         * The current project.
         * @type {Project}
         */
        this.project = SessionService.getProject();

        if (this.user !== null) {
            if (this.project !== null) {
                $state.go('projectsDashboard');
            } else {
                $state.go('projects');
            }
        }
    }
}

export const homeView = {
    controller: HomeView,
    controllerAs: 'vm',
    templateUrl: 'html/components/views/home-view.html'
};