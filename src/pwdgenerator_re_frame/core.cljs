(ns pwdgenerator-re-frame.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {}))

(def defaults {:no_words 5
               :uppercase "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
               :no_uppercase_alpha 5
               :lowercase "abcdefghijklmnopqrstuvwxyz"
               :no_lowercase_alpha 5
               :symbols "!@#$&*.:?+="
               :no_symbols 1
               :numerics "0123456789"
               :no_numerics 3
               :word_separator " "})

(def form-field-defs {:no_words {:label "Number of Words "
                                 :size 3
                                 :maxlength 3}
                      :uppercase {:label "Upper Case Alpha Character Set "
                                  :size 35
                                  :maxlength 26}
                      :no_uppercase_alpha {:label "Number of Upper Case Alpha Characters "
                                           :size 3
                                           :maxlength 3}
                      :lowercase {:label "Lower Case Alpha Character Set "
                                  :size 35
                                  :maxlength 26}
                      :no_lowercase_alpha {:label "Number of Lower Case Alpha Characters "
                                           :size 3
                                           :maxlength 3}
                      :numerics {:label "Numeric Character Set "
                                  :size 10
                                 :maxlength 10}
                      :no_numerics {:label "Number of Numeric Characters "
                                    :size 3
                                    :maxlength 3}
                      :symbols {:label "Symbol Character Set "
                                :size 10
                                :maxlength 10}
                      :no_symbols {:label "Number of Symbol Characters "
                                   :size 3
                                   :maxlength 3}})

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

(defn form-field [field s]
  (let [defs (field form-field-defs)]
    [:div {:id (str field "-input")}
     [:label (:label defs) 
      [:input {:type :text
               :size (:size defs)
               :maxLength (:maxlength defs)
               :value (field @s)
               :on-change #(swap! s assoc field (-> % .-target .-value))}]]]))

(defn form-fields [s]
  (map #(form-field % s) (keys form-field-defs)))

(defn pwdgenerator [pw]
  (let [s (reagent/atom (merge defaults {:value pw}))]
    (fn []
      (let [validations (for [[desc f] password-validations]
                          [desc (f (:value @s))])
            valid? (every? identity (map second validations))
            color (when (:dirty? @s) (if valid? "green" "red"))]
        [:form
         [:div {:id :dbdump} (pr-str @s)]
         [:div {:id :debugger}]
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
         [:div {:id "show-password-input"}
          [:label [:input {:type :checkbox
                           :checked (:show? @s)
                           :on-change #(swap! s assoc
                                              :show?
                                              (-> % .-target .-checked))}]
           " Show password?"]]
         (form-fields s)
         [:div {:id "word-separtor-input"}
          [:label "Word Separator "
           [:input {:type :text
                    :size 3
                    :maxLength 3
                    :value (:word_separator @s)
                    :on-change #(swap! s assoc :word_separator (-> % .-target .-value))}]
           " " (pr-str (:word_separator @s))]]
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
