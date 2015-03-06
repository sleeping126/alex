(function () {
    'use strict';

    angular
        .module('weblearner.services')
        .factory('LearnerResultChartService', LearnerResultChartService);

    LearnerResultChartService.$inject = ['_'];

    /**
     * The service to create n3 line chart data from learner results. Can create bar chart data from multiple final
     * learner results and area chart data from multiple complete learner results.
     *
     * @param _ - The Lodash library
     * @returns {{createDataFromMultipleFinalResults: createDataFromMultipleFinalResults, createDataFromMultipleCompleteResults: createDataFromMultipleCompleteResults, properties: {RESETS: string, SYMBOLS: string, DURATION: string}}}
     * @constructor
     */
    function LearnerResultChartService(_) {

        // The learner result properties
        var properties = {
            RESETS: 'amountOfResets',
            SYMBOLS: 'sigma',
            DURATION: 'duration'
        };

        // The available service data
        var service = {
            createDataFromMultipleFinalResults: createDataFromMultipleFinalResults,
            createDataFromMultipleCompleteResults: createDataFromMultipleCompleteResults,
            properties: properties
        };
        return service;

        /**
         * Creates bar chart data from a list of final learner results which includes the data itself and options.
         *
         * @param results - The learner results from that the chart data should be created
         * @param property - The learner results property from that the data should be used
         * @returns {{data: Array, options: {series: {y: string, color: string, type: string, axis: string, id: string}[], stacks: Array, axes: {x: {type: string, key: string}, y: {type: string, min: number}}, lineMode: string, tension: number, tooltip: {mode: string}, drawLegend: boolean, drawDots: boolean, columnsHGap: number}}}
         */
        function createDataFromMultipleFinalResults(results, property) {

            var dataSets = [];
            var dataValues = [];
            var options = {
                series: [
                    {
                        y: "val_0",
                        color: "#2ca02c",
                        type: "column",
                        axis: "y",
                        id: "series_0"
                    }
                ],
                stacks: [],
                axes: {x: {type: "linear", key: "x"}, y: {type: "linear", min: 0}},
                lineMode: "linear",
                tension: 0.7,
                tooltip: {mode: "scrubber"},
                drawLegend: false,
                drawDots: true,
                columnsHGap: 3
            };

            // extract values from learner results by a property
            switch (property) {
                case properties.RESETS:
                    dataValues = _.pluck(results, properties.RESETS);
                    break;
                case properties.SYMBOLS:
                    dataValues = _.map(_.pluck(results, properties.SYMBOLS), function (n) {
                        return n.length
                    });
                    break;
                case properties.DURATION:
                    dataValues = _.pluck(results, properties.DURATION);
                    break;
                default :
                    break;
            }

            // create n3 line chart dataSets from extracted values
            for (var i = 0; i < dataValues.length; i++) {
                dataSets.push({
                    x: i,
                    val_0: dataValues[i]
                });
            }

            // create x axis labels for each test result
            options.axes.x.labelFunction = function (l) {
                if (l % 1 == 0 && l >= 0 && l < results.length) {
                    return 'Test ' + results[l].testNo;
                }
            };

            return {
                data: dataSets,
                options: options
            };
        }

        /**
         * Creates area chart data from a list of complete learner results which includes the data itself and options.
         *
         * @param results - A list of complete learner results
         * @param property - The learner result property from which the chart data should be created
         * @returns {{data: Array, options: {series: Array, stacks: Array, axes: {x: {type: string, key: string}, y: {type: string, min: number}}, lineMode: string, tension: number, tooltip: {mode: string}, drawLegend: boolean, drawDots: boolean, columnsHGap: number}}}
         */
        function createDataFromMultipleCompleteResults(results, property) {

            var dataSets = [];
            var dataValues = [];
            var maxSteps = 0;
            var options = {
                series: [],
                stacks: [],
                axes: {x: {type: "linear", key: "x"}, y: {type: "linear", min: 0}},
                lineMode: "linear",
                tension: 0.7,
                tooltip: {mode: "scrubber"},
                drawLegend: true,
                drawDots: true,
                columnsHGap: 3
            };
            var i, j;

            // extract values from learner results by a property
            switch (property) {
                case properties.RESETS:
                    _.forEach(results, function (result) {
                        dataValues.push(_.pluck(result, properties.RESETS));
                    });
                    break;
                case properties.SYMBOLS:
                    _.forEach(results, function (result) {
                        dataValues.push(_.map(_.pluck(result, properties.SYMBOLS), function (n) {
                            return n.length;
                        }));
                    });
                    break;
                case properties.DURATION:
                    _.forEach(results, function (result) {
                        dataValues.push(_.pluck(result, properties.DURATION));
                    });
                    break;
                default :
                    break;
            }

            // find value from test results where #steps is max
            for (i = 0; i < dataValues.length; i++) {
                if (dataValues[i].length > maxSteps) {
                    maxSteps = dataValues[i].length;
                }
            }

            // fill all other values with zeroes
            for (i = 0; i < dataValues.length; i++) {
                if (dataValues[i].length < maxSteps) {
                    for (j = dataValues[i].length; j < maxSteps; j++) {
                        dataValues[i][j] = 0;
                    }
                }
            }

            // create data sets
            for (i = 0; i < dataValues.length; i++) {
                var data = {x: i};
                for (j = 0; j < maxSteps; j++) {
                    data['val_' + j] = dataValues[j][i];
                }
                dataSets.push(data);
            }

            // create options for each test
            for (i = 0; i < dataSets.length; i++) {
                options.series.push({
                    y: 'val_' + i,
                    color: 'rgba(0,255,0,' + (0.2 * (i + 1)) + ')',
                    type: 'area',
                    axis: 'y',
                    id: 'series_' + i,
                    label: 'Test ' + results[i][0].testNo
                })
            }

            // create customs x axis labels
            options.axes.x.labelFunction = function (l) {
                if (l % 1 == 0 && l >= 0 && l < maxSteps) {
                    return 'Step ' + (l + 1);
                }
            };

            return {
                data: dataSets,
                options: options
            }
        }
    }
}());