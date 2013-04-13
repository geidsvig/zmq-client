zmq-client
=========

zeromq client test sandbox

created to run through the 0mq Guide @ http://zguide.zeromq.org/page



GETTING STARTED
---------------

1) get your vagrant box prepared from project directory

vagrant up


2) update your /etc/hosts file with the geidsvig server hostname and ip pairs
   See AnsiblePlaybook file geidsvig/vagrant/etc-hosts


3) download AnsiblePlaybooks project and then inside AnsiblePlaybooks directory run

ansible-playbook -i inventories/vagrant -u vagrant -v --private-key=/Users/geidsvig/.vagrant.d/insecure_private_key --extra-vars "build_number=x" geidsvig/provision-zmq-client-server.yaml 


4) in project directory

sbt clean update dist

./make-deb.sh


5) deploy deb to vagrant

./deploy.sh ~/projects/AnsiblePlaybooks ~/ ~/projects/geidsvig/zmq-client

