/**
 * Created by Shaocong on 4/6/2017.
 */
//import
var path = require('path');
var libxmljs = require("libxmljs");
var async = require('async');
var readdirp = require('readdirp');
var fs = require('fs');
var util = require('util');
var folderLocation ="C:/TOMCAT/webapps/ROOT" ;
var domain = "http://www.theworldavatar.com/";

var es = require('event-stream');
/**out a asyn function, provide data :
 [
 {source: , target:  }
 ]


 ****/

//var connections = [];

function readConnections(options, callback) {

    let depth = options.depth || 0;
    let showImport = options.showImport || false;
    var connections = [];
   // if (connections && connections.length > 0) {
  //      callback(null, connections);
   // } else {

        walkThroughFolder2GetConns(function (err) {

            if (err) {
                callback(err);
                return;
            }
            callback(null, connections);

        });


  //  }

    /***
     * Walk through folder to get all connections
     *  * @param depth  : depth of reading into sub folders

     * @param callback
     */
    function walkThroughFolder2GetConns( callback) {

        //asyncly
//read dir of each file
//then xml extract the header, store this
        var fileList = [];
        readdirp({root: folderLocation, fileFilter: ['*.owl'], depth : depth})//only read one level, might be different for future with nested folder
            .on('data', function (entry) {
                console.log(entry.path);

                fileList.push(entry.path);

            })
            .on('end', function () {

                var fullPathList = [];

                for(var repath of fileList){
                    fullPathList.push(path.join(folderLocation, repath));
                }


                async.map(fullPathList, readAsync, function (err, results) {
                    // results is now an array of stats for each file
                    if (err) {
                        callback(err);
                    }
                    for (let i = 0; i < results.length; i++) {
                        readConnectionsFromOwl(results[i], fileList[i]);
                    }

                    callback();
                });
            });

        function readAsync(file, callback) {
            fs.readFile(file, 'utf8', callback);

        }



    }


    /***
     * Extract all connections in {source : ,target:} from a single owl file
     * @param file   : filebody
     * @param filepath
     */
    function readConnectionsFromOwl(file, filepath) {
        var xmlDoc = libxmljs.parseXml(file);
        var root = xmlDoc.root();
        //my uri???
        var myUri;
        var targets = [];
        myUri = (root.attrs()&&root.attrs().length > 0)?root.attrs()[0].value() : null;
        console.log("base: " + myUri);

        //loop through namespaces defined in root

        if(showImport) {
            for (var ns of root.namespaces()) {
                //console.log(ns.prefix()+ ":"+ns.href());
                //get base from xml
                if (ns.prefix() !== null) {//if prefix not null [ self namespace is defined with null prefix]
                    console.log("prefix:" + ns.prefix());

                    var href = processHref(ns.href()); // text process href to extract #
                    targets.push(href.trim());// add to target list
                }
            }
        }
        //loop through each node, note down value of tap : hasURI

        var namespaceOb = {};//construct namespaceOb for find in root with nested namespace
        namespaceOb['owl'] = "http://www.w3.org/2002/07/owl#";
        namespaceOb['Eco-industrialPark'] = "http://www.theworldavatar.com/OntoEIP/Eco-industrialPark.owl#";
		namespaceOb['system'] = "http://www.theworldavatar.com/OntoCAPE/OntoCAPE/upper_level/system.owl#";
        //console.log(util.inspect(namespaceOb));

        //find all node with hasIRI property
        let uris = root.find("//owl:NamedIndividual/Eco-industrialPark:hasIRI", namespaceOb);
        console.log("found node :"+uris.length);
        for(let curi of uris){
        //    console.log(curi.name());
            targets.push(curi.text().trim());//push to targets list
        }
		       //find all node with hasIRI property
        let urisS = root.find("//owl:NamedIndividual/system:hasIRI", namespaceOb);
        console.log("found node :"+urisS.length);
        for(let curi of urisS){
        //    console.log(curi.name());
            targets.push(curi.text().trim());//push to targets list
        }
		//find all node with system:hasIRI property

		
        //determine source url
        if (!myUri) {//base not defined in owl, then deduce from file path
            myUri = path.join(domain, filepath);
        }

        //pack into json
        for (let target of targets) {
            connections.push({source: myUri, target: target});
        }
    }
}




/***
 * Utility function: Process href to be actual file uri
 * If href contains mark char:#, delete it
 * @param href, href string to be processed
 * @returns processed href string
 */
function processHref(href) {

    if (href.endsWith("#")) {//TODO: other situations , like end with /?(also need actual prefix used to form a complete url)
        href = href.slice(0, -1);
    }
    return href;
}


module.exports = readConnections;
