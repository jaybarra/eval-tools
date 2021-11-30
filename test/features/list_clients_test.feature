Feature: CMR Search Test
  Eval Tools supports multiple CMR connections via independently configured clients

Scenario: Searching CMR
  Given the input "cmr ls"
  When the tool is run
  Then I get a list of cmr clients
