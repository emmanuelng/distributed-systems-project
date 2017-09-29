#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


function grant_access {
	if [ $# -eq 1 ]; then
		local policyFile=$DIR/$1/java.policy

		echo "grant codeBase \"file:"$DIR"/"$1"\" {" > $policyFile
		echo "	permission java.security.AllPermission;" >> $policyFile
		echo "};" >> $policyFile
		echo >> $policyFile
	fi
}

export CLASSPATH=$CLASSPATH:$DIR/client/
export CLASSPATH=$CLASSPATH:$DIR/client/MiddlewareInterface.jar
export CLASSPATH=$CLASSPATH:$DIR/client/Network.jar

if [ $# -eq 2 ]; then
	cd $DIR/client/src/
	grant_access client/
	java -Djava.security.policy=java.policy client.Client $1 $2
else
	echo "Usage: ./client.sh [servermachine] [port]"

fi

