dir_path=$1
filelist=`ls $dir_path`


for file in $filelist
do 
	echo "------"$file"-------"
	infoPath="/home/yueyang/data/recover_info/"$file"Recover.txt"
#################################################
############Caculate the Threshold###############
#################################################
	#tempDir="/home/yueyang/data/temp_recover_info/"
	#tempPath="/home/yueyang/data/temp_recover_info/"$file"Recover.txt"

	#if test ! -e $tempDir;then
    	#	mkdir $tempDir
	#fi
	
	#sort -k5n $infoPath>$tempPath
	#cutinfo=$(cat $tempPath | awk -F"\t" '{print $5}')
		#echo $cutinfo
	#array=($cutinfo)
		#echo ${array[@]}
		#echo ${array[0]}
	#len=${#array[@]}
	#echo $len
	
	#up_ind=$[len*3/4]
	#down_ind=$[len/4]
	#echo $up_ind
	#echo $down_ind
	#up=${array[$up_ind]}
	#down=${array[$down_ind]}
	#echo $up
	#echo $down
        #threshold=$(echo "scale=2;$up+1.5*$up-1.5*$down" | bc)
	threshold=0
	echo $threshold
#################################################
########Export the Metrics into Database#########
#################################################
	sh /home/yueyang/data/exportData.sh $file $threshold
	echo "------extracting "$file" finished!-------"
	rm -f $tempPath

	cd ../
done
