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

/** The Service that is used to download learn results as csv */
// @ngInject
class LearnerResultDownloadService {

    /**
     * Constructor
     * @param {DownloadService} DownloadService
     */
    constructor(DownloadService) {
        this.DownloadService = DownloadService;

        /**
         * The CSV data
         * @type {string}
         */
        this.csv = '';
    }

    /** Initialize the header of th csv */
    init() {
        this.csv = 'Project;Test No;Start Time;Step No;Algorithm;Eq Oracle;|Sigma|;#MQs;#EQs;#Symbol Calls;Duration (ms)\n';
    }

    /**
     * Adds a single learn result to the csv data
     * @param {LearnResult} result
     */
    addResult(result) {
        result.steps.forEach(step => {
            this.csv += result.project + ';';
            this.csv += result.testNo + ';';
            this.csv += '"' + step.statistics.startDate + '";';
            this.csv += step.stepNo + ';';
            this.csv += result.algorithm + ';';
            this.csv += step.eqOracle.type + ';';
            this.csv += result.symbols.length + ';';
            this.csv += step.statistics.mqsUsed + ';';
            this.csv += step.statistics.eqsUsed + ';';
            this.csv += step.statistics.symbolsUsed + ';';
            this.csv += step.statistics.duration + '\n';
        });
    }

    /** Creates an empty row so that multiple test runs can be exported at once */
    addEmptyLine() {
        this.csv += '\n';
    }

    /** Downloads the csv */
    download() {
        this.DownloadService.downloadCsv(this.csv);
    }
}

export default LearnerResultDownloadService;