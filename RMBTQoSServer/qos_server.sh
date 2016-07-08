#! /bin/sh
#

### BEGIN INIT INFO
# Provides:          qos_server
# Required-Start:    $local_fs $remote_fs $syslog $time
# Required-Stop:     $local_fs $remote_fs $syslog
# Should-Start:
# Should-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Start/Stop the qos_server
### END INIT INFO


PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
DIR=/home/netztest/qos_server
JAR=$DIR/RMBTQoSTesterver.jar
EXEC=/usr/bin/java
NAME=qos_server
DESC=qos_server
PIDFILE=/var/run/netztest/qos_server.pid
USER=netztest

if ! [ -x "/lib/lsb/init-functions" ]; then
        . /lib/lsb/init-functions
else
        echo "E: /lib/lsb/init-functions not found, lsb-base (>= 3.0-6) needed"
        exit 1
fi

set -e

case "$1" in
  start)
        log_daemon_msg "Starting $DESC" "$NAME"
        start-stop-daemon --start --quiet --chuid $USER --user $USER --pidfile $PIDFILE -m -b -d $DIR --oknodo --exec $EXEC -- -jar $JAR
        log_end_msg $?
        ;;
  stop)
        log_daemon_msg "Stopping $DESC" "$NAME"
        start-stop-daemon --stop --quiet --chuid $USER --user $USER --pidfile $PIDFILE --oknodo --exec $EXEC
        log_end_msg $?
        ;;
  restart)
        $0 stop
        sleep 1
        $0 start
        ;;
  *)
        log_failure_msg "Usage: $N {start|stop|restart}" 
        exit 1
        ;;
esac

exit 0

