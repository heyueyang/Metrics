dir_path=$1
filelist=`ls $dir_path`

for file in $filelist
do 
	echo "------"$file"-------"
	sudo mysql --local-infile -uroot -p111111 -e "DROP DATABASE IF EXISTS "$file";CREATE DATABASE IF NOT EXISTS "$file" DEFAULT CHARSET utf8 COLLATE utf8_general_ci;"
	cd $dir_path"/"$file
	sudo miningit -uroot -p111111 -d$file --extension=Patches,PatchLOC,BugFixMessage
	echo "------min "$file" finished!-------"
	cd ../
done

