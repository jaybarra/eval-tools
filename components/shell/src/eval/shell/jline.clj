(ns eval.shell.jline
  (:import
   [org.jline.terminal TerminalBuilder]
   [org.jline.reader LineReaderBuilder]
   [org.jline.reader Parser]
   [org.jline.reader Parser$ParseContext]
   [org.jline.reader LineReader$SuggestionType]))

(def parser
  (proxy [Parser] []
    (parse [^String line
            ^Integer _
            ^Parser$ParseContext _])
    (isEscapeChar [^Character _]
      false)))

(defn reader
  []
  (let [terminal (-> (TerminalBuilder/builder)
                     (.system true)
                     (.dumb true)
                     (.build))
        result (-> (LineReaderBuilder/builder)
                   (.terminal terminal)
                   (.parser parser)
                   (.build))]
    (.setAutosuggestion result LineReader$SuggestionType/COMPLETER)
    result))
