;; Freecoin - digital social currency toolkit

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2015 Dyne.org foundation
;; Copyright (C) 2015 Thoughtworks, Inc.

;; Sourcecode designed, written and maintained by
;; Denis Roio <jaromil@dyne.org>
;; Gareth Rogers <grogers@thoughtworks.com>

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns freecoin.routes
  (:require
   [clojure.pprint :as pp]
   [clojure.string :as str]
;;   [liberator.dev]
   [liberator.core :refer [resource defresource]]

   [liberator.representation :refer [as-response ring-response]]
   [compojure.core :refer [defroutes ANY]]

   ;; [environ.core :refer [env]]

   [freecoin.params :as param]

   [freecoin.utils :as util]
   [freecoin.auth :as auth]

   [freecoin.nxt :as nxt]
   [freecoin.wallet :as wallet]
   )
  )



(defn redirect-home [_]
  (liberator.representation/ring-response
   {:body "<html><head><meta http-equiv=\"refresh\" content=\"0; url=/\"></head></html>"
    :headers {"Location" "/"}})
  )

;; debug
(defresource echo [request]
  :allowed-methods [:get]
  :available-media-types ["text/html"]
  :authorized? (fn [ctx] (auth/check request))
;  :as-response (fn [d ctx] (#'cookie-response d ctx))
  :handle-ok (fn [ctx] (util/pretty ctx))

  )
  
;; routes
(defroutes app

  ;; debug
  (ANY "/echo" [request] (echo request))

  (ANY "/"        [request] (wallet/balance request))
  (ANY "/version" [] (resource
                      :available-media-types ["text/html" "application/json"]
                      :exists? {::hello  {:Freecoin "D-CENT"
                                          :version param/version
                                          :license "AGPLv3"
                                          :os-name (.. System getProperties (get "os.name"))
                                          :os-version (.. System getProperties (get "os.version"))
                                          :os-arch (.. System getProperties (get "os.arch"))}}

                      :handle-ok #(util/pretty (::hello %))
                      :handle-create #(::hello %)
                      ))
  
  ;; Wallet operations
  
  (ANY "/wallet" [request] (wallet/balance request))
  (ANY "/wallet/create" [request] (wallet/create request))
  (ANY "/wallet/create/:confirmation" [confirmation :as request]
       (wallet/confirm_create request confirmation))
  (ANY "/wallet/balance" [request] (wallet/balance request))
  (ANY "/wallet/qrcode" [request] (wallet/qrcode request))


  ;; Search function
  (ANY "/find/:key/:value" [key value :as request] (wallet/find request key value))

  ;; Money transfers
  (ANY "/give/:recipient/:amount" [recipient amount :as request]
       (wallet/give request recipient amount))




  ;; 1 to 1 NXT api mapping for functions taking up to 2 args
  (ANY "/nxt/:command" [command :as request]
       (nxt/api request {"requestType" command}))
  (ANY "/nxt/:cmd1/:key1/:val1" [cmd1 key1 val1 :as request]
       (nxt/api request {"requestType" cmd1 key1 val1}))
  (ANY "/nxt/:cmd1/:key1/:val1/:key2/:val2"
       [cmd1 key1 val1 key2 val2 :as request]
       (nxt/api request {"requestType" cmd1 key1 val1 key2 val2}))


  ) ; end of routes
