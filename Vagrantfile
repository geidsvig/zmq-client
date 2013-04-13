# -*- mode: ruby -*-
# vi: set ft=ruby :

# Default VagrantFile for developer machines

Vagrant::Config.run do |config|

    config.vm.box = "lucid64"

    config.vm.box_url = "http://files.vagrantup.com/lucid64.box"

    config.vm.forward_port 80, 8880

    config.vm.host_name = "vagrant-zmq-client"

    config.vm.network :hostonly, "192.168.33.70"

    config.vm.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]

end
