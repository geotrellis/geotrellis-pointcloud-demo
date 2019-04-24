# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.require_version ">= 1.8"

Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/trusty64"

  config.vm.network :private_network, ip: ENV.fetch("GT_POINTCLOUD_IP",  "10.10.10.10")

  config.vm.synced_folder "~/.aws", "/home/vagrant/.aws"
  config.vm.synced_folder "data", "/home/vagrant/data"
  config.vm.synced_folder "~/.ivy2", "/home/vagrant/.ivy2"
  config.vm.synced_folder "./", "/vagrant", type: "nfs", mount_options: ['rw', 'tcp', 'nolock']

  config.vm.provider :virtualbox do |vb|
    vb.memory = 2048
    vb.cpus = 2
  end

  # Webpack Dev Server
  config.vm.network :forwarded_port, guest: 8284, host: 8284
  config.vm.network :forwarded_port, guest: 8000, host: 8000
  config.vm.network :forwarded_port, guest: 7070, host: 7070

  # Change working directory to /vagrant upon session start.
  config.vm.provision "shell", inline: <<SCRIPT
    if ! grep -q "cd /vagrant" "/home/vagrant/.bashrc"; then
      echo "cd /vagrant" >> "/home/vagrant/.bashrc"
    fi
SCRIPT

  config.vm.provision "ansible" do |ansible|
    ansible.playbook = "deployment/ansible/pc-demo.yml"
    ansible.galaxy_role_file = "deployment/ansible/roles.yml"
  end
end
