mdm.config(["$locationProvider", "$httpProvider", function($locationProvider, $httpProvider) {
    $locationProvider.html5Mode(true);
    $httpProvider.responseInterceptors.push('AuthResponseInterceptor');
    $httpProvider.responseInterceptors.push('SpinnerInterceptor');
}]);

mdm.run(["$rootScope", "$location", "$http", "Cookies", "parties", "RequestCounter", "$locale", function($rootScope, $location, $http, Cookies, parties, RequestCounter, $locale) {

    $rootScope.locale = $locale.id
    
    var URL_PREFIX = "/v1",
        STYLE = "";

    $rootScope.currentParty = {};

    $rootScope.user = {};

    $rootScope.assetDuplicate = {};

    $rootScope.header = {};

    $rootScope.page = {
        title: "Installation center"
    }

    $rootScope.serviceUrls = {
        getAuthenticationUrl: function(user) {
            return URL_PREFIX + "/users/" + user + "/authentication";
        },
        getSourcesUrl: function() {
            return URL_PREFIX + "/sources";
        },
        getPartiesUrl: function() {
            return URL_PREFIX + "/parties"
        }
    }

    // set the application style based on the domain
    var url = $location.absUrl();
    for (var i = 0, l = parties.length; i < l; i++) {
    	var index = url.indexOf("//" + parties[i].PARTY + ".");
        if (index > -1) {
            if (parties[i].STYLE) {
                angular.element("head").append("<link href=\"themes/" + parties[i].STYLE + "/css/styles.css\" rel=\"stylesheet\" type=\"text/css\">");
            }
            $rootScope.currentParty = parties[i];
            break;
        }
    }
    STYLE = $rootScope.currentParty.STYLE ? "themes/" + $rootScope.currentParty.STYLE + "/" : "";
    $rootScope.currentParty.PREFIX = $rootScope.currentParty.PARTY ? $rootScope.currentParty.PARTY : "www";

    // check to see if there's a language parameter set (en-US, nl-NL)
/*    if ($location.search()["language"]) {
        localize.setLanguage($location.search()["language"]);
    }
*/
    // Templates object
    // FIXME: move to angular value?
    $rootScope.templates = {
        header: {
            HEADER_TEMPLATE: "header.html",
            HEADER_APP_TEMPLATE: "header_app.html",
            HEADER_DASHBOARD: "dashboard/header_app.html",
            LOGO: STYLE + "logo.html",
            LOGO_DASHBOARD: "dashboard/logo.html",
            FAVICON: "/" + STYLE + "img/favicon.ico"
        },
        footer: {
            FOOTER_TEMPLATE: "footer.html"
        }
    }

    // set the authToken for browser or phantomjs
    if ($location.search()["authToken"]) {
        console.log("authToken: " + $location.search()["authToken"]);
        $http.defaults.headers.common['Auth-Token'] = $location.search()["authToken"];
    } else if (Cookies.hasItem('token')) {
        console.log("authToken from cookie: " + Cookies.getItem('token'));
        $http.defaults.headers.common['Auth-Token'] = Cookies.getItem('token');
        $rootScope.user = JSON.parse(Cookies.getItem('user'));
    } else {
        console.log("No authToken");    	
    }

    $rootScope.$on("$viewContentLoaded", function() {
        if (!$http.defaults.headers.common['Auth-Token']) {
// Disabled to allow auto-login of demo user    	
//            $rootScope.$broadcast('loginRequired');
        }
    });

    // everytime a request is executed increment the request counter by using a request transformation function
    $http.defaults.transformRequest.push(function(data) {
        RequestCounter.increment();
        return data;
    });
}]);

mdm.value("parties", [
    {
    	PARTY: "koffiepartners",
    	STYLE: "koffiepartners"
    },
    {
    	PARTY: "urbaintrade",
    	STYLE: "urbaintrade"
    }
]);