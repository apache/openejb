#!/bin/sh
case $1 in 
   "build" ) ./bin/build.sh $2 $3 $4 $5;;
   "test" ) 
   
      case $2 in
      "") 
            echo "Running EJB compliance tests on IntraVM Server"
            ./bin/test.sh
            echo "Running EJB compliance tests on CORBA Server"
            echo " 1. Starting JNDI Server...";
            ./bin/launch_jndi.sh &> jndi.log &
            sleep 2
            echo " 2. Starting CORBA Server...";
            ./bin/launch_server.sh &> server.log &
            sleep 5
            echo " 3. Starting test client...";
            ./bin/launch_client.sh
         ;;
      "intra-vm") 
            echo "Running EJB compliance tests on IntraVM Server"
            ./bin/test.sh
         ;;
      "corba") 
            echo "Running EJB compliance tests on CORBA Server"
            echo " 1. Starting JNDI Server...";
            ./bin/launch_jndi.sh &> jndi.log &
            sleep 2
            echo " 2. Starting CORBA Server...";
            ./bin/launch_server.sh &> server.log &
            sleep 5
            echo " 3. Starting test client...";
            ./bin/launch_client.sh
         ;;
      esac   
   ;;
   "start" ) echo "start";
            echo " 1. Starting JNDI Server...";
            ./bin/launch_jndi.sh &> jndi.log &
            sleep 2
            echo " 2. Starting CORBA Server...";
            ./bin/launch_server.sh 
            ;;   
   "deploy" ) echo "deploy";;

   "--help" ) 
      case $2 in 
      "" ) cat ./bin/command.hlp ;;
      "build"  ) cat ./bin/build.hlp ;;   
      "test"  ) cat ./bin/test.hlp ;;   
      "start" ) cat ./bin/start.hlp ;;   
      "deploy" ) cat ./bin/deploy.hlp ;;   
      esac
   ;;
   "" ) cat ./bin/command.hlp;;

esac   
