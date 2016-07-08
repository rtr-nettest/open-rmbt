#!/bin/bash
#*******************************************************************************
# Copyright 2015 alladin-IT GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#*******************************************************************************

CUR_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
MIGRATION_PATH=$CUR_DIR/migration/

if [ ! -d "$MIGRATION_PATH" ]; then
  #migration directory doesn't exist, create it
  mkdir $MIGRATION_PATH
fi

cd $MIGRATION_PATH

TIMESTAMP=$(date +%Y%m%d%H%M%S)
GIT_BRANCH=$(git branch | sed -n '/\* /s///p' | sed 's/\//_/g')
NEW_MIGRATION_FILE="${TIMESTAMP}_${GIT_BRANCH}.sql"

echo "adding new migration $NEW_MIGRATION_FILE to path $(pwd)"

TIMESTAMP1=$(date +%Y-%m-%d)
TIMESTAMP2=$(date +%H:%M:%S)
GIT_BRANCH_NO_REPLACE="$(git branch | sed -n '/\* /s///p')"
STR="-- new migration on ${TIMESTAMP1} ${TIMESTAMP2} in branch: '${GIT_BRANCH_NO_REPLACE}'"

( echo $STR ; echo "" ; echo "" ) >> $NEW_MIGRATION_FILE
