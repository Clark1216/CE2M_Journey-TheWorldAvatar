# SensorLoggerMobileAppAgent
## Description
The SensorLoggerMobileAppAgent is intended to receive HTTP POST requests containing JSON payload from the [SensorLogger mobile app](https://github.com/tszheichoi/awesome-sensor-logger). This app can be downloaded from [GooglePlay](https://play.google.com/store/apps/details?id=com.kelvin.sensorapp&hl=en&gl=US) or [IOS](https://apps.apple.com/us/app/sensor-logger/id1531582925). SensorLoggerMobileAppAgent retrieves JSON payload from the SensorLogger mobile app, parse the JSON Array and instantiate the data onto the knowledge graph using the timeseries client. The static relations are instantiated using the object graph mapper library.

## To deploy this agent with the stack locally
1) Prepare the stack-manager
The agent has been implemented to work with stack, which requires the stack to be [set up](https://github.com/cambridge-cares/TheWorldAvatar/tree/main/Deploy/stacks/dynamic/stack-manager).

2) Input the necessary credentials in the folders

You'll need to provide  your credentials in single-word text files located like this:
#### Under the main folder
```
./SensorLoggerMobileAppAgent/
    credentials/
        repo_username.txt
        repo_password.txt
```

#### Under the docker folder
```
./docker/
    credentials/
        repo_username.txt
        repo_password.txt
```

3) Set up [JPS_Access Agent](https://github.com/cambridge-cares/TheWorldAvatar/tree/main/Agents/AccessAgent) on the stack. 
   1) [Replace the placeholder for the stack name](https://github.com/cambridge-cares/TheWorldAvatar/tree/main/Agents/AccessAgent#spinning-up-the-access-agent-as-part-of-a-stack)  in the access-agent.json file within the access-agent-dev-stack folder.
   2) [Modify `routing.json`](https://github.com/cambridge-cares/TheWorldAvatar/tree/main/Agents/AccessAgent#Uploading-routing-information). Edit `"label":examplestack`, `<STACK NAME>` and `examplestack` with relevant namespace. 
   3) Change the port number of the access agent URL in uploadRouting.sh to `3838`.
   4) Change the endpoint URL in Instantiation Client under ModelContext.

4) Spin up stack manager
   1) Use the `bash ./stack.sh start <STACK-NAME> ` in the stack-manager directory. 
   2) Add in `storerouter` namespace in blazegraph. This is so that AccessAgent can reach the relevant endpoint URL. 
   3) Run `bash ./uploadRouting.sh` in the same directory of uploadRouting.sh. 
   
5) Configure downsampling frequency

6) Spin up the SensorLoggerMobileAppAgent
   1) `bash ./stack.sh build` to create a fresh image 
   2) Run `bash ./stack.sh start <STACK-NAME>`
   3) SensorMobileAppAgent is deployed in port `10102`

7) [Configure endpoints](https://github.com/tszheichoi/awesome-sensor-logger#Live-Data-Streaming) in SensorLogger mobile app. 
   1) Enable HTTP PUSH under settings 
   2) Specify URL, replace <LOCAL-URL> with the same network connected from both your local environment and your phone
      - `http://<LOCAL-URL>:10102/SensorLoggerMobileAppAgent/update`
      - `<LOCAL-URL>` can be obtained under IPV4 when you run `ipconfig` under cmd.
8) Start recording.


## Debugging the agent
#### Building the docker image 
On the Debug side panel of VSCode, run the `Build and debug` configuration. (This will fail but will produce the docker image).

#### Debugging
1) Insert breakpoints within the code.
2) On the Debug side panel of VSCode, run the `Debug` configuration.
3) It will prompt you to input the <STACK-NAME> (This will fail but will spin up the agent within the stack - `Failed to attach to remote debuggee VM. Reason: com.sun.jdi.connect.spi.ClosedConnectionException`)
4) Run `Reattach and debug` to enter the debug mode. 

#### Testing the agent
1) Send the POST Request of `SamplePOST_for_Stack` in `/SensorLoggerMobileAppAgent/sensorloggermobileapp_agent/src/main/resources`. You will receive 200 status code as response.
2) Or, you may use SensorLoggerMobileApp to send payload to debug after configuring the right endpoint. 

## Notes
### Common bugs
1) If Stack's Blazegraph requires password
   - Run `bash ./stack.sh rm all` to remove the stack and respin the stack 