#!/bin/bash

if [ ! -e node_modules ]
then
  mkdir node_modules
fi

case `uname -s` in
  MINGW*)
    USER_UID=1000
    GROUP_UID=1000
    ;;
  *)
    if [ -z ${USER_UID:+x} ]
    then
      USER_UID=`id -u`
      GROUP_GID=`id -g`
    fi
esac

buildNode () {
  case `uname -s` in
    MINGW*)
      docker-compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "yarn install --no-bin-links && node_modules/gulp/bin/gulp.js build"
      ;;
    *)
      docker-compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "yarn install && node_modules/gulp/bin/gulp.js build"
  esac
}

testNode () {
  rm -rf coverage
  rm -rf */build
  case `uname -s` in
    MINGW*)
      docker-compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "yarn install --no-bin-links && node_modules/gulp/bin/gulp.js drop-cache &&  yarn test"
      ;;
    *)
      docker-compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "yarn install && node_modules/gulp/bin/gulp.js drop-cache && yarn test"
  esac
}

testNodeDev () {
  rm -rf coverage
  rm -rf */build
  case `uname -s` in
    MINGW*)
      docker-compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "yarn install --no-bin-links && node_modules/gulp/bin/gulp.js drop-cache &&  yarn run test:dev"
      ;;
    *)
      docker-compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "yarn install && node_modules/gulp/bin/gulp.js drop-cache && yarn run test:dev"
  esac
}

buildGulp() {
    docker-compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "yarn install --no-bin-links && node_modules/gulp/bin/gulp.js build"
}

buildCss() {
    docker-compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "yarn run build:sass"
}

for param in "$@"
do
  case $param in
    buildNode)
      buildNode
      ;;
    install)
      buildNode && buildGradle
      ;;
    buildGulp)
      buildGulp
      ;;
    buildCss)
      buildCss
      ;;
    test)
      testNode ; testGradle
      ;;
    testNode)
      testNode
      ;;
    testNodeDev)
      testNodeDev
      ;;
    *)
      echo "Invalid argument : $param"
  esac
  if [ ! $? -eq 0 ]; then
    exit 1
  fi
done
