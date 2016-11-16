#!/bin/sh 
###########################################################################
################### Input data into database ###############################
###########################################################################
project=$1
input_file=$1"Com.csv" 
result_dir="/home/yueyang/data/complexity_csv/"
result_file=$1".csv"
thres=$2

sudo mysql -h localhost -uroot -p111111 -D $project< /home/yueyang/data/inputMetrics.sql
sudo mysql --local-infile -uroot -p111111 $project -e "LOAD DATA LOCAL INFILE '/home/yueyang/data/understand_metrics/"$input_file"' INTO TABLE complexity_info_temp FIELDS TERMINATED BY ','  IGNORE 1 LINES"

sudo mysql --local-infile -uroot -p111111 $project -e "insert into complexity_info select substring_index(Name,'\"',-2),substring_index(Name,'_',-2),substring_index(Name,'_',-1),AvgCyclomatic,AvgCyclomaticModified,AvgCyclomaticStrict,AvgEssential,AvgLine,AvgLineBlank,AvgLineCode,AvgLineComment,CountDeclClass,
CountDeclClassMethod,CountDeclClassVariable,CountDeclFunction,CountDeclInstanceMethod,CountDeclInstanceVariable,CountDeclMethod,CountDeclMethodDefault,
CountDeclMethodPrivate,CountDeclMethodProtected,CountDeclMethodPublic,CountLine,CountLineBlank,CountLineCode,CountLineCodeDecl,CountLineCodeExe,
CountLineComment,CountSemicolon,CountStmt,CountStmtDecl,CountStmtExe,MaxCyclomatic,MaxCyclomaticModified,MaxCyclomaticStrict,MaxNesting,RatioCommentToCode,SumCyclomatic,
SumCyclomaticModified,SumCyclomaticStrict,SumEssential
 from complexity_info_temp where Kind='File';"

sudo mysql --local-infile -uroot -p111111 $project -e "insert into metrics_info(file_id,commit_id,Type,AvgCyclomatic,AvgCyclomaticModified,AvgCyclomaticStrict,AvgEssential,AvgLine,AvgLineBlank,AvgLineCode,AvgLineComment,CountDeclClass,
CountDeclClassMethod,CountDeclClassVariable,CountDeclFunction,CountDeclInstanceMethod,CountDeclInstanceVariable,CountDeclMethod,CountDeclMethodDefault,
CountDeclMethodPrivate,CountDeclMethodProtected,CountDeclMethodPublic,CountLine,CountLineBlank,CountLineCode,CountLineCodeDecl,	CountLineCodeExe,
CountLineComment,CountSemicolon,CountStmt,CountStmtDecl,CountStmtExe,MaxCyclomatic,MaxCyclomaticModified,MaxCyclomaticStrict,MaxNesting,RatioCommentToCode,SumCyclomatic,
SumCyclomaticModified,SumCyclomaticStrict,SumEssential,changeloc,change_prone) select substring_index(Name1,'_',1),substring_index(Name2,'_',1),actions.type,AvgCyclomatic,AvgCyclomaticModified,AvgCyclomaticStrict,AvgEssential,AvgLine,AvgLineBlank,AvgLineCode,AvgLineComment,CountDeclClass,
CountDeclClassMethod,CountDeclClassVariable,CountDeclFunction,CountDeclInstanceMethod,CountDeclInstanceVariable,CountDeclMethod,CountDeclMethodDefault,
CountDeclMethodPrivate,CountDeclMethodProtected,CountDeclMethodPublic,CountLine,CountLineBlank,CountLineCode,CountLineCodeDecl,CountLineCodeExe,
CountLineComment,CountSemicolon,CountStmt,CountStmtDecl,CountStmtExe,MaxCyclomatic,MaxCyclomaticModified,MaxCyclomaticStrict,MaxNesting,RatioCommentToCode,SumCyclomatic,
SumCyclomaticModified,SumCyclomaticStrict,SumEssential,substring_index(Name3,'.',1), CASE WHEN substring_index(Name3,'.',1)>"$thres" THEN 'true' ELSE 'false' END from complexity_info inner join actions on actions.file_id=substring_index(Name1,'_',1) and actions.commit_id=substring_index(Name2,'_',1) where substring_index(Name3,'.',1)>0;"


#sudo mysql -uroot -p111111 change_prone -e "SELECT * FROM complexity_info  INTO OUTFILE 'training_metrics.csv'  FIELDS TERMINATED BY ','  OPTIONALLY ENCLOSED BY '"'  LINES TERMINATED BY '\n'"
#cp /var/lib/mysql/change_prone/training_metrics.csv /home/yueyang/data/training_metrics.csv
###########################################################################
################### Convert result file to CSV and save ###################
###########################################################################
if test ! -e $result_dir;then
    	mkdir $result_dir
fi
sudo mysql -uroot -p111111 $project -e "SELECT * FROM metrics_info">$result_dir$result_file 

sed -i 's/\t/,/g' /home/yueyang/data/complexity_csv/$result_file
chmod 777 -R  /home/yueyang/data/complexity_csv/$result_file

