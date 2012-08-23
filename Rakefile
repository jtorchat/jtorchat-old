if RUBY_PLATFORM != 'java'
	abort 'you have to run this from JRuby'
end

require 'rake'
require 'rake/clean'
require 'warbler'
require 'fileutils'

Warbler::Task.new

CLEAN.include('jtorchat.zip', 'build/jtorchat.jar')

task :default => :run

task :run do
	require 'bundler'
	Bundler.setup :default, 'development'

	$:.unshift './lib'
	load 'bin/jtorchat'
end

task :build => [:clean, :jar] do
	FileUtils.mv 'jtorchat.jar', 'build/'

	Zip::ZipFile.open 'jtorchat.zip', Zip::ZipFile::CREATE do |zip|
		zip.mkdir 'jtorchat'

		Dir['build/*'].each {|path|
			zip.add "jtorchat/#{path['build/'.length .. -1]}", path
		}
	end
end


