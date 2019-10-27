
REPO_FILES=`git ls-files | grep -E ".*\.xtend|.*\.java|.*\.xtext|.*\.xsemantics"`


for FILE in $REPO_FILES ; do
    MODIFICATION_DATE=`git log -1 --pretty='%aI' -- $FILE`
    MODIFICATION_YEAR=`date --date="$MODIFICATION_DATE" +%Y`

    echo "$MODIFICATION_YEAR $MODIFICATION_DATE $FILE"

COPYRIGHT_NOTES=$(cat <<-END
/*
 * Copyright $MODIFICATION_YEAR Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


END
)

    if [[ $(cat $FILE | grep "Copyright") == "" ]]; then
        # File has no copyright string
        echo -e "\tAdding new copyright string"
        cat <(echo -e "${COPYRIGHT_NOTES}") $FILE | sponge $FILE
    else
        echo -e "\tUpdating existing copyright string"
        #cat $FILE | awk '{ if ( NR > 3  ) { print } }' | sponge $FILE
        #cat <(echo -e "${COPYRIGHT_NOTES}") $FILE | sponge $FILE
        OLD_COPYRIGHT_STRING="Copyright [0-9]{4} Nicola Atzei"
        NEW_COPYRIGHT_STRING="Copyright $MODIFICATION_YEAR Nicola Atzei"
        sed -i -r "s/$OLD_COPYRIGHT_STRING/$NEW_COPYRIGHT_STRING/g" $FILE
    fi
done
