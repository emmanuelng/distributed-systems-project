#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
declare -A APPS

# For each app, define the main class
APPS[client-client]='client.Client'
APPS[client-performance]='client.performance.PerformanceTest'
APPS[server-cars]='cars.impl.CarManagerImpl'
APPS[server-flights]='flights.impl.FlightManagerImpl'
APPS[server-hotels]='hotels.impl.HotelManagerImpl'
APPS[server-customers]='customers.impl.CustomerManagerImpl'
APPS[server-middleware]='middleware.impl.MiddlewareImpl'


if [ $# -gt 1 ]; then
	dirname=$1"-"$2

	if [ ${APPS[$dirname]+1} ]; then
		# Build the app
		$DIR/gradlew $dirname:build
		echo

		# Go to the directory
		cd $DIR/$dirname

		# Generate the policy file
		echo "grant codeBase \"file:"$DIR"/"$dirname"/build/libs/"$dirname"-1.0.jar\" {" > $DIR/$dirname/java.policy
		echo "	permission java.security.AllPermission;" >> $DIR/$dirname/java.policy
		echo "};" >> $DIR/$dirname/java.policy
		echo >> $DIR/$dirname/java.policy

		# Set the classpath
		classpath="$DIR/client-client/build/libs/client-client-1.0.jar"
		classpath=$classpath":$DIR/client-performance/build/libs/client-performance-1.0.jar"
		classpath=$classpath":$DIR/server-common/build/libs/server-common-1.0.0.jar"
		classpath=$classpath":$DIR/server-cars/build/libs/server-cars-1.0.jar"
		classpath=$classpath":$DIR/server-flights/build/libs/server-flights-1.0.jar"
		classpath=$classpath":$DIR/server-hotels/build/libs/server-hotels-1.0.jar"
		classpath=$classpath":$DIR/server-middleware/build/libs/server-middleware-1.0.jar"
		classpath=$classpath":$DIR/server-customers/build/libs/server-customers-1.0.jar"
		export CLASSPATH=$classpath

		# Run the app
		codebase="file:$DIR/$dirname/build/libs/$dirname-1.0.jar"

		if [ $1 == 'server' ]; then
			codebase=$codebase" file:$DIR/server-common/build/libs/server-common-1.0.0.jar"

		fi

		java -Djava.security.policy=$DIR/$dirname/java.policy -Djava.rmi.server.codebase="$codebase" ${APPS[$dirname]} ${@:3}
	else
		echo 'Invalid arguments.'
		echo
	fi
else
	echo 'Usage: ./run.sh client client [args]'
	echo '   or  ./run.sh server [cars|flights|hotels|middleware|customers] [args]'
	echo
fi
