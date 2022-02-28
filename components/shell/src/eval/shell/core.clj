(ns eval.shell.core
  (:require 
   [clojure.string :as str]
   [eval.shell.jline :as jline]
   [eval.user-input.interface :as user-input])
  (:import
   [org.jline.reader EndOfFileException]
   [org.jline.reader UserInterruptException])
  (:refer-clojure :exclude [next]))

(def ws-dir (atom nil))
(def ws-file (atom nil))

(defn prompt []
  (let [prefix (cond @ws-dir "dir:"
                     @ws-file "file:"
                     :else "")]
    (str prefix "$ ")))

#_(defn print-logo [color-mode]
  (println "                  _      _ + _   _");
  (println (str (color/grey color-mode "#####") "   _ __  ___| |_  _| |-| |_| |_"));
  (println (str (color/green color-mode "#####") "  | '_ \\/ _ \\ | || | | |  _| ' \\"));
  (println (str (color/blue color-mode "#####") "  | .__/\\___/_|\\_, |_|_|\\__|_||_|"));
  (println (str "       |_|          |__/ " version/name)))

(defn print-logo
  [_color-mode]
  (println "EVAL TOOLS"))

(defn enhance [user-input dir file]
  (assoc user-input :is-shell true
         :ws-dir dir
         :ws-file file))

(defn execute-command [command-executor user-input color-mode]
  (try
    (let [input (-> user-input
                    (enhance @ws-dir @ws-file))]
      (command-executor input))
    (catch Throwable e
      (println (.getMessage e)))))

(defn start
  [command-executor {:keys [ws-dir ws-file is-tap] :as user-input} workspace-fn workspace color-mode]
  (let [reader (jline/reader)]
    (print-logo color-mode)
    
    (try
      (loop []
        (flush)
        (when-let [line (.readLine reader (prompt))]
          (let [{:keys [cmd color-mode] :as input} (user-input/extract-params (str/split line #"\S"))]
            (when-not (contains? #{"exit" "quit"} cmd)
              (cond
                (= "shell" cmd) (println "  Can't start a shell inside another shell.")
                (str/blank? line) nil
                :else (execute-command command-executor input color-mode))
              (flush)
              (recur)))))
      (catch EndOfFileException _)
      (catch UserInterruptException _))))
