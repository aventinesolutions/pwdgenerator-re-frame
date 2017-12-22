require 'dotenv/load'
require 'net/ftp'
require 'yaml'
require 'pathname'

desc 'check environment'
task :check_env do
  errors = []
  %w[SERVER USER PASSWORD].each do |param|
    key = "FTP_#{param}"
    val = ENV[key]
    errors.push("must define environment value for #{key}") unless val && val.length > 0
  end
  fail errors.join(', ') unless errors.length == 0
  puts 'environment is good to go!'
end

desc 'clean, compile and minify'
task :compile do
  `lein clean && lein cljsbuild once min`
end

desc 'connect to FTP server'
task connect: :check_env do
  $connection            = Net::FTP.new
  $connection.debug_mode = true
  $connection.connect(ENV['FTP_SERVER'])
  $connection.login(ENV['FTP_USER'], ENV['FTP_PASSWORD'])
end

desc 'read deployment specifications'
task :spec do
  $spec = YAML.load_file('deploy.yml')
end

desc 'change to base folder on remote'
task :goto_remote_base do
  begin
    $connection.chdir($spec['remote_base'])
  rescue Net::FTPPermError
    $connection
  end
end

desc 'change to base folder locally'
task :goto_local_base do
  Dir.chdir($spec['local_base'])
end

desc 'deploy the specified local files to the remote server'
task :deploy_files do
  $spec['files'].each do |glob|
    Pathname.glob(glob).each do |path|
      p path
    end
  end
end

desc 'close FTP connection'
task :close do
  $connection.close
end

desc 'debug'
task debug: [:connect, :spec, :goto_local_base, :goto_remote_base, :deploy_files]
