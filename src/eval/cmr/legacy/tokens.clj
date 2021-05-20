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

(defn get-token!
  "Send a SOAP message to the Authentication endpoint to request a new echo-token.

  This will invalidate existing token for the user."
  [cmr-conn credentials opts]
  (cmr/api-action!
   cmr-conn
   {:method :post
    :url "/legacy-services/AuthenticationServiceportImpl"
    :headers {:content-type "text/xml"}
    :body (echo-token-soap-message (:username credentials)
                                   (:password credentials)
                                   (get opts :client-id "eval-tools"))}))
