#!/bin/bash
set -e
if [[ "$CIRCLE_BUILD_NUM" == '' ]]; then
  touch ~/.localCircleBuild
elif [[ "$CIRCLE_PR_NUMBER" != '' ]]; then
  touch ~/.prCircleBuild
elif [[ "$CIRCLE_BRANCH" != 'master' && "$CIRCLE_BRANCH" != 'rc' && "$CIRCLE_BRANCH" != 'release' ]]; then
  touch ~/.prCircleBuild
fi
