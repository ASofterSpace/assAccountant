IF NOT EXIST ..\Toolbox-Java\ (
	echo "It looks like you did not yet get the Toolbox-Java project - please do so (and put it as a folder next to this folder.)"
	EXIT 1
)

cd src\com\asofterspace

rd /s /q toolbox

md toolbox
cd toolbox

md accounting
md calendar
md coders
md configuration
md gui
md guiImages
md images
md io
md pdf
md projects
md utils
md virtualEmployees
md web

cd ..\..\..\..

copy "..\Toolbox-Java\src\com\asofterspace\toolbox\*.java" "src\com\asofterspace\toolbox"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\accounting\*.*" "src\com\asofterspace\toolbox\accounting"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\calendar\*.*" "src\com\asofterspace\toolbox\calendar"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\coders\*.*" "src\com\asofterspace\toolbox\coders"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\configuration\*.*" "src\com\asofterspace\toolbox\configuration"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\gui\*.*" "src\com\asofterspace\toolbox\gui"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\guiImages\*.*" "src\com\asofterspace\toolbox\guiImages"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\images\*.*" "src\com\asofterspace\toolbox\images"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\io\*.*" "src\com\asofterspace\toolbox\io"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\pdf\*.*" "src\com\asofterspace\toolbox\pdf"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\projects\*.*" "src\com\asofterspace\toolbox\projects"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\utils\*.*" "src\com\asofterspace\toolbox\utils"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\virtualEmployees\*.*" "src\com\asofterspace\toolbox\virtualEmployees"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\web\*.*" "src\com\asofterspace\toolbox\web"


cd server

rd /s /q toolbox

md toolbox
cd toolbox

md utils

cd ..\..

copy "..\Toolbox-JavaScript\toolbox\*.js" "server\toolbox"
copy "..\Toolbox-JavaScript\toolbox\utils\*.*" "server\toolbox\utils"


rd /s /q bin

md bin

cd src

dir /s /B *.java > sourcefiles.list

javac -deprecation -Xlint:all -encoding utf8 -d ../bin @sourcefiles.list

pause
