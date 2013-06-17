mdm.directive("xsDraggable", function() {
    return {
        restrict : 'A',
        link : function(scope, element, attrs, controllerObject) {
            angular.element(element).draggable({
                revert: "invalid",
                helper: "clone",
                appendTo: ".fields",
                start: function(e, ui) {
                    angular.element(e.target).hide();
                    ui.helper.addClass("dragging");
                },
                stop: function(e, ui) {
                    angular.element(e.target).show();
                }
            });
        }
    };
});

mdm.directive("xsDroppableMove", function() {
    return {
        restrict : 'A',
        link : function(scope, element, attrs, controllerObject) {
            angular.element(element).droppable({
                over: function(e, ui) {
                    var $this = angular.element(this),
                        $draggable = ui.draggable;

                    if ($this.parent().get(0) === $draggable.parent().get(0) && $this.parent().find('.field-container').index($draggable) < $this.parent().find('.field-container').index($this)) {
                        scope.droppable.dropIndex = $this.parent().find(".field-container").index($this) - 1;
                    } else {
                        scope.droppable.dropIndex = $this.parent().find(".field-container").index($this);
                    }
                    $this.children(".field-placeholder").width(ui.draggable.outerWidth()).addClass("show");
                    $this.siblings(".field-placeholder").removeClass("show");
                },
                out: function(e, ui) {
                    var $this = angular.element(this);
                    $this.children(".field-placeholder").removeClass("show");
                    if (!$this.parent().find('.over').length) {
                        scope.droppable.dropIndex = null;
                        $this.siblings(".field-placeholder").addClass("show");
                    }
                },
                deactivate: function(e, ui) {
                    element.children(".field-placeholder").removeClass("show");
                }
            });
        }
    };
});

mdm.directive("xsLoginModal", function() {
    return {
        restrict : 'A',
        link : function(scope, element, attrs, controllerObject) {

            var visible = false;

            scope.$on('loginRequired', function() {
                if (!visible) {
                    element.show();
                    visible = true;
                };
            });

            scope.$on('loginConfirmed', function() {
                element.fadeOut();
                visible = false;
            });
        }
    };
});

mdm.directive("xsInputSync", ["$timeout" , function($timeout) {
    return {
        restrict : "A",
        require: "?ngModel",
        link : function(scope, element, attrs, ngModel) {
            $timeout(function() {
                if (ngModel.$pristine && ngModel.$viewValue !== element.val()) {
                    scope.updateModel(ngModel.$name, element.val());
                }
            }, 500);
        }
    };
}]);

mdm.directive("xsListenReturn", function() {
    return {
        restrict: "A",
        link: function(scope, element, attrs) {
            element.on("keyup", function(e) {
                if (e.keyCode === 13) {
                    scope[attrs["xsListenReturn"]]();
                    scope.$apply();
                }
            });
        }
    }
});

mdm.directive("xsReportGraph", function() {
    return {
        restrict: "A",
        link: function(scope, element, attrs) {

            var section,
                graph,
                graphData,
                data,
                loaded = false;

            attrs.$observe('position', function(value) {
                loaded = true;
                section = parseInt(value.split(",")[0], 10);
                graph = parseInt(value.split(",")[1], 10);
                graphData = scope[attrs["xsReportGraph"]][section]["graphs"][graph];
                data = graphData.data;

                var margin = {top: 20, right: 20, bottom: 100, left: 40},
                    width = element.width() - margin.left - margin.right,
                    height = 700 - margin.top - margin.bottom;

                var formatPercent = d3.format(".0");

                var x = d3.scale.ordinal()
                    .rangeRoundBands([0, width], .1);

                var y = d3.scale.linear()
                    .range([height, 0]);

                var xAxis = d3.svg.axis()
                    .scale(x)
                    .orient("bottom");

                var yAxis = d3.svg.axis()
                    .scale(y)
                    .orient("left")
                    .tickFormat(formatPercent);

                var svg = d3.select(element.get(0)).append("svg")
                    .attr("width", width + margin.left + margin.right)
                    .attr("height", height + margin.top + margin.bottom)
                    .append("g")
                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                data.forEach(function(d) {
                    d.val = +d.val;
                });

                x.domain(data.map(function(d) { return d.label; }));
                y.domain([0, d3.max(data, function(d) { return d.val; })]);

                svg.append("g")
                    .attr("class", "x axis")
                    .attr("transform", "translate(0," + height + ")")
                    .call(xAxis)
                    .selectAll("text")
                    .attr("dx", "-5em")
                    .attr("dy", ".15em")
                    .attr("transform", function(d) {
                        return "rotate(-65)"
                    });;

                svg.append("g")
                    .attr("class", "y axis")
                    .call(yAxis)
                    .append("text")
                    .attr("transform", "rotate(-90)")
                    .attr("y", 6)
                    .attr("dy", ".71em")
                    .style("text-anchor", "end")
                    .text(graphData.field);

                svg.selectAll(".bar")
                    .data(data)
                    .enter().append("rect")
                    .attr("class", "bar")
                    .attr("x", function(d) { return x(d.label); })
                    .attr("width", x.rangeBand())
                    .attr("y", function(d) { return y(d.val); })
                    .attr("height", function(d) { return height - y(d.val); });
            });
        }
    }
});

mdm.directive("xsDatepicker", function() {
    return {
        restrict: "A",
        require: '?ngModel',
        link: function(scope, element, attrs, ngModel) {
            if (!Modernizr.touch) {
                element.datepicker().on('changeDate', function(e) {
                    ngModel.$setViewValue(element.val());
                    scope.$apply();
                    element.datepicker('hide');
                });
            }
        }
    }
});

mdm.directive("xsMultiselect", ["$timeout", function($timeout) {
    return {
        restrict: "A",
        require: '?ngModel',
        link: function(scope, element, attrs, ngModel) {
            var width = element.width();
            element.multiselect({
                selectedList: 5,
                beforeopen: function() {
                    angular.element(".ui-multiselect-menu").css("width", element.siblings(".ui-multiselect").outerWidth());
                }
            }).multiselectfilter({
                label: ""
            });
            scope.$watch(attrs.xsMultiselect, function() {
                element.multiselect('refresh');
                element.multiselectfilter("updateCache");
                element.siblings(".ui-multiselect").css("width", "100%")
            });
        }
    }
}]);

mdm.directive("xsReportCompareGraph", function() {
    return {
        restrict: "A",
        link: function(scope, element, attrs) {

            var section,
                graph,
                graphData,
                data,
                loaded = false;

            attrs.$observe('position', function(value) {
                loaded = true;
                section = parseInt(value.split(",")[0], 10);
                graph = parseInt(value.split(",")[1], 10);
                graphData = scope[attrs["xsReportCompareGraph"]][section]["graphs"][graph];
                data = graphData.data;
                var groups = [[], []];

                data.forEach(function(d) {
                    groups[0].push(d.val);
                    groups[1].push(d.old);
                });

                var margin = {top: 20, right: 20, bottom: 100, left: 40},
                    outerW = element.width(), outerH = 700,
                    w = outerW - margin.left - margin.right,
                    h = outerH - margin.top - margin.bottom;

                var numberSeries = groups.length;  // series in each group

                var formatPercent = d3.format(".0");

                var x0 = d3.scale.ordinal()
                    .domain(data.map(function(d) { return d.label; }))
                    .rangeBands([0, w], 0.1);

                var x1 = d3.scale.ordinal()
                    .domain(d3.range(numberSeries))
                    .rangeBands([0, x0.rangeBand()]);

                var y = d3.scale.linear()
                    .domain([0, d3.max(data, function(d) {
                        if (d.old && d.old > d.val) {
                            return d.old;
                        }
                        return d.val;
                    })])
                    .range([h, 0]);

                var xAxis = d3.svg.axis()
                    .scale(x0)
                    .orient("bottom");

                var yAxis = d3.svg.axis()
                    .scale(y)
                    .orient("left")
                    .tickFormat(formatPercent);

                var vis = d3.select(element.get(0))
                    .append("svg:svg")
                    .attr("width", outerW)
                    .attr("height", outerH)
                    .append("g")
                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                vis.append("g")
                    .attr("class", "x axis")
                    .attr("transform", "translate(0," + h + ")")
                    .call(xAxis)
                    .selectAll("text")
                    .attr("dx", "-5em")
                    .attr("dy", ".15em")
                    .attr("transform", function(d) {
                        return "rotate(-65)"
                    });

                vis.append("g")
                    .attr("class", "y axis")
                    .call(yAxis)
                    .append("text")
                    .attr("transform", "rotate(-90)")
                    .attr("y", 6)
                    .attr("dy", ".71em")
                    .style("text-anchor", "end")
                    .text(graphData.field);

                var series = vis.selectAll("g.series")
                    .data(groups)
                    .enter().append("svg:g")
                    .attr("class", "series") // Not strictly necessary, but helpful when inspecting the DOM
                    .attr("transform", function (d, i) { return "translate(" + x1(i) + ")"; });

                series.selectAll("rect")
                    .data(Object)
                    .enter().append("svg:rect")
                    .attr("class", "bar")
                    .attr("x", 0)
                    .attr("y", function (d) { return y(d); })
                    .attr("width", x1.rangeBand())
                    .attr("height", function (d) { return h - y(d); })
                    .attr("transform", function (d, i) { return "translate(" + x0(i) + ")"; });

            });
        }
    }
});

mdm.directive("xsSelect", function() {
    return {
        restrict: "A",
        link: function(scope, element, attrs) {
            element.customSelect({
                iconElement: true,
                iconElementClass: "ui-icon-triangle-1-s",
                iconElementPosition: "before"
            });
            scope.$watch(attrs.ngModel, function() {
                element.trigger("update-selected");
            });
        }
    }
});