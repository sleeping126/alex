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
 * The controller that is used to display the details of a learn result in a modal dialog. The data that is passed
 * to this controller should be an object with a property 'result' which contains a learn result object. If none is
 * given, nothing will be displayed.
 */
export class LearnResultDetailsModalComponent {

    /** Constructor. */
    constructor() {

        /**
         * The result to display information from.
         * @type {LearnResult[]}
         */
        this.result = null;

        /**
         * The index of the current step.
         * @type {number}
         */
        this.current = null;

        /**
         * The data of the tabs that are displayed.
         * @type {*[]}
         */
        this.tabs = [];
    }

    $onInit() {
        this.result = this.resolve.modalData.result;
        this.current = this.resolve.modalData.current;
        this.tabs.push({heading: 'Cumulated', result: this.result});

        // add a tab with details for the current step only if it is defined
        // otherwise display the tab with the cumulated results only
        if (typeof this.current !== 'undefined') {
            this.tabs.unshift({heading: 'Current', result: this.result.steps[this.current]});
        }
    }
}


export const learnResultDetailsModalComponent = {
    templateUrl: 'html/components/modals/learn-result-details-modal.html',
    bindings: {
        dismiss: '&',
        resolve: '='
    },
    controller: LearnResultDetailsModalComponent,
    controllerAs: 'vm',
};


/**
 * The directive that handles the modal dialog for displaying details about a learn result. Can only be used as
 * an attribute and expects a second attribute 'result' which should be the LearnResult whose details should be
 * shown. Attaches a click event on the element that opens the modal.
 *
 * Use it like this: '<button learn-result-details-modal-handle result="...">Click me!</button>'.
 *
 * @param $uibModal - The modal service.
 * @returns {{restrict: string, scope: {result: string}, link: Function}}
 */
// @ngInject
export function learnResultDetailsModalHandle($uibModal) {
    return {
        restrict: 'A',
        scope: {
            result: '=',
            current: '='
        },
        link(scope, el) {
            el.on('click', () => {
                $uibModal.open({
                    component: 'learnResultDetailsModal',
                    resolve: {
                        modalData: function () {
                            return {
                                result: scope.result,
                                current: scope.current
                            };
                        }
                    }
                });
            });
        }
    };
}