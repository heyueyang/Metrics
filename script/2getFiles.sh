dir_path=$1
filelist=`ls $dir_path`
echo $filelist
for file in $filelist
do 
	echo "------"$file"-------"

	sh /home/yueyang/data/GetFile.sh $file
	echo "------"$file" revovered finished!-------"
	cd ../
done
