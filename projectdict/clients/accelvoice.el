;;; accelvoice.el --- accelvoice (projectdict) interface for Emacs

;; Copyright (C) 2013 tomykaira

;; Author: tomykaira <tomykaira@gmail.com>
;; Created: 22 Oct 2013
;; Keywords: comm convenience tools

;; This file is not part of GNU Emacs.

;;; License
;;
;; The MIT License (MIT)

;; Copyright (c) 2013 tomykaira

;; Permission is hereby granted, free of charge, to any person obtaining a copy
;; of this software and associated documentation files (the "Software"), to deal
;; in the Software without restriction, including without limitation the rights
;; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
;; copies of the Software, and to permit persons to whom the Software is
;; furnished to do so, subject to the following conditions:

;; The above copyright notice and this permission notice shall be included in
;; all copies or substantial portions of the Software.

;; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
;; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
;; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
;; AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
;; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
;; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
;; THE SOFTWARE.

;;; Code:

;; variables

(defvar accelvoice--projectdict-path "/usr/local/bin/projectdict")
(defvar accelvoice--logfile "~/.accelvoice/projectdict.log")

;; internal variables

;; current running process object, or nil
(defvar accelvoice--current-process nil)

;; list of start position, end position, word for which the accelvoice is recognizing
(defvar accelvoice--current-completion nil)

;; functions

(defun accelvoice--start-projectdict ()
  "Start accelvoice backend process specifying dictionary file"
  (interactive)
  (when accelvoice--current-process
    (accelvoice--stop-projectdict))
  (let ((dictionary (expand-file-name (read-file-name "Dictionary: ")))
        (log (expand-file-name accelvoice--logfile)))
    (setq accelvoice--current-process
          (start-process "projectdict" "projectdict" accelvoice--projectdict-path dictionary log))
    (set-process-filter accelvoice--current-process 'accelvoice--process-filter)))

(defun accelvoice--complete ()
  "Run vocal completion with the symbol at point"
  (interactive)
  (if accelvoice--current-process
      (let ((bounds (bounds-of-thing-at-point 'symbol))
            start end word)
        (if bounds
            (setq start (car bounds)
                  end (cdr bounds)
                  word (buffer-substring-no-properties start end))
          (setq start (point) end (point) word ""))
        (setq accelvoice--current-completion (list start end word))
        (process-send-string accelvoice--current-process (concat word "\n")))
    (error "accelvoice process is not running.  Start with accelvoice--start-projectdict command.")))

(defun accelvoice--process-filter (process output)
  (if accelvoice--current-completion
      (let ((start (nth 0 accelvoice--current-completion))
            (end   (nth 1 accelvoice--current-completion))
            (result (replace-regexp-in-string "
" "" output)))
        (setq accelvoice--current-completion nil)
        (accelvoice--replace-region start end result))
    (with-current-buffer (process-buffer process)
      (let ((moving (= (point) (process-mark process))))
        (save-excursion
          ;; テキストを挿入し、プロセスマーカを進める
          (goto-char (process-mark process))
          (insert output)
          (set-marker (process-mark process) (point)))
        (if moving (goto-char (process-mark process)))))))

(defun accelvoice--replace-region (beg end rep)
  "Replace text in BUFFER in region (BEG END) with REP."
  (save-excursion
    (goto-char end)
    (insert rep)
    (delete-region beg end)))

(defun accelvoice--stop-projectdict ()
  "Stop accelvoice backend process"
  (interactive)
  (setq accelvoice--current-completion nil)
  (when accelvoice--current-process
    (when (eq 'run (process-status accelvoice--current-process))
        (process-send-eof accelvoice--current-process))
    (setq accelvoice--current-process nil)))


;;; accelvoice.el ends here
