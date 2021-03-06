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

import flatten from "lodash/flatten";
import {LearnConfiguration} from "../../entities/LearnConfiguration";

/**
 * The controller that handles the preparation of a learn process. Lists all symbol groups and its visible symbols.
 */
class LearnerSetupView {

    /**
     * Constructor.
     *
     * @param $state
     * @param {SymbolGroupResource} SymbolGroupResource
     * @param {SessionService} SessionService
     * @param {LearnerResource} LearnerResource
     * @param {ToastService} ToastService
     * @param {LearnResultResource} LearnResultResource
     */
    // @ngInject
    constructor($state, SymbolGroupResource, SessionService, LearnerResource, ToastService, LearnResultResource) {
        this.$state = $state;
        this.LearnerResource = LearnerResource;
        this.ToastService = ToastService;

        /**
         * The project that is in the session.
         * @type {Project}
         */
        this.project = SessionService.getProject();

        /**
         * All symbol groups that belong the the sessions project.
         * @type {SymbolGroup[]}
         */
        this.groups = [];

        /**
         * The learn results of previous learn processes.
         * @type {LearnResult[]}
         */
        this.learnResults = [];

        /**
         * A list of all symbols of all groups that is used in order to select them.
         * @type {AlphabetSymbol[]}
         */
        this.allSymbols = [];

        /**
         * A list of selected Symbols.
         * @type {AlphabetSymbol[]}
         */
        this.selectedSymbols = [];

        /**
         * The configuration that is send to the server for learning.
         * @type {LearnConfiguration}
         */
        this.learnConfiguration = new LearnConfiguration();

        /**
         * The symbol that should be used as a reset symbol.
         * @type {AlphabetSymbol|null}
         */
        this.resetSymbol = null;

        /**
         * Indicates whether there is a learning process that can be continued (the last one).
         * @type {boolean}
         */
        this.canContinueLearnProcess = false;

        // make sure that there isn't any other learn process active
        // redirect to the load screen in case there is an active one
        this.LearnerResource.isActive()
            .then(data => {
                if (data.active) {
                    if (data.project == this.project.id) {
                        this.ToastService.info('There is currently running a learn process.');
                        this.$state.go('learnerStart');
                    } else {
                        this.ToastService.danger('There is already running a test from another project.');
                        this.$state.go('project');
                    }
                } else {

                    // load all symbols in case there isn't any active learning process
                    SymbolGroupResource.getAll(this.project.id, true)
                        .then(groups => {
                            this.groups = groups;
                            this.allSymbols = flatten(this.groups.map(g => g.symbols));
                        })
                        .catch(err => console.log(err));

                    // load learn results so that their configuration can be reused
                    LearnResultResource.getAll(this.project.id)
                        .then(learnResults => {
                            this.learnResults = learnResults;
                        })
                        .catch(err => console.log(err));
                }
            })
            .catch(err => console.log(err));

        // get the status to check if there is a learn process that can be continued
        this.LearnerResource.getStatus().then(data => {
            this.canContinueLearnProcess = data !== null;
        });
    }

    /** @param {LearnConfiguration} config - The config to use. */
    setLearnConfiguration(config) {
        this.learnConfiguration = config;
    }

    /** @param {AlphabetSymbol} symbol - The symbol that will be used to reset the sul. */
    setResetSymbol(symbol) {
        this.resetSymbol = symbol;
    }

    /**
     * Starts the learning process if symbols are selected and a reset symbol is defined. Redirects to the
     * learning load screen on success.
     */
    startLearning() {
        if (this.resetSymbol === null) {
            this.ToastService.danger('You <strong>must</strong> selected a reset symbol in order to start learning!');
        } else {
            if (this.selectedSymbols.length > 0) {

                const i = this.selectedSymbols.findIndex(s => s.id === this.resetSymbol.id);
                if (i > -1) this.selectedSymbols.splice(i, 1);

                // add selected symbols and the reset symbol to the learn config
                this.selectedSymbols.forEach(symbol => {
                    this.learnConfiguration.addSymbol(symbol);
                });
                this.learnConfiguration.setResetSymbol(this.resetSymbol);

                // start learning
                this.LearnerResource.start(this.project.id, this.learnConfiguration)
                    .then(() => {
                        this.ToastService.success('Learn process started successfully.');
                        this.$state.go('learnerStart');
                    })
                    .catch(response => {
                        this.ToastService.danger('<p><strong>Start learning failed</strong></p>' + response.data.message);
                    });
            } else {
                this.ToastService.danger('You <strong>must</strong> at least select one symbol to start learning');
            }
        }
    }

    /**
     * Reuse the properties of a previous learn result for the current one.
     *
     * @param {LearnResult} result - The learn result from that the configuration should be reused.
     */
    reuseConfigurationFromResult(result) {
        this.learnConfiguration.algorithm = result.algorithm;
        this.learnConfiguration.eqOracle = result.steps[0].eqOracle;
        this.learnConfiguration.maxAmountOfStepsToLearn = result.maxAmountOfStepsToLearn;
        this.learnConfiguration.browser = result.browser;

        this.groups.forEach(group => {
            group.symbols.forEach(symbol => {
                symbol._selected = result.symbols.indexOf(symbol.id) > -1;
                if (symbol.id === result.resetSymbol) {
                    this.resetSymbol = symbol;
                }
            });
        });
    }
}

export const learnerSetupView = {
    controller: LearnerSetupView,
    controllerAs: 'vm',
    templateUrl: 'html/components/views/learner-setup-view.html'
};