#!/bin/bash
echo "Installing Essentials"
echo $INPUT_NPM_PACKAGE_NAME
apt update
apt install -y git default-jre sudo apt-utils apt-transport-https
npm install -g @oclif/dev-cli
echo "Installing SBT for Scala"
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
apt update
apt install -y sbt

echo "Cloning Optic"
cd /tmp
git clone https://github.com/trulyronak/optic
cd optic
echo "Checking out specific branch (eventually this will just be release)"
git checkout feature/debian-release-flow
echo "Building Optic"
source sourceme.sh
optic_build
echo "Packing Debian Release"
cd ./workspaces/local-cli
npm install
oclif-dev pack:deb
echo "Installing Ruby"
apt install ruby-full -y
gem install deb-s3
cd /tmp/optic/workspaces/local-cli/dist/deb/
export PATH_TO_DEB="/tmp/optic/workspaces/local-cli/dist/deb/api_8.2.1-1_amd64.deb"
echo $PATH_TO_DEB
# ls $PATH_TO_DEB
# du $PATH_TO_DEB
# /bin/bash
#mv /tmp/optic/workspaces/local-cli/dist/deb/api_8.2.2-1_amd64.deb /tmp/optic/workspaces/local-cli/dist/deb/api-8.2.2-1-amd64.deb
#export PATH_TO_DEB="/tmp/optic/workspaces/local-cli/dist/deb/api-8.2.2-1-amd64.deb"
echo "deb-s3 upload -e --access-key-id=$INPUT_AWS_ACCESS_KEY_ID --secret-access-key=$INPUT_AWS_SECRET_ACCESS_KEY --bucket $INPUT_BUCKET_NAME --preserve-versions $PATH_TO_DEB"
deb-s3 upload -e --access-key-id=$INPUT_AWS_ACCESS_KEY_ID --secret-access-key=$INPUT_AWS_SECRET_ACCESS_KEY --bucket $INPUT_BUCKET_NAME --preserve-versions $PATH_TO_DEB
ls /tmp/optic/workspaces/local-cli/dist/deb/
echo "PATH_TO_DEB: "$PATH_TO_DEB
/bin/bash
echo "PATH_TO_DEB after invoking new shell: "$PATH_TO_DEB
