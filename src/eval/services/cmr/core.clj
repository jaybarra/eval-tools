(ns eval.services.cmr.core)

(defn context->client
  "Extract a CMR client from system context"
  [{:keys []
    {:keys [instances]} :app/cmr} cmr-inst]
  (get instances cmr-inst))

(defn context->db
  "Extract the CMR db from system context"
  [{:keys []
    {:keys [db]} :app/cmr}]
  db)
