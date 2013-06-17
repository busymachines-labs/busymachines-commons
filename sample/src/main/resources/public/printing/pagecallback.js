system = require("system");
var fs = require("fs");

var usage = function() {
	console.log("Usage : bin/phantomjs script.js mode url outputFile");
	console.log("Parameters : ");
	console.log("mode : the mode in which the script should run : ");
	console.log("				pdf = generate pdf output");
	console.log("				csv = generate csv output");
	console.log("url : the url to load");
	console.log("outputFile : the path where to store the output !!! make sure you have the right extension to the filename !!!");
	console.log("");
	console.log("Example 1 : bin/phantomjs pdf http://www.parinte-profesor.ro the_page.pdf");
	console.log("Example 2 : bin/phantomjs csv http://www.parinte-profesor.ro the_page.csv");

	phantom.exit();
};

var url = null;
var mode = null;
var outputFile = null;

if (system.args.length < 4) {
    usage();
} 
else {
	mode = system.args[1];
	url = unescape(system.args[2]);
	outputFile = system.args[3];
}

console.log("mode="+mode);
console.log("url="+url);
console.log("outputFile="+outputFile);

var page = require("webpage").create();

page.onConsoleMessage = function(msg) { console.log(msg); };

page.onCallback = function(arg1,arg2) {
    switch (mode) {
        case "pdf" :

        // Possible override of the viewport size
        if (arg1 === undefined) {
            // leave the viewportSize to default
        } 
        else {
            page.viewportSize = arg1;
        }

        // Possible override of the paper size
        if (arg2 === undefined) {
            // leaves the paperSize to default
        } 
        else {
            page.paperSize = arg2;
        }

	    page.render(outputFile);
            console.log("Pdf output to : "+outputFile);
            phantom.exit();
            break;
        case "csv" :
            fs.write(outputFile,arg1,'w');
            console.log("CSV output to: "+outputFile);
            phantom.exit();
            break;
        default :
            console.log("Error: unrecognised mode "+mode);
            usage();
            break;
    }
}

page.open(url, function (status) {
    if (status !== "success") {
      console.log("Error: Failed to open url : "+url);
    }

    page.evaluate(function() {
//        // Return-value of the "onCallback" handler arrive here
/*
	  setInterval(function(){
	    window.callPhantom("Hello, I'm coming to you from the 'page' context");
	},10000);
*/
        //var callbackResponse = window.callPhantom("Hello, I'm coming to you from the 'page' context");
        //console.log("Received by the 'page' context: "+callbackResponse);
    });

});



