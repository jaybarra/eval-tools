(ns eval.database.core
  (:require
   [next.jdbc :as jdbc]))

(defn build
  "Creates a sql statement."
  [stmt]
  (throw (ex-info "Not yet implemented" {:statement stmt})))

(defn run-sql
  "Run the given sql statement in string."
  [db stmt]
  (jdbc/execute! db stmt))

(defn query
  "Execute a query."
  [db stmt]
  (throw (ex-info "Not yet implemented" {:statement stmt})))

(defn find-one
  "Finds and returns the first item found from a select statment."
  [db stmt]
  (throw (ex-info "Not yet implemented" {:statement stmt})))
