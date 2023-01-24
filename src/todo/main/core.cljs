(ns todo.main.core
  (:require
   [rum.core :as rum]
   ["react-sortablejs" :refer [ReactSortable]]
   ["@tanstack/react-query" :refer [QueryClient QueryClientProvider useQuery]]))

(defonce app-state (atom {:text "Hello world!"}))
(defonce query-client (QueryClient.))

(defn- get-react-query-info []
  (->
   (js/fetch "https://api.github.com/repos/tannerlinsley/react-query")
   (.then #(.json %))))

(rum/defc sort-list []
  (let [[state set-state!]
        (rum/use-state [{:id 1 :name "shrek"}
                        {:id 2 :name "fiona"}])]
    (.log js/console state)
    (rum/adapt-class ReactSortable
                     {:class "handle"
                       :list state
                       :setList set-state!}
                     (map (fn [item]
                            [:div
                             {:key (:id item)}
                              [:i.handle.fas.fa-arrows-alt]
                              [:span (:name item)]]) state))))

(rum/defc hello [data]
  (let [{:keys [isLoading error data]}
        (->
         {:queryKey  ["react-query"]
          :queryFn get-react-query-info}
         (clj->js)
         (useQuery)
         (js->clj :keywordize-keys true))]
    [:div
     [:h1 (:title data)]
     (sort-list)
     (cond
       isLoading
       [:div
        [:span "Loading ...."]]
       error
       [:div
        [:span (->> error
                    (.-message)
                    (str "An error has occurred"))]]
       :else
       [:div
        [:h2 (:name data)]
        [:p (:description data)]
        [:strong (->> data
                      :subscribers_count
                      (str "👀 "))]])]))

;;(rum/defc app []
;;    (js/React.createElement
;;      QueryClientProvider
;;      #js {:client query-client}
;;      (clj->js (hello {:foo "the data"}))))
(rum/defc app []
  (rum/adapt-class
   QueryClientProvider
   {:client query-client}
   (hello {:title "The Info"})))

(defn start []
  ;; start is called by init and after code reloading finishes
  ;; this is controlled by the :after-load in the config
  (rum/mount (app)
             (. js/document (getElementById "app"))))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))
