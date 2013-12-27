(ns omkara.datetime
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(defn DateTime [{:keys [date] :as props}]
  (om/component
    (dom/p nil (str "It is now " date))))

(def dt (atom {}))

(defn ^:export main [data]
  (reset! dt {:date (aget data "date")})
  (om/root dt DateTime (.getElementById js/document "example"))
  (js/setInterval #(reset! dt {:date (.toTimeString (js/Date.))}) 1000))

(defn ^:export render [data]
  (let [src (atom "")]
    (.renderComponentToString js/React
      (om/pure {} (DateTime {:date (aget data "date")}))
      (fn [string] (reset! src string)))
    @src))
