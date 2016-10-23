#!/bin/bash

	tempinfoPath="/home/yueyang/data/info/"$1"Recover.txt"
	infoPath="/home/yueyang/data/files/"voldemort"Recover.txt"
	sort -k5rn $tempinfoPath>$infoPath
	changeloc_arr=`cut -f5 $infoPath`
	len=${#changeloc_arr}
	echo $len
	threshold=0
	
	down_ind=`expr $len/4`
	up_ind=`expr $down_ind*3`
	echo $up_ind
	echo $down_ind

	up=$changeloc_arr[$up_ind]
	down=$changeloc_arr[$down_ind]
	
	threshold=`expr $up+1.5*$up-1.5*$down`
	#echo $up
	#echo $down
	echo $threshold
