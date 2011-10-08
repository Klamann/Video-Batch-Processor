Video Batch Processor
=====================

... is a tool that allows you to batch process any video files on your storage devices according to your specific needs. Search for video files matching specific criteria on your storage devices (name, extension, size) and batch-process them with the video converter of your choice.

Video Batch Processor (vbp) is not a video converter itself. It's more like an intelligent crawler, that extends the functionality of video converters that are unable to choose the correct files from large amounts of data.

Features
--------

* select individual files and folders on your local storage devices
* crawl selected folders (also recursively)
* specify the output location (using a rename pattern or a different folder)
* configure your own filter: file size, file extension, file name or even custom regex expressions.
* let your files be processed by a good free converter (currently only [Handbrake](http://handbrake.fr/) supported)

Notes
-----

No installation required

This application will create a folder named `.vbp` in your user home directory. In this folder a file `settings.ini` will be created to store your previous configuration. If you want to remove any traces of this software on your local data storage device, just remove the `.vbp`-folder.

On Windows operating systems, this folder can usually be found under `C:\Users\<yourname>\.vbp\`

On Unix-like operating systems it's usually `/home/<yourname>/.vbp/`
