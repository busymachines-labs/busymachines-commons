// <ol>
//     <li ng-repeat="child in data.children" tree>
//       <div>{{child.name}}</div>
//       <ol><branch></ol>
// </ol>
angular.module('directives', []).directive('tree', ['$compile', function ($compile) {
  'use strict';
  return {
    restrict: 'A',
    compile: function (tElement, tAttrs) {
 
      var branch = tElement.find('branch'),
          repeatExpr,
          childExpr,
          childrenExpr;
 
      if (!branch.length) {
        throw new Error('tree directive must contain a branch node.');
      }
 
      repeatExpr = (branch.attr('branch') || tAttrs.ngRepeat).match(/^(.*) in (?:.*\.)?(.*)$/);
      childExpr = repeatExpr[1];
      childrenExpr = repeatExpr[2];
      tElement.attr('ng-repeat', childExpr + ' in ' + childExpr + '.' + childrenExpr);
      
      return function link (scope, element) {
 
        scope.$depth = scope.$depth || 0;
        scope.$watch(childExpr, function(child) {
 
          var childScope = scope.$new();
 
          childScope[childrenExpr] = child[childrenExpr];
          childScope.$depth = scope.$depth + 1;
 
          element.find('branch').replaceWith($compile(tElement.clone())(childScope));
        });
      };
    }
  };
}]);