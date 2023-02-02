/**
*	This JS file handles functionality for the Power System visualisation.
 *	Authors: Michael Hillman (mdhillman@cmclinnovations.com), Wanni Xie (wx243@cam.ac.uk)
 *	Last Update Date: 25 August 2021
*/


// Setup initial state of side panel components
function resetSidePanel() {
	document.getElementById('chartContainer').style.display = "none";
	document.getElementById('metadataContainer').style.display = "none";
	document.getElementById('tableContainer').style.display = "none";
	//document.getElementById('legendContainer').style.display = "none";
	document.getElementById('dateContainer').style.display = "none";

	var titleHTML = `
		<h2>UK Power System</h2>
	`;
	setSidePanelTitle(titleHTML);

	var textHTML = `
		<p>The map to the left shows a sample of the power system data within the UK Digital Twin.</p>
		<p>Each node on the map represents a generator with the size of the node corresponding to the capacity of the generator, and the color 
		corresponding to the value of the estimated emissions intensity, or SDG indicator (depending on layer selection).</p>
	`;
	setSidePanelText(textHTML);
}

	/**
	 * Fired when a plant is selected.
	 * 
	 * @param name - plant name
	 * @param fuel - fuel
	 * @param capacity - capacity
	 * @param indicator - SDG indicator
	 * @param location - coordinates
	 */
	function selectPlant(name, fuel, capacity, indicator, location) {
		if (name == null) {
			// Do nothing
			return;
		}

		// Set title to offtake name
		setSidePanelTitle(`
		<h2>` + name + `</h2>
	`);

		// Pretty-print location
		var prettyLocation = "lat: " + roundN(location[1], 5) + ", long: " + roundN(location[0], 5);
		prettyLocation = "<a href='javascript:void(0)' onclick='panToLast()'>" + prettyLocation + "</a>"

		// Show meta data
		var metaHTML = `
		<table width="100%">
			<tr>
				<td width="25%">Fuel:</td>
				<td width="75%" style="text-align: right;">` + fuel + `</td>
			</tr>
			<tr>
				<td width="25%">Location:</td>
				<td width="75%" style="text-align: right;">` + prettyLocation + `</td>
			</tr>
			<tr>
				<td width="35%">Capacity:</td>
				<td width="65%" style="text-align: right;">` + capacity + ` MW</td>
			</tr>
	`;

		// Only add if present
		if (indicator != null) {
			metaHTML += `
			<tr>
				<td width="40%">Indicator 9.4.1:</td>
				<td width="60%" style="text-align: right;">` + indicator + ` kg/£</td>
			</tr>
		`;
		}

		metaHTML += "</table>";
		setSidePanelMeta(metaHTML);

		// Update text container 
		setSidePanelText(``);
}

/**
	 * Fired when a area or a regional boundaries is selected.
	 * 
	 * @param Location - place name
	 * @param Area_LACode - fuel
	 * @param TotalELecConsumption - capacity
	 * @param DomesticConsumption - SDG indicator
	 * @param Industrial_and_Commercial - coordinates
	 */
function selectArea(Location, Area_LACode, TotalELecConsumption, DomesticConsumption, Industrial_and_Commercial) {
	if (Location == null) {
		// Do nothing
		return;
	}

	// Set title to offtake name
	setSidePanelTitle(`
		<h2>` + Location + `</h2>
	`);

	// Show meta data
	var metaHTML = `
		<table width="100%">
			<tr>
				<td width="75%">LA Code:</td>
				<td width="25%" style="text-align: right;">` + Area_LACode + `</td>
			</tr>
			<tr>
				<td width="75%">Total Electricity Consumption:</td>
				<td width="15%" style="text-align: right;">` + TotalELecConsumption + ` GWh</td>
			</tr>
			<tr>
				<td width="75%">Domestic Consumption:</td>
				<td width="25%" style="text-align: right;">` + DomesticConsumption + ` GWh</td>
			</tr>
			<tr>
				<td width="65%">Industrial and Commercial Consumption:</td>
				<td width="35%" style="text-align: right;">` + Industrial_and_Commercial + ` GWh</td>
			</tr>
	`;

	metaHTML += "</table>";
	setSidePanelMeta(metaHTML);

	// Update text container 
	setSidePanelText(``);
}

/**
	 * Fired when a bus is clicked
	 */
function selectBus(Bus_num, Bus_type, para_Gs, para_Bs, para_area, para_basekV, para_zone, para_Vmax, para_Vmin) {
	if (Bus_num == null) {
		// Do nothing
		return;
	}

	// Set title to offtake name
	setSidePanelTitle(`
			<h2> Bus ` + Bus_num + `</h2>
		`);

	// Show meta data
	var metaHTML = `
			<table width="100%">
				<tr>
					<td width="75%">Bus type:</td>
					<td width="25%" style="text-align: right;">` + Bus_type + `</td>
				</tr>
				<tr>
					<td width="75%">Gs (shunt conductance):</td>
					<td width="15%" style="text-align: right;">` + para_Gs + ` MW</td>
				</tr>
				<tr>
					<td width="75%">Bs (shunt susceptance):</td>
					<td width="25%" style="text-align: right;">` + para_Bs + ` MVAr</td>
				</tr>
				<tr>
					<td width="75%">Bus Area (area number):</td>
					<td width="25%" style="text-align: right;">` + para_area + ` </td>
				</tr>
				<tr>
					<td width="75%">Base kV (base voltage):</td>
					<td width="25%" style="text-align: right;">` + para_basekV + ` kV</td>
				</tr>
				<tr>
					<td width="75%">Zone (loss zone):</td>
					<td width="25%" style="text-align: right;">` + para_zone + ` kV</td>
				</tr>
				<tr>
					<td width="75%">Vmax (maximum voltage magnitude):</td>
					<td width="25%" style="text-align: right;">` + para_Vmax + ` p.u.</td>
				</tr>
				<tr>
					<td width="75%">Vmin (minimum voltage magnitude):</td>
					<td width="25%" style="text-align: right;">` + para_Vmin + ` p.u.</td>
				</tr>
		`;

	metaHTML += "</table>";
	setSidePanelMeta(metaHTML);

	// Update text container 
	setSidePanelText(``);
}

/**
	 * Fired when a branch is clicked
	 */
function selectBranch(Name, From_Bus, To_Bus, para_R, para_X, para_B, para_RateA, para_RateB, para_RateC, para_RatioCoefficient, para_Angle, para_Status, para_AngleMax, para_AngleMin) {
	if (Name == null) {
		// Do nothing
		return;
	}

	// Set title to offtake name
	setSidePanelTitle(`
			<h2> ` + Name + `</h2>
		`);

	// Show meta data
	var metaHTML = `
			<table width="100%">
				<tr>
					<td width="75%">From Bus:</td>
					<td width="25%" style="text-align: right;"> Bus ` + From_Bus + `</td>
				</tr>
				<tr>
					<td width="75%">To Bus:</td>
					<td width="15%" style="text-align: right;"> Bus ` + To_Bus + `</td>
				</tr>
				<tr>
					<td width="55%">R (resistance):</td>
					<td width="45%" style="text-align: right;">` + para_R + ` p.u.</td>
				</tr>
				<tr>
					<td width="55%">X (reactance):</td>
					<td width="45%" style="text-align: right;">` + para_X + ` p.u.</td>
				</tr>
				<tr>
					<td width="70%">B (total line charging susceptance):</td>
					<td width="30%" style="text-align: right;">` + para_B + ` p.u.</td>
				</tr>
				<tr>
					<td width="55%">Rate A (long term rating):</td>
					<td width="45%" style="text-align: right;">` + para_RateA + ` MVA</td>
				</tr>
				<tr>
					<td width="75%">Rate B (short term rating):</td>
					<td width="25%" style="text-align: right;">` + para_RateB + ` MVA</td>
				</tr>
				<tr>
					<td width="75%">Rate C (emergency rating):</td>
					<td width="25%" style="text-align: right;">` + para_RateC + ` MVA</td>
				</tr>
				<tr>
					<td width="80%">Ratio Coefficient (transformer off nominal turns ratio):</td>
					<td width="20%" style="text-align: right;">` + para_RatioCoefficient + `</td>
				</tr>
				<tr>
					<td width="80%">Angle (transformer phase shift angle):</td>
					<td width="20%" style="text-align: right;">` + para_Angle + ` degrees</td>
				</tr>
				<tr>
					<td width="75%">Status (initial branch status):</td>
					<td width="25%" style="text-align: right;">` + para_Status + `</td>
				</tr>
				<tr>
					<td width="80%">Angle Min (minimum angle difference):</td>
					<td width="20%" style="text-align: right;">` + para_AngleMin + ` degrees</td>
				</tr>
				<tr>
					<td width="80%">Angle Max (maximum angle difference):</td>
					<td width="20%" style="text-align: right;">` + para_AngleMax + ` degrees</td>
				</tr>
		`;

	metaHTML += "</table>";
	setSidePanelMeta(metaHTML);

	// Update text container 
	setSidePanelText(``);
}


	/**
	 * Update the legend depending on the current layer.
	 * 
	 * @param type 
	 */
	function updateLegend(type) {
		var html = `<div id="legend">`;

		if (type.includes("power_")) {
			html += `
				<b>Legend:</b><br>
				<div id="padding" style="height: 6px;"></div>
				<img width="18px" src="legend-coal.svg"/>Coal<br>
				<img width="18px" src="legend-coalbiomass.svg"/>Biomass<br>
				<img width="18px" src="legend-hydro.svg"/>Hydro<br>
				<img width="18px" src="legend-naturalgas.svg"/>Natural Gas<br>
				<img width="18px" src="legend-nuclear.svg"/>Nuclear<br>
				<img width="18px" src="legend-oil.svg"/>Oil<br>
				<img width="18px" src="legend-pumphydro.svg"/>Pump Hydro<br>
				<img width="18px" src="legend-solar.svg"/>Solar<br>
				<img width="18px" src="legend-sourgas.svg"/>Sour Gas<br>
				<img width="18px" src="legend-waste.svg"/>Waste<br>
				<img width="18px" src="legend-wastead.svg"/>Waste (Anaerobic Digestion)<br>
				<img width="18px" src="legend-wastemsw.svg"/>Waste (Municipal Solid Waste)<br>
				<img width="18px" src="legend-wind.svg"/>Wind<br>
			`;
		} else if (type.includes("sdg_")) {
			html += `
				<img src="legend-sdg.png" class="legend-sdg" width="275px"/>
			`;
		} else if(type.includes("Electricity_")) {
			html += `
			<img src="legend-electricity_consumption.svg" class="legend-electricity_consumption" width="120" height="533"/>
		`;
		}
		html += `</div>`;

		setSidePanelLegend(html);
	}


	// Pretty print date
	function prettyPrintDate(date) {
		var day = "" + date.getDate();
		var month = months[date.getMonth()];

		var hour = "" + date.getHours();
		var minute = "" + date.getMinutes();

		if (day.length < 2) day = "0" + day;
		if (month.length < 2) month = "0" + month;
		if (hour.length < 2) hour = "0" + hour;
		if (minute.length < 2) minute = "0" + minute;

		return addOrd(day) + " " + month + ", " + hour + ":" + minute;
	}


	// Get number with ordinal
	function addOrd(n) {
		var ords = [, 'st', 'nd', 'rd'];
		var ord, m = n % 100;
		return n + ((m > 10 && m < 14) ? 'th' : ords[m % 10] || 'th');
	}


	// Round digit to N decimal places
	function roundN(value, digits) {
		var tenToN = 10 ** digits;
		return (Math.round(value * tenToN)) / tenToN;
	}

	// select the retrofit result_exanct
	function siteRankingResult(fuel, capacity, obj1, obj2, weighter, coordinates, rank) {
		// Set title to offtake name
		setSidePanelTitle(`
			<h2> ` + fuel + `</h2>
		`);

		// Pretty-print location
		var prettyLocation = "lat: " + roundN(coordinates[1], 5) + ", long: " + roundN(coordinates[0], 5);
		prettyLocation = "<a href='javascript:void(0)' onclick='panToLast()'>" + prettyLocation + "</a>"

		// Show meta data
		var metaHTML = `
		<table width="100%">
			<tr>
				<td width="25%">Fuel:</td>
				<td width="75%" style="text-align: right;">` + fuel + `</td>
			</tr>
			<tr>
				<td width="25%">Location:</td>
				<td width="75%" style="text-align: right;">` + prettyLocation + `</td>
			</tr>
			<tr>
				<td width="35%">Capacity:</td>
				<td width="65%" style="text-align: right;">` + capacity + ` MW</td>
			</tr>
			<tr>
				<td width="35%">SMR cost and risk cost:</td>
				<td width="65%" style="text-align: right;">` + obj1 + ` 10E7 (£)</td>
			</tr>
			<tr>
				<td width="35%">Weighed demanding distance:</td>
				<td width="65%" style="text-align: right;">` + obj2 + ` 10E7 (km)</td>
			</tr>

			<tr>
				<td width="35%">Rank:</td>
				<td width="65%" style="text-align: right;">` + rank + ` </td>
			</tr>
			<tr>
				<td width="35%">Weighter:</td>
				<td width="65%" style="text-align: right;">` + weighter + ` </td>
			</tr>
		`;


		metaHTML += "</table>";
		setSidePanelMeta(metaHTML);

		// Update text container 
		setSidePanelText(``);
}


	// select the retrofit result_exanct
	function selectRetrofit_extant(fuel, capacity, output, coordinates, status) {
		// Set title to offtake name
		setSidePanelTitle(`
			<h2> ` + fuel + `</h2>
		`);

		// Pretty-print location
		var prettyLocation = "lat: " + roundN(coordinates[1], 5) + ", long: " + roundN(coordinates[0], 5);
		prettyLocation = "<a href='javascript:void(0)' onclick='panToLast()'>" + prettyLocation + "</a>"

		// Show meta data
		var metaHTML = `
		<table width="100%">
			<tr>
				<td width="25%">Fuel:</td>
				<td width="75%" style="text-align: right;">` + fuel + `</td>
			</tr>
			<tr>
				<td width="25%">Location:</td>
				<td width="75%" style="text-align: right;">` + prettyLocation + `</td>
			</tr>
			<tr>
				<td width="35%">Capacity:</td>
				<td width="65%" style="text-align: right;">` + capacity + ` MW</td>
			</tr>
			<tr>
				<td width="35%">Output:</td>
				<td width="65%" style="text-align: right;">` + output + ` MW</td>
			</tr>
			<tr>
				<td width="35%">Status:</td>
				<td width="65%" style="text-align: right;">` + status + ` </td>
			</tr>
		`;


		metaHTML += "</table>";
		setSidePanelMeta(metaHTML);

		// Update text container 
		setSidePanelText(``);
}

	// select the retrofit result_exanct
	function selectRetrofit_smr(fuel, capacity, output, numberOfUnit, coordinates, status) {
		// Set title to offtake name
		setSidePanelTitle(`
			<h2> ` + fuel + `</h2>
		`);

		// Pretty-print location
		var prettyLocation = "lat: " + roundN(coordinates[1], 5) + ", long: " + roundN(coordinates[0], 5);
		prettyLocation = "<a href='javascript:void(0)' onclick='panToLast()'>" + prettyLocation + "</a>"

		// Show meta data
		var metaHTML = `
		<table width="100%">
			<tr>
				<td width="25%">Fuel:</td>
				<td width="75%" style="text-align: right;">` + fuel + `</td>
			</tr>
			<tr>
				<td width="25%">Location:</td>
				<td width="75%" style="text-align: right;">` + prettyLocation + `</td>
			</tr>
			<tr>
				<td width="35%">Capacity:</td>
				<td width="65%" style="text-align: right;">` + capacity + ` MW</td>
			</tr>
			<tr>
				<td width="35%">Number of SMR Units:</td>
				<td width="65%" style="text-align: right;">` + numberOfUnit + ` </td>
			</tr>
			<tr>
				<td width="35%">Output:</td>
				<td width="65%" style="text-align: right;">` + output + ` MW</td>
			</tr>
			<tr>
				<td width="35%">Status:</td>
				<td width="65%" style="text-align: right;">` + status + ` </td>
			</tr>
		`;


		metaHTML += "</table>";
		setSidePanelMeta(metaHTML);

		// Update text container 
		setSidePanelText(``);
}

	// select the retrofit result_exanct
	function majorEnergySource(smallArea_LACode, majorOutput, majorEnergySource) {
	// Set title to offtake name
	setSidePanelTitle(`
		<h2> ` + fuel + `</h2>
	`);

	// Show meta data
	var metaHTML = `
	<table width="100%">
		<tr>
			<td width="25%">Small Area LA Code:</td>
			<td width="75%" style="text-align: right;">` + smallArea_LACode + `</td>
		</tr>
		<tr>
			<td width="25%">Major Output:</td>
			<td width="75%" style="text-align: right;">` + majorOutput + ` MW</td>
		</tr>
		<tr>
			<td width="35%">Major Energy Source:</td>
			<td width="65%" style="text-align: right;">` + majorEnergySource + ` MW</td>
		</tr>
	`;


	metaHTML += "</table>";
	setSidePanelMeta(metaHTML);

	// Update text container 
	setSidePanelText(``);
}