infoPath="/mnt/hgfs/vmware_share/data/commit_rev.txt"

chmod 777 /mnt/hgfs/vmware_share/projects/
cat $infoPath | while read project_name commit_id rev branch_id branch_name
do
 cd /home/yueyang/gitFile/$project_name
 git checkout $branch_name
 git reset --hard $rev
  
if test -d /home/yueyang/gitFile/$project_name;then
   cp -r /home/yueyang/gitFile/$project_name  /mnt/hgfs/vmware_share/projects/$project_name
   chmod 777 -R /mnt/hgfs/vmware_share/projects/$project_name
fi
done
