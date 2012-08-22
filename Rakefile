require 'rake'
require 'rake/clean'
require 'warbler'
require 'fileutils'

Warbler::Task.new

CLEAN.include('jtorchat.zip', 'build/jtorchat.jar')

task :default => 'jar' do
	FileUtils.mv 'jtorchat.jar', 'build/'

	Zip::ZipFile.open 'jtorchat.zip', Zip::ZipFile::CREATE do |zip|
		zip.mkdir 'jtorchat'

		Dir['build/*'].each {|path|
			zip.add "jtorchat/#{path['build/'.length .. -1]}", path
		}
	end
end
