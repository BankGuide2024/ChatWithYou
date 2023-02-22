echo -n "Enter version introduction:"
read introduction

git status;
git add .;
commitTime=$(date "+%Y-%m-%d %H:%M:%S");
git commit -m "$introduction $commitTime";
git push