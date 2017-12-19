(ns pwdgenerator-re-frame.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as s]))

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

(def form-field-defs {:no_words {:order 1
                                 :label "Number of Words "
                                 :size 3
                                 :maxlength 3
                                 :numeric? true}
                      :uppercase {:order 2
                                  :label "Upper Case Alpha Character Set "
                                  :size 35
                                  :maxlength 26
                                  :numeric? false}
                      :no_uppercase_alpha {:order 3
                                           :label "Number of Upper Case Alpha Characters "
                                           :size 3
                                           :maxlength 3
                                           :numeric? true}
                      :lowercase {:order 4
                                  :label "Lower Case Alpha Character Set "
                                  :size 35
                                  :maxlength 26
                                  :numeric? false}
                      :no_lowercase_alpha {:order 5
                                           :label "Number of Lower Case Alpha Characters "
                                           :size 3
                                           :maxlength 3
                                           :numeric? true}
                      :numerics {:order 6
                                 :label "Numeric Character Set "
                                 :size 10
                                 :maxlength 10
                                 :numeric? false}
                      :no_numerics {:order 7
                                    :label "Number of Numeric Characters "
                                    :size 3
                                    :maxlength 3
                                    :numeric? true}
                      :symbols {:order 8
                                :label "Symbol Character Set "
                                :size 10
                                :maxlength 10
                                :numeric? false}
                      :no_symbols {:order 9
                                   :label "Number of Symbol Characters "
                                   :size 3
                                   :maxlength 3
                                   :numeric? true}})

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
  (map #(form-field % s) (sort-by #(:order (% form-field-defs)) (keys form-field-defs))))

(defn random-char [s]
  (nth s (rand-int (count s))))

(defn random-string [len s]
  (let [length (js/parseInt len)]
    (apply str (take length (repeatedly #(random-char s))))))

(defn uppercase-word [params]
  (random-string (:no_uppercase_alpha params) (:uppercase params)))

(defn lowercase-word [params]
  (random-string (:no_lowercase_alpha params) (:lowercase params)))

(defn numerics-symbols-word [params]
  (apply str
   (shuffle
    (seq
     (str (random-string (:no_numerics params) (:numerics params))
          (random-string (:no_symbols params) (:symbols params)))))))

(defn all-words [params]
  (let [generators [uppercase-word lowercase-word numerics-symbols-word]
        words (js/parseInt (:no_words params))]
    (shuffle
     (take words (repeatedly #((nth generators (rand-int (count generators))) params))))))

(defn generate-pw [params]
  (s/join (:word_separator params) (all-words params)))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:value (generate-pw defaults)}))

(rf/reg-event-db
 :generate
 (fn [db [_ s]]
   (assoc db :value (generate-pw s))))

(rf/reg-sub
 :value
 (fn [db] (:value db)))

(defn pwdgenerator []
  (let [s (reagent/atom (merge defaults {:show? true}))]
    (fn []
      (let [value @(rf/subscribe [:value])
            validations (for [[desc f] password-validations]
                          [desc (f (:value @s))])
            valid? (every? identity (map second validations))
            color (when (:dirty? @s) (if valid? "green" "red"))]
        [:form
         [:div {:id :dbdump} (pr-str @s)]
         [:div {:id :params} (pr-str value)]
         [:label {:style {:color color}} "Password"]
         [:input {:type (if (:show? @s) :text :password)
                  :style {:width "100%"
                          :border (str "1px solid " color)}
                  :value value
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
         [:div {:id "word-separator-input"}
          [:label "Word Separator "
           [:input {:type :text
                    :size 3
                    :maxLength 3
                    :value (:word_separator @s)
                    :on-change #(swap! s assoc :word_separator (-> % .-target .-value))}]
           " " (pr-str (:word_separator @s))]]
         [:div {:id :regenerate :on-click
                   (fn []
                     (rf/dispatch [:generate @s])) } "Regenerate"]
         (for [[desc valid?] validations]
           (when (:focus? @s)
             [:div {:style {:color (when (:dirty? @s)
                                     (if valid? "green" "red"))}}
              (when (:dirty? @s) (if valid? "✔ " "✘ "))
              desc]))]))))

(defn ui []
  [:div
   [pwdgenerator]])

(when-some [el (js/document.getElementById "pwdgenerator")]
  (defonce _init (rf/dispatch-sync [:initialize]))
  (reagent/render [ui] el))
