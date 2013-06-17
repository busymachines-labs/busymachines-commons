mdm.config(["$routeProvider", function($routeProvider) {
    $routeProvider.when("/stewart", {
        templateUrl: "stewart/stewart.html",
        controller: StewartCtrl,
        reloadOnSearch: false
    }).otherwise({
        redirectTo: "/"
    });
}]);