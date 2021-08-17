(ns eval.cmr.legacy.tokens
  "Actions for interacting with the CMR legacy systems."
  (:require
   [clj-http.client :as http]
   [clojure.xml :as xml]))

(defn echo-token-soap-message
  "Return a soap message for getting an echo-token from legacy systems.

  supported options
  * :ip-address
  * :act-as
  * :on-behalf-of"
  [username password client-id & [opts]]
  (format
   "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">
     <Body>
       <Login xmlns=\"http://echo.nasa.gov/echo/v10\">
         <username>%s</username>
         <password>%s</password>
         <clientInfo>
           <ClientId xmlns=\"http://echo.nasa.gov/echo/v10/types\">%s</ClientId>
           <UserIpAddress xmlns=\"http://echo.nasa.gov/echo/v10/types\">%s</UserIpAddress>
         </clientInfo>
         %s
         %s
       </Login>
     </Body>
   </Envelope>"
   username password client-id
   (get opts :ip-address "127.0.0.1")
   (if-let [act-as (get opts :act-as)]
     (format "<actAsUserName>%s</actAsUserName>" act-as)
     "")
   (if-let [on-behalf-of (get opts :on-behalf-of)]
     (format "<behalfOfProvider>%s</behalfOfProvider>" on-behalf-of)
     "")))

(defn token-xml-request-body
  [credentials]
  (format
   "<token>
      <username>%s</username>
      <password>%s</password>
      <client_id>%s</client_id>
      <user_ip_address>%s</user_ip_address>
    </token>"
   (:username credentials)
   (:password credentials)
   (get credentials :client-id "test-client")
   (get credentials :ip-address "127.0.0.1")))

(defn get-token
  "Request a token from CMR legacy services

  Credentials map:
  * username - Earthdata username
  * password - Earthdata password
  * client-id - optional
  * ip-address - optional"
  [credentials]
  {:method :post
   :url "/legacy-services/rest/tokens"
   :headers {"Content-Type" "application/xml"}
   :body (token-xml-request-body credentials)})

(defn get-token-info
  "Retrieve information about a given token."
  [token-id]
  {:method :post
   :url "/legacy-services/rest/tokens/get_token_info"
   :headers {"Content-Type" "application/json"}
   :body {:id token-id}})
