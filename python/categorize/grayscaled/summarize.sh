#!/bin/sh

for ii in `ls -1 -d clusters_*`
do
    echo "\n"

    for jj in `find $ii -name groupCounts.csv`
    do
        echo $jj ": " `wc -l $jj | awk '{print $1}'`
        head -2 $jj
        tail -2 $jj
    done

    echo "\n"
done

