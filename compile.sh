#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Compile the network classes
echo "Compiling the network classes..."
{
	cd $DIR/network/
	javac $DIR/network/network/common/*.java
	javac $DIR/network/network/client/*.java
	javac $DIR/network/network/server/*.java
	jar cvf Network.jar -C network/
} #&> /dev/null

# Compile the common classes
echo "Compiling the common server classes..."
{
	cd $DIR/server/common/
	javac ./*/*/*.java
} #&> /dev/null

# Compile the car manager
echo "Compiling the car manager..."
{
	cd $DIR/server/cars/
	export CLASSPATH=$DIR/server/cars/:$DIR/server/common/
	export CLASSPATH=$CLASSPATH:$DIR/network/Network.jar
	javac $DIR/server/cars/cars/CarManager.java
	javac $DIR/server/cars/cars/impl/*.java
	jar cvf CarManagerInterface.jar cars/*.class
	mv CarManagerInterface.jar $DIR/server/middleware/
} #&> /dev/null

# Compile the hotel manager
echo "Compiling the hotel manager..."
{
	cd $DIR/server/hotels/
	export CLASSPATH=$DIR/server/hotels/:$DIR/server/common/
	export CLASSPATH=$CLASSPATH:$DIR/network/Network.jar
	javac $DIR/server/hotels/hotels/HotelManager.java
	javac $DIR/server/hotels/hotels/impl/*.java
	jar cvf HotelManagerInterface.jar hotels/*.class
	mv HotelManagerInterface.jar $DIR/server/middleware/
} #&> /dev/null

# Compile the flight manager
echo "Compiling the flight manager..."
{
	cd $DIR/server/flights/
	export CLASSPATH=$DIR/server/flights/:$DIR/server/common/
	export CLASSPATH=$CLASSPATH:$DIR/network/Network.jar
	javac $DIR/server/flights/flights/FlightManager.java
	javac $DIR/server/flights/flights/impl/*.java
	jar cvf FlightManagerInterface.jar flights/*.class
	mv FlightManagerInterface.jar $DIR/server/middleware/
} #&> /dev/null

# Compile the customer manager
echo "Compiling the customer manager..."
{
	cd $DIR/server/customers/
	export CLASSPATH=$DIR/server/customers/:$DIR/server/common/
	export CLASSPATH=$CLASSPATH:$DIR/network/Network.jar
	javac $DIR/server/customers/customers/CustomerManager.java
	javac $DIR/server/customers/customers/impl/*.java
	# The customers are currently handled in the middleware server.
	# Uncomment the two following lines if the customers are handled in a separate server.
	# jar cvf CustomerManagerInterface.jar customers/*.class
	# mv CustomerManagerInterface.jar $DIR/server/middleware/
} #&> /dev/null

# Compile the middleware
echo "Compiling the middleware..."
{
	cd $DIR/server/middleware/
	export CLASSPATH=$CLASSPATH:$DIR/server/middleware/
	export CLASSPATH=$CLASSPATH:$DIR/server/common/
	export CLASSPATH=$CLASSPATH:$DIR/network/Network.jar
	export CLASSPATH=$CLASSPATH:$DIR/server/customers
	export CLASSPATH=$CLASSPATH:$DIR/server/middleware/CarManagerInterface.jar
	export CLASSPATH=$CLASSPATH:$DIR/server/middleware/FlightManagerInterface.jar
	export CLASSPATH=$CLASSPATH:$DIR/server/middleware/HotelManagerInterface.jar
	javac $DIR/server/middleware/middleware/Middleware.java
	javac $DIR/server/middleware/middleware/impl/*.java
	jar cvf MiddlewareInterface.jar middleware/*.class
	mv MiddlewareInterface.jar $DIR/client/
} #&> /dev/null

# Compile the client
echo "Compiling the client..."
{
	export CLASSPATH=$DIR/client/MiddlewareInterface.jar
	javac $DIR/client/src/client/*.java
} #&> /dev/null

echo "Done!"

