#!/usr/local/bin/bash

#java=/usr/local/bin/java
swizzle=$HOME/swizzle-jirareport-1.6.2-SNAPSHOT-dep.jar

template=${1?Specify a template name}
id=${2?Specify the numeric id of the project}
name=${3?Specify the name of the project}
to=${4?Specify the address to where the report should be sent}
url=${5:-jira}

java -cp target/jirareports-1.0-SNAPSHOT.jar \
    org.apache.openejb.tools.patches.PatchReport \
    -DserverUrl=https://issues.apache.org/$url/ \
    -DprojectId=$id -DprojectName=$name \
    -Demail=true \
    -Dfrom=dblevins@apache.org \
    -Dto=$to \
    -Dusername=jirareport \
    -Dpassword=ampad
#    org.codehaus.swizzle.jirareport.Main  patches.vm
#    org.apache.openejb.tools.patches.PatchReport
#    | /usr/sbin/sendmail -it
#mvn -Dtest=skip exec:java -Dexec.mainClass=org.apache.openejb.tools.patches.PatchReport \
#    -DserverUrl=https://issues.apache.org/$url/ \
#    -DprojectId=$id -DprojectName=$name \
#    -Demail=true \
#    -Dfrom=dblevins@apache.org \
#    -Dto=$to \
#    -Dusername=jirareport \
#    -Dpassword=ampad
##    org.codehaus.swizzle.jirareport.Main  patches.vm
##    org.apache.openejb.tools.patches.PatchReport
##    | /usr/sbin/sendmail -it
