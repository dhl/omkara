(ns omkara.react
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [clj-rhino :as js]))

(def scope-cache (atom {}))

(defn- new-root-scope [js-path]
  ;; ensure that the standard objects aren't sealed since cljs needs to update the Date prototype
  (let [scope (js/with-context-if-nil nil (fn [ctx] (.initStandardObjects ctx)))
        js-code (slurp js-path)]
    (js/with-context-if-nil nil
      (fn [ctx]
        ;; set to interpret (rather than compile) JS to get around 64K limit
        (.setOptimizationLevel ctx -1)
        (js/eval scope js-code :ctx ctx)))
    scope))

(defn- get-root-scope [js-path]
  (let [cached (get @scope-cache js-path)
        lastModified (.lastModified (io/file js-path))]
    (if (> lastModified (or (:lastModified cached) 0))
      (let [scope (new-root-scope js-path)]
        (swap! scope-cache assoc js-path { :scope scope :lastModified lastModified })
        scope)
      (:scope cached))))

(defn html-for-component [component-ns input-data]
  (let [root-scope (get-root-scope "public/main.embed.js")
        scope (js/new-scope nil root-scope)]
    (js/eval
      scope
      (str "var INPUT = " (json/write-str input-data) ";\n"
           ;; the component namespace should have an exported render(data) function which calls React.renderComponentToString()
           "omkara." component-ns ".render(INPUT);"))))
