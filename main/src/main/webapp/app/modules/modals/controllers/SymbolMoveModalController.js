(function () {
    'use strict';

    angular
        .module('ALEX.modals')
        .controller('SymbolMoveModalController', SymbolMoveModalController);

    SymbolMoveModalController.$inject = [
        '$scope', '$modalInstance', 'modalData', 'Symbol', 'SymbolResource', 'ToastService', '_'
    ];

    /**
     * The controller that handles the moving of symbols into another group.
     *
     * The template can be found at 'views/modals/symbol-move-modal.html'
     *
     * @param $scope
     * @param $modalInstance
     * @param modalData
     * @param Symbol
     * @param SymbolResource
     * @param Toast
     * @param _
     * @constructor
     */
    function SymbolMoveModalController($scope, $modalInstance, modalData, Symbol, SymbolResource, Toast, _) {

        /**
         * The list of symbols that should be moved
         * @type {Symbol[]}
         */
        $scope.symbols = _.map(modalData.symbols, Symbol.build);

        /**
         * The list of existing symbol groups
         * @type {SymbolGroup[]}
         */
        $scope.groups = modalData.groups;

        /**
         * The symbol group the symbols should be moved into
         * @type {SymbolGroup|null}
         */
        $scope.selectedGroup = null;

        /**
         * Moves the symbols into the selected group by changing the group property of each symbol and then batch
         * updating them on the server
         */
        $scope.moveSymbols = function () {
            if ($scope.selectedGroup !== null) {
                _.forEach($scope.symbols, function (symbol) {
                    delete symbol._selected;
                    symbol.group = $scope.selectedGroup.id;
                });

                SymbolResource.move($scope.symbols, $scope.selectedGroup)
                    .success(function () {
                        Toast.success('Symbols move to group <strong>' + $scope.selectedGroup.name + '</strong>');
                        $modalInstance.close({
                            symbols: modalData.symbols,
                            group: $scope.selectedGroup
                        });
                    })
                    .catch(function (response) {
                        Toast.danger('<p><strong>Moving symbols failed</strong></p>' + response.data.message);
                    })
            }
        };

        /**
         * Selects the group where the symbols should be moved into
         *
         * @param {SymbolGroup} group
         */
        $scope.selectGroup = function (group) {
            $scope.selectedGroup = $scope.selectedGroup === group ? null : group;
        };

        /**
         * Close the modal dialog
         */
        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }
    }
}());