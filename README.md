Video Batch Processor
=====================

... is a tool that allows you to batch process any video files on your 
storage devices according to your specific needs. Search for video files 
matching specific criteria (like name, extension, size) and batch-process 
them with the video converter of your choice.

Video Batch Processor (vbp) is not a video converter itself. It's more 
like an intelligent crawler, that extends the functionality of video 
converters that are unable to choose just the files you need.

Features
--------

* select individual files and folders on your local storage devices
* crawl selected folders (also recursively)
* specify the output location (using a rename pattern or a different folder)
* configure your own filter: file size, file extension, file name or even 
  custom regex expressions.
* let your files be processed by a good free converter (currently only 
  [Handbrake](http://handbrake.fr/) supported)

Installation
------------

vbp comes without an installer, simply launch the executable jar (windows
users may use the .exe-file instead). 

Notes
-----

This application will create a folder named `.vbp` in your user home 
directory. In this folder a file `settings.ini` will be created to store 
your previous configuration. If you want to remove any traces of this 
software on your local data storage device, just remove the `.vbp`-folder.

On Windows operating systems, this folder can usually be found under `C:\Users\<yourname>\.vbp\`

On Unix-like operating systems it's usually `/home/<yourname>/.vbp/`

Changelog
---------

see ´CHANGELOG.md´ in the [source repository](https://github.com/Klamann/Video-Batch-Processor).

Contact
-------

vbp is being developed by Sebastian Straub, <sebastian-straub@gmx.net>

For updates, visit the [Project Homepage](https://github.com/Klamann/Video-Batch-Processor).
For further information, visit [the wiki](https://github.com/Klamann/Video-Batch-Processor/wiki).

Found any bugs? Please report them to the [Issue Tracker](https://github.com/Klamann/Video-Batch-Processor/issues).


Copyright
---------

Copyright (C) 2011 Sebastian Straub, <sebastian-straub@gmx.net>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

