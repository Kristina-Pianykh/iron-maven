#!/usr/bin/env bash

# Kill all flink processes
ps aux | grep flink | grep -v grep | awk '{print $2}' | xargs kill
