#/bin/bash
#
# please configure the nic (network card) to bind to (external ip connecting to different machine)
#
export NIC_ADDR=localhost
#
# please configure the remote ip to read from / write to (can be localhost or some IP)
#
export REMOTE_HOST=localhost
#
# Optional arguments can be used to use unicast 
# OPTS="-Dcom.gs.multicast.enabled=false -Dcom.gs.transport_protocol.lrmi.bind-port=4996 -Dcom.gs.jini_lus.locators=10.0.0.110"
#
# configure the location where you store your gigaspaces license
# (trial license or open source can usually be found in the gigaspaces application folder)
# 
LICENSE="../src/test/resources/gslicense.xml"

#==========================================================================#

# enter target dir
pushd ./target

# copy license to default read location
cp -v $LICENSE ./lib/

# start command line testing application
java $OPTS -jar gs-stream-test-0.0.2-SNAPSHOT.jar

# return to folder
popd
