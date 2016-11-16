infoPath="/home/yueyang/data/commit_rev.txt"

chmod 777 /home/yueyang/projects/
cat $infoPath | while read project_name commit_id rev branch_id branch_name
do
 cd /home/yueyang/gitFile/$project_name
 git checkout $branch_name
 git reset --hard $rev
  
if test -d /home/yueyang/gitFile/$project_name;then
   cp -r /home/yueyang/gitFile/$project_name  /home/yueyang/projects/$project_name
   chmod 777 -R /home/yueyang/projects/$project_name
fi
done
