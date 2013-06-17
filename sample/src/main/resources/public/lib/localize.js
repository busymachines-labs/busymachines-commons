'use strict';

/*
 * An AngularJS Localization Service
 *
 * Written by Jim Lavin
 * http://codingsmackdown.tv
 *
 * Copied 14 apr 2013
 */

angular.module('localization', [])
    // localization service responsible for retrieving resource files from the server and
    // managing the translation dictionary
    .factory('localize', ['$http', '$rootScope', '$window', '$filter', '$location', function ($http, $rootScope, $window, $filter, $location) {
        var locale = ($location.search().locale || $window.navigator.userLanguage || $window.navigator.language).toLowerCase();
        var dashIndex = locale.indexOf('-');
        var locales = (dashIndex < 0) ? [locale, 'und'] : [locale, locale.substring(0, dashIndex), 'und'];
        var localize = {
            // use the $window service to get the language of the user's browser
            language: locale,
            locale: locale,
            locales: locales,
            // array to hold the localized resource string entries
            dictionary:[],
            // flag to indicate if the service hs loaded the resource file
            resourceFileLoaded:false,

            // success handler for all server communication
            successCallback:function (data) {
                // store the returned array in the dictionary
                localize.dictionary = data;
                // set the flag that the resource are loaded
                localize.resourceFileLoaded = true;
                // broadcast that the file has been loaded
                $rootScope.$broadcast('localizeResourcesUpdates');
            },

            // allows setting of language on the fly
            setLanguage: function(value) {
                localize.language = value;
                localize.initLocalizedResources();
            },

            // loads the language resource file from the server
            initLocalizedResources:function () {
                // build the url to retrieve the localized resource file
                var url = '/i18n/resources-locale_' + localize.language + '.js';
                // request the resource file
                $http({ method:"GET", url:url, cache:false }).success(localize.successCallback).error(function () {
                    // the request failed set the url to the default resource file
                    var url = '/i18n/resources-locale_default.js';
                    // request the default resource file
                    $http({ method:"GET", url:url, cache:false }).success(localize.successCallback);
                });
            },
            
            // loads the language resource file from the server
            initResources:function () {
                // build the url to retrieve the localized resource file
                var url = '/lib/angularjs-1.0.6/i18n/angular-locale_' + localize.language + '.js';
                $.ajax({
                    url: url,
                    dataType: "script",
                    success: function(date) {console.log("LOADED"); localize.successCallback},
                    failure: function(date) {console.log("ERROR:"+date); localize.successCallback}
                  });
                // request the resource file
                $http({ method:"GET", url:url, cache:false }).success(localize.initLocalizedResources).error(function () {
                    // the request failed set the url to the default resource file
                    var url = '/lib/angularjs-1.0.6/i18n/angular-locale_en.js';
                    // request the default resource file
                    $http({ method:"GET", url:url, cache:false }).success(localize.initLocalizedResources);
                });
            },
            
            // checks the dictionary for a localized resource string
            getLocalizedString: function(value) {
                // default the result to an empty string
                var result = '';

                // make sure the dictionary has valid data
                if ((localize.dictionary !== []) && (localize.dictionary.length > 0)) {
                    // use the filter service to only return those entries which match the value
                    // and only take the first result
                    var entry = $filter('filter')(localize.dictionary, function(element) {
                            return element.key === value;
                        }
                    )[0];

                    // set the result
                    result = entry.value;
                }
                // return the value to the call
                return result;
            },
            
            chooseLocalizedString: function(value, locales) {
                if (jQuery.type(value) != "object") {
                    return value.toString();
                }
                locales = locales || this.locales;
                locales = Array.isArray(locales) ? locales : [locales];
                for (var i = 0; i < locales.length; i++) {
                    var val = value[locales[i]];
                    if (typeof val != "undefined") {
                        return val;
                    }
                };
                return "?";
            },
            
            getDate: function(date, format) {
                return "Some Date";
            }
        };

        // force the load of the resource file
        localize.initResources();

        // return the local instance when called
        return localize;
    } ])
    // simple translation filter
    // usage {{ TOKEN | translate }}
    .filter('translate', ['localize', function (localize) {
        return function (input) {
            return localize.getLocalizedString(input);
        };
    }])
    .filter('chooseTranslation', ['localize', function (localize) {
        return function (input, locales) {
            return localize.chooseLocalizedString(input, locales);
        };
    }])
    // simple translation filter
    // usage {{ DATE | i18n-date [format] }}
    .filter('date2', ['$locale', 'localize', function ($locale, localize) {
        console.log(localize.language);
        return function (date, format) {
            return localize.getDate(date, format);
        };
    }])
    // translation directive that can handle dynamic strings
    // updates the text value of the attached element
    // usage <span data-i18n="TOKEN" ></span>
    // or
    // <span data-i18n="TOKEN|VALUE1|VALUE2" ></span>
    .directive('i18n', ['localize', function(localize){
        var i18nDirective = {
            restrict:"EAC",
            updateText:function(elm, token){
                var values = token.split('|');
                if (values.length >= 1) {
                    // construct the tag to insert into the element
                    var tag = localize.getLocalizedString(values[0]);
                    // update the element only if data was returned
                    if ((tag !== null) && (tag !== undefined) && (tag !== '')) {
                        if (values.length > 1) {
                            for (var index = 1; index < values.length; index++) {
                                var target = '{' + (index - 1) + '}';
                                tag = tag.replace(target, values[index]);
                            }
                        }
                        // insert the text into the element
                        elm.text(tag);
                    };
                }
            },

            link:function (scope, elm, attrs) {
                scope.$on('localizeResourcesUpdates', function() {
                    i18nDirective.updateText(elm, attrs.i18n);
                });

                attrs.$observe('i18n', function (value) {
                    i18nDirective.updateText(elm, attrs.i18n);
                });
            }
        };

        return i18nDirective;
    }])
    // translation directive that can handle dynamic strings
    // updates the attribute value of the attached element
    // usage <span data-i18n-attr="TOKEN|ATTRIBUTE" ></span>
    // or
    // <span data-i18n-attr="TOKEN|ATTRIBUTE|VALUE1|VALUE2" ></span>
    .directive('i18nAttr', ['localize', function (localize) {
        var i18NAttrDirective = {
            restrict: "EAC",
            updateText:function(elm, token){
                var values = token.split('|');
                // construct the tag to insert into the element
                var tag = localize.getLocalizedString(values[0]);
                // update the element only if data was returned
                if ((tag !== null) && (tag !== undefined) && (tag !== '')) {
                    if (values.length > 2) {
                        for (var index = 2; index < values.length; index++) {
                            var target = '{' + (index - 2) + '}';
                            tag = tag.replace(target, values[index]);
                        }
                    }
                    // insert the text into the element
                    elm.attr(values[1], tag);
                }
            },
            link: function (scope, elm, attrs) {
                scope.$on('localizeResourcesUpdated', function() {
                    i18NAttrDirective.updateText(elm, attrs.i18nAttr);
                });

                attrs.$observe('i18nAttr', function (value) {
                    i18NAttrDirective.updateText(elm, value);
                });
            }
        };

        return i18NAttrDirective;
    }]);