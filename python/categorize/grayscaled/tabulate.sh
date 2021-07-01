#!/bin/sh

rootDir=$1
cd $rootDir
for dataDir in `ls -d */* | grep -v results`
do
    cd $dataDir
    find . -name '*.png' | awk -F'/' '{print $2}' | sort -n | uniq -c | sort - > groupCounts.csv
    cd ../..
done
cd ..
