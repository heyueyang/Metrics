
gitFile=$1
FilePath=/home/yueyang/project/
infoPath="/home/yueyang/data/info_new/"$gitFile"Recover.txt"


echo $infoPath
#if ! -f $infoPath;then
#    exit
#fi

cd $FilePath$gitFile
recoverPath=../recover_new/$gitFile"AllFiles"/
if test -e $recoverPath;then
    #rm $gitFile"AllFiles"
    exit
fi
mkdir $recoverPath
echo $recoverPath

cat $infoPath | while read file_id commit_id current_file_path rev changeloc recent_time 
do
 git reset $rev $current_file_path
 git checkout $current_file_path
  
if test -e $current_file_path;then
   cp $current_file_path $recoverPath$file_id"_"$commit_id"_"$changeloc".java"
fi
done

