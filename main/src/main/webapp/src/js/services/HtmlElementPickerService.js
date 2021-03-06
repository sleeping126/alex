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

// the instance of the service
let instance = null;

/**
 * The service fot the html element picker.
 */
export class HtmlElementPickerService {

    /**
     * Constructor.
     */
    constructor() {
        if (instance !== null) return instance;

        /**
         * The promise that is used to communicate between the picker and the handle.
         * @type {Promise|null}
         */
        this.deferred = null;

        /**
         * The url that was opened until the picker got closed.
         * @type {string|null}
         */
        this.lastUrl = null;

        instance = this;
    }
}