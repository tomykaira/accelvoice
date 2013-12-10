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

;; dependencies

(require 'deferred)
(require 'url)
(require 'json)

;; variables

(defvar accelvoice--projectdict-path "/usr/local/bin/projectdict")
(defvar accelvoice--logfile "~/.accelvoice/projectdict.log")

;; internal variables

;; current running process object, or nil
(defvar accelvoice--current-process nil)

;; plist of tag, candidate-count, start position, end position, word
;; for which the accelvoice is recognizing
(defvar accelvoice--current-completion nil)

;; functions

(defun accelvoice--start-projectdict ()
  "Start accelvoice backend process specifying project root directory"
  (interactive)
  (when accelvoice--current-process
    (accelvoice--stop-projectdict))
  (let ((project-root (expand-file-name (read-file-name "Project root: ")))
        (log (expand-file-name accelvoice--logfile)))
    (setq accelvoice--current-process
          (start-process "projectdict" "projectdict" accelvoice--projectdict-path project-root log))))

(defun accelvoice--complete ()
  "Run vocal completion with the symbol at point"
  (interactive)
  (if accelvoice--current-process
      (let ((bounds (bounds-of-thing-at-point 'symbol))
            start end word)
        (when bounds
          (setq start (car bounds)
                end (cdr bounds)
                word (buffer-substring-no-properties start end))
          (if (> end start)
              (accelvoice--call-start-completion (list :start start :end end :word word)))))
    (error "accelvoice process is not running.  Start with accelvoice--start-projectdict command.")))

(defun accelvoice--call-start-completion (data)
  "Call start-completion command"
  (lexical-let*
      ((current-completion data)
       (word (plist-get data :word))
       (params `((language . "ruby") (prefix . ,word))))
    (deferred:$
      (deferred:url-post "http://localhost:8302/start-completion" params)
      (deferred:nextc it
        (lambda (buf)
          (let ((response (accelvoice--safe-json-read-from-buffer buf)))
            (when response
              (let ((count (cdr (assoc 'candidate_count response))))
                (plist-put current-completion :tag (cdr (assoc 'tag response)))
                (plist-put current-completion :candidate-count count)
                (setq accelvoice--current-completion current-completion)
                (force-mode-line-update)
                (unless (= count 0)
                    (accelvoice--call-result current-completion)))))
          (kill-buffer buf))))))

(defun accelvoice--call-result (current-completion)
  (lexical-let* ((current-completion current-completion)
                 (tag (plist-get current-completion :tag))
                 (params (list (cons 'tag tag))))
    (deferred:$
      (deferred:url-post "http://localhost:8302/result" params)
      (deferred:nextc it
        (lambda (buf)
          (let* ((response (accelvoice--safe-json-read-from-buffer buf))
                 (selected (and response (cdr (assoc 'result response)))))
            (when selected
              (accelvoice--replace-region
               (plist-get current-completion :start)
               (plist-get current-completion :end)
               selected)))
          (when (string= tag (plist-get accelvoice--current-completion :tag))
            (setq accelvoice--current-completion nil))
          (kill-buffer buf))))))

(defun accelvoice--safe-json-read-from-buffer (buffer)
  (ignore-errors
    (with-current-buffer buffer
      (goto-char (point-min))
      (json-read))))

(defun accelvoice--replace-region (beg end rep)
  "Replace text in BUFFER in region (BEG END) with REP."
  (let ((proc (lambda ()
                (goto-char end)
                (insert rep)
                (delete-region beg end))))
    (if (= end (point))
        (funcall proc)
      (save-excursion
        (funcall proc)))))

(defun accelvoice--update-mode-line ()
  (let ((count (plist-get accelvoice--current-completion :candidate-count)))
    (concat
     "AVC:"
     (if count (number-to-string count) "--"))))

(defun accelvoice--stop-projectdict ()
  "Stop accelvoice backend process"
  (interactive)
  (setq accelvoice--current-completion nil)
  (when accelvoice--current-process
    (when (eq 'run (process-status accelvoice--current-process))
        (process-send-eof accelvoice--current-process))
    (setq accelvoice--current-process nil)))

(provide 'accelvoice)

;;; accelvoice.el ends here
