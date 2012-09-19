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

require 'gui/helpers'

class GUI
	import javax.swing.JFrame;
	import javax.swing.JLabel;

	attr_reader :options, :profile

	def initialize (options = {})
		@options = options
		@profile = options[:config] ? Torchat.new(options[:config]) : Torchat.profile(options[:profile])
	end

	def start
		@thread = Thread.new {
			EM.run {
				profile.start
			}
		}

		@frame = JFrame.new('jtorchat')
		@frame.getContentPane().add(JLabel.new('this be jtorchat'))
		@frame.setDefaultCloseOperation(JFrame::EXIT_ON_CLOSE)
		@frame.pack
		@frame.set_visible(true);
	end

	def stop
		profile.stop

		EM.stop_event_loop

		@thread.join
	end
end
