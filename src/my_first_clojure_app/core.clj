(ns my-first-clojure-app.core
  (:require [etaoin.api :as e]
            [clojure.tools.logging :as log])
  (:gen-class))

(def config
  {:url "https://smith.langchain.com"
   :email (or (System/getenv "LANGCHAIN_EMAIL") "parasbhandari2003@gmail.com")
   :password (or (System/getenv "LANGCHAIN_PASSWORD") "Parasgmail@123")
   :rotation-interval-days 30})

(defn wait-and-click [driver selector timeout]
  (try
    (when (e/wait-visible driver selector timeout)
      (let [el (e/query driver selector)]
        (e/click-el driver el)
        true))
    (catch Exception ex
      (log/errorf "Failed to click %s: %s" selector (.getMessage ex))
      false)))
(defn login-google [driver]
  (try
    (log/info "Opening LangSmith login page...")
    (e/go driver (:url config))
    (e/wait 3)
    (let [main-window (e/get-window-handle driver)
          google-selectors
          [{:xpath "//button[contains(., 'Continue with Google')]"}
           {:css "button[aria-label*='Google']"}
           {:css "button[jsname='LgbsSe']"}]]
      (loop [[sel & rest] google-selectors]
        (if sel
          (if (wait-and-click driver sel 10)
            (do
              (log/infof "Clicked Google button with selector: %s" sel)
              (e/wait 2)
              (let [popup-window (->> (e/get-window-handles driver)
                                      (remove #{main-window})
                                      first)]
                (if popup-window
                  (do
                    (e/switch-window driver popup-window)
                    (e/fill driver {:name "identifier"} (:email config))
                    (e/wait 1)
                    (wait-and-click driver {:id "identifierNext"} 10)
                    (e/wait 3)
                    (e/fill driver {:name "password"} (:password config))
                    (e/wait 1)
                    (wait-and-click driver {:id "passwordNext"} 10)
                    (e/wait 5)
                    (e/switch-window driver main-window)
                    true)
                  (do (log/error "Google login popup not found") false))))
            (recur rest))
          (do
            (log/error "No Google button selector worked")
            false))))
    (catch Exception ex
      (log/error "Login failed:" (.getMessage ex))
      false)))


(defn delete-api-keys [driver]
  (try
    (log/info "Going to API keys page...")
    (e/go driver (str (:url config) "/settings/api-keys"))
    (e/wait 3)
    (loop []
      (let [buttons (e/query-all driver {:xpath "//button[contains(., 'Delete')]"})]
        (if (seq buttons)
          (do
            (log/info "Deleting an API key...")
            (e/click-el driver (first buttons))
            (e/wait 1)
            (wait-and-click driver {:xpath "//button[contains(., 'Confirm')]"} 5)
            (e/wait 2)
            (recur))
          (do
            (log/info "No more API keys to delete")
            true))))
    (catch Exception ex
      (log/error "Error deleting API keys:" (.getMessage ex))
      false)))

(defn run-rotation []
  (let [driver (e/chrome {:headless false
                          :args ["--start-maximized"]})]
    (try
      (if (login-google driver)
        (delete-api-keys driver)
        (log/error "Login failed, skipping deletion"))
      (finally
        (e/quit driver)))))

(defn -main [& args]
  (log/info "Starting LangSmith API Key Rotation Bot")
  (run-rotation)
  (log/info "Finished running"))
