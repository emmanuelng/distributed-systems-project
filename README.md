# COMP 512 (Distributed systems) project
**Group 20**, Emmanuel **Ng Cheng Hin** and Joshua **Lau**

## Setup in Eclipse

* Clone the repository
* In Eclipse, go to `File > Import...` and select `General > Project from folder or archive`
* Select the `comp512-project/client/` folder
* Repeat for the `comp512-project/server/` folder

## Available scripts

* `./compile.sh`: compiles all the java files and updates the .jar files
* `./server.sh [middleware|cars|flights|hotels] [parameters]`: launches the servers
* `./client [middleware machine] [port]`: launches a client instance. The port is 1099 by default
