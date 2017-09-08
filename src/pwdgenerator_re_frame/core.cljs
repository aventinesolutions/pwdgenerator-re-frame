(ns pwdgenerator-re-frame.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {}))

(def password-validations
  [["At least 12 characters"
    (fn [s]
      (>= (count s) 12))]
   ["At least 50% unique characters"
    (fn [s]
      (-> s
          set
          count
          (/ (count s))
          (>= 0.5)))]])

(defn pwdgenerator [pw]
  (let [s (reagent/atom {:value pw
                         :no_words 5
                         :no_uppercase_alpha 5
                         :no_lowercase_alpha 5
                         :no_symbols 1
                         :no_numerics 3
                         :word_separator " "})]
    (fn []
      (let [validations (for [[desc f] password-validations]
                          [desc (f (:value @s))])
            valid? (every? identity (map second validations))
            color (when (:dirty? @s) (if valid? "green" "red"))]
        [:form
         [:div {:id :dbdump} (pr-str @s)] 
         [:label {:style {:color color}} "Password"]
         [:input {:type (if (:show? @s) :text :password)
                  :style {:width "100%"
                          :border (str "1px solid " color)}
                  :value (:value @s)
                  :on-focus #(swap! s assoc :focus? true)
                  :on-blur #(swap! s assoc :dirty? true)
                  :on-change #(swap! s assoc
                                     :dirty? true
                                     :value
                                     (-> % .-target .-value))}]
         [:label [:input {:type :checkbox
                          :checked (:show? @s)
                          :on-change #(swap! s assoc
                                             :show?
                                             (-> % .-target .-checked))}]
          " Show password?"]
         [:br]
         [:label "Number of Words "
          [:input {:type :text
                   :size 3
                   :maxLength 3
                   :value (:no_words @s)
                   :on-change #(swap! s assoc :no_words (-> % .-target .-value))}]]
         [:br]
         [:label "Number of Upper Case Alpha Characters "
          [:input {:type :text
                   :size 3
                   :maxLength 3
                   :value (:no_uppercase_alpha @s)
                   :on-change #(swap! s assoc :no_uppercase_alpha (-> % .-target .-value))}]]
         [:br]
         [:label "Number of Lower Case Alpha Characters "
          [:input {:type :text
                   :size 3
                   :maxLength 3
                   :value (:no_lowercase_alpha @s)
                   :on-change #(swap! s assoc :no_lowercase_alpha (-> % .-target .-value))}]]
         [:br]
         [:label "Number of Numeric Characters "
          [:input {:type :text
                   :size 3
                   :maxLength 3
                   :value (:no_numerics @s)
                   :on-change #(swap! s assoc :no_numerics (-> % .-target .-value))}]]
         [:br]
         [:label "Number of Symbol Characters "
          [:input {:type :text
                   :size 3
                   :maxLength 3
                   :value (:no_symbols @s)
                   :on-change #(swap! s assoc :no_symbols (-> % .-target .-value))}]]
         [:br]
         [:label "Word Separator "
          [:input {:type :text
                   :size 3
                   :maxLength 3
                   :value (:word_separator @s)
                   :on-change #(swap! s assoc :word_separator (-> % .-target .-value))}]]
         (for [[desc valid?] validations]
           (when (:focus? @s)
             [:div {:style {:color (when (:dirty? @s)
                                     (if valid? "green" "red"))}}
              (when (:dirty? @s) (if valid? "✔ " "✘ "))
              desc]))]))))

(defn ui []
  [:div
   [pwdgenerator ""]])

(when-some [el (js/document.getElementById "pwdgenerator")]
  (defonce _init (rf/dispatch-sync [:initialize]))
  (reagent/render [ui] el))
