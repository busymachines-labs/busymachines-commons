function HeaderCtrl($scope, $http, Cookies, $rootScope) {

    function logout() {
        $rootScope.$broadcast('loginRequired');
        delete $http.defaults.headers.common["Auth-Token"];
        Cookies.removeItem('token');
        Cookies.removeItem('user');
    }

    $scope.logout = function() {
        $http.delete($scope.serviceUrls.getAuthenticationUrl($scope.user.loginName)).success(logout).error(logout);

    }
}
HeaderCtrl.$inject = ["$scope", "$http", "Cookies", "$rootScope"];

function LoginCtrl($scope, $location, $http, Cookies, AuthService) {

    $scope.invalidCredentials = false;

    $scope.recoverPassword = function() {
        angular.element("#forgotPassword").modal("hide");
        return false;
    }

    $scope.showForgotPassword = function(e) {
        // don't submit form
        e.preventDefault();
        angular.element("#forgotPassword").modal("show");
        return false;
    }

    $scope.login = function() {
        $http.post($scope.serviceUrls.getAuthenticationUrl($scope.username), {
            "password": $scope.password,
            "partyName": $scope.currentParty.PARTY || null
        }).success(function(data, status, headers) {
            if (200 === status) {
                var expirationDate = new Date();
                expirationDate.setDate(expirationDate.getDate() + 1);
                // store the token in the cookies
                Cookies.setItem("token", headers()["auth-token"], expirationDate, "/", $location.host());
                // set the http header
                $http.defaults.headers.common["Auth-Token"] = headers()["auth-token"];
                // store the user data in the $rootScope.user object
                for (var i in data) {
                    $scope.user[i] = data[i];
                }
                // store the user object in the cookies too
                Cookies.setItem("user", JSON.stringify(data), expirationDate, "/", $location.host());
                // tell the AuthService to retry failed requests;
                AuthService.loginConfirmed();
            }
        }).error(function(data, status) {
            if (404 === status) {
                $scope.invalidCredentials = true;
            }
        });
        return false;
    }

    $scope.updateValidity = function() {
        $scope.invalidCredentials = false;
    }

    $scope.updateModel = function(name, value) {
        $scope[name] = value;
        $scope.$apply();
    }
}
LoginCtrl.$inject = ["$scope", "$location", "$http", "Cookies", "AuthService"];