#!/bin/bash

java -classpath "`dirname "$0"`/bin" -Xms16m -Xmx1024m com.asofterspace.accountant.Main drop_bank_statements import_bank_statements import
