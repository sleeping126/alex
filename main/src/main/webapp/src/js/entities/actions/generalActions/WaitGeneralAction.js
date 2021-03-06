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
 * Wait for a certain amount of time before executing the next action.
 */
export class WaitGeneralAction extends Action {

    /**
     * Constructor.
     *
     * @param {object} obj - The object to create the action from.
     */
    constructor(obj) {
        super(actionType.WAIT, obj);

        /**
         * The time to wait in milliseconds.
         * @type {*|number}
         */
        this.duration = obj.duration || 0;
    }

    /**
     * A string representation of the action.
     *
     * @returns {string}
     */
    toString() {
        return 'Wait for ' + this.duration + 'ms';
    }
}