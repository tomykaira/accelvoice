# Tested environment

This software is developed under such environment.
It may work under other environment, but not supported.
Any report about working / not working is welcome.

- Ubuntu 12.04 LTS 32 bit
- OpenJDK 1.7.0_09
- gcc version 4.7.2

# How to use

## install dependencies

This tool uses gstreamer, sphinxbase, pocketsphinx, and flite for speech recognition.

- jna: From apt `apt-get install libjna-java`
- gsteramer: From apt `apt-get install libgstreamer0.10-dev`
- sphinxbase
    - Fetch from [CMU Sphinx - Browse /sphinxbase/0.8 at SourceForge.net](http://sourceforge.net/projects/cmusphinx/files/sphinxbase/0.8/)
    - configure, make, make install
- pocketsphinx
    - [CMU Sphinx - Browse /pocketsphinx/0.8 at SourceForge.net](http://sourceforge.net/projects/cmusphinx/files/pocketsphinx/0.8/)
    - configure, make, make install
    - install gst plugins

```shell
cd src/gst-plugin
make
sudo cp .libs/libgstpocketsphinx.so /usr/lib/gstreamer-0.10
```

- flite
    - Fetch from [Index of /flite/packed/flite-1.4](http://www.festvox.org/flite/packed/flite-1.4/)
    - configure, make, make install

## build project

This project uses Gradle (and gradle-wrapper) as a build tool.

Go to project home and run these commands.

    ./gradlew build   # build
    ./gradlew install # create executables of Java modules

## install

- Move `recognizer/build/lib/libaccel_recognizer.so` to a directory included in `$LD_LIBRARY_PATH` environment variable.
- Load `projectdict/clients/accelvoice.el` from your emacs setting, and `(require 'accelvoice)`

## configure emacs

Load client library and set projectdict path.

```
(setq accelvoice--root "/path/to/accelvoice")
(add-to-list 'load-path (concat accelvoice--root "/projectdict/clients"))
(require 'accelvoice)
(setq accelvoice--projectdict-path (concat accelvoice--root "/build/install/projectdict/bin/projectdict"))
(global-set-key (kbd "C-,") 'accelvoice--complete)
```

These configs make AccelVoice more useful on Emacs.

```lisp
;; Show completion status (esp. current candidates size) in mode line
(defun insert-before-vc-mode (rest-format)
  (if (and (listp (car rest-format)) (eq (car (car rest-format)) 'vc-mode))
      (cons '(:eval (accelvoice--update-mode-line)) rest-format)
    (cons (car rest-format) (insert-before-vc-mode (cdr rest-format)))))

(setq-default
 mode-line-format
 (insert-before-vc-mode mode-line-format))
```

```lisp
;; Start completion automatically when you type more than 1 words
(defun accelvoice--post-command-hook-function ()
  (when (and accelvoice--current-process (eq this-command 'self-insert-command))
    (let ((bounds (bounds-of-thing-at-point 'symbol)))
      (when (and bounds (< 1 (- (cdr bounds) (car bounds))))
        (accelvoice--complete)))))

(add-hook 'post-command-hook 'accelvoice--post-command-hook-function)
```

## use

Take [tomykaira/clockwork](https://github.com/tomykaira/clockwork/) as a sample project.
`git clone git@github.com:tomykaira/clockwork.git` under `~`.

- `M-x accelvoice--start-projectdict` opens the minibuffer.  Put path to the project clockwork`.
- Put "clo" into any buffer, and press `C-,`, or `M-x accelvoice--complete`.
- Then speak "clockwork" to the microphone.
- "clo" will be expanded to "clockwork".

If it gives unexpected result (for example "clock"), try several times and other words.
It it seems not working, check the log file `~/.accelvoice/projectdict.log`.

Here are key lines to find out the cause.

- "TIME [main] INFO  io.github.tomykaira.accelvoice.projectdict.App - Starting projectdict server for /path/to/clockwork/": Emacs correctly starts AccelVoice projectdict server
- "TIME [main] INFO  io.github.tomykaira.accelvoice.projectdict.Server - System initialized in ***: Server started successfully.
- "TIME [pool-1-thread-2] INFO  io.github.tomykaira.accelvoice.projectdict.HttpCompletionHandler - -1938647573: complete clo from clockwork clock clone": completion is correctly started
- "INFO: callbacks.c(31): vader start ********: vocal input is recognized.  If there is no line like this, ensure the mic is working.
- "ERROR: "fsg_search.c", line 1104: Final result does not match the grammar in frame ***": AccelVoice is working, but matching candidate is not found. Try again with better pronunciation ;)

# Contribution

If you find a problem or have a question, please report it from Issues.

If you fix a bug or implement enhancement, please send me a pull-request.

# License

GNU General Public License version 3.

Copyright 2013 tomykaira

    This file is part of AccelVoice.

    AccelVoice is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AccelVoice is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AccelVoice.  If not, see <http://www.gnu.org/licenses/>.

See COPYING for more details.
