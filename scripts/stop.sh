#!/bin/bash 

PID=$(pgrep -f ERM)
echo "Killing PID:" $PID "With Alias ERM"
kill $PID
