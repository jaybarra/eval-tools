(ns eval.shell.interface
  (:require
   [eval.shell.core :as core]))

(defn start
  [command-executor user-input workspace-fn workspace color-mode]
  (core/start command-executor user-input workspace-fn workspace color-mode))
