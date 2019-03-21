/**

 * Configurations for the project, used both in app && test
 *  @ root folder for owl files
 *  @ root node file name
 *  @ port
 */


var path = require('path')
var config = {};

config.baseUri = "http://www.theworldavatar.com"

config.crebase = "http://www.theworldavatar.com/damecoolquestion/ontochem"
config.ontokinbase = "http://www.theworldavatar.com/damecoolquestion/ontokin"

//configDevelop();
configDeploy();
config.agentShowcaseNode = path.join(config.root , "kb/subsetWorld.owl");
config.worldNode = path.join(config.root , "kb/TheWorld.owl");
config.ppNode = path.join(config.root , "kb/powerplants/WorldPowerPlants.owl");
config.jurongNode = path.join(config.root ,"kb/sgp/jurongisland/JurongIsland.owl");
config.b3Node = path.join(config.root , "kb/sgp/jurongisland/biodieselplant3/BiodieselPlant3.owl");
config.ontoENNode = path.join(config.root, "kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl");


//config.b2Node = path.join(config.root , "BiodieselPlant2.owl");
config.bmsFolder = path.join(config.root , "BMS");
config.bmsNode = path.join(config.bmsFolder , "CARES_Lab.owl");
config.bmsplotnode = path.join(config.bmsFolder, "BCA_RT_sensor1.owl");
config.semakauNode = path.join(config.root , "kb/sgp/semakauisland/SemakauIsland.owl");
config.landLotNode=path.join(config.root , "kb/sgp/jurongisland/JParkLandLots.owl");
config.ontochemNode= config.crebase + '/query';
config.ontokinNode= config.ontokinbase + '/query';

//TODO: this later should be wrapped in owl file
config.heatWasteScript = path.join(__dirname, "agents/WHR_network_optimization_trim.py")
config.heatWasteNode = path.join(config.root, "wasteheatnetwork.owl")





function configDevelop() {
    config.root = path.join(__dirname ,  "testFiles") ; // own folder for testing
    config.port = 3000;//port for deploy
    config.registerUrl = "http://localhost:2000";
    config.changeUrl = "http://localhost:3000";
    config.ppFolder = path.join(config.root, "powerplants")
    config.jurongNode = path.join(config.root ,"JurongIsland.owl");
    
    //"http://www.thewordavatar.com:82/change";
}

function configDeploy() {
    config.root = path.normalize("C:/TOMCAT/webapps/ROOT");
	//config.root2 = path.normalize("C:/TOMCAT/webapps/ROOT/kb/sgp/semakauisland");
    config.port = 82;//port for deploy
    config.registerUrl = "http://10.25.188.104";
    config.changeUrl = "http://www.theworldavatar.com:82";
    config.ppFolder = path.join(config.root , "kb/powerplants");
	//config.ppFolder = path.normalize("C:/TOMCAT/webapps/ROOT/kb/powerplants");

}













module.exports = config;