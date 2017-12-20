require 'dotenv/load'

desc 'check environment'
task :check_env do
  errors = []
  %w[SERVER USER PASSWORD].each do |param|
    key = "FTP_#{param}"
    val = ENV[key]
    errors.push("must define environment value for #{key}") unless val && val.length > 0
  end
  fail errors.join(', ') unless errors.length == 0
  p "environment is good to go!"
end
