catalogManager.factory("AssetModels", ["$resource", "$rootScope", function($resource, $rootScope) {
    return $resource(
        $rootScope.serviceUrls.getAssetModelsUrl(),
        {},
        {
            get: {
                method: "GET",
                isArray: true
            }
        }
    );
}]);

catalogManager.factory("Cookies", function() {

    // taken from https://developer.mozilla.org/en-US/docs/DOM/document.cookie
    return {
        getItem: function (sKey) {
            if (!sKey || !this.hasItem(sKey)) { return null; }
            return unescape(document.cookie.replace(new RegExp("(?:^|.*;\\s*)" + escape(sKey).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\=\\s*((?:[^;](?!;))*[^;]?).*"), "$1"));
        },
        setItem: function (sKey, sValue, vEnd, sPath, sDomain, bSecure) {
            if (!sKey || /^(?:expires|max\-age|path|domain|secure)$/i.test(sKey)) { return; }
            var sExpires = "";
            if (vEnd) {
                switch (vEnd.constructor) {
                    case Number:
                        sExpires = vEnd === Infinity ? "; expires=Tue, 19 Jan 2038 03:14:07 GMT" : "; max-age=" + vEnd;
                        break;
                    case String:
                        sExpires = "; expires=" + vEnd;
                        break;
                    case Date:
                        sExpires = "; expires=" + vEnd.toGMTString();
                        break;
                }
            }
            document.cookie = escape(sKey) + "=" + escape(sValue) + sExpires + (sDomain ? "; domain=" + sDomain : "") + (sPath ? "; path=" + sPath : "") + (bSecure ? "; secure" : "");
        },
        removeItem: function (sKey, sPath) {
            if (!sKey || !this.hasItem(sKey)) { return; }
            document.cookie = escape(sKey) + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT" + (sPath ? "; path=" + sPath : "");
        },
        hasItem: function (sKey) {
            return (new RegExp("(?:^|;\\s*)" + escape(sKey).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\=")).test(document.cookie);
        },
        keys: function () {
            var aKeys = document.cookie.replace(/((?:^|\s*;)[^\=]+)(?=;|$)|^\s*|\s*(?:\=[^;]*)?(?:\1|$)/g, "").split(/\s*(?:\=[^;]*)?;\s*/);
            for (var nIdx = 0; nIdx < aKeys.length; nIdx++) { aKeys[nIdx] = unescape(aKeys[nIdx]); }
            return aKeys;
        }
    };
});

catalogManager.factory('AuthService', ["$rootScope", "$injector", function($rootScope, $injector) {

    var buffer = [];

    function retry(config, deferred) {
        var $http = $injector.get('$http');
        $http(config).then(function(response) {
            deferred.resolve(response);
        });
    }

    function retryAll() {
        for (var i = 0; i < buffer.length; ++i) {
            retry(buffer[i].config, buffer[i].deferred);
        }
        buffer = [];
    }

    return {
        loginConfirmed: function() {
            $rootScope.$broadcast('loginConfirmed');
            retryAll();
        },
        pushToBuffer: function(config, deferred) {
            buffer.push({
                config: config,
                deferred: deferred
            })
        }
    }
}]);

catalogManager.factory("AuthResponseInterceptor", ["$q", "$rootScope", "AuthService", function($q, $rootScope, AuthService) {

    function success(response) {
        return response;
    }

    function error(response) {
        if (response.status === 401 && response.config.url.indexOf("/authentication") < 0) {
            var deferred = $q.defer();
            AuthService.pushToBuffer(response.config, deferred);
            if (typeof window.callPhantom === 'function') {
                console.log("Unauthorized");
                window.callPhantom("Unauthorized");
            }
            else {
                $rootScope.$broadcast('loginRequired');            	
            }

            return deferred.promise;
        }
        // otherwise
        return $q.reject(response);
    }

    return function(promise) {
        return promise.then(success, error);
    }
}]);

catalogManager.factory("SpinnerInterceptor", ["$q", "RequestCounter", function($q, RequestCounter) {
    function success(response) {
        RequestCounter.decrement();
        return response;
    }

    function error(response) {
        RequestCounter.decrement();
        return $q.reject(response);
    }

    return function(promise) {
        return promise.then(success, error);
    }
}]);

catalogManager.factory("RequestCounter", ["$rootScope", function($rootScope) {

    // FIXME: Don't know why but the first request doesn't count, so I'm setting it here
    var counter = 1;

    return {
        increment: function() {
            counter++;
            if (counter === 1) {
                $rootScope.$broadcast("showSpinner");
            }
        },
        decrement: function() {
            counter--;
            if (counter === 0) {
                $rootScope.$broadcast("hideSpinner");
            }
        }
    }
}]);