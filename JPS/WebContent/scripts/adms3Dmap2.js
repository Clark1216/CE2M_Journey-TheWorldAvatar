const listGeoJsonAddedToOSMB = []; // edit

const controlButtonsSetter = osmb => {
    var controlButtons = document.querySelectorAll('.control button');
    for (var i = 0, il = controlButtons.length; i < il; i++) {
        controlButtons[i].addEventListener('click', function(e) {
            var button = this;
            var parentClassList = button.parentNode.classList;
            var direction = button.classList.contains('inc') ? 1 : -1;
            var increment;
            var property;

            if (parentClassList.contains('tilt')) {
                property = 'Tilt';
                increment = direction*10;
            }
            if (parentClassList.contains('rotation')) {
                property = 'Rotation';
                increment = direction*10;
            }
            if (parentClassList.contains('zoom')) {
                property = 'Zoom';
                increment = direction*1;
            }
            if (property) {
                osmb['set'+ property](osmb['get'+ property]()+increment);
            }
        });
    }
};


const initadms3dmap  = (list, range, osmb, cityiri) => {
	
	console.log('list:', list);
	
	for(obj of listGeoJsonAddedToOSMB) {
		obj.destroy();
	}
	
	
    const parsedLowLeft = [range['xmin'],range['ymin']]
    const parsedTopRight = [range['xmax'],range['ymax']]
    

    const position = {};
    // if(location === "The Hague"){
    //     position.latitude = 52.07690;
    //     position.longitude = 4.29089;
    // } else if (location === "Berlin"){
    // 	position.latitude = 52.51461;
    // 	position.longitude = 13.23966;
    // }

    position.latitude = (range['ymin'] + range['ymax']) / 2 
    position.longitude = (range['xmin'] + range['xmax']) / 2

    osmb.setPosition(position);
    osmb.setZoom(15.7);
	osmb.setTilt(45.0);


    $.getJSON('/JPS/ADMSPowerPlantGetter',
    	{
    		'location':cityiri
    	},
    	data => {
    		const geojson = data;
    		try {
    			// console.log(JSON.stringify(geojson, null, 4));
    			listGeoJsonAddedToOSMB.push(osmb.addGeoJSON(geojson)); // edit
    		} catch (err) {
    			console.log(err.name);
    		}
    	});

    // --- Rendering 3D building models --- //

    console.log('making request to adms helper')
    let iri = "http://dbpedia.org/resource/The_Hague"
    	if(cityiri === 'Berlin'){
    		iri = "http://dbpedia.org/resource/Berlin"
    	}
    	else
    	{
    		iri = "http://dbpedia.org/resource/The_Hague"
    	}
    $.getJSON('/JPS/ADMSHelper',
        {
            listOfIRIs: JSON.stringify(list),
            'cityiri': iri
        },
        function(data) {
        	console.log('data:',data)
            var geojson = data;
            var arrayLength = geojson.length;
            console.log('--------------- data returned from helper -----------------')
            console.log(data);
            console.log('-----------------------------------------------------------')
            
            for (var i = 0; i < arrayLength; i++) {

                try {
                	listGeoJsonAddedToOSMB.push(osmb.addGeoJSON(geojson[i])); // edit
//                    console.log(JSON.stringify(geojson[i], null, 4));
                }
                catch(err) {
                    console.log(err.name)
                }
            }
        });
    
//    $.getJSON('/JPS/buildings/simpleshape', 
//    	{
//    		cityiri,
//    		buildingiris: JSON.stringify(list)
//    	},
//    	buildingData => {
//    		console.log(buildingData);
//    	})
    
    // --- Rendering 3D layer --- //
    let optionWrapperNode = document.getElementById("optionwrapper");
    while(optionWrapperNode.firstChild) {
    	optionWrapperNode.removeChild(optionWrapperNode.firstChild);
    }
    makeRadios('optionwrapper', POL_LIST, 'Select a pollutant:');
    
    var geojson = {
    		type: 'FeatureCollection',
            features: [{
                type: 'Feature',
                properties: {
                    //color: '#ff0000',
                    //roofColor: '#cc0000',
                    height: 0,
                    minHeight: 0
                },
                geometry: {
                    type: 'Polygon',
                    coordinates: [//TODO:　LINK THIS TO USER INPUT
                        [
                            [parsedLowLeft[0],parsedTopRight[1]],
                            [parsedTopRight[0],parsedTopRight[1]],
                            [parsedTopRight[0],parsedLowLeft[1]],
                            [parsedLowLeft[0],parsedLowLeft[1]],
                            [parsedLowLeft[0],parsedTopRight[1]]
                        ]
                    ]
                }
            }]
    };
    
    getContourMaps('/JPS/ADMSOutputAll').then(dataurls => {
    	
    	var idxSrc = 0, idxH = 0, preObj;
    	$(".radiogroup").change(function(){
    		var radioValue = $("input[name='radio']:checked").val();
    		idxSrc = POL_LIST.indexOf(radioValue);
    		console.log('src change to ' + idxSrc)
    		if(preObj) preObj.destroy();
    		preObj =  osmb.addGeoJSON(geojson,{ elevation: HEIGHT_INTERVAL * (idxH), hasTexture:dataurls[idxH*POL_NUM +idxSrc]});
    		listGeoJsonAddedToOSMB.push(preObj); // edit
        });
        
    	//  var dataurls = data.dataurls, heights =data.heights
        preObj = osmb.addGeoJSON(geojson,{ elevation: 0, hasTexture:dataurls[0]});
        listGeoJsonAddedToOSMB.push(preObj); // edit
        
        let sliderWrapperNode = document.getElementById("sliderwrapper");
        while(sliderWrapperNode.firstChild) {
        	sliderWrapperNode.removeChild(sliderWrapperNode.firstChild);
        }
        //init at zero position
        makeSlider('sliderwrapper', HEIGHT_NUM, function (event, ui) {
        	if(preObj) preObj.destroy();
            idxH = ui.value
            $( "#height-show" ).val(idxH*10 );

            console.log('sliderto ' + idxH)
            preObj =  osmb.addGeoJSON(geojson,{ elevation: HEIGHT_INTERVAL * (idxH), hasTexture:dataurls[idxH*POL_NUM +idxSrc]});
            listGeoJsonAddedToOSMB.push(preObj);
        })
    }, err => {console.log(err)})
    //***************************************************************************


    // osmb.highlight('w420847275','#00ff00');
    //
    // osmb.on('pointermove', function(e) {
    //     osmb.getTarget(e.detail.x, e.detail.y, function(id) {
    //         if (id) {
    //             document.body.style.cursor = 'pointer';
    //             osmb.highlight(id, '#ff0000');
    //             //osmb.highlight('w420847275','#00ff00');
    //
    //         } else {
    //             document.body.style.cursor = 'default';
    //             osmb.highlight(null);
    //             osmb.highlight('w420847275','#00ff00');
    //         }
    //     });
    // });  
};


