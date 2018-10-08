#!/bin/bash
set -e
# When running CircleCI locally, don't do anything.
if [[ "$CIRCLE_BUILD_NUM" != '' && "$CIRCLE_BRANCH" != 'HEAD' ]]; then
  # Get the product name from the product token.
  API_REQ="{\"requestType\":\"getOrganizationProductVitals\",\"orgToken\":\"$WHITESOURCE_ORG_TOKEN\"}"
  PRODUCT_NAME=$(curl -H "Content-Type: application/json" -H "charset: UTF-8" https://saas.whitesourcesoftware.com/api -d $API_REQ | jq -r '.productVitals[].name')
  # Augment the build script to use WhiteSource.
  echo >> build.gradle
  cat >>build.gradle <<EOF
whitesource {
    orgToken '${WHITESOURCE_ORG_TOKEN}'
    productName '${PRODUCT_NAME}'
    dependencyFilter includeAllDependencies
    includeConfiguration configurations.runtime
    includeConfiguration configurations.compile
    includeConfiguration configurations.testCompile
}
EOF
else
  echo 'This is a local CircleCI build; skipping WhiteSource.'
fi
