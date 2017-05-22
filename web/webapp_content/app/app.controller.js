(function() {
    'use strict';

    angular
        .module('org.perfrepo')
        .controller('AppController', AppController);

    function AppController(_comparisonSession, _info, authenticationService, comparisonSessionService, userModalService,
                           modalService, $state, $scope) {
        var vm = this;
        vm.logout = logout;
        vm.settings = settings;
        vm.support = support;
        vm.removeTestExecution = removeTestExecution;
        vm.removeAllTestExecutions = removeAllTestExecutions;
        vm.compareTestExecutions = compareTestExecutions;
        vm.comparisonSession = _comparisonSession;
        vm.info = _info;
        vm.user = authenticationService.getUser();

        vm.navigationItems = [
            {
                title: "Dashboard",
                iconClass: "fa fa-dashboard",
                href: $state.href('app.dashboard')
            },
            {
                title: "Tests",
                iconClass : "fa fa-shield",
                href: $state.href('app.testOverview')
            },
            {
                title: "Test executions",
                iconClass : "fa fa-flash",
                href: $state.href('app.testExecutionOverview')

            },
            {
                title: "Reports",
                iconClass : "fa fa-line-chart",
                href: $state.href('app.reportOverview')
            }
        ];

        $scope.$on('comparisonSessionChange', function(event, testExecutions) {
            vm.comparisonSession = testExecutions;
        });

        function removeTestExecution(id) {
            comparisonSessionService.removeFromComparison([id]).then(function (data) {
                vm.comparisonSession = data;
            });
        }

        function removeAllTestExecutions() {
            var testExecutionIds = [];
            angular.forEach(vm.comparisonSession, function(testExecution) {
                testExecutionIds.push(testExecution.id);
            });
            comparisonSessionService.removeFromComparison(testExecutionIds).then(function (data) {
                vm.comparisonSession = data;
            });
        }

        function compareTestExecutions() {
            $state.go('app.reportPreview');
        }

        function logout() {
            authenticationService.logout();
        }

        function settings() {
            var modalInstance = userModalService.editUser(vm.user.id);
        }

        function support() {
            modalService.getSupport();
        }
    }
})();