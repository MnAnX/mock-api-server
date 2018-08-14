#!/bin/bash
nohup bin/mock-api-service &
echo $! > pid