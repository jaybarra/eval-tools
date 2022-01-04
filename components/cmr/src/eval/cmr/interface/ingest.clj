(ns eval.cmr.commands.interface.ingest
  "Ingest commands for use with [[cmr/invoke]]."
  (:require
   [eval.cmr.commands.ingest :as ingest]))

(defn validate-concept-metadata
  "Returns a command to validate a given concept.
  Supported concept-types:
  + collection
  + granule"
  [concept-type provider-id native-id concept-data options]
  (ingest/validate-concept-metadata concept-type
                                    provider-id
                                    native-id
                                    concept-data
                                    options))

(defn create-concept
  "Returns a command to create a given concept."
  [concept-type provider-id concept options]
  (ingest/create-concept concept-type
                         provider-id
                         concept
                         options))

(defn update-concept
  "Alias for [[create-concept]]"
  [concept-type provider-id concept options]
  (ingest/update-concept concept-type 
                         provider-id 
                         concept 
                         options))

(defn delete-concept
  "Mark a concept as deleted in CMR"
  [concept-type provider-id concept-native-id]
  (ingest/delete-concept concept-type
                         provider-id
                         concept-native-id))

(defn create-association
  "Create an association beteween collection and variable"
  [collection-id collection-revision variable-id]
  (ingest/create-association collection-id
                             collection-revision
                             variable-id))
