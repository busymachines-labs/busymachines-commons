mdm.config(["$routeProvider", function($routeProvider) {
    $routeProvider.when("/", {
        templateUrl: "dashboard/dashboard.html",
        controller: DashboardCtrl
    });
}]);

function DashboardCtrl($scope, Sources) {
    $scope.templates.header.COLOR = "purple";
}
DashboardCtrl.$inject = ["$scope", "Sources"];
