(function () {
    'use strict';

    angular
        .module('ALEX.core')
        .directive('learnResultPanel', learnResultPanel);

    learnResultPanel.$inject = ['paths', 'learnAlgorithms'];

    /**
     * The directive that displays a browsable list of learn results. For each result, it can display the observation
     * table, if L* was used, or the Discrimination Tree from the corresponding algorithm.
     *
     * It expects an attribute 'results' which should contain a list of the learn results that should be displayed. It
     * can for example be the list of all intermediate results of a complete test or multiple single results from
     * multiple tests.
     *
     * Content that is written inside the tag will be displayed a the top right corner beside the index browser. So
     * just add small texts or additional buttons in there.
     *
     * Use it like '<learn-result-panel results="..."> ... </learn-result-panel>'
     *
     * @param {Object} paths - The constant for application paths
     * @param {Object} learnAlgorithms - The constant for available learn algorithms
     * @returns {{scope: {results: string}, transclude: boolean, templateUrl: string, controller: *[]}}
     */
    function learnResultPanel(paths, learnAlgorithms) {
        return {
            scope: {
                results: '='
            },
            replace: true,
            transclude: true,
            templateUrl: paths.COMPONENTS + '/core/views/directives/learn-result-panel.html',
            link: link
        };

        function link(scope) {

            /**
             * Enum for displayable modes.
             * 0 := show hypothesis
             * 1 := show internal data structure
             * @type {{HYPOTHESIS: number, INTERNAL: number}}
             */
            scope.modes = {
                HYPOTHESIS: 0,
                OBSERVATION_TABLE: 1,
                DISCRIMINATION_TREE: 2
            };

            /**
             * Available learn algorithms
             * @type {Object}
             */
            scope.learnAlgorithms = learnAlgorithms;

            /**
             * The layout settings for the displayed hypothesis
             * @type {undefined|Object}
             */
            scope.layoutSettings;

            /**
             * The mode that is used
             * @type {number}
             */
            scope.mode = scope.modes.HYPOTHESIS;

            /**
             * The index of the step from the results that should be shown
             * @type {number}
             */
            scope.pointer = scope.results.length - 1;

            /**
             * Checks if the property 'algorithmInformation' is define which holds the internal data structure
             * for the algorithm of a learn result
             * @returns {boolean|*}
             */
            scope.hasInternalDataStructure = function () {
                return scope.results[scope.pointer].configuration.algorithm !== learnAlgorithms.TTT;
            };

            /**
             * Switches the mode to the one to display the internal data structure
             */
            scope.showInternalDataStructure = function () {
                switch (scope.results[scope.pointer].configuration.algorithm) {
                    case learnAlgorithms.LSTAR:
                        scope.mode = scope.modes.OBSERVATION_TABLE;
                        break;
                    case learnAlgorithms.DISCRIMINATION_TREE:
                        scope.mode = scope.modes.DISCRIMINATION_TREE;
                        break;
                    default:
                        break;
                }
            };

            /**
             * Switches the mode to the one to display the hypothesis
             */
            scope.showHypothesis = function () {
                scope.mode = scope.modes.HYPOTHESIS;
            };

            /**
             * Shows the first result of the test process
             */
            scope.firstStep = function () {
                scope.pointer = 0;
            };

            /**
             * Shows the previous result of the test process or the last if the first one is displayed
             */
            scope.previousStep = function () {
                if (scope.pointer - 1 < 0) {
                    scope.lastStep();
                } else {
                    scope.pointer--;
                }
            };

            /**
             * Shows the next result of the test process or the first if the last one is displayed
             */
            scope.nextStep = function () {
                if (scope.pointer + 1 > scope.results.length - 1) {
                    scope.firstStep();
                } else {
                    scope.pointer++;
                }
            };

            /**
             * Shows the last result of the test process
             */
            scope.lastStep = function () {
                scope.pointer = scope.results.length - 1;
            };
        }
    }
}());