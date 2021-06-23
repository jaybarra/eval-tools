(ns eval.cmr.legacy.tokens
  "Actions for interacting with the CMR legacy systems."
  (:require
   [clj-http.client :as http]
   [eval.cmr.core :as cmr]))

(defn- echo-token-soap-message
  "Return a soap message for getting an echo-token from legacy systems."
  [username password client-id & [ip-address act-as on-behalf-of]]
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
         <actAsUserName>%s</actAsUserName>
         <behalfOfProvider>%s</behalfOfProvider>
       </Login>
     </Body>
   </Envelope>"
   username password client-id
   (or ip-address "127.0.0.1")
   (or act-as "")
   (or on-behalf-of "")))

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
  [credentials]
  {:method :post
   :url "/legacy-services/rest/tokens"
   :headers {"Content-Type" "application/xml"}
   :body (token-xml-request-body credentials)})
