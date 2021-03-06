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

import {Action} from "../Action";
import {actionType} from "../../../constants";

/**
 * Searches for a piece of text or a regular expression in the HTML document.
 */
export class CheckForTextWebAction extends Action {

    /**
     * Constructor.
     *
     * @param {object} obj - The object to create the action from.
     */
    constructor(obj) {
        super(actionType.WEB_CHECK_TEXT, obj);

        /**
         * The piece of text to look for.
         * @type {*|string}
         */
        this.value = obj.value || '';

        /**
         * Whether the value is a regular expression.
         * @type {*|boolean}
         */
        this.regexp = obj.regexp || false;
    }

    /**
     * A string representation of the action.
     *
     * @returns {string}
     */
    toString() {
        if (this.regexp) {
            return 'Check if the document matches "' + this.value + '"';
        } else {
            return 'Search for "' + this.value + '" in the document';
        }
    }
}