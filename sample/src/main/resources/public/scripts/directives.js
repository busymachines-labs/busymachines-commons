catalogManager.directive("appMain", function () {
    return {
        templateUrl: "app/views/main.html",
        replace: true,
        controller: ["$scope", "$location", function($scope, $location) {
            $scope.isActive = function (url) {
                return $location.path().indexOf(url) > -1;
            }
        }],
        link: function (scope, element, attrs) {
            scope.$watch(attrs.auNavbarTemplate, function (value) {
                scope.template = value;
            });
        }
    }
});
