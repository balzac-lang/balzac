
REPO_FILES=`git ls-files | grep -E ".*\.java|.*\.xtext|.*\.xsemantics"`


for FILE in $REPO_FILES ; do
    MODIFICATION_DATE=`git log -1 --pretty='%aI' -- $FILE`
    MODIFICATION_YEAR=`date --date="$MODIFICATION_DATE" +%Y`

    echo "$MODIFICATION_YEAR $MODIFICATION_DATE $FILE"

    if [[ $(cat $FILE | grep "Copyright") == "" ]]; then
        # File has no copyright string
        echo -e "\tAdding new copyright string"
        NEW_COPYRIGHT_STRING=`echo -e "/*\n * Copyright $MODIFICATION_YEAR Nicola Atzei\n */"`
        echo -e "$NEW_COPYRIGHT_STRING\n$(cat $FILE)" > $FILE
    else
        echo -e "\tUpdating existing copyright string"
        OLD_COPYRIGHT_STRING="Copyright [0-9]{4} Nicola Atzei"
        NEW_COPYRIGHT_STRING="Copyright $MODIFICATION_YEAR Nicola Atzei"
        sed -i -r "s/$OLD_COPYRIGHT_STRING/$NEW_COPYRIGHT_STRING/g" $FILE
    fi
done