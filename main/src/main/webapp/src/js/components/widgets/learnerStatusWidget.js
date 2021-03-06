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
 * The directive of the dashboard widget that displays the current status of the learner.
 */
class LearnerStatusWidget {

    /**
     * Constructor.
     *
     * @param {LearnerResource} LearnerResource
     * @param {ToastService} ToastService
     */
    // @ngInject
    constructor(LearnerResource, ToastService) {
        this.LearnerResource = LearnerResource;
        this.ToastService = ToastService;

        /**
         * Whether the learner is actively learning an application.
         * @type {boolean}
         */
        this.isActive = false;

        /**
         * Whether the learner has finished learning an application.
         * @type {boolean}
         */
        this.hasFinished = false;

        /**
         * The intermediate or final learning result.
         * @type {null|LearnResult}
         */
        this.result = null;

        this.LearnerResource.isActive()
            .then(data => {
                this.isActive = data.active;
                if (!data.active) {
                    this.LearnerResource.getStatus()
                        .then(data => {
                            if (data !== null) {
                                this.hasFinished = true;
                                this.result = data;
                            }
                        });
                }
            })
            .catch(err => console.log(err));
    }

    /**
     * Induces the Learner to stop learning after the current hypothesis model.
     */
    abort() {
        this.LearnerResource.stop()
            .then(() => {
                this.ToastService.info('The Learner stops with the next hypothesis');
            })
            .catch(err => console.log(err));
    }
}

export const learnerStatusWidget = {
    templateUrl: 'html/components/widgets/learner-status-widget.html',
    controller: LearnerStatusWidget,
    controllerAs: 'vm'
};