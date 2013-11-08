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

- gsteramer: From apt `apt-get install libgstreamer0.10-dev`
- sphinxbase
    - Fetch from [CMU Sphinx - Browse /sphinxbase/0.8 at SourceForge.net](http://sourceforge.net/projects/cmusphinx/files/sphinxbase/0.8/)
    - configure, make, make install
- pocketsphinx
    - [CMU Sphinx - Browse /pocketsphinx/0.8 at SourceForge.net](http://sourceforge.net/projects/cmusphinx/files/pocketsphinx/0.8/)
    - configure, make, make install
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
      (when (< 1 (- (cdr bounds) (car bounds)))
        (accelvoice--complete)))))

(add-hook 'post-command-hook 'accelvoice--post-command-hook-function)
```

# Contribution

If you find a problem or have a question, please report it from Issues.

If you fix a bug or implement enhancement, please send me a pull-request.
