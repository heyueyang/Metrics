#!/bin/bash
	tempPath="/home/yueyang/data/info/voldemortRecover.txt"
	infoPath="/home/yueyang/data/files/"voldemort"Recover.txt"
	sort -k5n $tempPath>$infoPath
	cutinfo=$(cat $infoPath | awk -F"\t" '{print $5}')
	#echo $cutinfo
	array=($cutinfo)
	#echo ${array[@]}
	#echo ${array[0]}
	len=${#array[@]}
	echo $len
	
	up_ind=$[len*3/4]
	down_ind=$[len/4]
	echo $up_ind
	echo $down_ind
	up=${array[$up_ind]}
	down=${array[$down_ind]}
	echo $up
	echo $down
        threshold=$(echo "scale=2;$up+1.5*$up-1.5*$down" | bc)
	echo $threshold
