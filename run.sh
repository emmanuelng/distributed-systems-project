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
		echo "grant codeBase \"file:$DIR/$dirname/build/libs/$dirname-1.0.jar\" {" > $DIR/$dirname/java.policy
		echo "	permission java.security.AllPermission;" >> $DIR/$dirname/java.policy
		echo "};" >> $DIR/$dirname/java.policy
		echo >> $DIR/$dirname/java.policy

		# Get the parameters
		policy="$DIR/$dirname/java.policy"
		classpath="$DIR/$dirname/build/libs/$dirname-1.0.jar"
		codebase="file:$DIR/$dirname/build/libs/$dirname-1.0.jar"

		if [ $1 == 'server' ]; then
			classpath=$classpath":$DIR/server-common/build/libs/server-common-1.0.jar"
			codebase=$codebase" file:$DIR/server-common/build/libs/server-common-1.0.jar"

			# Add the RM APIs for the middleware
			if [ $2 == 'middleware' ]; then
				classpath=$classpath":$DIR/server-cars/build/libs/server-cars-1.0.jar"
				classpath=$classpath":$DIR/server-flights/build/libs/server-flights-1.0.jar"
				classpath=$classpath":$DIR/server-hotels/build/libs/server-hotels-1.0.jar"
				classpath=$classpath":$DIR/server-customers/build/libs/server-customers-1.0.jar"
			fi

			# Add permissions for the common codebase
			echo "grant codeBase \"file:$DIR/server-common/build/libs/server-common-1.0.jar\" {" >> $DIR/$dirname/java.policy
			echo "	permission java.security.AllPermission;" >> $DIR/$dirname/java.policy
			echo "};" >> $DIR/$dirname/java.policy
			echo >> $DIR/$dirname/java.policy
		else
			# Add the middleware API to classpath
			classpath=$classpath":$DIR/server-middleware/build/libs/server-middleware-1.0.jar"
		fi

		# Run the app
		cd $DIR
		export CLASSPATH=$classpath
		java -cp $classpath -Djava.security.policy=$policy -Djava.rmi.server.codebase="$codebase" ${APPS[$dirname]} ${@:3}
	else
		echo 'Invalid arguments.'
	fi
else
	echo 'Usage: ./run.sh client client [args]'
	echo '   or  ./run.sh server [cars|flights|hotels|middleware|customers] [args]'
fi

echo
