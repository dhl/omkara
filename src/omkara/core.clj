(ns omkara.core
  (:require
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [clojure.tools.logging :as log]
    [liberator.core :refer [defresource]]
    [compojure.core :refer [defroutes GET ANY]]
    [compojure.route :refer [not-found]]
    [compojure.handler :as handler]
    [ring.util.mime-type :refer [ext-mime-type]]
    [net.cgrand.enlive-html :refer :all]
    [omkara.react :as react]))

(defn- normalized-file [dir path]
  (let [f (io/file dir path)]
    (when (.startsWith (.getCanonicalPath f) (.getCanonicalPath (io/file dir)))
          (if (.isDirectory f)
              (io/file f "index.html")
              f))))

(defresource static-directory [static-dir]
  :available-media-types
  #(let [f (normalized-file static-dir (get-in % [:request :route-params :*]))]
    (if-let [mime-type (ext-mime-type (.getName f))]
      [mime-type]
      ["application/octet-stream"]))

  :exists?
  #(if-let [f (normalized-file static-dir (get-in % [:request :route-params :*]))]
    [(.exists f) {::file f}])

  :handle-ok (fn [{f ::file}] f)

  :last-modified (fn [{f ::file}] (when f (.lastModified f))))

(deftemplate skin-tmpl "index.html" [data body]
  [:#page-data]   (substitute { :tag :script :content (str "var INPUT = " (json/write-str data) ";") })
  [:#example]     (html-content body))

(defroutes routes
  (GET "/" []
    (let [data {:date "the long, dark tea-time of the soul"}
          body (react/html-for-component "datetime" data)]
      (apply str (skin-tmpl data body))))
  (GET "/*" [] (static-directory "public/"))
  (not-found "Resource not found"))

(def app
  (-> routes
      (handler/site)))
