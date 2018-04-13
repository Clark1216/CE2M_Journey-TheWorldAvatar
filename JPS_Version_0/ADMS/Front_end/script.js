/**
 * Created by MASTE on 6/20/2017.
 */




var points = []
var graph = [];         // An array of arrays of layers
var layers = [];        // the array that stores the layers
var markers = [];       // all the contour shapes are visualized on the map in the form of markers on google map
var map ;               // the google map object
var max = 0;
var scaleDiv = 7000;
var zoomlevel = 14;

var num = 7
var frameCounter = 1;
loadFrame(1);           // the index indicate the xth json in the folder



self.setInterval(doLoad,20000);  // set the updating time interval in milliseconds


function doLoad() {

    layers = [];
    points = [];
    Plotly.deleteTraces('graph', 0);  // clear the shape from the last frame

        loadFrame(2);

}


//
function loadFrame (index) {

    let myFirstPromise = new Promise((resolve, reject) => {

	max = 0;
        setTimeout(function(){
            $.getJSON("result.json", function(result){
                // read the json file
                points = result;
                resolve("Success!");// when points is filled, proceed to myFirstPromise.
			
				
				for(var i = 0; i < points.length; i++)
				{
					var line = points[i]
					for(var j = 0; j < line.length; j++)
					{
						var num = line[j];
						if(num > max)
						{
							max = num
						}
					}
				}
				
				
				
				
            });
         }, 0);
    });


    // read the json file first and only execute the following codes after the data is filled and "resolve("Success!")" is executed
    myFirstPromise.then((successMessage) => {


        doPlot();// draw the plot basing on the data stored in points array


            var main_svg = document.getElementsByClassName('main-svg')[0].getElementsByClassName('contourfill');

            // after the graph is generated by Plotly.js library, go through the dom tree and get the contourfill class elements
            // which are childnodes of the 'main-svg' element


            var children = main_svg;
            for(var i = 0; i < main_svg.length; i++)
            {
                var g = main_svg[i];
                var paths = g.getElementsByTagName('path');

                graph = [];

                for(var j = 0; j < paths.length ; j++)
                {
                    var path =  paths[j].getAttribute('d');
                    var color = paths[j].getAttribute('style').split('fill: ')[1].split(';')[0];
                    // get the rgb value from the graph element and assign it to the variable color



                    var svgPath =
                        {
                            path: path,
                            anchor: new google.maps.Point(300,670), // larger y moves up
                            // this is very important, in order to align the svg(contour) with the map
                            // basing on the actual position, a constant offset must be set.
                            fillColor: color,
                            // the fill color of this marker is set to be the rgb color of its parent graph element
                            fillOpacity: 0.2,
                            // set the opacity of the graph (from 0 to 1)
                            strokeWeight: 0.5,
                            draggable: true,
                            //scaledSize: new google.maps.Size(50, 50),
                            scale: 1.5 * Math.pow(2,zoomlevel - 1) / scaleDiv
                            // initial size of the svg shapes for alignment
                            //
                        };

                    graph.push(svgPath);    // push the marker into the graph, a graph array stores all the marker of the same color level
                }

                layers.push(graph);         // push the graph into the layers

            }
                if(index == 1)              // when the index is 1, initiate the map
                {
                 initMap(layers);
                 setMarkers(layers);
                }
                else
                {
                    for(var i = 0; i < markers.length; i++)
                    {
                        markers[i].setOptions({icon:null});// clear all the markers on the map
                    }
                    setMarkers(layers);
                }
                 var graphdiv = document.getElementById('graph');
                 graphdiv.style = 'display:none';
                 var infoLayer = document.getElementsByClassName('infolayer')[0];

                 var scale = document.getElementById('scale');
                 scale.innerHTML = infoLayer.outerHTML;
                 var rects = scale.getElementsByTagName('rect');
                 var texts = scale.getElementsByTagName('text');



                 for(var i = 0; i < rects.length; i++)
                 {
                     var text = texts[i];
                     var rect = rects[i];
                     text.setAttribute('x','5');
                     rect.setAttribute('x','55');

                     console.log('=======',text)
                 }





                 // hide the plotly.js map  use 'display:block' to show the element
        });
}




function doPlot() {

    var data = [ {

        z: points,
        opacity: 0.5 ,

        type: 'contour',                // draw a contour map

        colorbar:{
            title: 'Color Bar Title',
            titleside: 'right',
            titlefont: {
                size: 14,
                family: 'Arial, sans-serif'
            }
        },
        autocontour: false,
        contours: {
            start: 0,
            end: max,
            size: max/10
        }
    }];


    Plotly.plot('graph', data, {
        images: [
            {
                "source": "map of singapore.JPG",
                "xref": "x",
                "yref": "y",
                "x": 3,
                "y": 0,
                "sizex": 380,
                "sizey": 240,
                "sizing": 'stretch',
                "opacity": 1,
                "xanchor": "left",
                "yanchor": "bottom",
                "layer": 'below'
            }
        ]
    }
, {
            paper_bgcolor: 'rgba(0,0,0,0)',
            plot_bgcolor: 'rgba(0,0,0,0)'
        }
    )

}
// the main function for
function setMarkers(layer) {
    var myLatLng = {lat: 1.2668080555555554, lng: 103.67884555555555};


    markers = [];

    for(var i = 0; i < layers.length; i++)
    {
        var graphs = layers[i];
        for(var j = 0; j < graphs.length; j++)
        {
            var someMarker = new google.maps.Marker({
                map: map,
                position: myLatLng,
                icon: graphs[j]
            });
            markers.push(someMarker);
            someMarker.setClickable(false);
        }
    }



    google.maps.event.addListener(map, 'zoom_changed', function() {

        var zoom = map.getZoom();
		
        zoomlevel = zoom;
        for(var i = 0; i < markers.length;i++)
        {
            var marker = markers[i];
            var picon = marker.getIcon();

            var scale = 1.5 * Math.pow(2,zoom - 1) / scaleDiv;
            picon.scale= 1 * scale;
            marker.setOptions({icon:picon});
        }

		
		console.log('changed to', Math.pow(2,zoom - 1) / scaleDiv)
    });

}

function initMap() {

    // the predefined center lat and lng for the map
    var myLatLng = {lat: 1.2668080555555554, lng: 103.67884555555555};


    // initiate the map within the element of id 'map'
    map = new google.maps.Map(document.getElementById('map'), {
        center: myLatLng,
        scrollwheel: true,          // allow user to use scrollwheel to zoom in or zoom out
        zoom: zoomlevel,                   // set the default zoom level to be 15
        panControl: true,
        panControlOptions: {
            position: google.maps.ControlPosition.TOP_RIGHT
        }
    });

    // Create a map object and specify the DOM element for display.
    // Create a marker and set its position.

}