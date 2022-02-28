(ns eval.user-input.core
  (:require
   [eval.user-input.params :as params]
   [eval.utils.interface :as util]))

(defn extract
  [args]
  {:cmd (first args)
   :params (-> args rest vec)})

(defn parse-params 
  [args single-arg-commands]
  (let [{:keys [cmd params]} (extract args)
        {:keys [named-args unnamed-args]} (params/extract params single-arg-commands)
        {:keys [cmr
                provider
                format
                concept-type
                no-exit!
                verbose!]} named-args]
    (util/remove-blank-keys
     {:args (vec args)
      :cmd (keyword cmd)
      :cmr cmr
      :format (keyword (or format :json))
      :provider provider
      :concept-type (keyword concept-type)
      :no-exit? (= "true" no-exit!)
      :verbose? (= "true" verbose!)
      :unnamed-args (vec unnamed-args)})))

(comment
  (parse-params ["search" "cmr:sit" ":verbose" "concept-type:collection" "format:json"] #{})
  )
