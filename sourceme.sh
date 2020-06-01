#!/usr/bin/env bash
# usage: $ source ./sourceme.sh
export OPTIC_SRC_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
echo "Optic development scripts will run from $OPTIC_SRC_DIR"
alias apidev="OPTIC_DAEMON_ENABLE_DEBUGGING=yes OPTIC_UI_HOST=http://localhost:3000 OPTIC_AUTH_UI_HOST=http://localhost:4005 $OPTIC_SRC_DIR/workspaces/local-cli/bin/run"
alias apistage="OPTIC_DAEMON_ENABLE_DEBUGGING=yes $OPTIC_SRC_DIR/workspaces/local-cli/bin/run"

## Don't expose this below, instead create workspaces/local-cli/.env and add to there, so we can all 
## use our own local clusters to run against.
# export OPTIC_LOCAL_CLI__API_GATEWAY="https://yx9uc028d6.execute-api.us-east-1.amazonaws.com/stage"
alias cidev="$OPTIC_SRC_DIR/workspaces/ci-cli/bin/run"
alias agentdev="$OPTIC_SRC_DIR/workspaces/agent-cli/bin/run"
optic_workspace_clean() {
  (
    set -o errexit
    cd "$OPTIC_SRC_DIR"
    yarn wsrun --stages --report --fast-exit ws:clean
  )
}
optic_workspace_build() {
  (
    set -o errexit
    cd "$OPTIC_SRC_DIR"
    yarn wsrun --stages --report --fast-exit ws:build
  )
}
optic_workspace_build_with_notification() {
  (
    set -o errexit
    optic_workspace_build && sh ./workspace-scripts/build/on-success.sh || sh ./workspace-scripts/build/on-failure.sh
  )
}
optic_watch() {
  (
    set -o errexit
    cd "$OPTIC_SRC_DIR"
    optic_workspace_clean
    yarn run watch --filter=workspace-scripts/watch-filter.js "source sourceme.sh && optic_workspace_build_with_notification"
  )
}
check_ws() {
  yarn workspaces info | sed -e '2,$!d' -e '$d' | jq -r 'keys[] as $k | "\($k): \(.[$k].mismatchedWorkspaceDependencies)"'
}
alias check-workspace-dependencies="check-ws"
show_ws_versions() {
  find ./workspaces -type f -iname package.json -not -path "*node_modules*" -print0 | xargs -0 cat | jq ".name, .version"
}
alias wsinfo="show-ws-versions"
search_ws() {
  find ./workspaces -type f -not -path "*node_modules*" -print0 | xargs -0 grep -il $@
}
optic_build() {
  (
    set -o errexit
    cd "$OPTIC_SRC_DIR"

    yarn install
    yarn run build-domain
    optic_workspace_clean
    optic_workspace_build
  )
}
optic_build_and_publish_locally() {
  (
    set -o errexit
    optic_build
    cd "$OPTIC_SRC_DIR"
    yarn run publish-local
  )
}
optic_release_and_install_locally() {
  if [[ -z "$1" ]]; then
    echo "No version provided"
    exit 1
  fi
  (
    set -o errexit

    cd "$OPTIC_SRC_DIR"
    yarn run bump "$1"

    optic_build_and_publish_locally
    optic_install_from_local_registry
  )
}
optic_install_from_local_registry() {
  (
    set -o errexit
    YARN_REGISTRY=http://localhost:4873 yarn global add @useoptic/cli --registry=http://localhost:4873
  )
}
# DEBUG=optic* apidev daemon:stop && DEBUG=optic* apidev agent:start
