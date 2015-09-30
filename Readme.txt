Ofproxy allows external Openflow controllers manage switch/node connected to ODL controller.

Although current implementation is not finished it allows third-party controller to:
- get information about connected switch;
- set up flows;
- send PacketOut messages
- get statistics.
Major unfinished pieces marked in the code with tag "TODO"

Test scenario:
- Build application
  mvn cleam install
- Copy created feature file to folder "deploy" in BVC
  cp ~/.m2/repository/com/elbrys/sdn/ofproxy-features/1.0.0-SNAPSHOT/ofproxy-features-1.0.0-SNAPSHOT-features.xml deploy/
- start bvc
- install ofproxy feature
  feature:install ofproxyapp
- Connect test switch.
- Issue rpc call to configure third-party switch controller connection 
  - open URL http://localhost:8181/apidoc/explorer/index.html
  - goto ofproxyappApp and post {"ofproxyapp:input":{"datapathId":"330239583806411","controllerIp":"127.0.0.1","controllerPort":"6633" }} where
	datapathId - DataPath id of connected switch
	controllerIp - third-party controller IP address
	controllerPort - thirdparty controller port
- Test trhird-party controller connection
