#!/bin/bash

java -classpath "`dirname "$0"`/bin" -Xms16m -Xmx1024m -XX:+UseStringDeduplication com.asofterspace.accountant.AssAccountant "$@"
