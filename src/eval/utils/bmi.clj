(ns eval.utils.bmi)

(defn calculate-bmi
  "Calculate BMI based on height and weight"
  [cm kg]
  (let [m (/ cm 100)]
    (double (/ kg (* m m)))))

(defn bmi-status
  "Convert BMI to status."
  [bmi]
  (condp >= bmi
    18.5 :underweight
    24.9 :normal
    29.9 :overweight
    :obese))
