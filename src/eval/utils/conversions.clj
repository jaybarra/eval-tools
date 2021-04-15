(ns eval.utils.conversions)

;; Mass conversions ==================================================

(def mass-conversion 2.2046226218488)

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
