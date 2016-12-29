
gitFile=$1
FilePath=/home/yueyang/gitFile/
infoPath="/home/yueyang/data/recover_info/"$gitFile"Recover.txt"


echo $infoPath
#if ! -f $infoPath;then
#    exit
#fi

cd $FilePath$gitFile
recoverPath=/home/yueyang/recover_projects/$gitFile"AllFiles"/
if test -e $recoverPath;then
    #rm $gitFile"AllFiles"
    exit
fi
mkdir $recoverPath

echo $recoverPath

cat $infoPath | while read file_id commit_id current_file_path rev changeloc recent_time 
do
 git reset -q $rev $current_file_path
 git checkout $current_file_path
  
if test -e $current_file_path;then
   cp $current_file_path $recoverPath$file_id"_"$commit_id"_"$changeloc".java"
fi
chmod 777 -R $recoverPath
done

