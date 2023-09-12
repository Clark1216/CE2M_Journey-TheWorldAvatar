PanelHandler.prototype.prepareMetaContainers = function(e, t) {
        var a = document.getElementById("metaTabs"),
            a = (null == a && this.appendContent("<div id='buttonTabs'></div> <br><div id='metaTabs'></div>"), document.getElementById("metaContainer"));
        null == a && this.appendContent("<div id='metaContainer'></div>");
        let n = document.getElementById("treeButton"),
            i = document.getElementById("timeButton");
        e && (null === n && (document.getElementById("metaTabs").innerHTML += `
                    <button id="treeButton" class="tablinks" onclick="manager.openMetaTab(this.id, 'metaTreeContainer')">Metadata</button>
                `, n = document.getElementById("treeButton")), null === document.getElementById("metaTreeContainer")) && (document.getElementById("metaContainer").innerHTML += "<div id='metaTreeContainer' class='tabcontent'></div>"), t && (null === i && (document.getElementById("metaTabs").innerHTML += `
                    <button id="timeButton" class="tablinks" onclick="manager.openMetaTab(this.id, 'metaTimeContainer')">Time Series</button>
                `, i = document.getElementById("timeButton")), null === document.getElementById("metaTimeContainer")) && (document.getElementById("metaContainer").innerHTML += "<div id='metaTimeContainer' style='display: none;' class='tabcontent'></div>"), null != n && (n.style.display = e ? "block" : "none"), null != i && (i.style.display = t ? "block" : "none"), e && !t ? (n.style.width = "100%", n.style.borderRadius = "10px", this.manager.openMetaTab("treeButton", "metaTreeContainer")) : !e && t ? (i.style.width = "100%", i.style.borderRadius = "10px", this.manager.openMetaTab("timeButton", "metaTimeContainer")) : e && t && (n.style.width = "50%", n.style.borderRadius = "10px 0 0 10px", this.manager.openMetaTab("treeButton", "metaTreeContainer"));
        document.getElementById("buttonTabs").innerHTML = "<button id='getClassScheduleButton'  class='custom-button2''>Class Schedule</button>";
        document.getElementById("buttonTabs").innerHTML += "<button id='getWeatherDataButton'  class='custom-button2''>Weather Data</button>";
        document.getElementById("buttonTabs").innerHTML += "<button id='getShowHeatmapButton'  class='custom-button2''>Power Heatmap</button>";
        a = document.getElementById("footerContainer");
        null !== a && (a.style.display = "none")


        document.getElementById('getClassScheduleButton').addEventListener('click', function () {
            alert('Getting class schedule data...');
            var iframe = document.createElement("iframe");
            // Set attributes for the iframe
            iframe.src = "http://localhost:3838/analytics/d-solo/a3e4e240-87cc-455e-888a-fd3af4e9e64d/ntu-dashboard?orgId=1&var-schedule=All&from=1672731037713&to=1672787129841&theme=light&panelId=1";
            iframe.width = "450";
            iframe.height = "800";
            iframe.frameBorder = "0"; // Note: It's frameBorder, not frameborder

            // Get the container element
            var container = document.getElementById("metaTabs");
            // Append the iframe to the container
            container.replaceWith(iframe);
            container = document.getElementById("metaContainer");
            container.replaceWith(" ");
        });


        // Function for Building Usage Button
        const acedemicBuildings = [441, 55, 434, 37, 438, 46, 232, 306, 443, 424, 415, 21, 329, 303, 302, 409, 218, 404, 51, 349, 314, 142, 166, 432, 429, 444, 156, 305, 430, 420, 411, 427, 437, 428, 52, 402, 101, 436, 318, 450, 179, 127, 83, 448, 304, 311, 414, 400, 97, 38, 483, 375, 439, 114, 435, 412, 343, 363, 384, 103, 239, 248, 370, 245, 237, 254, 246, 364, 247, 244, 243, 203, 235, 61, 120, 40, 47, 488, 59, 449, 1, 50, 382, 50, 41];
        const undergradHalls = [17, 401, 286, 58, 291, 292, 294, 95, 281, 296, 276, 406, 372, 272, 270, 342, 90, 265, 392, 152, 75, 362, 5, 3, 317, 295, 397, 178, 283, 187, 11, 126, 7, 128, 319, 53, 6, 242, 290, 22, 313, 13, 322, 301, 337, 345, 190, 15, 323, 325, 110, 354, 484, 282, 387, 327, 88, 186, 396, 212, 44, 348, 45, 347, 162, 330, 271, 280, 386, 299, 268, 30, 27, 379, 183, 352, 274, 398, 293, 355, 278, 309, 312, 324, 350, 445, 425, 332, 300, 344,334, 367, 316, 39, 481, 57, 395, 234, 431, 423, 338, 480, 479, 328, 335, 321, 91, 339, 393, 341, 189, 145, 394, 320, 48, 16, 14, 9, 8, 310, 28, 389, 10, 78, 23, 24, 26, 12, 29, 20, 308, 297, 356, 298, 369];
        const staffHalls = [273, 275, 336, 285, 289, 79, 315, 25, 326, 2, 353, 221, 351, 188, 287,284, 86, 288, 391, 346, 191, 368, 385, 18]
        const graduateHalls = [69, 397, 295, 317, 3, 5]
        const admin = [429, 314, 142, 166, 432, 178, 398, 352, 183, 454]
        const multipurpose = [399, 442, 333, 277, 360]

        /*
        document.getElementById('getBuildingUsageButton').addEventListener('click', function () {
            alert('Showing Building Usage...');
            let primitives = MapHandler.MAP.scene.primitives;

            // Loop through each primitive
            for (let i = 0; i < primitives.length; i++) {
                let primitive = primitives.get(i);

                // Check if the primitive is a 3D tileset
                if (primitive instanceof Cesium.Cesium3DTileset) {
                    primitive.tileVisible.addEventListener(function (tile) {
                        let content = tile.content;
                        let featuresLength = content.featuresLength;
                        for (let j = 0; j < featuresLength; j++) {
                            let feature = content.getFeature(j);
                            let featureId = feature.getProperty('id'); // Replace 'id' with the correct property name for the ID
                            if (acedemicBuildings.includes(featureId)) {
                                feature.color = new Cesium.Color(76/255, 175/255, 80/255, 1.0);
                            }
                            if (undergradHalls.includes(featureId)) {
                                feature.color = new Cesium.Color(255/255, 183/255, 77/255, 1.0);
                            }
                            if (staffHalls.includes(featureId)) {
                                feature.color = new Cesium.Color(121/255, 85/255, 72/255, 1.0);
                            }
                            if (graduateHalls.includes(featureId)) {
                                feature.color = new Cesium.Color(240/255, 98/255, 146/255, 1.0);
                            }
                            if (admin.includes(featureId)) {
                                feature.color = new Cesium.Color(158/255, 158/255, 158/255, 1.0);
                            }
                            if (multipurpose.includes(featureId)) {
                                feature.color = new Cesium.Color(103/255, 58/255, 183/255, 1.0);
                            }
                        }
                    });
                }
            }
        });
        */

        document.getElementById('getWeatherDataButton').addEventListener('click', function () {
            alert('Getting weather data...');

            var iframe = document.createElement("iframe");
            var header = document.createElement("b");  // Using h2 as an example, you can adjust the heading level as needed
            header.textContent = "Weather dashboard";
            // Set attributes for the iframe
            iframe.src = "http://localhost:3838/analytics/d-solo/f9f0c568-ebe8-4971-95c0-b4d1729692a3/new-dashboard-2?orgId=1&from=1672531200000&to=1672588799000&theme=light&panelId=1";
            //iframe.src = "http://localhost:3838/analytics/d-solo/f73dc197-c9c5-4dab-848d-e3c1f9140874/new-dashboard3?orgId=1&from=1672531200000&to=1673046000000&theme=light&panelId=1";
            iframe.width = "450";
            iframe.height = "270";
            iframe.frameBorder = "0"; // Note: It's frameBorder, not frameborder
            // Append the iframe to the container
            //container.appendChild(header);
            var container = document.getElementById("metaTabs");
            container.replaceWith(iframe);

            var iframe = document.createElement("iframe");
            var header = document.createElement("b");  // Using h2 as an example, you can adjust the heading level as needed
            header.textContent = "Weather forecast";
            // Set attributes for the iframe
            iframe.src = "http://localhost:3838/analytics/d-solo/c7376c69-1c16-4d12-89f6-450707cd8897/new-dashboard?orgId=1&from=1672531200000&to=1672588799000&theme=light&panelId=1";
            iframe.width = "450";
            iframe.height = "280";
            iframe.frameBorder = "0"; // Note: It's frameBorder, not frameborder

            // Get the container element

            // Append the iframe to the container
            var container = document.getElementById("contentContainer");
            container.appendChild(iframe);
            container = document.getElementById("metaContainer");
            container.replaceWith(" ");

        });


        // for Heatmap button
        document.getElementById('getShowHeatmapButton').addEventListener('click', function () {

            console.log("viewer? ", MapHandler.MAP instanceof Cesium.Viewer);
            let bounds = {
            west: 103.6687938762,
            east: 103.6963979333,
            south: 1.3372667367,
            north: 1.3605829566,
            };
            let heatMap = CesiumHeatmap.create(
            MapHandler.MAP, // your cesium viewer
            bounds, // bounds for heatmap layer
            {
              // heatmap.js options go here
              //minOpacity: 0.2,
              maxOpacity: 0.4,
              //useEntitiesIfAvailable: false,
              radius: 200,
              blur: 0.9,
              gradient: {  // the gradient used if not given in the heatmap options object
            '.3': '#d9e7fc',
            '.65': '#2a7aed',
            '.8': '#fbd801',
            '.95': '#c91212'
            },
            },
            );
            let data = [
            { x: 103.6818572460, y: 1.3419334155, value: 100 },   // School of Physical and Mathematical Sciences
            { x: 103.6800064641, y: 1.3442181323, value: 100 },  // Nanyang Auditorium
            { x: 103.6791746564, y: 1.3450496383, value: 100 },   // School of Biological Sciences
            { x: 103.6800881613, y: 1.3454565079, value: 100 },     // Block N1.3
            { x: 103.6796595995, y: 1.3467408413, value: 100 },     // Block N2
            { x: 103.6800130775, y: 1.3473157834, value: 41 },     // Block N2.1
            { x: 103.6757490809, y: 1.3445919035, value: 75 },     // Experimental Medicine Building
            { x: 103.6854021333, y: 1.3442537009, value: 76 },     // Hall of Residence 4
            { x: 103.6883786891, y: 1.3458209704, value: 100 },    // Pioneer Hall
            { x: 103.6853405644, y: 1.3483400219, value: 80 },     // Canteen 2
            { x: 103.6877703643, y: 1.3494060864, value: 40 },      // The Wave
            { x: 103.6881162008, y: 1.3514382803, value: 21 },     // Nanyang Executive Centre
            { x: 103.6859452065, y: 1.3539692593, value: 30 },      // Hall 10
            { x: 103.6811869703, y: 1.3508321630, value: 30 }      // Hall 16
            ];
            let valueMin = 0;
            let valueMax = 50;
            heatMap.setWGS84Data(valueMin, valueMax, data);
            });
}


Manager.prototype.showFeature = function(e, t) {
        if (null == t && null != e.properties) {
            t = e.properties;
        } else if (null === t) {
            return void console.warn("Selected feature has no properties, cannot show any side panel content!");
        }
        let a = getName(t);
        if (null == a) {
            a = e.hasOwnProperty("id") && "object" != typeof e.id ? "Feature " + e.id : "NTU Digital Twin Visualisation";
        }

        this.panelHandler.setTitle("<img src='twa-logo-with-title-blue-256.png'><br><h3>" + a + "</h3>");
        document.getElementById("titleContainer").classList.add("clickable");

        let n = t.description;
        if (null === n && t.desc) {
            n = t.desc;
        }

        if (null !== n && void 0 !== n) {
            this.panelHandler.setContent("<div class='description'>" + n + "</div>");
        } else {
            this.panelHandler.setContent("");
        }

        this.panelHandler.addSupportingData(e, t);

        let tButton = document.getElementById("treeButton");
        let iButton = document.getElementById("timeButton");

        if (void 0 === n && null === tButton && null === iButton) {
            this.panelHandler.setContent("<div class='description'>No data is available for this location.</div>");
        }

        $("#sidePanelInner").tabs("option", "active", 0);
        document.getElementById("returnContainer").style.display = "table";

        window.currentFeature = e;
}


// Overwrite the addSupportingData function
PanelHandler.prototype.addSupportingData = function(e, a) {
    // Your new implementation here
        a = filterNulls(a);
        let t = a.endpoint;
        let n = a.iri;

        this.prepareMetaContainers(true, true);
        document.getElementById("metaTreeContainer").innerHTML = "<i>Retrieving data...</i>";

        const keys = Object.keys(a);
        keys.forEach(key => {
            console.log(`Property: ${key}, Value: ${a[key]}`);
        });

        console.warn("a: ", a);
        console.warn("filtered a: ", filterNulls(a));
        console.warn(Object.keys(a)[0]);
        console.warn(a[Object.keys(a)[0]]);

        let i = this;
        let iriValue;

        if(Object.keys(a)[0] === "id"){
            console.warn("a.id: ", a.id);
            // for branch ids
            if (a.id === "12"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_0";
            }
            else if (a.id === "23"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_1";
            }
            else if (a.id === "34"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_2";
            }
            else if (a.id === "45"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_3";
            }
            else if (a.id === "29"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_4";
            }
            else if (a.id === "910"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_5";
            }
            else if (a.id === "26"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_6";
            }
            else if (a.id === "67"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_7";
            }
            else if (a.id === "68"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_8";
            }
            else if (a.id === "311"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_9";
            }
            else if (a.id === "1112"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_10";
            }
            else if (a.id === "1213"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_11";
            }
            else if (a.id === "414"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_12";
            }
            else if (a.id === "415"){
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Branch_13";
            }

            // for bus ids
            else if (a.id === 120) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_SBS";
            } else if (a.id === 40) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_N1_3";
            } else if (a.id === 442) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_NYA";
            } else if (a.id === 47) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_N_2";
            } else if (a.id === 454) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_N_2_1";
            } else if (a.id === 488) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_SPMS";
            } else if (a.id === 17) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_HALL_4";
            } else if (a.id === 387) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_PIONEER_HALL";
            } else if (a.id === 277) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_THE_WAVE";
            } else if (a.id === 190) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_CANTEEN_2";
            } else if (a.id === 1) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_EMB";
            } else if (a.id === 92) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_RTP";
            } else if (a.id === 399) {
                iriValue = "https://www.theworldavatar.com/kg/ontopowsys/NTU_Building_NEC";
            }
            else {
                iriValue = a.id;
            }
        }else{
            iriValue = a.iri;
            //For showing the class schedule chart
            console.log("a.iri: ", a.iri);
            if(a.iri.includes("https://www.theworldavatar.com/kg/ontopowsys/NTU_Venue")){
                console.log("This is a venue");
                // Get the container element
                var container = document.getElementById("metaTabs");

                var iframe = document.createElement("iframe");
                var header = document.createElement("b");  // Using h2 as an example, you can adjust the heading level as needed
                header.textContent = "Class Schedule";
                // Set attributes for the iframe
                iframe.src = "http://localhost:3838/analytics/d-solo/a3e4e240-87cc-455e-888a-fd3af4e9e64d/ntu-dashboard?orgId=1&var-schedule=All&from=1672531200000&to=1672617599000&var-classroom=LT"+ a[Object.keys(a)[0]] +"&theme=light&panelId=2";
                iframe.width = "450";
                iframe.height = "100";
                iframe.frameBorder = "0"; // Note: It's frameBorder, not frameborder
                // Append the iframe to the container
                //container.appendChild(header);
                container.appendChild(iframe);

                var iframe = document.createElement("iframe");
                var header = document.createElement("b");  // Using h2 as an example, you can adjust the heading level as needed
                header.textContent = "Aircon planning";
                // Set attributes for the iframe
                iframe.src = "http://localhost:3838/analytics/d-solo/a3e4e240-87cc-455e-888a-fd3af4e9e64d/ntu-dashboard?orgId=1&from=1672531200000&to=1672617599000&var-classroom=LT"+ a[Object.keys(a)[0]] +"&theme=light&var-schedule=All&panelId=5";
                iframe.width = "450";
                iframe.height = "200";
                iframe.frameBorder = "0"; // Note: It's frameBorder, not frameborder
                // Append the iframe to the container
                //container.appendChild(header);
                container.appendChild(iframe);

                var iframe = document.createElement("iframe");
                var header = document.createElement("b");  // Using h2 as an example, you can adjust the heading level as needed
                header.textContent = "HVAC setting suggestions";
                // Set attributes for the iframe
                iframe.src = "http://localhost:3838/analytics/d-solo/a3e4e240-87cc-455e-888a-fd3af4e9e64d/ntu-dashboard?orgId=1&from=1672531200000&to=1672617599000&var-classroom=LT"+ a[Object.keys(a)[0]] +"&var-schedule=All&theme=light&panelId=4";
                iframe.width = "450";
                iframe.height = "200";
                iframe.frameBorder = "0"; // Note: It's frameBorder, not frameborder
                // Append the iframe to the container
                //container.appendChild(header);
                container.appendChild(iframe);

            }
        }

        console.warn("sent request: http://localhost:3838/feature-info-agent/get", iriValue);

        return $.getJSON("http://localhost:3838/feature-info-agent/get", {
            iri: iriValue
        }, function(e) {
            let t;
            if (e === null || Array.isArray(e) && e.length === 0 || Object.keys(e).length === 0) {
                i.showBuiltInData(a);
            } else {
                t = e.meta;
                e = e.time;
                if (t !== null) {
                    console.log("Got a meta object!");
                }
                if (e !== null) {
                    console.log("Got a time object!");
                }

                document.getElementById("metaTreeContainer").innerHTML = "";
                if (t !== null) {
                    t = JSONFormatter.formatJSON(t);
                    if (document.getElementById("metaTreeContainer") === null) {
                        console.log("TREE CONTAINER IS NULL, WHAT?!");
                    }

                    if (Array.isArray(t) && t.length === 0 || typeof t === 'string' && t === '') {
                        this.showBuiltInData(a);
                    } else {
                        t = JsonView.renderJSON(t, document.getElementById("metaTreeContainer"));
                        JsonView.expandChildren(t);
                        JsonView.selectiveCollapse(t);
                    }
                } else {
                    i.showBuiltInData(a);
                }

                document.getElementById("metaTimeContainer").innerHTML = "";
                if (e !== null) {
                    i.timeseriesHandler.parseData(e);
                    i.timeseriesHandler.showData("metaTimeContainer");
                    document.getElementById("time-series-select").onchange(null);
                } else {
                    console.warn("No 'time' node found, skipping timeseries visualisation.");
                }
                i.prepareMetaContainers(true, e !== null);


            }
        }).fail(function() {
            console.warn("Could not get valid response from the agent, will show any in-model content instead...");
            i.showBuiltInData(a);
        });
};


/* Add to existing functions
MapHandler_Cesium.prototype.initialiseMap = (function(_super) {
    return function(mapOptions) {
        // Call the original initialiseMap method
        _super.call(this, mapOptions);

        // Define the addOPFButton function
        this.addOPFButton = function() {
            console.log("OPF Button added!");
        };

        // Call the newly defined addOPFButton function
        this.addOPFButton();

        // Similarly, you can define and call other button-adding functions here
        // this.addPVButton();
        // this.addBuildingUsageButton();
        // this.addShowHeatmapButton();
        // this.addClassScheduleButton();
    };
})(MapHandler_Cesium.prototype.initialiseMap);
*/