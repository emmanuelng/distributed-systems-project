#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Compile the car manager
echo "Compiling the car manager..."
cd $DIR/server/cars/
javac cars/CarManager.java
javac cars/impl/*.java
(jar cvf CarManagerInterface.jar cars/*.class)>/dev/null
mv CarManagerInterface.jar ../middleware
cd $DIR

# Compile the hotel manager
echo "Compiling the hotel manager..."
cd $DIR/server/hotels/
javac hotels/HotelManager.java
javac hotels/impl/*.java
(jar cvf HotelManagerInterface.jar hotels/*.class)>/dev/null
mv HotelManagerInterface.jar ../middleware
cd $DIR

# Compile the flight manager
echo "Compiling the flight manager..."
cd $DIR/server/flights/
javac flights/FlightManager.java
javac flights/impl/*.java
(jar cvf FlightManagerInterface.jar flights/*.class)>/dev/null
mv FlightManagerInterface.jar ../middleware
cd $DIR

# Compile the middleware
echo "Compiling the middleware..."
cd $DIR/server/middleware/
export CLASSPATH=$DIR/server/middleware/CarManager.jar
export CLASSPATH=$DIR/server/middleware/HotelManager.jar
export CLASSPATH=$DIR/server/middleware/FlightManager.jar
export CLASSPATH=$DIR/server/middleware/
javac middleware/Middleware.java
javac middleware/impl/*.java
(jar cvf MiddlewareInterface.jar middleware/*.class)>/dev/null
mv MiddlewareInterface.jar ../../client
set CLASSPATH=
cd $DIR

# Compile the client
echo "Compiling the client..."
cd $DIR/client/
export CLASSPATH=$DIR/client/MiddlewareInterface.jar
javac src/client/*.java
set CLASSPATH=

echo "Done!"

