#!/bin/sh

ANSIBLE_PLAYBOOKS_DIR=$1
USER_HOME=$2
DEB_PATH=$3

if [[ -z $ANSIBLE_PLAYBOOKS_DIR || -z $USER_HOME || -z $DEB_PATH ]]; then
  echo "Missing params"
  echo "Usage Example"
  echo "./deploy.sh ~/projects/AnsiblePlaybooks ~/ ~/projects/geidsvig/zmq-client"
  exit 0
fi

ansible-playbook -i $ANSIBLE_PLAYBOOKS_DIR/inventories/vagrant -u vagrant -v --private-key=$USER_HOME/.vagrant.d/insecure_private_key --extra-vars "build_number=x debpath=$DEB_PATH" $ANSIBLE_PLAYBOOKS_DIR/geidsvig/deploy-zmq-client-artifact.yaml

ansible-playbook -i $ANSIBLE_PLAYBOOKS_DIR/inventories/vagrant -u vagrant -v --private-key=$USER_HOME/.vagrant.d/insecure_private_key $ANSIBLE_PLAYBOOKS_DIR/geidsvig/configure-zmq-client-artifact.yaml

ansible-playbook -i $ANSIBLE_PLAYBOOKS_DIR/inventories/vagrant -u vagrant -v --private-key=$USER_HOME/.vagrant.d/insecure_private_key --extra-vars "cmd=restart" $ANSIBLE_PLAYBOOKS_DIR/geidsvig/control-zmq-client-artifact.yaml

