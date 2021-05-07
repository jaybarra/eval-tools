(ns eval.utils.conversions
  (:require
   [java-time :as jtime]))

;; Time conversions ==================================================

(defn timeMillis->timestamp
  "Convert a time in millis to a timestamp."
  [ctm]
  (jtime/instant ctm))

;; Mass conversions ==================================================

(def mass-conversion "Kg to Pound conversion factor" 2.2046226218488)

(defn kg->lb
  "Convert mass in kilograms to pounds."
  [mass]
  (* mass mass-conversion))

(defn lb->kg
  "Convert mass in pounds to kilograms."
  [mass]
  (/ mass mass-conversion))


;; Length conversions ================================================

(defn cm->in
  [length]
  (/ length 2.54))

(defn in->cm
  [length]
  (* length 2.54))

(defn ft->in
  [length]
  (* length 12.0))

(defn in->ft
  [length]
  (/ length 12.0))

(defn ft->mile
  [length]
  (/ length 5280.0))

(defn mile->ft
  [length]
  (* length 5280.0))
