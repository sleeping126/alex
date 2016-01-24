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
 * The controller that handles the page for displaying multiple complete learn results in a slide show.
 */
// @ngInject
class ResultsCompareView {

    /**
     * Constructor
     * @param $timeout
     * @param $stateParams
     * @param SessionService
     * @param LearnResultResource
     * @param ErrorService
     */
    constructor($timeout, $stateParams, SessionService, LearnResultResource, ErrorService) {
        this.$timeout = $timeout;
        this.LearnResultResource = LearnResultResource;
        this.ErrorService = ErrorService;

        /**
         * The project that is in the session
         * @type {Project}
         */
        this.project = SessionService.getProject();

        /**
         * All final learn results from all tests that were made for a project
         * @type {LearnResult[]}
         */
        this.results = [];

        /**
         * The list of active panels where each panel contains a complete learn result set
         * @type {LearnResult[][]}
         */
        this.panels = [];

        /**
         * The list of layout settings for the current hypothesis that is shown in a panel
         * @type {Object[]}
         */
        this.layoutSettings = [];

        // load all final learn results of all test an then load the complete test results from the test numbers
        // that are passed from the url in the panels
        if (!$stateParams.testNos) {
            this.ErrorService.setErrorMessage("There are no test numbers defined in the URL");
        } else {
            const testNos = $stateParams.testNos.split(',');
            this.LearnResultResource.getAll(this.project.id)
                .then(results => {
                    this.results = results;

                    this.results.forEach(result => {
                        if (testNos.indexOf(String(result.testNo)) > -1) {
                            this.panels.push(result);
                        }
                    })
                });
        }
    }

    /**
     * Loads a complete learn result set from a learn result in the panel with a given index
     *
     * @param {LearnResult} result - The learn result whose complete set should be loaded in a panel
     * @param {number} index - The index of the panel the complete set should be displayed in
     */
    fillPanel(result, index) {
        this.panels[index] = result;
    }

    /**
     * Adds a new empty panel
     */
    addPanel() {
        this.panels.push(null);
    }

    /**
     * Removes a panel by a given index
     * @param {number} index - The index of the panel to remove
     */
    closePanel(index) {
        this.panels[index] = null;
        this.panels.splice(index, 1);
    }
}

export const resultsCompareView = {
    controller: ResultsCompareView,
    controllerAs: 'vm',
    templateUrl: 'views/pages/results-compare.html'
};