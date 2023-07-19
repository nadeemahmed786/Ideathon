#!/bin/bash

#--- Set local path for Project here -----
MAINPATH=$PWD

current_time=$(date '+%m.%d.%Y-%I.%M%p')
COUNTER=0
STATIC=0

# Web Hook URL for Slack Channels
# XXXX - has to be replaced with actual tokens
URL=https://hooks.slack.com/services/XXXXXX/XXXXXX/XXXXXXX

cd $MAINPATH

USER="$(whoami)"
curl -X POST -H 'Content-type: application/json' --data "{'text':'\`Visual Test started by $HOSTNAME[$USER]\` :hourglass_flowing_sand:'}" ${URL}

RED='\033[0;31m'
LGREEN='\033[0;32m'
NC='\033[0m'
LGREEN='\033[1;32m'
YELLOW='\033[1;33m'

# Go to Results directory and create a logs file
cd ${MAINPATH}\results

# Run test function
run_test() {
  CWD=${pwd}
  cd $PWD
  # To Execute Project
  printf '%s\n' "${PWD##*/} is running..."
  /opt/maven/bin/mvn test
  sleep 10

  cd $MAINPATH/results
  curl -F file=@Output.zip -F "initial_comment=Visual Test Report" -F channels=XXXXXX -H "Authorization: Bearer XXXXXXXX" https://slack.com/api/files.upload
}

# --------------Test run--------------
printf "${RED} ==> Testing has begun\n ${NC}"
start=`date +%s`

#Tests to run only once initially
run_test
wait

end=`date +%s`
runtime=$((end-start))
printf "${LGREEN} \n==> Done.\n ${NC}"
# --------------End run--------------


#Total Runtime
printf "${YELLOW} \nTOTAL RUNTIME ==> ${runtime} SECONDS \n${NC}"

cd $MBT
(
  printf "\nTOTAL RUNTIME ==> "
  printf '%dh:%dm:%ds\n' $((runtime/3600)) $((runtime%3600/60)) $((runtime%60))
) | column -t -s $'\t' >> tableLog.txt

cd $MBT

#To print in slack
curl -X POST -H 'Content-type: application/json' --data "{'text':'\`VISUAL-TEST IS FINISHED NOW IN $HOSTNAME[$USER]\` :checkered_flag:'}" ${URL}
curl -X POST -H 'Content-type: application/json' --data "{'text': '*[VISUAL-TEST | DONE] $current_time*'}" ${URL}