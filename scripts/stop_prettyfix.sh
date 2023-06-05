#!/bin/bash

# get standard Aqua environment variables
source ~/.aquarc

# date format
format="+%Y-%m-%d %H:%M:%S"

# if not running, exit (not an error)
app=PRETTYFIX
PID=`pgrep -f -- -D${app}`
if [ -z "${PID}" ]
then
	printf "`date \"${format}\"` [%-30s] WARN: ${app} was not running\n" ${0##*/}
	exit
fi

# kill pids
for pid in ${PID}
do
	printf "`date \"${format}\"` [%-30s] INFO: Humanely killing ${app}, pid ${pid}\n" ${0##*/}
	kill ${pid}

	# wait up to 5 seconds for app to stop
	for ((count=0; count<10; count++))
	do
		sleep 0.5
		if [ ! -d /proc/${pid} ]
		then
			continue 2 # continue in this loop and outer loop
		fi
	done

	# do it the hard way
	printf "`date \"${format}\"` [%-30s] WARN: Brutally killing ${app}, pid ${pid}\n" ${0##*/}
	kill -9 ${pid}
done
