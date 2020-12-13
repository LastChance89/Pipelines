#!/bin/bash 

RUNNING="$(netstat -tulpn | grep 8080)"

while [ ` expr length "$RUNNING" ` = 0 ]
do 
	echo "server not deployed, waiting 30 seconds...."
	sleep 30 
	RUNNING="$(netstat -tulpn | grep 8080)"

done
