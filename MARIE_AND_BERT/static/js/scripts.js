/*
 * Copyright (c) CMCL Innovations - All Rights Reserved
 *
 * This application and all inherent data, source files, information and graphics are
 * the copyright and sole property of Computational Modelling Cambridge Ltd (CMCL Innovations).
 *
 * Any unauthorised redistribution or reproduction of part, or all, of the contents of this
 * applicationin any form is prohibited under UK Copyright Law. You may not, except with the
 * express written permission of CMCL Innovations, distribute or commercially exploit this
 * application or it's content. All other rights reserved.
 *
 * For more information please contact support@cmclinnovations.com
 *
 * ------------------------------------------------------------
 *
 * This script contains functionality for the Marie chemistry chatbot,
 * including selection of random questions, submitting requests, and
 * parsing the resulting data for HTML display.
 *
 */


/*
 * Copyright (c) CMCL Innovations - All Rights Reserved
 *
 * This application and all inherent data, source files, information and graphics are
 * the copyright and sole property of Computational Modelling Cambridge Ltd (CMCL Innovations).
 *
 * Any unauthorised redistribution or reproduction of part, or all, of the contents of this
 * applicationin any form is prohibited under UK Copyright Law. You may not, except with the
 * express written permission of CMCL Innovations, distribute or commercially exploit this
 * application or it's content. All other rights reserved.
 *
 * For more information please contact support@cmclinnovations.com
 *
 * ------------------------------------------------------------
 *
 * This script contains functionality for the Marie chemistry chatbot,
 * including selection of random questions, submitting requests, and
 * parsing the resulting data for HTML display.
 *
 */

// Variables accessed throughout the script
$('document').ready(function(){
    // =================== search button and enter in input field =======
    $('#ask-button').click(function (e){
        askQuestion(e);
    });

    $('#input-field').keypress(function(e){
        if(e.which === 13){//Enter key pressed
            askQuestion(e);
        }
    });
    google.charts.load('current', {packages: ['corechart', 'line']});
});


var scriptURL = document.currentScript.src;
var scriptDir = scriptURL.substring(0, scriptURL.lastIndexOf("/") + 1);
var imageDir = "/user/images/";


// Type variable was missing, have added with value when last present.
// Needs checking to see if this was supposed to be dynamic - Michael
let type = "worldavatar";

// Location of the chatbot itself
var botURL = "/marie/request/";
// let url = botURL + "chemistry_chatbot/query?type=" + type;
let url = "search"

// Hide the results row by default
var resultsRow = document.getElementById("results-row");
resultsRow.style.display = "none";

// Currently asking a question?
var asking = 0;

// Add ENTER detection on search field
document.getElementById("input-field").addEventListener("keyup", function(event) {
	if(event.keyCode === 13) {
		askQuestion();
	}
});

// Add auto-complete for species names in the search box
$('#input-field').autocomplete({
	source: species,
	minLength: 2,
	max: 10,
	scroll: true
});

// Register click listeners for sample questions
linkSampleQuestions();


/*
 Find all 'sample-question' elements and register a click listener.
 */
function linkSampleQuestions() {
	// Find all 'sample-question' elements
	var sampleQuestions = document.getElementsByClassName("sample-question");

	// Bind a click listener to call pipeQuestionFunction
	for (var i = 0; i < sampleQuestions.length; i++) {
		let sampleQuestion = sampleQuestions.item(i);

		sampleQuestion.addEventListener(
			'click',
			function () { pipeQuestion(sampleQuestion.textContent); },
			false
		);
	}
}


/*
 Pipe the input text into the 'input-field' element and fire it off.
 */
function pipeQuestion(question) {
	// Pipe question to text field
	document.getElementById('input-field').value = question;
	$('#input-field').css("color", "inherit");
	window.scrollTo(0, 0);

	// Fire query automatically (requested by MK)
	askQuestion();
}


/*
 Pick a random question and enter it into the search control.
 Note that this depends on the fact that the marie-dict.js script
 is also loaded.
 */
function shuffleQuestion() {
	// Find all 'sample-question' elements
	let index = Math.floor(Math.random() * random_questions.length);
	let sampleQuestion = random_questions[index];
	pipeQuestion(sampleQuestion);
}


/*
	Resets the results containers with default content.
*/
function resetResults() {
	let spinner = imageDir + "spinner.svg";
	let html = "<img src=\"" + spinner + "\" style=\"vertical-align: middle;\" width=\"22px\">  Loading, please wait...";

	let chatbotResults = document.getElementById("chatbot-results");
	chatbotResults.innerHTML = html;
	$("#data_chart").remove();
}


/*
 Send the current question to the chatbot.
 */
function askQuestion() {
	if(asking > 0) {
		// No concurrent questions
		return;
	}

	resetResults();
    $("#data_chart").remove();

	let spinner = imageDir + "spinner.svg";

	// Make the ask button into a loading spinner
	let askButton = document.getElementById("ask-button");
	let imgTags = askButton.getElementsByTagName("img");
	imgTags.item(0).src = spinner;

	// Get the question currently within the input-field
	let inputField = document.getElementById('input-field');
	let question = inputField.value;

	if (question == "") {
		// Show an error
		$('#input-field').val("Please enter a question before submitting.");
		$('#input-field').css("color", "red");
		imgTags.item(0).src = imageDir + "search.svg";
		return;
	}


	// Build the URL for the question
	question = question.replace('	', ' ');
	// question = question.replace(/[/+]/g, 'add_sign');

	var promises = [];

	asking = 1;
	// Make the request for the world avatar
	makeRequest(question, "worldavatar", "json", handleResults, promises);

	// Reset the search button when all requests are complete
	$.when.apply($, promises).then(function() {
		// Revert button to search icon
		let askButton = document.getElementById("ask-button");
		let imgTags = askButton.getElementsByTagName("img");
		imgTags.item(0).src = imageDir + "search.svg";
		$('#input-field').css("color", "inherit");
	}, function() {
		// Error occurred, dealt with elsewhere
	});

	// Show the results row
	resultsRow.style.display = "block";
}


/*
 Make a single HTTP request to the chatbot.
*/
function makeRequest(question, type, resultType, successFunction, promises) {
	//let url = botURL + "chemistry_chatbot/query?type=" + type;
	url = "search"
    console.log('url is ', url)
	let data = { "question": question };

	// Make the call
	promises.push($.ajax({
		url: url,
		data: data,
		dataType: resultType,
		timeout: (10000 * 60),
		success: function (data) {

			successFunction(data);
			asking--;
		},
		error: function (xhr, ajaxOptions, thrownError) {
			console.log(xhr.status);
			console.log(thrownError);
			// for test, use results
			// successFunction(results.pop())
			successFunction(null);
			asking--;
		}
	}));
}

/*
 Process the results from a WorldAvatar request
*/

// convertToJSONResults
//

/* This is the function that handles all results, including

*/

function handleResults(rawResult){

	if(rawResult === null || rawResult === undefined || rawResult === "null") {
		let msg = "<span style=\"color: red; padding-left: 15px;\">The World Avatar failed to provide an answer.</span>";
		let chatbotResults = document.getElementById("chatbot-results");
		if(chatbotResults !== null) chatbotResults.innerHTML = msg;
        return;
	}

	// 1. convert any results to JSON object/ JSON array
	// 2. identify chart data from non-chart data
	// 3. call processChatbotResults or process matrix data accordingly
	var jsonData = null;
	jsonData = convertToJSONResults(rawResult);
	if ('attribute' in jsonData || 'multiple_results' in jsonData) {
        process_matrix_data(jsonData);
    }
    else{
        processChatbotResults(jsonData);
    }
}


/*
 Process the results from a WorldAvatar request.
*/
function processChatbotResults(jsonData) {

    let chatbotResults = null;


    if (Array.isArray(jsonData)) {
        //console.log('The object is identified as an Array', jsonData);
		try {
            // JSON array
            chatbotResults = parseJSONArray(jsonData);
		} catch (error) {
			chatbotResults = parseJSONObject(jsonData);
		}


        } else {
            // JSON object
            chatbotResults = parseJSONObject(jsonData);
        }
	// Find the div container to add results to
	let resultsContainer = document.getElementById("chatbot-results");
	resultsContainer.innerHTML = chatbotResults;
}

/*
 Parses the input JSON Object into a HTML table.
*/
function parseJSONObject(jsonResults) {
	let valueSet = {};
	valueSet["Result"] = [];

	// Get the variable headers
	let headObject = jsonResults["head"];

	if (headObject == null) {
		// May not be a JSON object?
        try {
            return parseJSONArray(jsonResults);
        }
        catch (err){
            // this  could be a result from the pce agent, we need to handle it differently ..
            // make it an JSON array then pass it to parseJSONArray
            if ('result' in jsonResults){
                let jsonData = [jsonResults['result']];
                return parseJSONArray(jsonData);
            }
            console.log('Ill-formatted data')
        }
	}

	let headVars = headObject["vars"];
	headVars.forEach((key) => {
		let values = valueSet[key];
		if (values == null) {
			values = [];
			valueSet[key] = values;
		}
	});

	// Store the values from each array entry
	let resultsObject = jsonResults["results"];
	let bindings = resultsObject["bindings"];

	let index = 1;
	bindings.forEach((item) => {
		// Store result index
		valueSet["Result"].push(index);
		index++;

		Object.keys(item).forEach((key) => {
			// Store value
			let subitem = item[key];
			valueSet[key].push(subitem["value"]);
		})
	});

	return toTable(valueSet);
}

/*
 Parses the input JSON Array into a HTML table.
*/
function parseJSONArray(jsonResults) {
	let forTable = {};
	forTable["Result"] = [];

	parseResult(jsonResults, forTable);
	return toTable(forTable);
}

/**
*
*/
function parseResult(jsonResult, forTable) {
	if(Array.isArray(jsonResult)) {
		jsonResult.forEach((result) => {
			parseResult(result, forTable);
		});

	} else if(jsonResult["result"]) {
		parseResult(jsonResult["result"], forTable);

	} else if(jsonResult["multiple_results"]) {
		let results = jsonResult["multiple_results"];
		results.forEach((result) => {
			parseResult(results, forTable);
		});

	} else {
		forTable["Result"].push(forTable["Result"].length + 1);

		Object.keys(jsonResult).forEach((key) => {
			if (!forTable[key]) {
				forTable[key] = [];
			}
			forTable[key].push(parseValue(jsonResult[key]));
		})
	}
}

/**
*	Parses a JSON value into a reasonable string for display.
*/
function parseValue(value) {
	if(Object.keys(value).length === 2 && value["value"] && value["unit"]) {

		let actualValues = value["value"];
		let valueString = actualValues;
		if(Array.isArray(actualValues)) {
			valueString = actualValues.join("<br/>");
		}

		return valueString + "<br/>" + "[" + value["unit"] + "]";
	}

	if(Array.isArray(value)) {
		return value.join("<br/>");
	}
	return value;
}

/*
 Converts input dictionary (of column-mahor data) into a HTML table.
*/
function toTable(valueSet) {
	// Build into HTML table
	let html = "<table class=\"chatbot-table\"><tr>";

	// Headers
	Object.keys(valueSet).forEach((header) => {
		html += "<th>" + header + "</th>";
	});
	html += "</tr>";

	// Values
	let rows = valueSet["Result"].length;

	for (var r = 0; r < rows; r++) {
		html += "<tr>";

		for (var key in valueSet) {
			let values = valueSet[key];
			let value = values[r];

			html += "<td>";

			if(isValidURL(String(value))) {

				if(isImageURL(String(value))) {
					// Image with link to source
					html += "<a href=\"" + value + "\">";
					html += "<img width=\"200px\" src=\"" + value + "\"/>";
					html += "</a>";
				} else {
					// Text link
					html += "<a href=\"" + value + "\">" + value + "</a>";
				}
			} else {
				// Try removing spaces from the value, if it then matches
				// an entry in the species list, keep it without the spaces
				let tempValue = String(value).replace(/\s/g, '');

				if(species.includes(tempValue)) {
					html += tempValue;
				} else {
					html += value;
				}
			}
			html += "</td>"
		}
		html += "</tr>";
	}

	html += "</table>";
	return html;
}

/*
 Returns true if the input string is a valid URL.
*/
function isValidURL(possibleURL) {
	if(possibleURL.startsWith("http:") || possibleURL.startsWith("https:")) {
		let url;

		try {
			url = new URL(possibleURL);
			return true;
		} catch (error) {
			return false;
		}
	}

	return false;
}

/*
 Returns true if the input URL links to an image.
*/
function isImageURL(url) {
	if(url.endsWith(".svg")) {
		return true;
	}
	if(url.endsWith(".bmp")) {
		return true;
	}
	if(url.endsWith(".png")) {
		return true;
	}
	if(url.endsWith(".jpg")) {
		return true;
	}
	if(url.endsWith(".jpeg")) {
		return true;
	}
	return false;
}
// to process matrix data returned from the agent extension ...
function process_matrix_data(matrix){
    let elements = [];
    if ("multiple_results" in matrix){
        elements = matrix['multiple_results'];
        prepare_chart_data(elements);
        makeTable(elements);
    }
    else{
        if(typeof matrix['value'] === "string"){
            // single value, table only
            elements = [matrix]
            makeTable(elements)
        }else{
            // serial value, use chart to visualise
            elements = [matrix];
            prepare_chart_data(elements);
            makeTable(elements)
        }
    }
}

function prepare_chart_data(elements){
    // find the value, find the other array
    // y, x
    elements.forEach(function (element){
        let y_data = element['value']
        let y_unit = element['unit']
        let x_data = []
        let x_data_title = ''
        let x_data_unit = ''
        let attribute = element['attribute']
        let fixed_data_title = ''
        let fixed_data = ''
        let fixed_data_unit = ''
        console.log(attribute)
        // find the fixed value and the serial value
        // find the key that is neither value nor attribute
        Object.keys(element).forEach(function (k) {
            if(k!== 'value' && k!== 'attribute'){
                // see whether this is single value or serial
                if(typeof element[k]['value'] === "object"){
                    // this is serial, x axis
                    let x_data_object = element[k]; // containing values and a unit
                    x_data_unit = x_data_object['unit'];
                    x_data = x_data_object['value'];
                    x_data_title = k; // store the title of x axis too
                }else{
                    // this is the fixed value
                    fixed_data_title = k;
                    let fixed_data_object = element[k];
                    fixed_data_unit = fixed_data_object['unit'];
                    fixed_data = fixed_data_object['value'];
                }
            }
        });
        let rows = make_rows(x_data, y_data);
        drawLineChart(rows, attribute, y_unit, x_data_title, x_data_unit, fixed_data_title, fixed_data, fixed_data_unit);
    });
}

function make_rows(x_data, y_data){
    let rows = [];
    for (let i = 0; i < y_data.length; i++) {
        rows.push([parseFloat(x_data[i]), parseFloat(y_data[i])]);
    }
    return rows
}

const hash = function(str, seed = 0) {
    let h1 = 0xdeadbeef ^ seed, h2 = 0x41c6ce57 ^ seed;
    for (let i = 0, ch; i < str.length; i++) {
        ch = str.charCodeAt(i);
        h1 = Math.imul(h1 ^ ch, 2654435761);
        h2 = Math.imul(h2 ^ ch, 1597334677);
    }
    h1 = Math.imul(h1 ^ (h1>>>16), 2246822507) ^ Math.imul(h2 ^ (h2>>>13), 3266489909);
    h2 = Math.imul(h2 ^ (h2>>>16), 2246822507) ^ Math.imul(h1 ^ (h1>>>13), 3266489909);
    return 4294967296 * (2097151 & h2) + (h1>>>0);
};

function drawLineChart(rows, attribute, y_unit, x_data_title, x_data_unit, fixed_data_title, fixed_data, fixed_data_unit) {
      // create a new id for this element
    	let element_id = 'data_chart'

        $('<div>', {
            id: element_id,
            class: 'chart',
        }).appendTo('#chart_div');
		// append the data chart result to chart_div block, which uses the name #data_chart



      $(element_id).ready(function() {

          var data = new google.visualization.DataTable();
          data.addColumn('number', 'X');
          data.addColumn('number', attribute);
          data.addRows(rows);
          var options = {

            title: attribute + ' (' + y_unit + ') at ' + fixed_data_title + ' of ' + fixed_data + ' ' + fixed_data_unit,
            hAxis: {
              title: x_data_title + ' (' + x_data_unit + ')',
            },
            backgroundColor: '#f1f8e9',
            legend: {position: 'none'}
          };
          var chart = new google.visualization.LineChart(document.getElementById(element_id));
          chart.draw(data, options);
      });


    }


function makeTable(matrix_set){

    console.log('Making a table from matrix set')
    // let test_valueSet = [{'x': '1', 'y': '2'},{'x': '1', 'y': '2'},{'x': '1', 'y': '2'},{'x': '1', 'y': '2'}]
    matrix_set.forEach(function (matrix) {
        let x_data = matrix['value'];
        let array = []
        let x_data_list = []
        if (typeof x_data === 'string'){
            x_data_list.push([x_data]);
        }
        else{
            x_data_list = x_data
        }
        x_data_list.forEach(function (x,index){
            let valueSet = {};
            let x_data_title = matrix['attribute'];
            let x_data_unit = matrix['unit'];
            valueSet[x_data_title] = x
            valueSet['unit'] = x_data_unit;

        Object.keys(matrix).forEach(function (k) {
            if(k!== 'value' && k!== 'attribute' && k!== 'unit'){
                let content = '';
                let unit = matrix[k]['unit'];
                let value = matrix[k]['value'];
                if (typeof value === 'string'){
                    content = value + ' ' + unit;
                    valueSet[k] = content
                }else{
                    valueSet[k] = value[index] + ' ' + unit;
                }
            }
        });
        array.push(valueSet);
    });
        processChatbotResults(array);
    });
}

/*
 Convert raw results returned from the back end to JSON Array or JSON Object
*/
function convertToJSONResults(rawResults) {
    	// Parse the results


	if (rawResults === null || rawResults === undefined || rawResults === "" || rawResults === "null") {
		let msg = "<span style=\"color: red; padding-left: 15px;\">The World Avatar failed to provide an answer.</span>";
		let chatbotResults = document.getElementById("chatbot-results");
		if(chatbotResults !== null) chatbotResults.innerHTML = msg;
        return null;
	} else {
		// Get the data into JSON form (if not already);
		let jsonData = null;

		try {
			jsonData = JSON.parse(rawResults);
            console.log('result is parsed into JSON from String', jsonData)
            console.log('Type of the object is ', typeof(jsonData))
		} catch (err1) {
            console.log('err1')

			try {
				jsonData = JSON.parse(JSON.stringify(rawResults));
                return jsonData;

			} catch (err2) {
                console.log('err2')
				jsonData = rawResults;
			}
		}
        return jsonData;

	}
}

