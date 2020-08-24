#!/bin/bash

prefix='package.'

cplog() {
        if [ -f "nohup.out" ]
        then
                cp -f nohup.out nohup.out.bak
        fi
}

start() {
	echo `pwd`" starting..."
	if [ ! -f "run.pid" ]
        then
                touch run.pid
        fi
	pid=`cat run.pid`
	if [ -z "$pid" ]
	then
		cplog
		export LD_LIBRARY_PATH=`pwd`"/lib":$LD_LIBRARY_PATH
		# export JAVA_HOME="/usr/local/java/jdk1.8.0_131"
		export CLASSPATH=".:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar"
		nohup ./bin/PackageServer > nohup.out 2>&1 &
		echo $! > run.pid
	fi
}

stop() {
	echo `pwd`" stopping..."
	pid=`cat run.pid`
	if [ -n "$pid" ]
	then
		echo 'stop '$pid'...'
		kill $pid
		while [ 1 ]
		do
			check_pid=`ps -ef | grep $pid | grep -v grep | wc -l`
			if [ $check_pid == 0 ]
			then
				break
			fi
			sleep 2
		done
		echo '' > run.pid
	fi
}

case "$2" in
	start)
		cd $prefix$1
		$2
		exit 0
		;;
	stop)
		cd $prefix$1
		$2
		exit 0
		;;
	*)
		echo 'Usage: cmd name start|stop'
		echo 'service.sh dev start'
		exit 0
		;;
esac
