#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
VALID=('client-client' 'server-cars' 'server-flights' 'server-hotels' 'server-middleware' 'server-customers')

if [ $# -gt 1 ]; then
	dirname=$1"-"$2

	if echo ${VALID[@]} | grep -q -w "$dirname"; then
		# Generate the policy file
		echo "grant codeBase \"file:"$DIR"/"$dirname"\" {" > $DIR/$dirname/java.policy
		echo "	permission java.security.AllPermission;" >> $DIR/$dirname/java.policy
		echo "};" >> $DIR/$dirname/java.policy
		echo >> $DIR/$dirname/java.policy

		# Launch the program
		cd $dirname
		../gradlew run ${@:3}
	else
		echo 'Invalid arguments.'
		echo
	fi
else
	echo 'Usage: ./run.sh client client [args]'
	echo '   or  ./run.sh server [cars|flights|hotels|middleware|customers] [args]'
	echo
fi
