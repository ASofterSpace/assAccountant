#!/bin/bash

if [[ ! -d ../Toolbox-Java ]]; then
	echo "It looks like you did not yet get the Toolbox-Java project - please do so (and put it as a folder next to this folder.)"
	exit 1
fi

cd src/com/asofterspace

rm -rf toolbox

mkdir toolbox
cd toolbox

mkdir accounting
mkdir calendar
mkdir coders
mkdir configuration
mkdir gui
mkdir guiImages
mkdir images
mkdir io
mkdir pdf
mkdir projects
mkdir utils
mkdir virtualEmployees
mkdir web

cd ../../../..

cp ../Toolbox-Java/src/com/asofterspace/toolbox/*.java src/com/asofterspace/toolbox
cp ../Toolbox-Java/src/com/asofterspace/toolbox/accounting/*.* src/com/asofterspace/toolbox/accounting
cp ../Toolbox-Java/src/com/asofterspace/toolbox/calendar/*.* src/com/asofterspace/toolbox/calendar
cp ../Toolbox-Java/src/com/asofterspace/toolbox/coders/*.* src/com/asofterspace/toolbox/coders
cp ../Toolbox-Java/src/com/asofterspace/toolbox/configuration/*.* src/com/asofterspace/toolbox/configuration
cp ../Toolbox-Java/src/com/asofterspace/toolbox/gui/*.* src/com/asofterspace/toolbox/gui
cp ../Toolbox-Java/src/com/asofterspace/toolbox/guiImages/*.* src/com/asofterspace/toolbox/guiImages
cp ../Toolbox-Java/src/com/asofterspace/toolbox/images/*.* src/com/asofterspace/toolbox/images
cp ../Toolbox-Java/src/com/asofterspace/toolbox/io/*.* src/com/asofterspace/toolbox/io
cp ../Toolbox-Java/src/com/asofterspace/toolbox/pdf/*.* src/com/asofterspace/toolbox/pdf
cp ../Toolbox-Java/src/com/asofterspace/toolbox/projects/*.* src/com/asofterspace/toolbox/projects
cp ../Toolbox-Java/src/com/asofterspace/toolbox/utils/*.* src/com/asofterspace/toolbox/utils
cp ../Toolbox-Java/src/com/asofterspace/toolbox/virtualEmployees/*.* src/com/asofterspace/toolbox/virtualEmployees
cp ../Toolbox-Java/src/com/asofterspace/toolbox/web/*.* src/com/asofterspace/toolbox/web


cd server

rm -rf toolbox

mkdir toolbox
cd toolbox

mkdir utils

cd ../..

cp ../Toolbox-JavaScript/toolbox/*.js server/toolbox
cp ../Toolbox-JavaScript/toolbox/utils/*.* server/toolbox/utils


rm -rf bin

mkdir bin

cd src

find . -name "*.java" > sourcefiles.list

javac -deprecation -Xlint:all -encoding utf8 -d ../bin @sourcefiles.list
