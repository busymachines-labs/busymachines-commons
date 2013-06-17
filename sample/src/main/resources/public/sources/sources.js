mdm.config([ "$routeProvider", function($routeProvider) {
    $routeProvider.when("/sources", {
        templateUrl : "sources/sources.html",
        controller : SourcesCtrl,
        reloadOnSearch : false
    }).otherwise({
        redirectTo : "/"
    });
} ]);

mdm.factory("Sources", [ "$resource", "$rootScope", function($resource, $rootScope) {
    return $resource($rootScope.serviceUrls.getSourcesUrl());
} ]);

function SourcesCtrl($scope, $http, $location, $routeParams, Sources) {
    $scope.title = "Sources"
    $scope.currentTab = "5"
    $scope.source = null
    $scope.jsonSource = function() {
        return JSON.stringify(angular.fromJson(angular.toJson($scope.source)), undefined, 2) 
    }
    $scope.newSource = function() {
        $scope.sources.push({
            name : "New Source"
        })
    }
    $scope.refresh = function() {
        Sources.query({}, function(data) {
            $scope.sources = data
        });
    }
    $scope.selectSource = function(source) {
        $scope.source = source
    }
    $scope.refresh();
}

mdm.filter('printSchedule', ['$filter', function($filter) {
    return function(schedule) {
        var time = new Date(schedule.time);
        switch (schedule.repeat) {
        case "daily":
            return "Daily at " + $filter('date')(time, 'shortTime');
        case "weekly":
            return "Weekly on " + $filter('date')(time, 'EEEE') + " at " + $filter('date')(time, 'shortTime');
        case "monthly":
            return "Monthly on day " + $filter('date')(time, 'dd') + " at " + $filter('date')(time, 'shortTime');
        case "yearly":
            return "Yearly on " + $filter('date')(time, 'dd MMMM') + " at " + $filter('date')(time, 'shortTime');
        }
        return "Once at " + $filter('date')(time, 'dd MMMM yyyy') + " at " + $filter('date')(time, 'shortTime');;
    };
}]);

SourcesCtrl.$inject = [ "$scope", "$http", "$location", "$routeParams", "Sources" ];