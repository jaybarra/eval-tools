(ns eval.elasticsearch.plugins.core
  (:gen-class
   :name eval.elasticsearch.plugins.SampleScriptPlugin
   :extends org.elasticsearch.plugins.Plugin
   :implements [org.elasticsearch.plugins.ScriptPlugin])
  (:import
   [cmr.elasticsearch.plugins SpatialScriptEngine]
   [java.util Collection Collections]))

 
;;  public ScriptEngine getScriptEngine (Settings settings
;;                                                Collection<ScriptContext<?>> contexts) {return new MyExpertScriptEngine ();
                                                                                       }
