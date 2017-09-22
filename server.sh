#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function error_msg {
	echo "Usage: ./server [middleware|cars|flights|hotels] [parameters]"
}

function grant_access {
	if [ $# -eq 1 ]; then
		local policyFile=$DIR/$1/java.policy

		echo "grant codeBase \"file:"$DIR"/"$1"\" {" > $policyFile
		echo "	permission java.security.AllPermission;" >> $policyFile
		echo "};" >> $policyFile
		echo >> $policyFile
	fi
}

function launch {
	java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$DIR/server/$1/ $1.impl.CarManagerImpl 1099
}

if [ $# -gt 0 ]; then
	if [ $1 == "middleware" ]; then
		echo "Launching middleware..."
		cd $DIR/server/middleware/
        grant_access server/middleware/
        java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$DIR/server/middleware/ middleware.impl.MiddlewareImpl ${@:2}
	elif [ $1 == "cars" ]; then
		echo "Launching car manager..."
		cd $DIR/server/cars/
		grant_access server/cars/
		java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$DIR/server/cars/ cars.impl.CarManagerImpl ${@:2}
	elif [ $1 == "flights" ]; then
		echo "Launching flight manager..."
		cd $DIR/server/flights/
        grant_access server/flights/
        java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$DIR/server/flights/ flights.impl.FlightManagerImpl ${@:2}
	elif [ $1 == "hotels" ]; then
		echo "Launching hotel manager..."
		cd $DIR/server/hotels/
        grant_access server/hotels/
        java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$DIR/server/hotels/ hotels.impl.HotelManagerImpl ${@:2}
	else
		error_msg
	fi
else
	error_msg
fi

