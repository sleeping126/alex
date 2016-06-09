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
 * Use: <symbol-list-item symbol="..."></symbol-list-item>
 */
// @ngInject
class SymbolListItem {

    /**
     * Constructor
     * @param $attrs
     */
    constructor($attrs) {

        /**
         * Flag that indicates if the actions should be displayed.
         * @type {boolean}
         */
        this.collapsed = false;

        /**
         * Flag that indicates if the checkbox should be displayed.
         * @type {boolean}
         */
        this.isSelectable = angular.isDefined($attrs.selectionModel);
    }

    /**
     * Toggles the status of the collapsed action list between visible and not visible.
     */
    toggleCollapsed() {
        this.collapsed = !this.collapsed;
    }
}

const symbolListItem = {
    templateUrl: 'html/components/symbol-list-item.html',
    controller: SymbolListItem,
    controllerAs: 'vm',
    transclude: true,
    bindings: {
        symbol: '='
    }
};

export {symbolListItem};