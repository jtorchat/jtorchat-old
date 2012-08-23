#--
# jtorchat is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published
# by the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# jtorchat is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with jtorchat. If not, see <http://www.gnu.org/licenses/>.
#++

require 'java'

class GUI
	module Jar
		@object = java.lang.Object.new

		def self.open (path)
			unless io = @object.java_class.resource_as_stream("/#{path}").to_io
				raise Errno::ENOENT, path
			end

			return io unless block_given?

			begin
				yield io
			ensure
				io.close
			end
		end

		def self.read (path)
			open(path) { |f| f.read }
		end
	end
end
