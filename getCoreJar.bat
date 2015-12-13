call gradle -p d:\ray\dev\conradapps\rabbit clean
call gradle -p d:\ray\dev\conradapps\rabbit jar
echo "foo"
cp -v d:/ray/dev/conradapps/rabbit/build/libs/rabbit.jar app/libs/
