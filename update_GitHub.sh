git status;
git add .;
str="Update Date:"
commitTime=$(date "+%Y-%m-%d %H:%M:%S");
git commit -m "$str $commitTime";
git push